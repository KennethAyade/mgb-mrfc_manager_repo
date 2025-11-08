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
 * Generate an authenticated URL for downloading a private file
 * @param publicId Cloudinary public_id of the file (without extension)
 * @param resourceType Type of file (image, video, raw)
 * @returns Signed URL that bypasses authentication
 */
export const getAuthenticatedUrl = (
  publicId: string,
  resourceType: 'image' | 'video' | 'raw' = 'raw'
): string => {
  try {
    // Remove file extension from public_id if present
    const cleanPublicId = publicId.replace(/\.(pdf|jpg|jpeg|png|mp4|mov)$/i, '');
    
    // Generate a signed URL that expires in 1 hour
    const signedUrl = cloudinary.url(cleanPublicId, {
      resource_type: resourceType,
      type: 'upload',
      sign_url: true,
      secure: true,
      expires_at: Math.floor(Date.now() / 1000) + 3600 // 1 hour from now
    });

    console.log(`📍 Generated authenticated URL for: ${cleanPublicId}`);
    return signedUrl;
  } catch (error) {
    console.error('Error generating authenticated URL:', error);
    throw new Error('Failed to generate authenticated URL');
  }
};

/**
 * Download a file from Cloudinary using direct URL
 * @param fileUrl The direct Cloudinary URL from database
 * @returns Buffer containing the file data
 */
export const downloadFromCloudinaryUrl = async (
  fileUrl: string
): Promise<Buffer> => {
  try {
    const https = await import('https');
    
    console.log(`📥 Downloading from Cloudinary with API authentication`);
    console.log(`📍 URL: ${fileUrl.substring(0, 80)}...`);
    
    return new Promise((resolve, reject) => {
      https.get(fileUrl, (response) => {
        if (response.statusCode !== 200) {
          console.error(`❌ Cloudinary returned status: ${response.statusCode}`);
          reject(new Error(`Cloudinary returned status ${response.statusCode}`));
          return;
        }

        const chunks: Buffer[] = [];
        response.on('data', (chunk) => chunks.push(chunk));
        response.on('end', () => {
          console.log(`✅ Downloaded ${Buffer.concat(chunks).length} bytes`);
          resolve(Buffer.concat(chunks));
        });
        response.on('error', reject);
      }).on('error', reject);
    });
  } catch (error) {
    console.error('Cloudinary download error:', error);
    throw new Error('Failed to download file from storage');
  }
};

/**
 * Download a file from Cloudinary using Admin API (bypasses access restrictions)
 * @param publicId Cloudinary public_id of the file
 * @param resourceType Type of file (image, video, raw)
 * @returns Buffer containing the file data
 */
export const downloadFromCloudinary = async (
  publicId: string,
  resourceType: 'image' | 'video' | 'raw' = 'raw'
): Promise<Buffer> => {
  try {
    const https = await import('https');
    
    // Generate authenticated URL
    const authenticatedUrl = getAuthenticatedUrl(publicId, resourceType);
    
    console.log(`📥 Downloading from Cloudinary via signed URL`);
    
    return new Promise((resolve, reject) => {
      https.get(authenticatedUrl, (response) => {
        if (response.statusCode !== 200) {
          reject(new Error(`Cloudinary returned status ${response.statusCode}`));
          return;
        }

        const chunks: Buffer[] = [];
        response.on('data', (chunk) => chunks.push(chunk));
        response.on('end', () => resolve(Buffer.concat(chunks)));
        response.on('error', reject);
      }).on('error', reject);
    });
  } catch (error) {
    console.error('Cloudinary download error:', error);
    throw new Error('Failed to download file from storage');
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
