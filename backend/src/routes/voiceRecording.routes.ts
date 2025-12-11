/**
 * ================================================
 * VOICE RECORDING ROUTES
 * ================================================
 * Handles voice recording endpoints
 * Base path: /api/v1/voice-recordings
 *
 * ENDPOINTS:
 * POST   /voice-recordings/upload           - Upload new voice recording
 * GET    /voice-recordings/agenda/:agendaId - Get recordings for meeting
 * GET    /voice-recordings/:id              - Get single recording
 * PUT    /voice-recordings/:id              - Update recording metadata
 * DELETE /voice-recordings/:id              - Delete recording
 */

import { Router } from 'express';
import { authenticate, adminOnly } from '../middleware/auth';
import { uploadAudio } from '../middleware/upload';
import {
  uploadVoiceRecording,
  getVoiceRecordingsByAgenda,
  getVoiceRecordingById,
  updateVoiceRecording,
  deleteVoiceRecording,
  streamVoiceRecording
} from '../controllers/voiceRecording.controller';

const router = Router();

/**
 * POST /voice-recordings/upload
 * Upload new voice recording
 * ADMIN and SUPER_ADMIN only
 */
router.post(
  '/upload',
  authenticate,
  adminOnly,
  uploadAudio.single('audio'),
  uploadVoiceRecording
);

/**
 * GET /voice-recordings/agenda/:agendaId
 * Get all recordings for a meeting/agenda
 * All authenticated users can view
 */
router.get(
  '/agenda/:agendaId',
  authenticate,
  getVoiceRecordingsByAgenda
);

/**
 * GET /voice-recordings/:id/stream
 * Stream voice recording audio file
 * Downloads from S3 and streams to client (bypasses S3 access restrictions)
 * All authenticated users can stream
 */
router.get(
  '/:id/stream',
  authenticate,
  streamVoiceRecording
);

/**
 * GET /voice-recordings/:id
 * Get single voice recording by ID
 * All authenticated users can view
 */
router.get(
  '/:id',
  authenticate,
  getVoiceRecordingById
);

/**
 * PUT /voice-recordings/:id
 * Update voice recording metadata (name, description)
 * ADMIN and SUPER_ADMIN only
 */
router.put(
  '/:id',
  authenticate,
  adminOnly,
  updateVoiceRecording
);

/**
 * DELETE /voice-recordings/:id
 * Delete voice recording
 * ADMIN and SUPER_ADMIN only
 */
router.delete(
  '/:id',
  authenticate,
  adminOnly,
  deleteVoiceRecording
);

export default router;
