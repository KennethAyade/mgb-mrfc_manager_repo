/**
 * ================================================
 * ATTENDANCE TRACKING ROUTES
 * ================================================
 * Handles attendance records for quarterly meetings with photo capture
 * Base path: /api/v1/attendance
 *
 * ENDPOINTS:
 * GET    /attendance/meeting/:agendaId - Get attendance for a meeting
 * POST   /attendance                   - Record attendance with photo
 * PUT    /attendance/:id                - Update attendance record
 * DELETE /attendance/:id                - Delete attendance record
 */

import { Router, Request, Response } from 'express';
import { authenticate, adminOnly } from '../middleware/auth';
import { uploadPhoto, cleanupTempFile } from '../middleware/upload';
import { uploadToCloudinary, deleteFromCloudinary, CLOUDINARY_FOLDERS } from '../config/cloudinary';

const router = Router();

/**
 * ================================================
 * GET /attendance/meeting/:agendaId
 * ================================================
 * Get attendance records for a specific meeting
 * All authenticated users can view
 */
router.get('/meeting/:agendaId', authenticate, async (req: Request, res: Response) => {
  try {
    const { Attendance, Agenda, Proponent, User } = require('../models');

    // Parse and validate agenda ID
    const agendaId = parseInt(req.params.agendaId);
    if (isNaN(agendaId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid agenda ID' }
      });
    }

    // Verify meeting exists
    const agenda = await Agenda.findByPk(agendaId);
    if (!agenda) {
      return res.status(404).json({
        success: false,
        error: { code: 'AGENDA_NOT_FOUND', message: 'Meeting not found' }
      });
    }

    // Authorization check for USER role
    // General meetings (mrfc_id = null) are accessible to all users
    if (req.user?.role === 'USER' && agenda.mrfc_id !== null) {
      const userMrfcIds = req.user.mrfcAccess || [];
      if (!userMrfcIds.includes(agenda.mrfc_id)) {
        return res.status(403).json({
          success: false,
          error: {
            code: 'MRFC_ACCESS_DENIED',
            message: 'You do not have access to this MRFC-specific meeting'
          }
        });
      }
    }

    // Get attendance records
    const attendance = await Attendance.findAll({
      where: { agenda_id: agendaId },
      include: [
        {
          model: Proponent,
          as: 'proponent',
          attributes: ['id', 'name', 'company_name', 'contact_person'],
          required: false
        },
        {
          model: User,
          as: 'marker',
          attributes: ['id', 'full_name', 'username'],
          required: false
        }
      ],
      order: [['marked_at', 'DESC']]
    });

    // Calculate summary
    const total = attendance.length;
    const present = attendance.filter((a: any) => a.is_present).length;
    const absent = total - present;
    const rate = total > 0 ? (present / total) * 100 : 0;

    return res.json({
      success: true,
      data: {
        attendance,
        summary: {
          total_attendees: total,
          present,
          absent,
          attendance_rate: parseFloat(rate.toFixed(2))
        }
      }
    });
  } catch (error: any) {
    console.error('Get attendance error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_ATTENDANCE_FAILED',
        message: error.message || 'Failed to retrieve attendance records'
      }
    });
  }
});

/**
 * ================================================
 * POST /attendance
 * ================================================
 * Record attendance for a meeting with optional photo
 * All authenticated users can record attendance
 *
 * REQUEST BODY (multipart/form-data):
 * - agenda_id: number (required)
 * - proponent_id: number (optional, for proponent attendance)
 * - attendee_name: string (required if no proponent_id)
 * - attendee_position: string (optional)
 * - attendee_department: string (optional)
 * - is_present: boolean (default: true)
 * - photo: file (optional, image file)
 * - remarks: string (optional)
 */
router.post('/', authenticate, uploadPhoto.single('photo'), async (req: Request, res: Response) => {
  let uploadedFilePath: string | null = null;
  let cloudinaryPublicId: string | null = null;

  try {
    const { Attendance, Agenda, Proponent, User, AuditLog } = require('../models');

    // Parse request data
    const {
      agenda_id,
      proponent_id,
      attendee_name,
      attendee_position,
      attendee_department,
      is_present,
      remarks
    } = req.body;

    // Validate required fields
    if (!agenda_id) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'agenda_id is required'
        }
      });
    }

    // Validate that either proponent_id OR attendee_name is provided
    if (!proponent_id && !attendee_name) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'Either proponent_id or attendee_name is required'
        }
      });
    }

    // Verify meeting exists
    const agenda = await Agenda.findByPk(agenda_id);
    if (!agenda) {
      return res.status(404).json({
        success: false,
        error: { code: 'AGENDA_NOT_FOUND', message: 'Meeting not found' }
      });
    }

    // Authorization check for USER role
    // General meetings (mrfc_id = null) are accessible to all users
    if (req.user?.role === 'USER' && agenda.mrfc_id !== null) {
      const userMrfcIds = req.user.mrfcAccess || [];
      if (!userMrfcIds.includes(agenda.mrfc_id)) {
        return res.status(403).json({
          success: false,
          error: {
            code: 'MRFC_ACCESS_DENIED',
            message: 'You do not have access to this MRFC-specific meeting'
          }
        });
      }
    }

    // If proponent_id provided, verify it exists
    if (proponent_id) {
      const proponent = await Proponent.findByPk(proponent_id);
      if (!proponent) {
        return res.status(404).json({
          success: false,
          error: { code: 'PROPONENT_NOT_FOUND', message: 'Proponent not found' }
        });
      }
    }

    // Check for duplicate attendance
    const whereClause: any = { agenda_id };
    if (proponent_id) {
      whereClause.proponent_id = proponent_id;
    } else {
      whereClause.attendee_name = attendee_name;
    }

    const existing = await Attendance.findOne({ where: whereClause });
    if (existing) {
      return res.status(409).json({
        success: false,
        error: {
          code: 'ATTENDANCE_EXISTS',
          message: 'Attendance already recorded for this person'
        }
      });
    }

    // Handle photo upload to Cloudinary
    let photoUrl = null;
    let photoCloudinaryId = null;

    if (req.file) {
      uploadedFilePath = req.file.path;

      const uploadResult = await uploadToCloudinary(
        uploadedFilePath,
        CLOUDINARY_FOLDERS.ATTENDANCE,
        'image'
      );

      photoUrl = uploadResult.url;
      photoCloudinaryId = uploadResult.publicId;

      // Clean up temporary file
      cleanupTempFile(uploadedFilePath);
      cloudinaryPublicId = photoCloudinaryId;
    }

    // Create attendance record
    const attendance = await Attendance.create({
      agenda_id: parseInt(agenda_id),
      proponent_id: proponent_id ? parseInt(proponent_id) : null,
      attendee_name: attendee_name || null,
      attendee_position: attendee_position || null,
      attendee_department: attendee_department || null,
      is_present: is_present === 'true' || is_present === true || is_present === undefined,
      photo_url: photoUrl,
      photo_cloudinary_id: photoCloudinaryId,
      marked_by: req.user?.userId,
      remarks: remarks || null
    });

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'CREATE',
      entity_type: 'ATTENDANCE',
      entity_id: attendance.id,
      old_values: null,
      new_values: {
        agenda_id: attendance.agenda_id,
        attendee: attendee_name || proponent_id,
        has_photo: !!photoUrl
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    // Fetch complete attendance record with associations
    const completeAttendance = await Attendance.findByPk(attendance.id, {
      include: [
        {
          model: Proponent,
          as: 'proponent',
          attributes: ['id', 'name', 'company_name'],
          required: false
        }
      ]
    });

    return res.status(201).json({
      success: true,
      message: 'Attendance recorded successfully',
      data: completeAttendance
    });
  } catch (error: any) {
    console.error('Record attendance error:', error);

    // Cleanup uploaded file on error
    if (uploadedFilePath) {
      cleanupTempFile(uploadedFilePath);
    }

    // Cleanup Cloudinary upload on error
    if (cloudinaryPublicId) {
      try {
        await deleteFromCloudinary(cloudinaryPublicId, 'image');
      } catch (cleanupError) {
        console.error('Failed to cleanup Cloudinary upload:', cleanupError);
      }
    }

    res.status(500).json({
      success: false,
      error: {
        code: 'ATTENDANCE_RECORDING_FAILED',
        message: error.message || 'Failed to record attendance'
      }
    });
  }
});

/**
 * ================================================
 * PUT /attendance/:id
 * ================================================
 * Update attendance record
 * Creator or ADMIN can update
 */
router.put('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { Attendance, AuditLog } = require('../models');

    // Parse and validate ID
    const attendanceId = parseInt(req.params.id);
    if (isNaN(attendanceId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid attendance ID' }
      });
    }

    // Find attendance record
    const attendance = await Attendance.findByPk(attendanceId);
    if (!attendance) {
      return res.status(404).json({
        success: false,
        error: { code: 'ATTENDANCE_NOT_FOUND', message: 'Attendance record not found' }
      });
    }

    // Authorization: Only creator or ADMIN can update
    const isCreator = attendance.marked_by === req.user?.userId;
    const isAdmin = req.user?.role === 'ADMIN' || req.user?.role === 'SUPER_ADMIN';

    if (!isCreator && !isAdmin) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Only the creator or admin can update this attendance record'
        }
      });
    }

    // Store old values for audit
    const oldValues = {
      is_present: attendance.is_present,
      remarks: attendance.remarks
    };

    // Update fields
    const { is_present, remarks } = req.body;
    if (is_present !== undefined) attendance.is_present = is_present;
    if (remarks !== undefined) attendance.remarks = remarks;

    await attendance.save();

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'UPDATE',
      entity_type: 'ATTENDANCE',
      entity_id: attendance.id,
      old_values: oldValues,
      new_values: {
        is_present: attendance.is_present,
        remarks: attendance.remarks
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.json({
      success: true,
      message: 'Attendance updated successfully',
      data: attendance
    });
  } catch (error: any) {
    console.error('Update attendance error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'ATTENDANCE_UPDATE_FAILED',
        message: error.message || 'Failed to update attendance'
      }
    });
  }
});

/**
 * ================================================
 * DELETE /attendance/:id
 * ================================================
 * Delete attendance record
 * Creator or ADMIN can delete
 */
router.delete('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { Attendance, AuditLog } = require('../models');

    // Parse and validate ID
    const attendanceId = parseInt(req.params.id);
    if (isNaN(attendanceId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid attendance ID' }
      });
    }

    // Find attendance record
    const attendance = await Attendance.findByPk(attendanceId);
    if (!attendance) {
      return res.status(404).json({
        success: false,
        error: { code: 'ATTENDANCE_NOT_FOUND', message: 'Attendance record not found' }
      });
    }

    // Authorization: Only creator or ADMIN can delete
    const isCreator = attendance.marked_by === req.user?.userId;
    const isAdmin = req.user?.role === 'ADMIN' || req.user?.role === 'SUPER_ADMIN';

    if (!isCreator && !isAdmin) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Only the creator or admin can delete this attendance record'
        }
      });
    }

    // Delete photo from Cloudinary if exists
    if (attendance.photo_cloudinary_id) {
      try {
        await deleteFromCloudinary(attendance.photo_cloudinary_id, 'image');
      } catch (error) {
        console.error('Failed to delete photo from Cloudinary:', error);
        // Continue with deletion even if Cloudinary cleanup fails
      }
    }

    // Store info for audit log
    const attendanceInfo = {
      agenda_id: attendance.agenda_id,
      attendee: attendance.attendee_name || attendance.proponent_id,
      had_photo: !!attendance.photo_url
    };

    // Delete attendance record
    await attendance.destroy();

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'DELETE',
      entity_type: 'ATTENDANCE',
      entity_id: attendanceId,
      old_values: attendanceInfo,
      new_values: null,
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.json({
      success: true,
      message: 'Attendance deleted successfully'
    });
  } catch (error: any) {
    console.error('Delete attendance error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'ATTENDANCE_DELETE_FAILED',
        message: error.message || 'Failed to delete attendance'
      }
    });
  }
});

export default router;
