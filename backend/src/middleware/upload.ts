/**
 * ================================================
 * FILE UPLOAD MIDDLEWARE
 * ================================================
 * Handles file uploads using Multer
 * Supports photo uploads for attendance tracking
 */

import multer from 'multer';
import path from 'path';
import fs from 'fs';

/**
 * Configure multer for storing files temporarily
 * Files will be uploaded to AWS S3 and then deleted locally
 */
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = path.join(__dirname, '../../uploads/temp');

    // Create directory if it doesn't exist
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }

    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    // Generate unique filename: timestamp-randomstring-originalname
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    const ext = path.extname(file.originalname);
    const basename = path.basename(file.originalname, ext);
    cb(null, `${basename}-${uniqueSuffix}${ext}`);
  }
});

/**
 * File filter for photo uploads
 * Only allow image files
 */
const photoFileFilter = (req: Express.Request, file: Express.Multer.File, cb: multer.FileFilterCallback) => {
  const allowedMimeTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];

  if (allowedMimeTypes.includes(file.mimetype)) {
    cb(null, true);
  } else {
    cb(new Error('Invalid file type. Only JPEG, PNG, and WebP images are allowed.'));
  }
};

/**
 * Multer configuration for photo uploads
 * Max file size: 5MB
 */
export const uploadPhoto = multer({
  storage: storage,
  fileFilter: photoFileFilter,
  limits: {
    fileSize: 5 * 1024 * 1024, // 5MB
    files: 1 // Only one photo per request
  }
});

/**
 * Multer configuration for document uploads
 * Max file size: 100MB (S3 can handle much larger files)
 */
const documentFileFilter = (req: Express.Request, file: Express.Multer.File, cb: multer.FileFilterCallback) => {
  const allowedMimeTypes = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'image/jpeg',
    'image/jpg',
    'image/png'
  ];

  if (allowedMimeTypes.includes(file.mimetype)) {
    cb(null, true);
  } else {
    cb(new Error('Invalid file type. Only PDF, DOC, DOCX, XLS, XLSX, and images are allowed.'));
  }
};

export const uploadDocument = multer({
  storage: storage,
  fileFilter: documentFileFilter,
  limits: {
    fileSize: 100 * 1024 * 1024, // 100MB (increased for large PDFs)
    files: 1
  }
});

/**
 * File filter for audio uploads
 * Only allow audio files (MP3, M4A, WAV, OGG, WebM)
 */
const audioFileFilter = (req: Express.Request, file: Express.Multer.File, cb: multer.FileFilterCallback) => {
  const allowedMimeTypes = [
    'audio/mpeg',           // MP3
    'audio/mp3',            // MP3 alternative
    'audio/mp4',            // M4A
    'audio/x-m4a',          // M4A alternative
    'audio/m4a',            // M4A alternative
    'audio/wav',            // WAV
    'audio/wave',           // WAV alternative
    'audio/x-wav',          // WAV alternative
    'audio/ogg',            // OGG
    'audio/webm',           // WebM
    'audio/aac',            // AAC
    'audio/3gpp',           // 3GP audio
    'audio/amr'             // AMR (common on Android)
  ];

  if (allowedMimeTypes.includes(file.mimetype)) {
    cb(null, true);
  } else {
    cb(new Error(`Invalid file type: ${file.mimetype}. Only audio files (MP3, M4A, WAV, OGG, WebM, AAC) are allowed.`));
  }
};

/**
 * Multer configuration for audio uploads
 * Max file size: 50MB (about 60 minutes of M4A audio at 128kbps)
 */
export const uploadAudio = multer({
  storage: storage,
  fileFilter: audioFileFilter,
  limits: {
    fileSize: 50 * 1024 * 1024, // 50MB
    files: 1
  }
});

/**
 * Cleanup temporary file
 * @param filePath Path to the temporary file
 */
export const cleanupTempFile = (filePath: string): void => {
  try {
    if (fs.existsSync(filePath)) {
      fs.unlinkSync(filePath);
      console.log(`✅ Cleaned up temporary file: ${filePath}`);
    }
  } catch (error) {
    console.error(`❌ Failed to cleanup file ${filePath}:`, error);
  }
};
