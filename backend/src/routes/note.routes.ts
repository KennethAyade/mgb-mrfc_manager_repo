/**
 * ================================================
 * PERSONAL NOTES ROUTES
 * ================================================
 * Handles user personal notes for MRFCs
 * Base path: /api/v1/notes
 *
 * ENDPOINTS:
 * GET    /notes         - List user's notes (filterable by MRFC)
 * POST   /notes         - Create new note
 * PUT    /notes/:id     - Update note
 * DELETE /notes/:id     - Delete note
 */

import { Router, Request, Response } from 'express';
import { authenticate } from '../middleware/auth';
import { Op } from 'sequelize';

const router = Router();

/**
 * ================================================
 * GET /notes
 * ================================================
 * List all notes created by the authenticated user
 * Users can only see their own notes
 *
 * QUERY PARAMETERS:
 * - mrfc_id: number (optional, filter by MRFC)
 * - search: string (optional, search in note content)
 * - page: number (default: 1)
 * - limit: number (default: 20, max: 100)
 * - sort_by: 'created_at' | 'updated_at' (default: 'updated_at')
 * - sort_order: 'ASC' | 'DESC' (default: 'DESC')
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/notes?mrfc_id=25&page=1&limit=20
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "notes": [
 *       {
 *         "id": 30,
 *         "mrfc": {
 *           "id": 25,
 *           "mrfc_number": "MRFC-2025-001",
 *           "project_title": "Gold Mining Project"
 *         },
 *         "content": "Need to follow up on environmental clearance documents",
 *         "is_pinned": true,
 *         "created_at": "2025-01-15T10:00:00Z",
 *         "updated_at": "2025-01-16T14:30:00Z"
 *       }
 *     ],
 *     "pagination": {
 *       "page": 1,
 *       "limit": 20,
 *       "total": 15,
 *       "totalPages": 1,
 *       "hasNext": false,
 *       "hasPrev": false
 *     }
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse query parameters
 * 2. Build WHERE clause:
 *    - Filter by user_id (always req.user.userId)
 *    - Optional filter by mrfc_id
 *    - Optional search in content
 * 3. Calculate pagination
 * 4. Query notes table with JOIN to mrfcs
 * 5. Order by is_pinned DESC first, then by sort_by
 * 6. Return notes with pagination
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 400: Invalid query parameters
 * - 500: Database error
 */
router.get('/', authenticate, async (req: Request, res: Response) => {
  try {
    const { Note, Mrfc } = require('../models');

    // Step 1: Parse query parameters
    const { mrfc_id, agenda_id, search, page = '1', limit = '20', sort_by = 'updated_at', sort_order = 'DESC' } = req.query;

    // Step 2: Build filter conditions - users can only see their own notes
    const where: any = { user_id: req.user?.userId };
    if (mrfc_id) where.mrfc_id = parseInt(mrfc_id as string);
    if (agenda_id) where.agenda_id = parseInt(agenda_id as string);
    if (search) where.content = { [Op.iLike]: `%${search}%` };

    // Step 3: Calculate pagination
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    // Step 4: Query notes
    const { count, rows: notes } = await Note.findAndCountAll({
      where,
      include: [{
        model: Mrfc,
        as: 'mrfc',
        attributes: ['id', 'name', 'municipality'],
        required: false
      }],
      limit: limitNum,
      offset,
      order: [
        ['is_pinned', 'DESC'],
        [sort_by as string, sort_order as string]
      ]
    });

    // Step 5: Return results
    return res.json({
      success: true,
      data: {
        notes,
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
    console.error('Note listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'NOTE_LISTING_FAILED',
        message: error.message || 'Failed to retrieve notes'
      }
    });
  }
});

/**
 * ================================================
 * POST /notes
 * ================================================
 * Create new personal note for an MRFC
 * All authenticated users can create notes
 *
 * REQUEST BODY:
 * {
 *   "mrfc_id": 25,
 *   "content": "Need to follow up on environmental clearance documents",
 *   "is_pinned": false
 * }
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "Note created successfully",
 *   "data": {
 *     "id": 30,
 *     "user_id": 5,
 *     "mrfc_id": 25,
 *     "content": "Need to follow up on environmental clearance documents",
 *     "is_pinned": false,
 *     "created_at": "2025-01-15T10:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Validate input with Joi schema
 * 2. Verify MRFC exists
 * 3. For USER role: Verify MRFC access
 * 4. Create note record:
 *    - user_id: req.user.userId (automatic)
 *    - mrfc_id: from request body
 *    - content: from request body
 *    - is_pinned: from request body (default false)
 * 5. Return created note
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 400: Validation error (empty content, etc.)
 * - 403: USER creating note for inaccessible MRFC
 * - 404: MRFC not found
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Notes are private (only visible to creator)
 * - Content is required and must not be empty
 * - USER can only create notes for MRFCs they have access to
 * - ADMIN can create notes for any MRFC
 * - is_pinned defaults to false
 */
router.post('/', authenticate, async (req: Request, res: Response) => {
  try {
    const { Note, Mrfc } = require('../models');

    const { mrfc_id, agenda_id, content, is_pinned, title } = req.body;

    // Validate content
    if (!content || content.trim() === '') {
      return res.status(400).json({
        success: false,
        error: { code: 'VALIDATION_ERROR', message: 'Note content is required' }
      });
    }

    // Step 1: Verify MRFC exists (if provided)
    if (mrfc_id) {
      const mrfc = await Mrfc.findByPk(mrfc_id);
      if (!mrfc) {
        return res.status(404).json({
          success: false,
          error: { code: 'MRFC_NOT_FOUND', message: 'MRFC not found' }
        });
      }

      // Step 2: Authorization check for USER role
      if (req.user?.role === 'USER') {
        const userMrfcIds = req.user.mrfcAccess || [];
        if (!userMrfcIds.includes(Number(mrfc.id))) {
          return res.status(403).json({
            success: false,
            error: {
              code: 'MRFC_ACCESS_DENIED',
              message: 'You do not have access to this MRFC'
            }
          });
        }
      }
    }

    // Step 3: Create note
    const note = await Note.create({
      user_id: req.user?.userId,
      mrfc_id: mrfc_id || null,
      agenda_id: agenda_id || null,
      title: title || null,
      content: content.trim(),
      is_pinned: is_pinned || false
    });

    // Step 4: Return created note
    return res.status(201).json({
      success: true,
      message: 'Note created successfully',
      data: note
    });
  } catch (error: any) {
    console.error('Note creation error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'NOTE_CREATION_FAILED',
        message: error.message || 'Failed to create note'
      }
    });
  }
});

/**
 * ================================================
 * PUT /notes/:id
 * ================================================
 * Update personal note
 * Users can only update their own notes
 *
 * URL PARAMETERS:
 * - id: number (note ID)
 *
 * REQUEST BODY:
 * {
 *   "content": "Updated note content",
 *   "is_pinned": true
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Note updated successfully",
 *   "data": {
 *     "id": 30,
 *     "content": "Updated note content",
 *     "is_pinned": true,
 *     "updated_at": "2025-01-16T14:30:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse note ID from URL params
 * 2. Find note in database
 * 3. If not found: Return 404
 * 4. Authorization check: Verify note belongs to current user
 * 5. Update note content and/or is_pinned
 * 6. Return updated note
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Trying to update another user's note
 * - 404: Note not found
 * - 400: Validation error
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Users can only update their own notes
 * - Cannot change mrfc_id or user_id
 * - Can update content and is_pinned flag
 */
router.put('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { Note } = require('../models');

    // Step 1: Find note
    const note = await Note.findByPk(req.params.id);
    if (!note) {
      return res.status(404).json({
        success: false,
        error: { code: 'NOTE_NOT_FOUND', message: 'Note not found' }
      });
    }

    // Step 2: Authorization check - users can only update their own notes
    if (Number(note.user_id) !== req.user?.userId) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'You can only update your own notes'
        }
      });
    }

    // Step 3: Update note
    await note.update({
      title: req.body.title !== undefined ? req.body.title : note.title,
      content: req.body.content !== undefined ? req.body.content : note.content,
      is_pinned: req.body.is_pinned !== undefined ? req.body.is_pinned : note.is_pinned
    });

    // Step 4: Return updated note
    return res.json({
      success: true,
      message: 'Note updated successfully',
      data: note
    });
  } catch (error: any) {
    console.error('Note update error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'NOTE_UPDATE_FAILED',
        message: error.message || 'Failed to update note'
      }
    });
  }
});

/**
 * ================================================
 * DELETE /notes/:id
 * ================================================
 * Delete personal note
 * Users can only delete their own notes
 *
 * URL PARAMETERS:
 * - id: number (note ID)
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Note deleted successfully"
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse note ID from URL params
 * 2. Find note in database
 * 3. If not found: Return 404
 * 4. Authorization check: Verify note belongs to current user
 * 5. Delete note record
 * 6. Return success message
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Trying to delete another user's note
 * - 404: Note not found
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Users can only delete their own notes
 * - Permanent deletion (no soft delete for notes)
 * - No cascade effects (notes are independent)
 */
router.delete('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    const { Note } = require('../models');

    // Step 1: Find note
    const note = await Note.findByPk(req.params.id);
    if (!note) {
      return res.status(404).json({
        success: false,
        error: { code: 'NOTE_NOT_FOUND', message: 'Note not found' }
      });
    }

    // Step 2: Authorization check - users can only delete their own notes
    if (Number(note.user_id) !== req.user?.userId) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'You can only delete your own notes'
        }
      });
    }

    // Step 3: Delete note
    await note.destroy();

    // Step 4: Return success
    return res.json({
      success: true,
      message: 'Note deleted successfully'
    });
  } catch (error: any) {
    console.error('Note deletion error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'NOTE_DELETION_FAILED',
        message: error.message || 'Failed to delete note'
      }
    });
  }
});

export default router;
