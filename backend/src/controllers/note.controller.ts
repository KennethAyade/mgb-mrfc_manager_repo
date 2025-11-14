/**
 * ================================================
 * PERSONAL NOTES CONTROLLER
 * ================================================
 * Handles personal notes management for users
 */

import { Request, Response } from 'express';
import { Note, Mrfc, Quarter } from '../models';
import sequelize from '../config/database';

/**
 * List user's personal notes
 * GET /api/v1/notes
 */
export const listNotes = async (req: Request, res: Response): Promise<void> => {
  try {
    const { mrfc_id, quarter_id, page = '1', limit = '20' } = req.query;
    const currentUser = (req as any).user;

    // Build filter conditions (always filter by current user)
    const where: any = { user_id: currentUser?.userId };
    if (mrfc_id) where.mrfc_id = mrfc_id;
    if (quarter_id) where.quarter_id = quarter_id;

    // Query notes with pagination
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    const { count, rows: notes } = await Note.findAndCountAll({
      where,
      include: [
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'mrfc_number', 'project_title'],
          required: false
        },
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'quarter_number', 'year'],
          required: false
        }
      ],
      limit: limitNum,
      offset,
      order: [['created_at', 'DESC']]
    });

    res.json({
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
};

/**
 * Create new note
 * POST /api/v1/notes
 */
export const createNote = async (req: Request, res: Response): Promise<void> => {
  try {
    const { title, content, mrfc_id, quarter_id, tags } = req.body;
    const currentUser = (req as any).user;

    // Create note
    const note = await Note.create({
      user_id: currentUser?.userId,
      title,
      content,
      mrfc_id: mrfc_id || null,
      quarter_id: quarter_id || null,
      tags: tags || []
    });

    res.status(201).json({
      success: true,
      message: 'Note created successfully',
      data: {
        ...note.toJSON(),
        id: Number(note.id),
        user_id: Number(note.user_id),
        mrfc_id: note.mrfc_id ? Number(note.mrfc_id) : null,
        quarter_id: note.quarter_id ? Number(note.quarter_id) : null
      }
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
};

/**
 * Update note
 * PUT /api/v1/notes/:id
 */
export const updateNote = async (req: Request, res: Response): Promise<void> => {
  try {
    const noteId = parseInt(req.params.id);
    const currentUser = (req as any).user;
    const { title, content, tags } = req.body;

    // Find note (ensure it belongs to current user)
    const note = await Note.findOne({
      where: { id: noteId, user_id: currentUser?.userId }
    });

    if (!note) {
      res.status(404).json({
        success: false,
        error: {
          code: 'NOTE_NOT_FOUND',
          message: 'Note not found or you do not have permission to edit it'
        }
      });
      return;
    }

    // Update note
    await note.update({
      title,
      content,
      tags
    });

    res.json({
      success: true,
      message: 'Note updated successfully',
      data: {
        ...note.toJSON(),
        id: Number(note.id),
        user_id: Number(note.user_id),
        mrfc_id: note.mrfc_id ? Number(note.mrfc_id) : null,
        quarter_id: note.quarter_id ? Number(note.quarter_id) : null
      }
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
};

/**
 * Delete note
 * DELETE /api/v1/notes/:id
 */
export const deleteNote = async (req: Request, res: Response): Promise<void> => {
  try {
    const noteId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find note (ensure it belongs to current user)
    const note = await Note.findOne({
      where: { id: noteId, user_id: currentUser?.userId }
    });

    if (!note) {
      res.status(404).json({
        success: false,
        error: {
          code: 'NOTE_NOT_FOUND',
          message: 'Note not found or you do not have permission to delete it'
        }
      });
      return;
    }

    // Delete note
    await note.destroy();

    res.json({
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
};
