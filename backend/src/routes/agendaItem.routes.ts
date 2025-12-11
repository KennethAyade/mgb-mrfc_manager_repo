/**
 * ================================================
 * AGENDA ITEMS ROUTES
 * ================================================
 * Handles discussion topics for meetings
 * All users can add agenda items
 * Auto-tagged with contributor name and username
 * Base path: /api/v1/agenda-items
 *
 * ENDPOINTS:
 * GET    /agenda-items/meeting/:agendaId       - List all items for a meeting
 * GET    /agenda-items/meeting/:agendaId/other-matters - List other matters items
 * POST   /agenda-items                         - Create new agenda item (ALL authenticated users)
 * PUT    /agenda-items/:id                     - Update item (creator or ADMIN only)
 * DELETE /agenda-items/:id                     - Delete item (creator or ADMIN only)
 * POST   /agenda-items/:id/toggle-highlight    - Toggle highlight status (ADMIN only)
 * POST   /agenda-items/:id/mark-other-matter   - Mark as other matter (ADMIN only)
 */

import { Router, Request, Response } from 'express';
import { authenticate, adminOnly } from '../middleware/auth';

const router = Router();

/**
 * ================================================
 * GET /agenda-items/meeting/:agendaId
 * GET /agenda-items/agenda/:agendaId (alias)
 * ================================================
 * List all agenda items for a specific meeting
 * All authenticated users can view
 *
 * URL PARAMETERS:
 * - agendaId: number (meeting ID)
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/agenda-items/meeting/1
 * GET /api/v1/agenda-items/agenda/1
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "agenda_items": [
 *       {
 *         "id": 1,
 *         "agenda_id": 1,
 *         "title": "Discussion on waste disposal practices",
 *         "description": "Review current practices and propose improvements",
 *         "added_by": 5,
 *         "added_by_name": "Juan Dela Cruz",
 *         "added_by_username": "jdelacruz",
 *         "order_index": 1,
 *         "created_at": "2025-01-15T08:00:00Z"
 *       }
 *     ],
 *     "total": 5
 *   }
 * }
 */

// Handler function for getting agenda items
const getAgendaItemsHandler = async (req: Request, res: Response) => {
  try {
    const { AgendaItem, Agenda, User } = require('../models');

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

    // Authorization check for USER role (skip for general meetings where mrfc_id is null)
    if (req.user?.role === 'USER' && agenda.mrfc_id !== null) {
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

    // Get all agenda items for this meeting
    // Regular users see APPROVED items + their own PROPOSED/DENIED items
    // Admins see all items (PROPOSED, APPROVED, DENIED) so they can review
    let agendaItems;

    if (req.user?.role === 'USER') {
      // Regular users see:
      // 1. All APPROVED items (for the Agenda tab) - excluding other matters
      // 2. Their own PROPOSED and DENIED items (for the Proposals tab)
      const { Op } = require('sequelize');
      agendaItems = await AgendaItem.findAll({
        where: {
          agenda_id: agendaId,
          is_other_matter: false,  // Exclude other matters from main agenda
          [Op.or]: [
            { status: 'APPROVED' },
            { added_by: req.user.userId }
          ]
        },
        order: [
          ['order_index', 'ASC'],
          ['created_at', 'ASC']
        ]
      });
    } else {
      // Admins see all items (excluding other matters from main agenda view)
      agendaItems = await AgendaItem.findAll({
        where: {
          agenda_id: agendaId,
          is_other_matter: false  // Exclude other matters from main agenda
        },
        order: [
          ['order_index', 'ASC'],
          ['created_at', 'ASC']
        ]
      });
    }

    return res.json({
      success: true,
      data: agendaItems  // Return array directly, not wrapped in object
    });
  } catch (error: any) {
    console.error('Get agenda items error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_AGENDA_ITEMS_FAILED',
        message: error.message || 'Failed to retrieve agenda items'
      }
    });
  }
};

// Register both routes (meeting and agenda paths for compatibility)
router.get('/meeting/:agendaId', authenticate, getAgendaItemsHandler);
router.get('/agenda/:agendaId', authenticate, getAgendaItemsHandler);

/**
 * ================================================
 * POST /agenda-items
 * ================================================
 * Create new agenda item
 * ALL authenticated users can add items (not just admins)
 * Auto-tags with contributor name and username from JWT
 *
 * REQUEST BODY:
 * {
 *   "agenda_id": 1,
 *   "title": "Discussion topic",
 *   "description": "Detailed description...",
 *   "order_index": 1
 * }
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "Agenda item added successfully",
 *   "data": {
 *     "id": 5,
 *     "agenda_id": 1,
 *     "title": "Discussion topic",
 *     "description": "Detailed description...",
 *     "added_by": 3,
 *     "added_by_name": "Juan Dela Cruz",
 *     "added_by_username": "jdelacruz",
 *     "created_at": "2025-01-15T09:00:00Z"
 *   }
 * }
 */
router.post('/', authenticate, async (req: Request, res: Response) => {
  try {
    const { AgendaItem, Agenda, User, AuditLog } = require('../models');

    // Validate required fields
    const { agenda_id, title, description, order_index, mrfc_id, proponent_id, file_category, is_other_matter } = req.body;

    if (!agenda_id || !title) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'Missing required fields: agenda_id, title'
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

    // Authorization check for USER role (skip for general meetings where mrfc_id is null)
    if (req.user?.role === 'USER' && agenda.mrfc_id !== null) {
      const userMrfcIds = req.user.mrfcAccess || [];
      if (!userMrfcIds.includes(agenda.mrfc_id)) {
        return res.status(403).json({
          success: false,
          error: {
            code: 'MRFC_ACCESS_DENIED',
            message: 'You do not have access to add items to this meeting'
          }
        });
      }
    }

    // Get contributor info from JWT (already includes name/username)
    const contributorName = req.user?.username || 'Unknown User';

    // Fetch full user details for complete info
    const contributor = await User.findByPk(req.user?.userId);
    const fullName = contributor?.full_name || contributorName;
    const username = contributor?.username || contributorName;

    // Determine status based on user role
    // Regular users create PROPOSED items, Admins create APPROVED items
    const userRole = req.user?.role;
    const isAdmin = userRole === 'ADMIN' || userRole === 'SUPER_ADMIN';
    const itemStatus = isAdmin ? 'APPROVED' : 'PROPOSED';

    // Create agenda item with auto-tagging
    const agendaItem = await AgendaItem.create({
      agenda_id,
      title,
      description: description || null,
      status: itemStatus,
      added_by: req.user?.userId,
      added_by_name: fullName,
      added_by_username: username,
      order_index: order_index || 0,
      mrfc_id: mrfc_id || null,
      proponent_id: proponent_id || null,
      file_category: file_category || null,
      is_other_matter: is_other_matter === true  // Default false unless explicitly true
    });

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'CREATE',
      entity_type: 'AGENDA_ITEM',
      entity_id: agendaItem.id,
      old_values: null,
      new_values: {
        agenda_id: agendaItem.agenda_id,
        title: agendaItem.title,
        added_by_name: agendaItem.added_by_name
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.status(201).json({
      success: true,
      message: `Agenda item added successfully by ${fullName}`,
      data: agendaItem
    });
  } catch (error: any) {
    console.error('Create agenda item error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'CREATE_AGENDA_ITEM_FAILED',
        message: error.message || 'Failed to create agenda item'
      }
    });
  }
});

/**
 * ================================================
 * PUT /agenda-items/:id
 * ================================================
 * Update agenda item
 * Only the creator or ADMIN can update
 *
 * REQUEST BODY:
 * {
 *   "title": "Updated title",
 *   "description": "Updated description",
 *   "order_index": 2
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Agenda item updated successfully",
 *   "data": { ... }
 * }
 */
router.put('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { AgendaItem, AuditLog } = require('../models');

    // Parse and validate ID
    const itemId = parseInt(req.params.id);
    if (isNaN(itemId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid agenda item ID' }
      });
    }

    // Find agenda item
    const agendaItem = await AgendaItem.findByPk(itemId);
    if (!agendaItem) {
      return res.status(404).json({
        success: false,
        error: { code: 'AGENDA_ITEM_NOT_FOUND', message: 'Agenda item not found' }
      });
    }

    // Authorization: Only creator or ADMIN can update
    const isCreator = agendaItem.added_by === req.user?.userId;
    const isAdmin = req.user?.role === 'ADMIN' || req.user?.role === 'SUPER_ADMIN';

    if (!isCreator && !isAdmin) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Only the creator or admin can update this agenda item'
        }
      });
    }

    // Store old values for audit
    const oldValues = {
      title: agendaItem.title,
      description: agendaItem.description,
      order_index: agendaItem.order_index
    };

    // Update fields
    const { title, description, order_index } = req.body;
    if (title) agendaItem.title = title;
    if (description !== undefined) agendaItem.description = description;
    if (order_index !== undefined) agendaItem.order_index = order_index;

    await agendaItem.save();

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'UPDATE',
      entity_type: 'AGENDA_ITEM',
      entity_id: agendaItem.id,
      old_values: oldValues,
      new_values: {
        title: agendaItem.title,
        description: agendaItem.description,
        order_index: agendaItem.order_index
      },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.json({
      success: true,
      message: 'Agenda item updated successfully',
      data: agendaItem
    });
  } catch (error: any) {
    console.error('Update agenda item error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'UPDATE_AGENDA_ITEM_FAILED',
        message: error.message || 'Failed to update agenda item'
      }
    });
  }
});

/**
 * ================================================
 * DELETE /agenda-items/:id
 * ================================================
 * Delete agenda item
 * Only the creator or ADMIN can delete
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Agenda item deleted successfully"
 * }
 */
router.delete('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { AgendaItem, AuditLog } = require('../models');

    // Parse and validate ID
    const itemId = parseInt(req.params.id);
    if (isNaN(itemId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid agenda item ID' }
      });
    }

    // Find agenda item
    const agendaItem = await AgendaItem.findByPk(itemId);
    if (!agendaItem) {
      return res.status(404).json({
        success: false,
        error: { code: 'AGENDA_ITEM_NOT_FOUND', message: 'Agenda item not found' }
      });
    }

    // Authorization: Only creator or ADMIN can delete
    const isCreator = agendaItem.added_by === req.user?.userId;
    const isAdmin = req.user?.role === 'ADMIN' || req.user?.role === 'SUPER_ADMIN';

    if (!isCreator && !isAdmin) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Only the creator or admin can delete this agenda item'
        }
      });
    }

    // Store info for audit log
    const itemInfo = {
      title: agendaItem.title,
      added_by_name: agendaItem.added_by_name,
      agenda_id: agendaItem.agenda_id
    };

    // Delete item
    await agendaItem.destroy();

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'DELETE',
      entity_type: 'AGENDA_ITEM',
      entity_id: agendaItem.id,
      old_values: itemInfo,
      new_values: null,
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    return res.json({
      success: true,
      message: 'Agenda item deleted successfully'
    });
  } catch (error: any) {
    console.error('Delete agenda item error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DELETE_AGENDA_ITEM_FAILED',
        message: error.message || 'Failed to delete agenda item'
      }
    });
  }
});

/**
 * ================================================
 * GET /agenda-items/my-proposals
 * ================================================
 * Get current user's proposed agenda items (all statuses)
 * Regular users can see their own PROPOSED, APPROVED, DENIED items
 */
router.get('/my-proposals', authenticate, async (req: Request, res: Response) => {
  try {
    const { AgendaItem } = require('../models');

    const items = await AgendaItem.findAll({
      where: { added_by: req.user?.userId },
      order: [['created_at', 'DESC']]
    });

    console.log(`📋 Found ${items.length} proposal(s) for user ${req.user?.userId}`);

    res.json({
      success: true,
      data: items
    });
  } catch (error: any) {
    console.error('Get my proposals error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'FETCH_FAILED',
        message: error.message || 'Failed to fetch proposals'
      }
    });
  }
});

/**
 * ================================================
 * POST /agenda-items/:id/approve
 * ================================================
 * Approve a proposed agenda item (ADMIN only)
 */
router.post('/:id/approve', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    const { AgendaItem } = require('../models');
    const itemId = parseInt(req.params.id);

    const item = await AgendaItem.findByPk(itemId);
    if (!item) {
      return res.status(404).json({
        success: false,
        error: { code: 'ITEM_NOT_FOUND', message: 'Agenda item not found' }
      });
    }

    if (item.status !== 'PROPOSED') {
      return res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_STATUS',
          message: 'Only PROPOSED items can be approved'
        }
      });
    }

    await item.update({
      status: 'APPROVED',
      approved_by: req.user?.userId,
      approved_at: new Date()
    });

    console.log(`✅ Agenda item ${itemId} approved by user ${req.user?.userId}`);

    res.json({
      success: true,
      message: 'Agenda item approved',
      data: item
    });
  } catch (error: any) {
    console.error('Approve agenda item error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'APPROVAL_FAILED',
        message: error.message || 'Failed to approve item'
      }
    });
  }
});

/**
 * ================================================
 * POST /agenda-items/:id/deny
 * ================================================
 * Deny a proposed agenda item with remarks (ADMIN only)
 */
router.post('/:id/deny', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    const { AgendaItem } = require('../models');
    const itemId = parseInt(req.params.id);
    const { denial_remarks } = req.body;

    if (!denial_remarks || denial_remarks.trim().length === 0) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'REMARKS_REQUIRED',
          message: 'Denial remarks are required'
        }
      });
    }

    const item = await AgendaItem.findByPk(itemId);
    if (!item) {
      return res.status(404).json({
        success: false,
        error: { code: 'ITEM_NOT_FOUND', message: 'Agenda item not found' }
      });
    }

    if (item.status !== 'PROPOSED') {
      return res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_STATUS',
          message: 'Only PROPOSED items can be denied'
        }
      });
    }

    await item.update({
      status: 'DENIED',
      denied_by: req.user?.userId,
      denied_at: new Date(),
      denial_remarks: denial_remarks.trim()
    });

    console.log(`❌ Agenda item ${itemId} denied by user ${req.user?.userId}`);

    res.json({
      success: true,
      message: 'Agenda item denied',
      data: item
    });
  } catch (error: any) {
    console.error('Deny agenda item error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DENIAL_FAILED',
        message: error.message || 'Failed to deny item'
      }
    });
  }
});

/**
 * ================================================
 * GET /agenda-items/meeting/:agendaId/other-matters
 * ================================================
 * List all "Other Matters" items for a specific meeting
 * All authenticated users can view
 */
router.get('/meeting/:agendaId/other-matters', authenticate, async (req: Request, res: Response) => {
  try {
    const { AgendaItem, Agenda } = require('../models');

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
    if (req.user?.role === 'USER' && agenda.mrfc_id !== null) {
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

    // Get other matters items
    let otherMatters;
    if (req.user?.role === 'USER') {
      const { Op } = require('sequelize');
      otherMatters = await AgendaItem.findAll({
        where: {
          agenda_id: agendaId,
          is_other_matter: true,
          [Op.or]: [
            { status: 'APPROVED' },
            { added_by: req.user.userId }
          ]
        },
        order: [
          ['order_index', 'ASC'],
          ['created_at', 'ASC']
        ]
      });
    } else {
      otherMatters = await AgendaItem.findAll({
        where: {
          agenda_id: agendaId,
          is_other_matter: true
        },
        order: [
          ['order_index', 'ASC'],
          ['created_at', 'ASC']
        ]
      });
    }

    return res.json({
      success: true,
      data: otherMatters
    });
  } catch (error: any) {
    console.error('Get other matters error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_OTHER_MATTERS_FAILED',
        message: error.message || 'Failed to retrieve other matters'
      }
    });
  }
});

/**
 * ================================================
 * POST /agenda-items/:id/toggle-highlight
 * ================================================
 * Toggle highlight status of an agenda item (ADMIN only)
 * When highlighted, all users see a green background
 */
router.post('/:id/toggle-highlight', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    const { AgendaItem, AuditLog } = require('../models');
    const itemId = parseInt(req.params.id);

    if (isNaN(itemId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid agenda item ID' }
      });
    }

    const item = await AgendaItem.findByPk(itemId);
    if (!item) {
      return res.status(404).json({
        success: false,
        error: { code: 'ITEM_NOT_FOUND', message: 'Agenda item not found' }
      });
    }

    // Toggle highlight status
    const newHighlightStatus = !item.is_highlighted;
    await item.update({
      is_highlighted: newHighlightStatus,
      highlighted_by: newHighlightStatus ? req.user?.userId : null,
      highlighted_at: newHighlightStatus ? new Date() : null
    });

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'UPDATE',
      entity_type: 'AGENDA_ITEM',
      entity_id: item.id,
      old_values: { is_highlighted: !newHighlightStatus },
      new_values: { is_highlighted: newHighlightStatus },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    console.log(`🔦 Agenda item ${itemId} ${newHighlightStatus ? 'highlighted' : 'unhighlighted'} by user ${req.user?.userId}`);

    res.json({
      success: true,
      message: `Agenda item ${newHighlightStatus ? 'highlighted' : 'unhighlighted'}`,
      data: item
    });
  } catch (error: any) {
    console.error('Toggle highlight error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'TOGGLE_HIGHLIGHT_FAILED',
        message: error.message || 'Failed to toggle highlight'
      }
    });
  }
});

/**
 * ================================================
 * POST /agenda-items/:id/mark-other-matter
 * ================================================
 * Mark/unmark an agenda item as "Other Matter" (ADMIN only)
 */
router.post('/:id/mark-other-matter', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    const { AgendaItem, AuditLog } = require('../models');
    const itemId = parseInt(req.params.id);
    const { is_other_matter } = req.body;

    if (isNaN(itemId)) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_ID', message: 'Invalid agenda item ID' }
      });
    }

    const item = await AgendaItem.findByPk(itemId);
    if (!item) {
      return res.status(404).json({
        success: false,
        error: { code: 'ITEM_NOT_FOUND', message: 'Agenda item not found' }
      });
    }

    const oldValue = item.is_other_matter;
    const newValue = is_other_matter === true;

    await item.update({
      is_other_matter: newValue
    });

    // Create audit log
    await AuditLog.create({
      user_id: req.user?.userId,
      action: 'UPDATE',
      entity_type: 'AGENDA_ITEM',
      entity_id: item.id,
      old_values: { is_other_matter: oldValue },
      new_values: { is_other_matter: newValue },
      ip_address: req.ip,
      user_agent: req.headers['user-agent']
    });

    console.log(`📋 Agenda item ${itemId} ${newValue ? 'marked as' : 'removed from'} other matters by user ${req.user?.userId}`);

    res.json({
      success: true,
      message: `Agenda item ${newValue ? 'marked as other matter' : 'removed from other matters'}`,
      data: item
    });
  } catch (error: any) {
    console.error('Mark other matter error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'MARK_OTHER_MATTER_FAILED',
        message: error.message || 'Failed to mark other matter'
      }
    });
  }
});

export default router;
