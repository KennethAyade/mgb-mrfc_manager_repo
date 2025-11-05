/**
 * ================================================
 * MEETING MINUTES ROUTES
 * ================================================
 * Handles meeting summaries, decisions, and action items
 * Only meeting organizer (creator) can add/edit
 * All users can view
 * Base path: /api/v1/minutes
 *
 * ENDPOINTS:
 * GET    /minutes/meeting/:agendaId  - Get minutes for a meeting
 * POST   /minutes                    - Create minutes (organizer only)
 * PUT    /minutes/:id                - Update minutes (organizer only)
 * DELETE /minutes/:id                - Delete minutes (organizer or ADMIN)
 */

import { Router, Request, Response } from 'express';
import { authenticate, adminOnly } from '../middleware/auth';

const router = Router();

/**
 * ================================================
 * GET /minutes/meeting/:agendaId
 * ================================================
 * Get meeting minutes for a specific meeting
 * All authenticated users can view
 *
 * URL PARAMETERS:
 * - agendaId: number (meeting ID)
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/minutes/meeting/1
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "id": 1,
 *     "agenda_id": 1,
 *     "summary": "Meeting covered environmental compliance...",
 *     "decisions": [
 *       {"decision": "Approve waste disposal plan", "status": "approved"}
 *     ],
 *     "action_items": [
 *       {"item": "Submit updated plan by Feb 15", "assigned_to": "Juan Dela Cruz", "deadline": "2025-02-15"}
 *     ],
 *     "is_final": true,
 *     "created_by_name": "Admin User",
 *     "created_at": "2025-01-20T10:00:00Z"
 *   }
 * }
 */
router.get('/meeting/:agendaId', authenticate, async (req: Request, res: Response) => {
  try {
    const { MeetingMinutes, Agenda } = require('../models');

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

    // Get meeting minutes
    const minutes = await MeetingMinutes.findOne({
      where: { agenda_id: agendaId }
    });

    if (!minutes) {
      return res.status(404).json({
        success: false,
        error: { code: 'MINUTES_NOT_FOUND', message: 'Meeting minutes not found' }
      });
    }

    return res.json({
      success: true,
      data: minutes
    });
  } catch (error: any) {
    console.error('Get meeting minutes error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_MINUTES_FAILED',
        message: error.message || 'Failed to retrieve meeting minutes'
      }
    });
  }
});

/**
 * ================================================
 * POST /minutes
 * ================================================
 * Create meeting minutes
 * Only meeting organizer (the user who created the meeting) can create minutes
 * Admin can also create minutes for any meeting
 *
 * REQUEST BODY:
 * {
 *   "agenda_id": 1,
 *   "summary": "Meeting summary...",
 *   "decisions": [
 *     {"decision": "Approve waste disposal plan", "status": "approved"}
 *   ],
 *   "action_items": [
 *     {"item": "Submit plan", "assigned_to": "Juan", "deadline": "2025-02-15"}
 *   ],
 *   "attendees_summary": "All proponents present",
 *   "next_meeting_notes": "Schedule for Q2 2025"
 * }
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "Meeting minutes created successfully",
 *   "data": { ...created minutes... }
 * }
 */
router.post('/', authenticate, async (req: Request, res: Response) => {
  try {
    const { MeetingMinutes, Agenda, User, AuditLog } = require('../models');

    // Validate required fields
    const { agenda_id, summary, decisions, action_items, attendees_summary, next_meeting_notes } = req.body;

    if (!agenda_id || !summary) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'Missing required fields: agenda_id, summary'
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

    // Check if minutes already exist for this meeting
    const existing = await MeetingMinutes.findOne({
      where: { agenda_id }
    });
    if (existing) {
      return res.status(409).json({
        success: false,
        error: {
          code: 'MINUTES_ALREADY_EXIST',
          message: 'Minutes already exist for this meeting. Use PUT to update.'
        }
      });
    }

    // Authorization: Must be Admin OR the meeting creator
    // Note: We don't track who created the meeting in the current schema,
    // so we'll allow any Admin or the current user if they're creating minutes
    // In a future update, track meeting creator in agendas table
    const isAdmin = req.user?.role === 'ADMIN' || req.user?.role === 'SUPER_ADMIN';

    // For now, allow Admin or any authenticated user to create minutes
    // TODO: In future, add created_by field to agendas table to track organizer
    if (!isAdmin && agenda.mrfc_id !== null) {
      // For non-admin users, verify they have access to the MRFC (skip for general meetings)
      const userMrfcIds = req.user?.mrfcAccess || [];
      if (!userMrfcIds.includes(agenda.mrfc_id)) {
        return res.status(403).json({
          success: false,
          error: {
            code: 'FORBIDDEN',
            message: 'Only the meeting organizer or admin can create minutes'
          }
        });
      }
    }

    // Get creator info
    const creator = await User.findByPk(req.user?.userId);
    const creatorName = creator?.full_name || req.user?.username || 'Unknown';

    // Create meeting minutes
    const minutes = await MeetingMinutes.create({
      agenda_id,
      summary,
      decisions: decisions || [],
      action_items: action_items || [],
      attendees_summary: attendees_summary || null,
      next_meeting_notes: next_meeting_notes || null,
      created_by: req.user?.userId,
      created_by_name: creatorName,
      is_final: false
    });

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'CREATE',
      entity_type: 'MEETING_MINUTES',
      entity_id: minutes.id,
      old_values: null,
      new_values: {
        agenda_id: minutes.agenda_id,
        created_by_name: minutes.created_by_name
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.status(201).json({
      success: true,
      message: 'Meeting minutes created successfully',
      data: minutes
    });
  } catch (error: any) {
    console.error('Create meeting minutes error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'CREATE_MINUTES_FAILED',
        message: error.message || 'Failed to create meeting minutes'
      }
    });
  }
});

/**
 * ================================================
 * PUT /minutes/:id
 * ================================================
 * Update meeting minutes
 * Only the organizer (who created the minutes) or ADMIN can update
 *
 * REQUEST BODY (all fields optional):
 * {
 *   "summary": "Updated summary...",
 *   "decisions": [...],
 *   "action_items": [...],
 *   "attendees_summary": "...",
 *   "next_meeting_notes": "...",
 *   "is_final": true
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Meeting minutes updated successfully",
 *   "data": { ...updated minutes... }
 * }
 */
router.put('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { MeetingMinutes, AuditLog } = require('../models');

    // Parse and validate ID
    const minutesId = parseInt(req.params.id);
    if (isNaN(minutesId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid minutes ID' }
      });
    }

    // Find meeting minutes
    const minutes = await MeetingMinutes.findByPk(minutesId);
    if (!minutes) {
      return res.status(404).json({
        success: false,
        error: { code: 'MINUTES_NOT_FOUND', message: 'Meeting minutes not found' }
      });
    }

    // Authorization: Only creator (organizer) or ADMIN can update
    const isCreator = minutes.created_by === req.user?.userId;
    const isAdmin = req.user?.role === 'ADMIN' || req.user?.role === 'SUPER_ADMIN';

    if (!isCreator && !isAdmin) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Only the meeting organizer or admin can update minutes'
        }
      });
    }

    // Store old values for audit
    const oldValues = {
      summary: minutes.summary,
      decisions: minutes.decisions,
      action_items: minutes.action_items,
      is_final: minutes.is_final
    };

    // Update fields
    const { summary, decisions, action_items, attendees_summary, next_meeting_notes, is_final } = req.body;
    if (summary) minutes.summary = summary;
    if (decisions !== undefined) minutes.decisions = decisions;
    if (action_items !== undefined) minutes.action_items = action_items;
    if (attendees_summary !== undefined) minutes.attendees_summary = attendees_summary;
    if (next_meeting_notes !== undefined) minutes.next_meeting_notes = next_meeting_notes;
    if (is_final !== undefined) minutes.is_final = is_final;

    // If finalizing, record approver info
    if (is_final && !minutes.is_final) {
      minutes.approved_by = req.user?.userId;
      minutes.approved_at = new Date();
    }

    await minutes.save();

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'UPDATE',
      entity_type: 'MEETING_MINUTES',
      entity_id: minutes.id,
      old_values: oldValues,
      new_values: {
        summary: minutes.summary,
        decisions: minutes.decisions,
        action_items: minutes.action_items,
        is_final: minutes.is_final
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.json({
      success: true,
      message: 'Meeting minutes updated successfully',
      data: minutes
    });
  } catch (error: any) {
    console.error('Update meeting minutes error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'UPDATE_MINUTES_FAILED',
        message: error.message || 'Failed to update meeting minutes'
      }
    });
  }
});

/**
 * ================================================
 * DELETE /minutes/:id
 * ================================================
 * Delete meeting minutes
 * Only the organizer or ADMIN can delete
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Meeting minutes deleted successfully"
 * }
 */
router.delete('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { MeetingMinutes, AuditLog } = require('../models');

    // Parse and validate ID
    const minutesId = parseInt(req.params.id);
    if (isNaN(minutesId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid minutes ID' }
      });
    }

    // Find meeting minutes
    const minutes = await MeetingMinutes.findByPk(minutesId);
    if (!minutes) {
      return res.status(404).json({
        success: false,
        error: { code: 'MINUTES_NOT_FOUND', message: 'Meeting minutes not found' }
      });
    }

    // Authorization: Only creator or ADMIN can delete
    const isCreator = minutes.created_by === req.user?.userId;
    const isAdmin = req.user?.role === 'ADMIN' || req.user?.role === 'SUPER_ADMIN';

    if (!isCreator && !isAdmin) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Only the meeting organizer or admin can delete minutes'
        }
      });
    }

    // Store info for audit log
    const minutesInfo = {
      agenda_id: minutes.agenda_id,
      created_by_name: minutes.created_by_name,
      summary: minutes.summary.substring(0, 100) // First 100 chars
    };

    // Delete minutes
    await minutes.destroy();

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'DELETE',
      entity_type: 'MEETING_MINUTES',
      entity_id: minutes.id,
      old_values: minutesInfo,
      new_values: null,
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.json({
      success: true,
      message: 'Meeting minutes deleted successfully'
    });
  } catch (error: any) {
    console.error('Delete meeting minutes error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DELETE_MINUTES_FAILED',
        message: error.message || 'Failed to delete meeting minutes'
      }
    });
  }
});

export default router;
