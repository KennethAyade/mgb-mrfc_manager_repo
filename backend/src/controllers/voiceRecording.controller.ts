/**
 * ================================================
 * VOICE RECORDING CONTROLLER
 * ================================================
 * Handles voice recording CRUD operations
 * All users have full access to create, read, and delete recordings
 */

import { Request, Response } from 'express';
import { VoiceRecording, User, Agenda } from '../models';
import { uploadToS3, deleteFromS3, downloadFromS3 } from '../config/s3';
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

/**
 * Stream voice recording
 * GET /api/v1/voice-recordings/:id/stream
 * Downloads file from S3 and streams to client (bypasses S3 access restrictions)
 */
export const streamVoiceRecording = async (req: Request, res: Response): Promise<void> => {
  try {
    const id = parseInt(req.params.id);

    // Find recording
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

    console.log(`📥 Streaming voice recording: ${recording.recording_name}`);
    console.log(`📍 S3 URL: ${recording.file_url}`);

    // Validate that file has S3 URL
    if (!recording.file_url) {
      res.status(500).json({
        success: false,
        error: {
          code: 'MISSING_FILE_URL',
          message: 'Recording is missing file URL'
        }
      });
      return;
    }

    // Determine MIME type based on file extension
    const fileName = recording.file_name || 'recording.m4a';
    const ext = fileName.split('.').pop()?.toLowerCase() || 'm4a';
    const mimeType = {
      'm4a': 'audio/mp4',
      'mp3': 'audio/mpeg',
      'wav': 'audio/wav',
      'ogg': 'audio/ogg',
      'webm': 'audio/webm',
      'aac': 'audio/aac',
      '3gp': 'audio/3gpp',
      'amr': 'audio/amr'
    }[ext] || 'audio/mp4'; // Default to audio/mp4 for M4A files

    // Set response headers before streaming
    res.setHeader('Content-Type', mimeType);
    res.setHeader('Content-Disposition', `inline; filename="${encodeURIComponent(fileName)}"`);
    res.setHeader('Accept-Ranges', 'bytes');
    if (recording.file_size) {
      res.setHeader('Content-Length', recording.file_size.toString());
    }

    try {
      // Download file from S3 using backend AWS credentials
      console.log(`📥 Fetching audio file from S3`);
      const fileBuffer = await downloadFromS3(recording.file_url);

      console.log(`📤 Sending audio file to client`);

      // Send the buffer to client
      res.send(fileBuffer);

      console.log(`✅ Stream complete: ${recording.recording_name}`);

    } catch (downloadError: any) {
      console.error(`❌ S3 download error:`, downloadError.message);

      if (!res.headersSent) {
        res.status(500).json({
          success: false,
          error: {
            code: 'S3_ERROR',
            message: 'Failed to fetch file from storage'
          }
        });
      }
    }

  } catch (error: any) {
    console.error('Voice recording stream error:', error);
    if (!res.headersSent) {
      res.status(500).json({
        success: false,
        error: {
          code: 'STREAM_FAILED',
          message: error.message || 'Failed to stream voice recording'
        }
      });
    }
  }
};
