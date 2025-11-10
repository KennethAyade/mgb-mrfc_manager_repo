/**
 * AWS S3 CONFIGURATION
 * =====================
 * Configuration for AWS S3 file storage service
 * Replaces Cloudinary for document storage
 */

import { S3Client, PutObjectCommand, DeleteObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import * as dotenv from 'dotenv';
import * as fs from 'fs';
import * as path from 'path';

dotenv.config();

/**
 * S3 Configuration from environment variables
 */
const S3_CONFIG = {
  bucketName: process.env.S3_BUCKET_NAME || 'adhub-s3-demo',
  region: process.env.AWS_REGION || 'us-east-1',
  accessKeyId: process.env.AWS_ACCESS_KEY_ID,
  secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY
};

/**
 * Validate S3 configuration
 */
if (!S3_CONFIG.accessKeyId || !S3_CONFIG.secretAccessKey) {
  console.warn('⚠️  AWS credentials not configured. S3 uploads will fail.');
  console.warn('   Please set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY in .env');
}

/**
 * Initialize S3 Client
 */
export const s3Client = new S3Client({
  region: S3_CONFIG.region,
  credentials: {
    accessKeyId: S3_CONFIG.accessKeyId || '',
    secretAccessKey: S3_CONFIG.secretAccessKey || ''
  }
});

/**
 * Folder structure in S3
 */
export const S3_FOLDERS = {
  DOCUMENTS: 'mgb-mrfc/documents',
  PHOTOS: 'mgb-mrfc/photos',
  TEMP: 'mgb-mrfc/temp'
};

/**
 * Upload a file to S3
 * @param filePath Local file path
 * @param folder S3 folder prefix
 * @param contentType MIME type of the file
 * @returns Upload result with URL and key
 */
export const uploadToS3 = async (
  filePath: string,
  folder: string,
  contentType: string = 'application/pdf'
): Promise<{ url: string; key: string; size: number }> => {
  try {
    // Read file
    const fileBuffer = fs.readFileSync(filePath);
    const fileName = path.basename(filePath);
    const s3Key = `${folder}/${fileName}`;

    console.log(`📤 Uploading to S3...`);
    console.log(`   - Bucket: ${S3_CONFIG.bucketName}`);
    console.log(`   - Key: ${s3Key}`);
    console.log(`   - Size: ${fileBuffer.length} bytes`);

    // Upload to S3
    const command = new PutObjectCommand({
      Bucket: S3_CONFIG.bucketName,
      Key: s3Key,
      Body: fileBuffer,
      ContentType: contentType
      // Note: ACL removed - bucket uses bucket policy for public access instead
    });

    await s3Client.send(command);

    // Generate public URL
    const url = `https://${S3_CONFIG.bucketName}.s3.${S3_CONFIG.region}.amazonaws.com/${s3Key}`;

    console.log(`✅ Uploaded to S3: ${s3Key}`);
    console.log(`📍 URL: ${url}`);

    return {
      url: url,
      key: s3Key,
      size: fileBuffer.length
    };
  } catch (error: any) {
    console.error('S3 upload error:', error);
    throw new Error(`Failed to upload file to S3: ${error.message}`);
  }
};

/**
 * Delete a file from S3
 * @param s3Key S3 key of the file
 */
export const deleteFromS3 = async (s3Key: string): Promise<void> => {
  try {
    console.log(`🗑️  Deleting from S3: ${s3Key}`);

    const command = new DeleteObjectCommand({
      Bucket: S3_CONFIG.bucketName,
      Key: s3Key
    });

    await s3Client.send(command);
    console.log(`✅ Deleted from S3: ${s3Key}`);
  } catch (error: any) {
    console.error('S3 delete error:', error);
    throw new Error(`Failed to delete file from S3: ${error.message}`);
  }
};

/**
 * Generate a pre-signed URL for downloading a private file
 * @param s3Key S3 key of the file
 * @param expiresIn Expiration time in seconds (default: 1 hour)
 * @returns Signed URL
 */
export const getSignedDownloadUrl = async (
  s3Key: string,
  expiresIn: number = 3600
): Promise<string> => {
  try {
    const command = new GetObjectCommand({
      Bucket: S3_CONFIG.bucketName,
      Key: s3Key
    });

    const signedUrl = await getSignedUrl(s3Client, command, { expiresIn });
    console.log(`📍 Generated signed URL for: ${s3Key} (expires in ${expiresIn}s)`);
    
    return signedUrl;
  } catch (error: any) {
    console.error('Error generating signed URL:', error);
    throw new Error(`Failed to generate signed URL: ${error.message}`);
  }
};

/**
 * Download a file from S3
 * @param fileUrl The S3 URL or key
 * @returns Buffer containing the file data
 */
export const downloadFromS3 = async (fileUrl: string): Promise<Buffer> => {
  try {
    // Extract S3 key from URL if full URL is provided
    let s3Key = fileUrl;
    if (fileUrl.includes('amazonaws.com/')) {
      s3Key = fileUrl.split('amazonaws.com/')[1];
    }

    console.log(`📥 Downloading from S3: ${s3Key}`);

    const command = new GetObjectCommand({
      Bucket: S3_CONFIG.bucketName,
      Key: s3Key
    });

    const response = await s3Client.send(command);
    
    // Convert stream to buffer
    const chunks: Uint8Array[] = [];
    for await (const chunk of response.Body as any) {
      chunks.push(chunk);
    }
    const buffer = Buffer.concat(chunks);

    console.log(`✅ Downloaded from S3: ${buffer.length} bytes`);
    return buffer;
  } catch (error: any) {
    console.error('S3 download error:', error);
    throw new Error(`Failed to download file from S3: ${error.message}`);
  }
};

/**
 * Check if S3 is properly configured
 */
export const isS3Configured = (): boolean => {
  return !!(S3_CONFIG.accessKeyId && S3_CONFIG.secretAccessKey && S3_CONFIG.bucketName);
};

/**
 * Get S3 configuration info (for debugging)
 */
export const getS3Info = () => {
  return {
    configured: isS3Configured(),
    bucket: S3_CONFIG.bucketName,
    region: S3_CONFIG.region,
    hasCredentials: !!(S3_CONFIG.accessKeyId && S3_CONFIG.secretAccessKey)
  };
};

export default {
  uploadToS3,
  deleteFromS3,
  getSignedDownloadUrl,
  downloadFromS3,
  isS3Configured,
  getS3Info,
  S3_FOLDERS
};

