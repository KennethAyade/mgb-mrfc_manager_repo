/**
 * ================================================
 * ATTENDANCE TRACKING CONTROLLER
 * ================================================
 * Handles attendance recording for quarterly meetings
 */

import { Request, Response } from 'express';
import { Attendance, Agenda, Proponent, User } from '../models';
import sequelize from '../config/database';

/**
 * List attendance records with filtering
 * GET /api/v1/attendance
 */
export const listAttendance = async (req: Request, res: Response): Promise<void> => {
  try {
    const { agenda_id, proponent_id } = req.query;

    // Build filter conditions
    const where: any = {};
    if (agenda_id) where.agenda_id = agenda_id;
    if (proponent_id) where.proponent_id = proponent_id;

    // Query attendance records
    const attendance = await Attendance.findAll({
      where,
      include: [
        {
          model: Agenda,
          as: 'agenda',
          attributes: ['id', 'agenda_number', 'title']
        },
        {
          model: Proponent,
          as: 'proponent',
          attributes: ['id', 'company_name', 'contact_person']
        },
        {
          model: User,
          as: 'marker',
          attributes: ['id', 'full_name']
        }
      ],
      order: [['marked_at', 'DESC']]
    });

    res.json({
      success: true,
      data: {
        attendance
      }
    });
  } catch (error: any) {
    console.error('Attendance listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'ATTENDANCE_LISTING_FAILED',
        message: error.message || 'Failed to retrieve attendance records'
      }
    });
  }
};

/**
 * Record attendance
 * POST /api/v1/attendance
 */
export const recordAttendance = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      agenda_id,
      proponent_id,
      representative_name,
      representative_position,
      is_present,
      time_in,
      time_out,
      photo_url,
      notes
    } = req.body;
    const currentUser = (req as any).user;

    // Verify agenda exists
    const agenda = await Agenda.findByPk(agenda_id);
    if (!agenda) {
      res.status(404).json({
        success: false,
        error: {
          code: 'AGENDA_NOT_FOUND',
          message: 'Agenda not found'
        }
      });
      return;
    }

    // Verify proponent exists
    const proponent = await Proponent.findByPk(proponent_id);
    if (!proponent) {
      res.status(404).json({
        success: false,
        error: {
          code: 'PROPONENT_NOT_FOUND',
          message: 'Proponent not found'
        }
      });
      return;
    }

    // Check if attendance already exists
    const existing = await Attendance.findOne({
      where: { agenda_id, proponent_id }
    });

    if (existing) {
      res.status(409).json({
        success: false,
        error: {
          code: 'ATTENDANCE_EXISTS',
          message: 'Attendance already recorded for this agenda and proponent'
        }
      });
      return;
    }

    // Create attendance record
    // TODO: Integrate with Cloudinary for photo upload
    const attendance = await Attendance.create({
      agenda_id,
      proponent_id,
      representative_name,
      representative_position,
      is_present: is_present !== false,
      time_in,
      time_out,
      photo_url: photo_url || null,
      notes,
      marked_by: currentUser?.userId
    });

    res.status(201).json({
      success: true,
      message: 'Attendance recorded successfully',
      data: attendance
    });
  } catch (error: any) {
    console.error('Attendance recording error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'ATTENDANCE_RECORDING_FAILED',
        message: error.message || 'Failed to record attendance'
      }
    });
  }
};

/**
 * Update attendance record
 * PUT /api/v1/attendance/:id
 */
export const updateAttendance = async (req: Request, res: Response): Promise<void> => {
  try {
    const attendanceId = parseInt(req.params.id);
    const {
      representative_name,
      representative_position,
      is_present,
      time_in,
      time_out,
      photo_url,
      notes
    } = req.body;

    // Find attendance record
    const attendance = await Attendance.findByPk(attendanceId);
    if (!attendance) {
      res.status(404).json({
        success: false,
        error: {
          code: 'ATTENDANCE_NOT_FOUND',
          message: 'Attendance record not found'
        }
      });
      return;
    }

    // Update attendance
    await attendance.update({
      representative_name,
      representative_position,
      is_present,
      time_in,
      time_out,
      photo_url,
      notes
    });

    res.json({
      success: true,
      message: 'Attendance updated successfully',
      data: attendance
    });
  } catch (error: any) {
    console.error('Attendance update error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'ATTENDANCE_UPDATE_FAILED',
        message: error.message || 'Failed to update attendance'
      }
    });
  }
};
