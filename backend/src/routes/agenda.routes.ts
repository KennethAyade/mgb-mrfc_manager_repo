/**
 * ================================================
 * AGENDA MANAGEMENT ROUTES
 * ================================================
 * Handles quarterly meeting agenda items
 * Base path: /api/v1/agendas
 *
 * ENDPOINTS:
 * GET    /agendas            - List all agendas (paginated, filterable)
 * POST   /agendas            - Create new agenda item (ADMIN only)
 * GET    /agendas/:id        - Get agenda details by ID
 * PUT    /agendas/:id        - Update agenda information (ADMIN only)
 * DELETE /agendas/:id        - Delete agenda item (ADMIN only)
 */

import { Router, Request, Response } from 'express';
import { authenticate, adminOnly } from '../middleware/auth';

const router = Router();

/**
 * ================================================
 * GET /agendas
 * ================================================
 * List all agenda items with pagination and filtering
 * USER: Can only see agendas for MRFCs they have access to
 * ADMIN: Can see all agendas
 *
 * QUERY PARAMETERS:
 * - page: number (default: 1)
 * - limit: number (default: 20, max: 100)
 * - quarter_id: number (filter by quarter)
 * - mrfc_id: number (filter by MRFC)
 * - status: 'SCHEDULED' | 'DISCUSSED' | 'APPROVED' | 'REJECTED' | 'DEFERRED'
 * - sort_by: 'agenda_number' | 'created_at' (default: 'agenda_number')
 * - sort_order: 'ASC' | 'DESC' (default: 'ASC')
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/agendas?quarter_id=1&status=SCHEDULED&page=1&limit=20
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "agendas": [
 *       {
 *         "id": 15,
 *         "agenda_number": "Item 5",
 *         "quarter": {
 *           "id": 1,
 *           "quarter_number": 1,
 *           "year": 2025,
 *           "meeting_date": "2025-03-15T09:00:00Z"
 *         },
 *         "mrfc": {
 *           "id": 25,
 *           "mrfc_number": "MRFC-2025-001",
 *           "project_title": "Gold Mining Project",
 *           "proponent": {
 *             "company_name": "ABC Mining Corp."
 *           }
 *         },
 *         "status": "SCHEDULED",
 *         "discussion_notes": null,
 *         "decision": null,
 *         "created_at": "2025-01-10T08:00:00Z"
 *       }
 *     ],
 *     "pagination": {
 *       "page": 1,
 *       "limit": 20,
 *       "total": 45,
 *       "totalPages": 3,
 *       "hasNext": true,
 *       "hasPrev": false
 *     }
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse and validate query parameters
 * 2. Build WHERE clause for filters (quarter_id, mrfc_id, status)
 * 3. Authorization check:
 *    - If USER: Add filter WHERE mrfc_id IN (user's mrfc_access array)
 *    - If ADMIN: No additional filter
 * 4. Calculate pagination (offset = (page - 1) * limit)
 * 5. Query agendas table with JOINs to quarters, mrfcs, and proponents
 * 6. Get total count for pagination metadata
 * 7. Return agendas array with pagination info
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 400: Invalid query parameters
 * - 500: Database error
 */
router.get('/', authenticate, async (req: Request, res: Response) => {
  try {
    const { Agenda, Quarter, Mrfc, Proponent } = require('../models');
    const { Op } = require('sequelize');

    // Step 1: Parse query parameters
    const {
      page = '1',
      limit = '20',
      quarter_id,
      mrfc_id,
      status,
      sort_by = 'meeting_date',
      sort_order = 'DESC'
    } = req.query;

    // Step 2: Validate parameters
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    // Step 3: Build filter conditions
    const where: any = {};
    if (quarter_id) where.quarter_id = parseInt(quarter_id as string);

    // Handle mrfc_id filtering: 0 means general meetings (NULL), otherwise filter by specific MRFC
    if (mrfc_id !== undefined) {
      const mrfcIdNum = parseInt(mrfc_id as string);
      if (mrfcIdNum === 0) {
        where.mrfc_id = null; // General meetings
      } else {
        where.mrfc_id = mrfcIdNum; // Specific MRFC
      }
    }

    if (status) where.status = status;

    // Step 4: Apply user MRFC access filter for USER role
    if (req.user?.role === 'USER') {
      const userMrfcIds = req.user.mrfcAccess || [];
      if (userMrfcIds.length > 0) {
        where.mrfc_id = { [Op.in]: userMrfcIds };
      } else {
        // User has no MRFC access, return empty result
        return res.json({
          success: true,
          data: {
            agendas: [],
            pagination: {
              page: pageNum,
              limit: limitNum,
              total: 0,
              totalPages: 0,
              hasNext: false,
              hasPrev: false
            }
          }
        });
      }
    }

    // Step 5: Query agendas (meetings)
    const { count, rows: agendas } = await Agenda.findAndCountAll({
      where,
      include: [
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'name', 'quarter_number', 'year', 'start_date', 'end_date']
        },
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'name', 'municipality', 'province'],
          include: [{
            model: Proponent,
            as: 'proponents',
            attributes: ['id', 'company_name', 'contact_person'],
            limit: 5 // Limit proponents to avoid excessive data
          }]
        }
      ],
      limit: limitNum,
      offset,
      order: [[sort_by as string, sort_order as string]]
    });

    // Step 6: Transform agendas to include 'quarter' field for Android compatibility
    const transformedAgendas = agendas.map((agenda: any) => {
      const agendaData = agenda.toJSON();
      if (agendaData.quarter && agendaData.quarter.quarter_number) {
        // Add 'quarter' field as "Q1", "Q2", etc.
        agendaData.quarter.quarter = `Q${agendaData.quarter.quarter_number}`;
      }
      return agendaData;
    });

    // Step 7: Return paginated results
    return res.json({
      success: true,
      data: {
        agendas: transformedAgendas,
        pagination: {
          page: pageNum,
          limit: limitNum,
          total: count,
          totalPages: Math.ceil(count / limitNum),
          hasNext: pageNum * limitNum < count,
          hasPrev: pageNum > 1
        }
      }
    });
  } catch (error: any) {
    console.error('Agenda listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AGENDA_LISTING_FAILED',
        message: error.message || 'Failed to retrieve agendas'
      }
    });
  }
});

/**
 * ================================================
 * POST /agendas
 * ================================================
 * Create new agenda item for quarterly meeting
 * ADMIN only
 *
 * REQUEST BODY:
 * {
 *   "quarter_id": 1,
 *   "mrfc_id": 25,
 *   "agenda_number": "Item 5",
 *   "status": "SCHEDULED",
 *   "discussion_notes": "Initial notes...",
 *   "decision": null
 * }
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "Agenda item created successfully",
 *   "data": {
 *     "id": 15,
 *     "quarter_id": 1,
 *     "mrfc_id": 25,
 *     "agenda_number": "Item 5",
 *     "status": "SCHEDULED",
 *     "created_at": "2025-01-15T09:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Validate input with Joi schema
 * 2. Verify quarter_id exists in quarters table
 * 3. Verify mrfc_id exists in mrfcs table
 * 4. Check if MRFC already has agenda for this quarter (prevent duplicates)
 * 5. Validate agenda_number format (e.g., "Item 1", "Item 2")
 * 6. Create agenda record
 * 7. Create audit log entry
 * 8. Optionally: Create notification for assigned users
 * 9. Return created agenda data
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin
 * - 400: Validation error
 * - 404: Quarter or MRFC not found
 * - 409: MRFC already has agenda for this quarter
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Each MRFC can only appear once per quarter
 * - agenda_number should be unique within a quarter
 * - Default status is SCHEDULED
 * - discussion_notes and decision are optional initially
 */
router.post('/', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    const { Agenda, Quarter, Mrfc, AuditLog } = require('../models');

    // Step 1: Validate required fields
    const { mrfc_id, quarter_id, meeting_date, meeting_time, location, status } = req.body;

    // mrfc_id can be null for general meetings, but quarter_id and meeting_date are required
    if (quarter_id === undefined || quarter_id === null || !meeting_date) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'Missing required fields: quarter_id, meeting_date'
        }
      });
    }

    // Step 2: Verify quarter exists
    const quarter = await Quarter.findByPk(quarter_id);
    if (!quarter) {
      return res.status(404).json({
        success: false,
        error: { code: 'QUARTER_NOT_FOUND', message: 'Quarter not found' }
      });
    }

    // Step 3: Verify MRFC exists (only if mrfc_id is provided)
    let mrfc = null;
    if (mrfc_id) {
      mrfc = await Mrfc.findByPk(mrfc_id);
      if (!mrfc) {
        return res.status(404).json({
          success: false,
          error: { code: 'MRFC_NOT_FOUND', message: 'MRFC not found' }
        });
      }

      // Step 4: Check for duplicate (one meeting per MRFC per quarter)
      const existing = await Agenda.findOne({
        where: {
          quarter_id: quarter_id,
          mrfc_id: mrfc_id
        }
      });
      if (existing) {
        return res.status(409).json({
          success: false,
          error: {
            code: 'AGENDA_EXISTS',
            message: `This MRFC already has a meeting scheduled for ${quarter.name}`
          }
        });
      }
    }
    // For general meetings (mrfc_id is null), allow multiple meetings per quarter

    // Step 5: Create meeting (agenda)
    const agenda = await Agenda.create({
      mrfc_id,
      quarter_id,
      meeting_date,
      meeting_time: meeting_time || null,
      location: location || null,
      status: status || 'DRAFT'
    });

    // Step 6: Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'CREATE',
      entity_type: 'AGENDA',
      entity_id: agenda.id,
      old_values: null,
      new_values: {
        mrfc_id: agenda.mrfc_id,
        quarter_id: agenda.quarter_id,
        meeting_date: agenda.meeting_date,
        location: agenda.location
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    // Step 7: Fetch created agenda with relations
    const createdAgenda = await Agenda.findByPk(agenda.id, {
      include: [
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'name', 'quarter_number', 'year', 'start_date', 'end_date']
        },
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'name', 'municipality']
        }
      ]
    });

    // Step 8: Transform agenda to include 'quarter' field for Android compatibility
    const agendaData = createdAgenda?.toJSON();
    if (agendaData && agendaData.quarter && agendaData.quarter.quarter_number) {
      agendaData.quarter.quarter = `Q${agendaData.quarter.quarter_number}`;
    }

    // Step 9: Return created meeting
    const meetingType = mrfc ? `${mrfc.name} - ${quarter.name}` : `General Meeting - ${quarter.name}`;
    return res.status(201).json({
      success: true,
      message: `Meeting created successfully for ${meetingType}`,
      data: agendaData
    });
  } catch (error: any) {
    console.error('Agenda creation error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AGENDA_CREATION_FAILED',
        message: error.message || 'Failed to create meeting'
      }
    });
  }
});

/**
 * ================================================
 * GET /agendas/:id
 * ================================================
 * Get detailed agenda information by ID
 * USER: Must have access to the MRFC associated with this agenda
 * ADMIN: Can access any agenda
 *
 * URL PARAMETERS:
 * - id: number (agenda ID)
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/agendas/15
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "id": 15,
 *     "agenda_number": "Item 5",
 *     "quarter": {
 *       "id": 1,
 *       "quarter_number": 1,
 *       "year": 2025,
 *       "meeting_date": "2025-03-15T09:00:00Z",
 *       "meeting_location": "MGB Main Office"
 *     },
 *     "mrfc": {
 *       "id": 25,
 *       "mrfc_number": "MRFC-2025-001",
 *       "project_title": "Gold Mining Project",
 *       "proponent": {
 *         "company_name": "ABC Mining Corp.",
 *         "contact_person": "John Smith"
 *       }
 *     },
 *     "status": "DISCUSSED",
 *     "discussion_notes": "Detailed discussion notes from the meeting...",
 *     "decision": "Approved with conditions...",
 *     "attendance": [
 *       { "user": { "full_name": "Jane Doe" }, "present": true }
 *     ],
 *     "created_at": "2025-01-10T08:00:00Z",
 *     "updated_at": "2025-03-15T12:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse agenda ID from URL params
 * 2. Query agendas table with full details
 * 3. Include quarter, MRFC, proponent, and attendance information
 * 4. Authorization check:
 *    - If USER: Verify mrfc_id is in user's mrfc_access array
 *    - If ADMIN: Allow access
 * 5. If not found: Return 404
 * 6. Return complete agenda data
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: User does not have access to this agenda's MRFC
 * - 404: Agenda not found
 * - 500: Database error
 */
router.get('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { Agenda, Quarter, Mrfc, Proponent, Attendance, MatterArising, User } = require('../models');

    // Step 1: Parse and validate ID
    const agendaId = parseInt(req.params.id);
    if (isNaN(agendaId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid agenda ID' }
      });
    }

    // Step 2: Find meeting with full details
    const agenda = await Agenda.findByPk(agendaId, {
      include: [
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'name', 'quarter_number', 'year', 'start_date', 'end_date']
        },
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'name', 'municipality', 'province', 'contact_person', 'contact_number'],
          include: [{
            model: Proponent,
            as: 'proponents',
            attributes: ['id', 'company_name', 'contact_person', 'status']
          }]
        },
        {
          model: Attendance,
          as: 'attendance',
          include: [{
            model: Proponent,
            as: 'proponent',
            attributes: ['id', 'company_name', 'contact_person']
          }, {
            model: User,
            as: 'marker',
            attributes: ['id', 'full_name', 'username']
          }]
        },
        {
          model: MatterArising,
          as: 'matters_arising',
          attributes: ['id', 'issue', 'status', 'assigned_to', 'date_raised', 'date_resolved']
        }
      ]
    });

    // Step 3: Check if exists
    if (!agenda) {
      return res.status(404).json({
        success: false,
        error: { code: 'AGENDA_NOT_FOUND', message: 'Meeting not found' }
      });
    }

    // Step 4: Authorization check for USER role
    if (req.user?.role === 'USER') {
      const userMrfcIds = req.user.mrfcAccess || [];
      if (!userMrfcIds.includes(agenda.mrfc_id)) {
        return res.status(403).json({
          success: false,
          error: {
            code: 'MRFC_ACCESS_DENIED',
            message: 'You do not have access to this meeting'
          }
        });
      }
    }

    // Step 5: Transform agenda to include 'quarter' field for Android compatibility
    const agendaData = agenda.toJSON();
    if (agendaData.quarter && agendaData.quarter.quarter_number) {
      agendaData.quarter.quarter = `Q${agendaData.quarter.quarter_number}`;
    }

    // Step 6: Return meeting data
    return res.json({ success: true, data: agendaData });
  } catch (error: any) {
    console.error('Get agenda error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_AGENDA_FAILED',
        message: error.message || 'Failed to retrieve meeting'
      }
    });
  }
});

/**
 * ================================================
 * PUT /agendas/:id
 * ================================================
 * Update agenda information
 * ADMIN only
 *
 * URL PARAMETERS:
 * - id: number (agenda ID)
 *
 * REQUEST BODY:
 * {
 *   "agenda_number": "Item 5A",
 *   "status": "DISCUSSED",
 *   "discussion_notes": "Updated discussion notes...",
 *   "decision": "Approved with the following conditions..."
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Agenda updated successfully",
 *   "data": {
 *     "id": 15,
 *     "agenda_number": "Item 5A",
 *     "status": "DISCUSSED",
 *     "updated_at": "2025-01-15T10:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse agenda ID from URL params
 * 2. Find agenda in database
 * 3. If not found: Return 404
 * 4. Validate status transitions (optional business logic)
 * 5. Update agenda record
 * 6. Create audit log entry
 * 7. If status changed to APPROVED/REJECTED: Create notifications
 * 8. Return updated agenda data
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin
 * - 404: Agenda not found
 * - 400: Validation error or invalid status transition
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Cannot change quarter_id or mrfc_id after creation
 * - Status transitions should be logical (e.g., SCHEDULED -> DISCUSSED -> APPROVED)
 * - discussion_notes required when status is DISCUSSED
 * - decision required when status is APPROVED or REJECTED
 */
router.put('/:id', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    const { Agenda, Quarter, Mrfc, AuditLog } = require('../models');

    // Step 1: Parse and validate ID
    const agendaId = parseInt(req.params.id);
    if (isNaN(agendaId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid agenda ID' }
      });
    }

    // Step 2: Find existing meeting
    const agenda = await Agenda.findByPk(agendaId);
    if (!agenda) {
      return res.status(404).json({
        success: false,
        error: { code: 'AGENDA_NOT_FOUND', message: 'Meeting not found' }
      });
    }

    // Step 3: Store old values for audit log
    const oldValues = {
      meeting_date: agenda.meeting_date,
      meeting_time: agenda.meeting_time,
      location: agenda.location,
      status: agenda.status
    };

    // Step 4: Extract updatable fields from request body
    const { meeting_date, meeting_time, location, status } = req.body;

    // Step 5: Validate status if provided
    const validStatuses = ['DRAFT', 'PUBLISHED', 'COMPLETED', 'CANCELLED'];
    if (status && !validStatuses.includes(status)) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_STATUS',
          message: `Status must be one of: ${validStatuses.join(', ')}`
        }
      });
    }

    // Step 6: Update meeting fields
    if (meeting_date) agenda.meeting_date = meeting_date;
    if (meeting_time !== undefined) agenda.meeting_time = meeting_time;
    if (location !== undefined) agenda.location = location;
    if (status) agenda.status = status;

    await agenda.save();

    // Step 7: Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'UPDATE',
      entity_type: 'AGENDA',
      entity_id: agenda.id,
      old_values: oldValues,
      new_values: {
        meeting_date: agenda.meeting_date,
        meeting_time: agenda.meeting_time,
        location: agenda.location,
        status: agenda.status
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    // Step 8: Fetch updated meeting with relations
    const updatedAgenda = await Agenda.findByPk(agenda.id, {
      include: [
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'name', 'quarter_number', 'year', 'start_date', 'end_date']
        },
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'name', 'municipality']
        }
      ]
    });

    // Step 9: Transform agenda to include 'quarter' field for Android compatibility
    const agendaData = updatedAgenda?.toJSON();
    if (agendaData && agendaData.quarter && agendaData.quarter.quarter_number) {
      agendaData.quarter.quarter = `Q${agendaData.quarter.quarter_number}`;
    }

    // Step 10: Return updated meeting
    return res.json({
      success: true,
      message: 'Meeting updated successfully',
      data: agendaData
    });
  } catch (error: any) {
    console.error('Agenda update error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AGENDA_UPDATE_FAILED',
        message: error.message || 'Failed to update meeting'
      }
    });
  }
});

/**
 * ================================================
 * DELETE /agendas/:id
 * ================================================
 * Delete agenda item
 * ADMIN only
 *
 * URL PARAMETERS:
 * - id: number (agenda ID)
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Agenda deleted successfully"
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse agenda ID from URL params
 * 2. Find agenda in database
 * 3. If not found: Return 404
 * 4. Check if agenda has been discussed (status = DISCUSSED/APPROVED/REJECTED)
 * 5. If discussed: Prevent deletion or require confirmation
 * 6. Delete related records (attendance, notifications)
 * 7. Delete agenda record
 * 8. Create audit log entry
 * 9. Return success message
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin
 * - 404: Agenda not found
 * - 409: Cannot delete discussed/completed agenda
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Can only delete SCHEDULED agendas (not yet discussed)
 * - Once an agenda is DISCUSSED, it should not be deleted (historical record)
 * - Cascade delete: attendance records, notifications
 */
router.delete('/:id', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    const { Agenda, Attendance, MatterArising, AuditLog } = require('../models');
    const sequelize = require('../config/database').default;

    // Step 1: Parse and validate ID
    const agendaId = parseInt(req.params.id);
    if (isNaN(agendaId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid agenda ID' }
      });
    }

    // Step 2: Find meeting
    const agenda = await Agenda.findByPk(agendaId);
    if (!agenda) {
      return res.status(404).json({
        success: false,
        error: { code: 'AGENDA_NOT_FOUND', message: 'Meeting not found' }
      });
    }

    // Step 3: Check if can be deleted (only DRAFT meetings can be deleted)
    if (agenda.status === 'COMPLETED') {
      return res.status(409).json({
        success: false,
        error: {
          code: 'CANNOT_DELETE_COMPLETED_MEETING',
          message: 'Cannot delete completed meetings (historical record)'
        }
      });
    }

    // Step 4: Store meeting info for audit log
    const meetingInfo = {
      mrfc_id: agenda.mrfc_id,
      quarter_id: agenda.quarter_id,
      meeting_date: agenda.meeting_date,
      location: agenda.location,
      status: agenda.status
    };

    // Step 5: Delete with transaction (CASCADE will handle related records)
    await sequelize.transaction(async (t: any) => {
      // Delete related records explicitly for audit trail
      const attendanceCount = await Attendance.count({ where: { agenda_id: agenda.id }, transaction: t });
      const mattersCount = await MatterArising.count({ where: { agenda_id: agenda.id }, transaction: t });

      await Attendance.destroy({ where: { agenda_id: agenda.id }, transaction: t });
      await MatterArising.destroy({ where: { agenda_id: agenda.id }, transaction: t });

      // Delete meeting
      await agenda.destroy({ transaction: t });

      // Create audit log
      await AuditLog.create({
        user_id: req.user?.userId,
        action: 'DELETE',
        entity_type: 'AGENDA',
        entity_id: agenda.id,
        old_values: meetingInfo,
        new_values: null,
        ip_address: req.ip,
        user_agent: req.headers['user-agent']
      }, { transaction: t });

      console.log(`Meeting ${agendaId} deleted: ${attendanceCount} attendance records, ${mattersCount} matters arising removed`);
    });

    // Step 6: Return success
    return res.json({
      success: true,
      message: 'Meeting deleted successfully'
    });
  } catch (error: any) {
    console.error('Agenda deletion error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AGENDA_DELETION_FAILED',
        message: error.message || 'Failed to delete meeting'
      }
    });
  }
});

export default router;
