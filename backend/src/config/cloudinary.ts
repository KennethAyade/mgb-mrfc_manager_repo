/**
 * CLOUDINARY CONFIGURATION
 * =========================
 * Configuration for Cloudinary file storage service
 * Free tier: 25GB storage + 25GB bandwidth per month
 */

import { v2 as cloudinary } from 'cloudinary';
import dotenv from 'dotenv';

dotenv.config();

/**
 * Configure Cloudinary with credentials from environment variables
 */
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key: process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET,
  secure: true // Always use HTTPS
});

/**
 * Folder structure in Cloudinary
 */
export const CLOUDINARY_FOLDERS = {
  DOCUMENTS: process.env.CLOUDINARY_FOLDER_DOCUMENTS || 'mgb-mrfc/documents',
  ATTENDANCE: process.env.CLOUDINARY_FOLDER_ATTENDANCE || 'mgb-mrfc/attendance',
  RECORDINGS: process.env.CLOUDINARY_FOLDER_RECORDINGS || 'mgb-mrfc/recordings'
};

/**
 * Upload a file to Cloudinary
 * @param filePath Local file path or buffer
 * @param folder Cloudinary folder to upload to
 * @param resourceType Type of file (image, video, raw for documents)
 * @returns Upload result with URL and public_id
 */
export const uploadToCloudinary = async (
  filePath: string,
  folder: string,
  resourceType: 'image' | 'video' | 'raw' | 'auto' = 'auto'
) => {
  try {
    const result = await cloudinary.uploader.upload(filePath, {
      folder: folder,
      resource_type: resourceType,
      use_filename: true,
      unique_filename: true,
      access_mode: 'public', // Ensure files are publicly accessible
      type: 'upload', // Upload type for public access
      invalidate: false, // Don't invalidate CDN cache
      overwrite: false // Don't overwrite existing files
    });

    console.log(`✅ Uploaded to Cloudinary: ${result.public_id}`);
    console.log(`📍 Access mode: ${result.access_mode || 'public'}`);
    console.log(`📍 Resource type: ${result.resource_type}`);
    console.log(`📍 URL: ${result.secure_url}`);

    return {
      url: result.secure_url,
      publicId: result.public_id,
      format: result.format,
      size: result.bytes
    };
  } catch (error) {
    console.error('Cloudinary upload error:', error);
    throw new Error('Failed to upload file to storage');
  }
};

/**
 * Delete a file from Cloudinary
 * @param publicId Cloudinary public_id of the file
 * @param resourceType Type of file (image, video, raw)
 */
export const deleteFromCloudinary = async (
  publicId: string,
  resourceType: 'image' | 'video' | 'raw' = 'raw'
) => {
  try {
    await cloudinary.uploader.destroy(publicId, {
      resource_type: resourceType
    });
    console.log(`✅ Deleted file from Cloudinary: ${publicId}`);
  } catch (error) {
    console.error('Cloudinary delete error:', error);
    throw new Error('Failed to delete file from storage');
  }
};

/**
 * Test Cloudinary connection
 */
export const testCloudinaryConnection = async (): Promise<void> => {
  try {
    await cloudinary.api.ping();
    console.log('✅ Cloudinary connection successful');
  } catch (error) {
    console.error('❌ Cloudinary connection failed:', error);
    throw error;
  }
};

export default cloudinary;
