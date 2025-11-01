/**
 * ================================================
 * MATTERS ARISING ROUTES
 * ================================================
 * Handles follow-up items from previous meetings
 * Base path: /api/v1/matters-arising
 *
 * ENDPOINTS:
 * GET    /matters-arising/meeting/:agendaId - Get matters for a meeting
 * POST   /matters-arising                    - Create matter arising
 * PUT    /matters-arising/:id                - Update matter (status/resolution)
 * DELETE /matters-arising/:id                - Delete matter
 */

import { Router, Request, Response } from 'express';
import { authenticate, adminOnly } from '../middleware/auth';

const router = Router();

/**
 * ================================================
 * GET /matters-arising/meeting/:agendaId
 * ================================================
 * Get all matters arising for a specific meeting
 * All authenticated users can view
 */
router.get('/meeting/:agendaId', authenticate, async (req: Request, res: Response) => {
  try {
    const { MatterArising, Agenda } = require('../models');

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

    // Get matters arising
    const matters = await MatterArising.findAll({
      where: { agenda_id: agendaId },
      order: [['date_raised', 'DESC'], ['created_at', 'DESC']]
    });

    // Calculate summary statistics
    const total = matters.length;
    const pending = matters.filter((m: any) => m.status === 'PENDING').length;
    const inProgress = matters.filter((m: any) => m.status === 'IN_PROGRESS').length;
    const resolved = matters.filter((m: any) => m.status === 'RESOLVED').length;

    return res.json({
      success: true,
      data: {
        matters,
        summary: {
          total,
          pending,
          in_progress: inProgress,
          resolved,
          resolution_rate: total > 0 ? parseFloat(((resolved / total) * 100).toFixed(2)) : 0
        }
      }
    });
  } catch (error: any) {
    console.error('Get matters arising error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_MATTERS_FAILED',
        message: error.message || 'Failed to retrieve matters arising'
      }
    });
  }
});

/**
 * ================================================
 * POST /matters-arising
 * ================================================
 * Create a new matter arising
 * Admin or meeting organizer can create
 *
 * REQUEST BODY:
 * {
 *   "agenda_id": 1,
 *   "issue": "Description of the issue",
 *   "assigned_to": "Person responsible",
 *   "date_raised": "2025-01-20",
 *   "remarks": "Additional notes"
 * }
 */
router.post('/', authenticate, async (req: Request, res: Response) => {
  try {
    const { MatterArising, Agenda, AuditLog } = require('../models');

    // Validate required fields
    const { agenda_id, issue, assigned_to, date_raised, remarks } = req.body;

    if (!agenda_id || !issue || !date_raised) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'Missing required fields: agenda_id, issue, date_raised'
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

    // Create matter arising
    const matter = await MatterArising.create({
      agenda_id,
      issue,
      assigned_to: assigned_to || null,
      date_raised,
      status: 'PENDING',
      remarks: remarks || null
    });

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'CREATE',
      entity_type: 'MATTER_ARISING',
      entity_id: matter.id,
      old_values: null,
      new_values: {
        agenda_id: matter.agenda_id,
        issue: matter.issue.substring(0, 100)
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.status(201).json({
      success: true,
      message: 'Matter arising created successfully',
      data: matter
    });
  } catch (error: any) {
    console.error('Create matter arising error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'CREATE_MATTER_FAILED',
        message: error.message || 'Failed to create matter arising'
      }
    });
  }
});

/**
 * ================================================
 * PUT /matters-arising/:id
 * ================================================
 * Update matter arising (status, resolution, etc.)
 * Admin or meeting organizer can update
 *
 * REQUEST BODY (all optional):
 * {
 *   "issue": "Updated description",
 *   "status": "IN_PROGRESS",
 *   "assigned_to": "Updated assignee",
 *   "date_resolved": "2025-02-15",
 *   "remarks": "Updated remarks"
 * }
 */
router.put('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { MatterArising, AuditLog } = require('../models');

    // Parse and validate ID
    const matterId = parseInt(req.params.id);
    if (isNaN(matterId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid matter ID' }
      });
    }

    // Find matter
    const matter = await MatterArising.findByPk(matterId);
    if (!matter) {
      return res.status(404).json({
        success: false,
        error: { code: 'MATTER_NOT_FOUND', message: 'Matter arising not found' }
      });
    }

    // Store old values for audit
    const oldValues = {
      issue: matter.issue,
      status: matter.status,
      assigned_to: matter.assigned_to,
      date_resolved: matter.date_resolved,
      remarks: matter.remarks
    };

    // Update fields
    const { issue, status, assigned_to, date_resolved, remarks } = req.body;
    if (issue !== undefined) matter.issue = issue;
    if (status !== undefined) matter.status = status;
    if (assigned_to !== undefined) matter.assigned_to = assigned_to;
    if (date_resolved !== undefined) matter.date_resolved = date_resolved;
    if (remarks !== undefined) matter.remarks = remarks;

    // Auto-set date_resolved when status changes to RESOLVED
    if (status === 'RESOLVED' && !matter.date_resolved) {
      matter.date_resolved = new Date().toISOString().split('T')[0];
    }

    await matter.save();

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'UPDATE',
      entity_type: 'MATTER_ARISING',
      entity_id: matter.id,
      old_values: oldValues,
      new_values: {
        issue: matter.issue,
        status: matter.status,
        assigned_to: matter.assigned_to,
        date_resolved: matter.date_resolved,
        remarks: matter.remarks
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.json({
      success: true,
      message: 'Matter arising updated successfully',
      data: matter
    });
  } catch (error: any) {
    console.error('Update matter arising error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'UPDATE_MATTER_FAILED',
        message: error.message || 'Failed to update matter arising'
      }
    });
  }
});

/**
 * ================================================
 * DELETE /matters-arising/:id
 * ================================================
 * Delete matter arising
 * Admin only
 */
router.delete('/:id', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    const { MatterArising, AuditLog } = require('../models');

    // Parse and validate ID
    const matterId = parseInt(req.params.id);
    if (isNaN(matterId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid matter ID' }
      });
    }

    // Find matter
    const matter = await MatterArising.findByPk(matterId);
    if (!matter) {
      return res.status(404).json({
        success: false,
        error: { code: 'MATTER_NOT_FOUND', message: 'Matter arising not found' }
      });
    }

    // Store info for audit log
    const matterInfo = {
      agenda_id: matter.agenda_id,
      issue: matter.issue.substring(0, 100),
      status: matter.status
    };

    // Delete matter
    await matter.destroy();

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'DELETE',
      entity_type: 'MATTER_ARISING',
      entity_id: matterId,
      old_values: matterInfo,
      new_values: null,
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.json({
      success: true,
      message: 'Matter arising deleted successfully'
    });
  } catch (error: any) {
    console.error('Delete matter arising error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DELETE_MATTER_FAILED',
        message: error.message || 'Failed to delete matter arising'
      }
    });
  }
});

export default router;
