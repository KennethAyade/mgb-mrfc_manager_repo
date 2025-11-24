/**
 * ================================================
 * VOICE RECORDING CONTROLLER
 * ================================================
 * Handles voice recording CRUD operations
 * All users have full access to create, read, and delete recordings
 */

import { Request, Response } from 'express';
import { VoiceRecording, User, Agenda } from '../models';
import { uploadToS3, deleteFromS3 } from '../config/s3';
import { cleanupTempFile } from '../middleware/upload';
import fs from 'fs';

/**
 * Upload voice recording
 * POST /api/v1/voice-recordings/upload
 */
export const uploadVoiceRecording = async (req: Request, res: Response): Promise<void> => {
  try {
    const file = req.file;
    const { agenda_id, recording_name, description, duration } = req.body;
    const userId = (req as any).user?.userId;

    // Validate required fields
    if (!file) {
      res.status(400).json({
        success: false,
        error: {
          code: 'FILE_REQUIRED',
          message: 'Audio file is required'
        }
      });
      return;
    }

    if (!agenda_id) {
      cleanupTempFile(file.path);
      res.status(400).json({
        success: false,
        error: {
          code: 'AGENDA_ID_REQUIRED',
          message: 'Agenda ID is required'
        }
      });
      return;
    }

    if (!recording_name || recording_name.trim() === '') {
      cleanupTempFile(file.path);
      res.status(400).json({
        success: false,
        error: {
          code: 'RECORDING_NAME_REQUIRED',
          message: 'Recording name/title is required'
        }
      });
      return;
    }

    // Verify agenda exists
    const agenda = await Agenda.findByPk(agenda_id);
    if (!agenda) {
      cleanupTempFile(file.path);
      res.status(404).json({
        success: false,
        error: {
          code: 'AGENDA_NOT_FOUND',
          message: 'Agenda/Meeting not found'
        }
      });
      return;
    }

    // Get file stats
    const fileStats = fs.statSync(file.path);
    const fileSize = fileStats.size;

    // Upload to S3
    console.log(`📤 Uploading voice recording to S3: ${file.originalname}`);
    const s3Result = await uploadToS3(
      file.path,
      'mgb-mrfc/voice-recordings',
      file.mimetype
    );

    // Clean up temp file
    cleanupTempFile(file.path);

    // Create database record
    const voiceRecording = await VoiceRecording.create({
      agenda_id: parseInt(agenda_id),
      recording_name: recording_name.trim(),
      description: description?.trim() || null,
      file_name: file.originalname,
      file_url: s3Result.url,
      file_cloudinary_id: s3Result.key,
      duration: duration ? parseInt(duration) : null,
      file_size: fileSize,
      recorded_by: userId
    });

    console.log(`✅ Voice recording uploaded successfully: ID ${voiceRecording.id}`);

    // Fetch with user info for response
    const recordingWithUser = await VoiceRecording.findByPk(voiceRecording.id, {
      include: [{
        model: User,
        as: 'recorder',
        attributes: ['id', 'username', 'full_name']
      }]
    });

    res.status(201).json({
      success: true,
      message: 'Voice recording uploaded successfully',
      data: recordingWithUser
    });
  } catch (error: any) {
    // Clean up temp file if exists
    if (req.file) {
      cleanupTempFile(req.file.path);
    }
    console.error('Voice recording upload error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'UPLOAD_FAILED',
        message: error.message || 'Failed to upload voice recording'
      }
    });
  }
};

/**
 * Get voice recordings for an agenda/meeting
 * GET /api/v1/voice-recordings/agenda/:agendaId
 */
export const getVoiceRecordingsByAgenda = async (req: Request, res: Response): Promise<void> => {
  try {
    const { agendaId } = req.params;

    // Verify agenda exists
    const agenda = await Agenda.findByPk(agendaId);
    if (!agenda) {
      res.status(404).json({
        success: false,
        error: {
          code: 'AGENDA_NOT_FOUND',
          message: 'Agenda/Meeting not found'
        }
      });
      return;
    }

    // Get all recordings for this agenda
    const recordings = await VoiceRecording.findAll({
      where: { agenda_id: agendaId },
      include: [{
        model: User,
        as: 'recorder',
        attributes: ['id', 'username', 'full_name']
      }],
      order: [['recorded_at', 'DESC']]
    });

    res.json({
      success: true,
      data: {
        recordings,
        count: recordings.length
      }
    });
  } catch (error: any) {
    console.error('Get voice recordings error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'FETCH_FAILED',
        message: error.message || 'Failed to fetch voice recordings'
      }
    });
  }
};

/**
 * Get single voice recording
 * GET /api/v1/voice-recordings/:id
 */
export const getVoiceRecordingById = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;

    const recording = await VoiceRecording.findByPk(id, {
      include: [{
        model: User,
        as: 'recorder',
        attributes: ['id', 'username', 'full_name']
      }]
    });

    if (!recording) {
      res.status(404).json({
        success: false,
        error: {
          code: 'RECORDING_NOT_FOUND',
          message: 'Voice recording not found'
        }
      });
      return;
    }

    res.json({
      success: true,
      data: recording
    });
  } catch (error: any) {
    console.error('Get voice recording error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'FETCH_FAILED',
        message: error.message || 'Failed to fetch voice recording'
      }
    });
  }
};

/**
 * Delete voice recording
 * DELETE /api/v1/voice-recordings/:id
 * Any authenticated user can delete (per requirements)
 */
export const deleteVoiceRecording = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;

    const recording = await VoiceRecording.findByPk(id);

    if (!recording) {
      res.status(404).json({
        success: false,
        error: {
          code: 'RECORDING_NOT_FOUND',
          message: 'Voice recording not found'
        }
      });
      return;
    }

    // Delete from S3 if key exists
    if (recording.file_cloudinary_id) {
      try {
        await deleteFromS3(recording.file_cloudinary_id);
        console.log(`✅ Deleted S3 file: ${recording.file_cloudinary_id}`);
      } catch (s3Error) {
        console.error('S3 deletion error (continuing anyway):', s3Error);
      }
    }

    // Delete database record
    await recording.destroy();

    console.log(`✅ Voice recording deleted: ID ${id}`);

    res.json({
      success: true,
      message: 'Voice recording deleted successfully'
    });
  } catch (error: any) {
    console.error('Delete voice recording error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DELETE_FAILED',
        message: error.message || 'Failed to delete voice recording'
      }
    });
  }
};

/**
 * Update voice recording metadata
 * PUT /api/v1/voice-recordings/:id
 */
export const updateVoiceRecording = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const { recording_name, description } = req.body;

    const recording = await VoiceRecording.findByPk(id);

    if (!recording) {
      res.status(404).json({
        success: false,
        error: {
          code: 'RECORDING_NOT_FOUND',
          message: 'Voice recording not found'
        }
      });
      return;
    }

    // Update fields
    if (recording_name !== undefined) {
      recording.recording_name = recording_name.trim();
    }
    if (description !== undefined) {
      recording.description = description?.trim() || null;
    }

    await recording.save();

    // Fetch with user info for response
    const updatedRecording = await VoiceRecording.findByPk(id, {
      include: [{
        model: User,
        as: 'recorder',
        attributes: ['id', 'username', 'full_name']
      }]
    });

    res.json({
      success: true,
      message: 'Voice recording updated successfully',
      data: updatedRecording
    });
  } catch (error: any) {
    console.error('Update voice recording error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'UPDATE_FAILED',
        message: error.message || 'Failed to update voice recording'
      }
    });
  }
};
