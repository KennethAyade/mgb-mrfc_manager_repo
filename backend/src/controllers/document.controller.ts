/**
 * ================================================
 * DOCUMENT MANAGEMENT CONTROLLER
 * ================================================
 * Handles document upload, download, and management for proponent compliance documents
 * Integrates with Cloudinary for file storage
 * Categories: MTF_REPORT, AEPEP, CMVR, SDMP, PRODUCTION, SAFETY, OTHER
 * Status: PENDING (awaiting review), ACCEPTED (approved), REJECTED (not accepted)
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { Document, DocumentCategory, DocumentStatus, User, Mrfc, Proponent, Quarter, AuditLog, AuditAction } from '../models';
import sequelize from '../config/database';
import { uploadToCloudinary, deleteFromCloudinary, CLOUDINARY_FOLDERS } from '../config/cloudinary';
import fs from 'fs/promises';
import path from 'path';
import crypto from 'crypto';

/**
 * List documents with filtering
 * GET /api/v1/documents
 * Query params: proponent_id, quarter_id, category, status, search, page, limit
 */
export const listDocuments = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      proponent_id,
      quarter_id,
      category,
      status,
      search,
      page = '1',
      limit = '20'
    } = req.query;
    const currentUser = (req as any).user;

    // Build filters
    const where: any = {};
    if (proponent_id) where.proponent_id = proponent_id;
    if (quarter_id) where.quarter_id = quarter_id;
    if (category) where.category = category;
    if (status) where.status = status;

    // Search by filename
    if (search) {
      where[Op.or] = [
        { file_name: { [Op.iLike]: `%${search}%` } },
        { original_name: { [Op.iLike]: `%${search}%` } }
      ];
    }

    // Query documents with related data
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    const { count, rows: documents } = await Document.findAndCountAll({
      where,
      include: [
        {
          model: User,
          as: 'uploader',
          attributes: ['id', 'full_name', 'email']
        },
        {
          model: Proponent,
          as: 'proponent',
          attributes: ['id', 'name', 'company_name', 'mrfc_id'],
          include: [
            {
              model: Mrfc,
              as: 'mrfc',
              attributes: ['id', 'name', 'municipality']
            }
          ]
        },
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'name', 'year', 'quarter_number']
        },
        {
          model: User,
          as: 'reviewer',
          attributes: ['id', 'full_name', 'email']
        }
      ],
      limit: limitNum,
      offset,
      order: [['upload_date', 'DESC']]
    });

    res.json({
      success: true,
      data: {
        documents,
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
    console.error('Document listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DOCUMENT_LISTING_FAILED',
        message: error.message || 'Failed to retrieve documents'
      }
    });
  }
};

/**
 * Get documents by MRFC ID
 * GET /api/v1/documents/mrfc/:mrfc_id
 * Query params: category, status
 */
export const getDocumentsByMrfc = async (req: Request, res: Response): Promise<void> => {
  try {
    const { mrfc_id } = req.params;
    const { category, status } = req.query;

    // Build filters
    const where: any = {};
    if (category) where.category = category;
    if (status) where.status = status;

    // Find all proponents for this MRFC
    const proponents = await Proponent.findAll({
      where: { mrfc_id: parseInt(mrfc_id) },
      attributes: ['id']
    });

    const proponentIds = proponents.map(p => p.id);

    // If no proponents found, return empty array
    if (proponentIds.length === 0) {
      res.json({
        success: true,
        data: []
      });
      return;
    }

    // Add proponent filter
    where.proponent_id = { [Op.in]: proponentIds };

    // Query documents
    const documents = await Document.findAll({
      where,
      include: [
        {
          model: User,
          as: 'uploader',
          attributes: ['id', 'full_name', 'email']
        },
        {
          model: Proponent,
          as: 'proponent',
          attributes: ['id', 'name', 'company_name', 'mrfc_id'],
          include: [
            {
              model: Mrfc,
              as: 'mrfc',
              attributes: ['id', 'name', 'municipality']
            }
          ]
        },
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'name', 'year', 'quarter_number']
        },
        {
          model: User,
          as: 'reviewer',
          attributes: ['id', 'full_name', 'email']
        }
      ],
      order: [['upload_date', 'DESC']]
    });

    res.json({
      success: true,
      data: documents
    });
  } catch (error: any) {
    console.error('Get documents by MRFC error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DOCUMENT_RETRIEVAL_FAILED',
        message: error.message || 'Failed to retrieve documents by MRFC'
      }
    });
  }
};

/**
 * Get documents by Proponent ID
 * GET /api/v1/documents/proponent/:proponent_id
 * Query params: category, status
 */
export const getDocumentsByProponent = async (req: Request, res: Response): Promise<void> => {
  try {
    const { proponent_id } = req.params;
    const { category, status } = req.query;

    // Build filters
    const where: any = { proponent_id: parseInt(proponent_id) };
    if (category) where.category = category;
    if (status) where.status = status;

    // Query documents
    const documents = await Document.findAll({
      where,
      include: [
        {
          model: User,
          as: 'uploader',
          attributes: ['id', 'full_name', 'email']
        },
        {
          model: Proponent,
          as: 'proponent',
          attributes: ['id', 'name', 'company_name', 'mrfc_id'],
          include: [
            {
              model: Mrfc,
              as: 'mrfc',
              attributes: ['id', 'name', 'municipality']
            }
          ]
        },
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'name', 'year', 'quarter_number']
        },
        {
          model: User,
          as: 'reviewer',
          attributes: ['id', 'full_name', 'email']
        }
      ],
      order: [['upload_date', 'DESC']]
    });

    res.json({
      success: true,
      data: documents
    });
  } catch (error: any) {
    console.error('Get documents by proponent error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DOCUMENT_RETRIEVAL_FAILED',
        message: error.message || 'Failed to retrieve documents by proponent'
      }
    });
  }
};

/**
 * Upload document with Cloudinary integration
 * POST /api/v1/documents/upload
 * Body: proponent_id, quarter_id, category
 * File: multipart/form-data (handled by multer middleware)
 *
 * Filename convention: {MRFCName}_{Category}_{Quarter}_{Year}.{ext}
 * Example: "Cebu-MRFC_MTF-REPORT_Q1_2025.pdf"
 */
export const uploadDocument = async (req: Request, res: Response): Promise<void> => {
  let tempFilePath: string | undefined;

  try {
    const { proponent_id, quarter_id, category, description } = req.body;
    const currentUser = (req as any).user;
    const file = (req as any).file; // From multer middleware

    // Validate required fields
    if (!proponent_id || !quarter_id || !category) {
      res.status(400).json({
        success: false,
        error: {
          code: 'MISSING_REQUIRED_FIELDS',
          message: 'proponent_id, quarter_id, and category are required'
        }
      });
      return;
    }

    // Validate file uploaded
    if (!file) {
      res.status(400).json({
        success: false,
        error: {
          code: 'NO_FILE_UPLOADED',
          message: 'No file was uploaded'
        }
      });
      return;
    }

    tempFilePath = file.path;

    // Validate category
    if (!Object.values(DocumentCategory).includes(category)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_CATEGORY',
          message: `Category must be one of: ${Object.values(DocumentCategory).join(', ')}`
        }
      });
      return;
    }

    // Fetch proponent with MRFC
    const proponent = await Proponent.findByPk(proponent_id, {
      include: [
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'name', 'municipality']
        }
      ]
    });

    if (!proponent) {
      res.status(404).json({
        success: false,
        error: {
          code: 'PROPONENT_NOT_FOUND',
          message: 'Proponent not found'
        }
      });
      return;
    }

    // Fetch quarter (required)
    const quarter = await Quarter.findByPk(quarter_id);
    if (!quarter) {
      res.status(404).json({
        success: false,
        error: {
          code: 'QUARTER_NOT_FOUND',
          message: 'Quarter not found'
        }
      });
      return;
    }

    // Generate standardized filename: {MRFCName}_{Category}_{Quarter}_{Year}.{ext}
    const mrfcName = (proponent as any).mrfc?.name || 'Unknown-MRFC';
    const sanitizedMrfcName = mrfcName.replace(/[^a-zA-Z0-9-]/g, '-');
    const sanitizedCategory = category.replace(/_/g, '-');
    const fileExtension = path.extname(file.originalname);
    const generatedFileName = `${sanitizedMrfcName}_${sanitizedCategory}_Q${quarter.quarter_number}_${quarter.year}${fileExtension}`;

    // Upload to Cloudinary
    const cloudinaryResult = await uploadToCloudinary(
      file.path,
      CLOUDINARY_FOLDERS.DOCUMENTS,
      'raw' // For PDFs and other document types
    );

    // Create document record in database
    const document = await sequelize.transaction(async (t) => {
      const newDocument = await Document.create(
        {
          proponent_id,
          quarter_id,
          file_name: generatedFileName,
          original_name: file.originalname,
          file_type: file.mimetype,
          file_size: file.size,
          category,
          file_url: cloudinaryResult.url,
          file_cloudinary_id: cloudinaryResult.publicId,
          status: DocumentStatus.PENDING,
          uploaded_by: currentUser?.userId
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: AuditAction.CREATE,
          entity_type: 'documents',
          entity_id: newDocument.id,
          new_values: {
            action_type: 'UPLOAD_DOCUMENT',
            filename: generatedFileName,
            original_name: file.originalname,
            proponent_id,
            quarter_id,
            category,
            file_size: file.size
          }
        },
        { transaction: t }
      );

      return newDocument;
    });

    // Clean up temporary file
    if (tempFilePath) {
      try {
        await fs.unlink(tempFilePath);
      } catch (cleanupError) {
        console.warn('Failed to delete temporary file:', cleanupError);
      }
    }

    res.status(201).json({
      success: true,
      message: 'Document uploaded successfully',
      data: {
        id: document.id,
        file_name: document.file_name,
        original_name: document.original_name,
        file_url: document.file_url,
        file_type: document.file_type,
        file_size: document.file_size,
        category: document.category,
        status: document.status,
        upload_date: document.upload_date,
        proponent_id: document.proponent_id,
        quarter_id: document.quarter_id
      }
    });
  } catch (error: any) {
    console.error('Document upload error:', error);

    // Clean up temporary file on error
    if (tempFilePath) {
      try {
        await fs.unlink(tempFilePath);
      } catch (cleanupError) {
        console.warn('Failed to delete temporary file after error:', cleanupError);
      }
    }

    res.status(500).json({
      success: false,
      error: {
        code: 'DOCUMENT_UPLOAD_FAILED',
        message: error.message || 'Failed to upload document'
      }
    });
  }
};

/**
 * Get document metadata by ID
 * GET /api/v1/documents/:id
 */
export const getDocumentById = async (req: Request, res: Response): Promise<void> => {
  try {
    const documentId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find document with all related data
    const document = await Document.findByPk(documentId, {
      include: [
        {
          model: Proponent,
          as: 'proponent',
          attributes: ['id', 'name', 'company_name', 'mrfc_id'],
          include: [
            {
              model: Mrfc,
              as: 'mrfc',
              attributes: ['id', 'name', 'municipality', 'province']
            }
          ]
        },
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'name', 'year', 'quarter_number', 'start_date', 'end_date']
        },
        {
          model: User,
          as: 'uploader',
          attributes: ['id', 'full_name', 'email']
        },
        {
          model: User,
          as: 'reviewer',
          attributes: ['id', 'full_name', 'email']
        }
      ]
    });

    if (!document) {
      res.status(404).json({
        success: false,
        error: {
          code: 'DOCUMENT_NOT_FOUND',
          message: 'Document not found'
        }
      });
      return;
    }

    res.json({
      success: true,
      data: document
    });
  } catch (error: any) {
    console.error('Get document error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_DOCUMENT_FAILED',
        message: error.message || 'Failed to retrieve document'
      }
    });
  }
};

/**
 * Download document file
 * GET /api/v1/documents/:id/download
 * Returns Cloudinary URL for file download
 */
export const downloadDocument = async (req: Request, res: Response): Promise<void> => {
  try {
    const documentId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find document
    const document = await Document.findByPk(documentId);
    if (!document) {
      res.status(404).json({
        success: false,
        error: {
          code: 'DOCUMENT_NOT_FOUND',
          message: 'Document not found'
        }
      });
      return;
    }

    // Log download action
    await AuditLog.create({
      user_id: currentUser?.userId,
      action: AuditAction.UPDATE,
      entity_type: 'documents',
      entity_id: documentId,
      new_values: {
        action_type: 'DOWNLOAD_DOCUMENT',
        file_name: document.file_name,
        original_name: document.original_name,
        file_url: document.file_url
      }
    });

    // Return Cloudinary URL for download
    res.json({
      success: true,
      message: 'Document download URL retrieved',
      data: {
        file_url: document.file_url,
        file_name: document.file_name,
        original_name: document.original_name,
        file_type: document.file_type,
        file_size: document.file_size
      }
    });
  } catch (error: any) {
    console.error('Document download error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DOCUMENT_DOWNLOAD_FAILED',
        message: error.message || 'Failed to retrieve download URL'
      }
    });
  }
};

/**
 * Stream document file directly (proxy download)
 * GET /api/v1/documents/:id/stream
 * Streams the file from Cloudinary through backend to bypass access restrictions
 */
export const streamDocument = async (req: Request, res: Response): Promise<void> => {
  try {
    const documentId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find document
    const document = await Document.findByPk(documentId);
    if (!document) {
      res.status(404).json({
        success: false,
        error: {
          code: 'DOCUMENT_NOT_FOUND',
          message: 'Document not found'
        }
      });
      return;
    }

    console.log(`📥 Streaming document: ${document.original_name}`);
    console.log(`📍 Cloudinary Public ID: ${document.file_cloudinary_id}`);

    // Validate that file has cloudinary_id
    if (!document.file_cloudinary_id) {
      res.status(500).json({
        success: false,
        error: {
          code: 'MISSING_CLOUDINARY_ID',
          message: 'Document is missing Cloudinary reference'
        }
      });
      return;
    }

    // Log download action
    await AuditLog.create({
      user_id: currentUser?.userId,
      action: AuditAction.UPDATE,
      entity_type: 'documents',
      entity_id: documentId,
      new_values: {
        action_type: 'STREAM_DOCUMENT',
        file_name: document.file_name,
        original_name: document.original_name
      }
    });

    // Use the secure_url that was saved during upload
    // This is the direct Cloudinary CDN URL
    const cloudinaryUrl = document.file_url;

    console.log(`📥 Attempting download from Cloudinary CDN`);
    console.log(`📍 URL: ${cloudinaryUrl}`);

    // Set response headers before streaming
    res.setHeader('Content-Type', document.file_type || 'application/pdf');
    res.setHeader('Content-Disposition', `inline; filename="${encodeURIComponent(document.original_name)}"`);
    if (document.file_size) {
      res.setHeader('Content-Length', document.file_size.toString());
    }

    const https = await import('https');

    // Try direct download from the secure_url
    https.get(cloudinaryUrl, (cloudinaryRes) => {
      if (cloudinaryRes.statusCode !== 200) {
        console.error(`❌ Cloudinary returned status: ${cloudinaryRes.statusCode}`);
        res.status(cloudinaryRes.statusCode || 500).json({
          success: false,
          error: {
            code: 'CLOUDINARY_ERROR',
            message: `Failed to fetch file from storage (HTTP ${cloudinaryRes.statusCode})`
          }
        });
        return;
      }

      console.log(`✅ Streaming file (${document.file_size} bytes)`);

      // Pipe the response from Cloudinary to our response
      cloudinaryRes.pipe(res);

      cloudinaryRes.on('end', () => {
        console.log(`✅ Stream complete: ${document.original_name}`);
      });

      cloudinaryRes.on('error', (error) => {
        console.error(`❌ Stream error:`, error);
        if (!res.headersSent) {
          res.status(500).json({
            success: false,
            error: {
              code: 'STREAM_ERROR',
              message: 'Error streaming file'
            }
          });
        }
      });
    }).on('error', (error) => {
      console.error(`❌ Failed to fetch from Cloudinary:`, error);
      if (!res.headersSent) {
        res.status(500).json({
          success: false,
          error: {
            code: 'FETCH_ERROR',
            message: 'Failed to fetch file from storage'
          }
        });
      }
    });

  } catch (error: any) {
    console.error('Document stream error:', error);
    if (!res.headersSent) {
      res.status(500).json({
        success: false,
        error: {
          code: 'DOCUMENT_STREAM_FAILED',
          message: error.message || 'Failed to stream document'
        }
      });
    }
  }
};

/**
 * Update document metadata and approval workflow
 * PUT /api/v1/documents/:id
 * Body: status (PENDING, ACCEPTED, REJECTED), remarks
 * ADMIN only for approval actions
 */
export const updateDocument = async (req: Request, res: Response): Promise<void> => {
  try {
    const documentId = parseInt(req.params.id);
    const currentUser = (req as any).user;
    const { status, remarks } = req.body;

    // Find document
    const document = await Document.findByPk(documentId);
    if (!document) {
      res.status(404).json({
        success: false,
        error: {
          code: 'DOCUMENT_NOT_FOUND',
          message: 'Document not found'
        }
      });
      return;
    }

    // Validate status if provided
    if (status && !Object.values(DocumentStatus).includes(status)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_STATUS',
          message: `Status must be one of: ${Object.values(DocumentStatus).join(', ')}`
        }
      });
      return;
    }

    // Prepare update data
    const updateData: any = {};

    // Status change (approval/rejection)
    if (status && status !== document.status) {
      updateData.status = status;

      // If accepting or rejecting, record reviewer
      if (status === DocumentStatus.ACCEPTED || status === DocumentStatus.REJECTED) {
        updateData.reviewed_by = currentUser?.userId;
        updateData.reviewed_at = new Date();
      }
    }

    // Remarks (approval/rejection notes)
    if (remarks !== undefined) {
      updateData.remarks = remarks;
    }

    // Update document
    await sequelize.transaction(async (t) => {
      await document.update(updateData, { transaction: t });

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: AuditAction.UPDATE,
          entity_type: 'documents',
          entity_id: documentId,
          new_values: {
            action_type: status === DocumentStatus.ACCEPTED ? 'APPROVE_DOCUMENT' :
                         status === DocumentStatus.REJECTED ? 'REJECT_DOCUMENT' :
                         'UPDATE_DOCUMENT',
            file_name: document.file_name,
            status: updateData.status,
            remarks: updateData.remarks
          }
        },
        { transaction: t }
      );
    });

    res.json({
      success: true,
      message: 'Document updated successfully',
      data: document
    });
  } catch (error: any) {
    console.error('Document update error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DOCUMENT_UPDATE_FAILED',
        message: error.message || 'Failed to update document'
      }
    });
  }
};

/**
 * Delete document
 * DELETE /api/v1/documents/:id
 * Deletes both database record and file from Cloudinary
 */
export const deleteDocument = async (req: Request, res: Response): Promise<void> => {
  try {
    const documentId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find document
    const document = await Document.findByPk(documentId);
    if (!document) {
      res.status(404).json({
        success: false,
        error: {
          code: 'DOCUMENT_NOT_FOUND',
          message: 'Document not found'
        }
      });
      return;
    }

    // Delete with transaction
    await sequelize.transaction(async (t) => {
      // Delete file from Cloudinary if cloudinary_id exists
      if (document.file_cloudinary_id) {
        try {
          await deleteFromCloudinary(document.file_cloudinary_id);
        } catch (cloudinaryError) {
          console.error('Cloudinary deletion error:', cloudinaryError);
          // Continue with database deletion even if Cloudinary deletion fails
        }
      }

      // Delete database record
      await document.destroy({ transaction: t });

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: AuditAction.DELETE,
          entity_type: 'documents',
          entity_id: documentId,
          old_values: {
            file_name: document.file_name,
            original_name: document.original_name,
            proponent_id: document.proponent_id,
            quarter_id: document.quarter_id,
            cloudinary_id: document.file_cloudinary_id
          }
        },
        { transaction: t }
      );
    });

    res.json({
      success: true,
      message: 'Document deleted successfully'
    });
  } catch (error: any) {
    console.error('Document deletion error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DOCUMENT_DELETION_FAILED',
        message: error.message || 'Failed to delete document'
      }
    });
  }
};

/**
 * Generate upload token for proponent
 * POST /api/v1/documents/generate-upload-token
 * Body: proponent_id, quarter_id, category, expires_in_hours (default: 48)
 * ADMIN only
 *
 * Returns a shareable URL that proponents can use to upload documents
 * without logging in. Token expires after specified hours.
 */
export const generateUploadToken = async (req: Request, res: Response): Promise<void> => {
  try {
    const { proponent_id, quarter_id, category, expires_in_hours = 48 } = req.body;
    const currentUser = (req as any).user;

    // Validate required fields
    if (!proponent_id || !quarter_id || !category) {
      res.status(400).json({
        success: false,
        error: {
          code: 'MISSING_REQUIRED_FIELDS',
          message: 'proponent_id, quarter_id, and category are required'
        }
      });
      return;
    }

    // Validate category
    if (!Object.values(DocumentCategory).includes(category)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_CATEGORY',
          message: `Category must be one of: ${Object.values(DocumentCategory).join(', ')}`
        }
      });
      return;
    }

    // Verify proponent exists
    const proponent = await Proponent.findByPk(proponent_id);
    if (!proponent) {
      res.status(404).json({
        success: false,
        error: {
          code: 'PROPONENT_NOT_FOUND',
          message: 'Proponent not found'
        }
      });
      return;
    }

    // Verify quarter exists
    const quarter = await Quarter.findByPk(quarter_id);
    if (!quarter) {
      res.status(404).json({
        success: false,
        error: {
          code: 'QUARTER_NOT_FOUND',
          message: 'Quarter not found'
        }
      });
      return;
    }

    // Generate unique token
    const token = crypto.randomBytes(32).toString('hex');

    // Calculate expiration
    const expiresAt = new Date();
    expiresAt.setHours(expiresAt.getHours() + expires_in_hours);

    // Store token data (in production, this should be stored in a database table)
    // For now, we'll encode it in the token itself using JWT-like approach
    const tokenData = {
      token,
      proponent_id,
      quarter_id,
      category,
      expires_at: expiresAt.toISOString(),
      created_by: currentUser?.userId
    };

    // Base64 encode the token data for the URL
    const encodedToken = Buffer.from(JSON.stringify(tokenData)).toString('base64url');

    // Generate shareable URL
    const baseUrl = process.env.BACKEND_URL || 'http://localhost:5000';
    const uploadUrl = `${baseUrl}/api/v1/documents/upload-via-token/${encodedToken}`;

    // Log token generation
    await AuditLog.create({
      user_id: currentUser?.userId,
      action: AuditAction.CREATE,
      entity_type: 'proponents',
      entity_id: proponent_id,
      new_values: {
        action_type: 'GENERATE_UPLOAD_TOKEN',
        proponent_id,
        quarter_id,
        category,
        expires_at: expiresAt.toISOString(),
        token: token.substring(0, 8) + '...' // Only log first 8 chars for security
      }
    });

    res.status(201).json({
      success: true,
      message: 'Upload token generated successfully',
      data: {
        token: encodedToken,
        upload_url: uploadUrl,
        proponent_id,
        quarter_id,
        category,
        expires_at: expiresAt,
        expires_in_hours
      }
    });
  } catch (error: any) {
    console.error('Token generation error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'TOKEN_GENERATION_FAILED',
        message: error.message || 'Failed to generate upload token'
      }
    });
  }
};

/**
 * Upload document via token (no authentication required)
 * POST /api/v1/documents/upload-via-token/:token
 * File: multipart/form-data (handled by multer middleware)
 *
 * Allows proponents to upload documents using a pre-generated token
 * without logging into the system. Token must not be expired.
 */
export const uploadViaToken = async (req: Request, res: Response): Promise<void> => {
  let tempFilePath: string | undefined;

  try {
    const { token } = req.params;
    const file = (req as any).file;

    // Validate file uploaded
    if (!file) {
      res.status(400).json({
        success: false,
        error: {
          code: 'NO_FILE_UPLOADED',
          message: 'No file was uploaded'
        }
      });
      return;
    }

    tempFilePath = file.path;

    // Decode and validate token
    let tokenData: any;
    try {
      const decodedString = Buffer.from(token, 'base64url').toString('utf-8');
      tokenData = JSON.parse(decodedString);
    } catch (decodeError) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_TOKEN',
          message: 'Invalid upload token format'
        }
      });
      return;
    }

    // Check token expiration
    const expiresAt = new Date(tokenData.expires_at);
    if (expiresAt < new Date()) {
      res.status(401).json({
        success: false,
        error: {
          code: 'TOKEN_EXPIRED',
          message: 'Upload token has expired'
        }
      });
      return;
    }

    // Extract token data
    const { proponent_id, quarter_id, category } = tokenData;

    // Fetch proponent with MRFC for filename generation
    const proponent = await Proponent.findByPk(proponent_id, {
      include: [
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'name', 'municipality']
        }
      ]
    });

    if (!proponent) {
      res.status(404).json({
        success: false,
        error: {
          code: 'PROPONENT_NOT_FOUND',
          message: 'Proponent not found'
        }
      });
      return;
    }

    // Fetch quarter
    const quarter = await Quarter.findByPk(quarter_id);
    if (!quarter) {
      res.status(404).json({
        success: false,
        error: {
          code: 'QUARTER_NOT_FOUND',
          message: 'Quarter not found'
        }
      });
      return;
    }

    // Generate standardized filename
    const mrfcName = (proponent as any).mrfc?.name || 'Unknown-MRFC';
    const sanitizedMrfcName = mrfcName.replace(/[^a-zA-Z0-9-]/g, '-');
    const sanitizedCategory = category.replace(/_/g, '-');
    const fileExtension = path.extname(file.originalname);
    const generatedFileName = `${sanitizedMrfcName}_${sanitizedCategory}_Q${quarter.quarter_number}_${quarter.year}${fileExtension}`;

    // Upload to Cloudinary
    const cloudinaryResult = await uploadToCloudinary(
      file.path,
      CLOUDINARY_FOLDERS.DOCUMENTS,
      'raw'
    );

    // Create document record
    const document = await sequelize.transaction(async (t) => {
      const newDocument = await Document.create(
        {
          proponent_id,
          quarter_id,
          file_name: generatedFileName,
          original_name: file.originalname,
          file_type: file.mimetype,
          file_size: file.size,
          category,
          file_url: cloudinaryResult.url,
          file_cloudinary_id: cloudinaryResult.publicId,
          status: DocumentStatus.PENDING,
          uploaded_by: null // No user authentication for token uploads
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: tokenData.created_by, // Admin who generated the token
          action: AuditAction.CREATE,
          entity_type: 'documents',
          entity_id: newDocument.id,
          new_values: {
            action_type: 'UPLOAD_DOCUMENT_VIA_TOKEN',
            filename: generatedFileName,
            original_name: file.originalname,
            proponent_id,
            quarter_id,
            category,
            file_size: file.size,
            upload_method: 'TOKEN'
          }
        },
        { transaction: t }
      );

      return newDocument;
    });

    // Clean up temporary file
    if (tempFilePath) {
      try {
        await fs.unlink(tempFilePath);
      } catch (cleanupError) {
        console.warn('Failed to delete temporary file:', cleanupError);
      }
    }

    res.status(201).json({
      success: true,
      message: 'Document uploaded successfully via token',
      data: {
        id: document.id,
        file_name: document.file_name,
        original_name: document.original_name,
        file_type: document.file_type,
        file_size: document.file_size,
        category: document.category,
        status: document.status,
        upload_date: document.upload_date
      }
    });
  } catch (error: any) {
    console.error('Token upload error:', error);

    // Clean up temporary file on error
    if (tempFilePath) {
      try {
        await fs.unlink(tempFilePath);
      } catch (cleanupError) {
        console.warn('Failed to delete temporary file after error:', cleanupError);
      }
    }

    res.status(500).json({
      success: false,
      error: {
        code: 'TOKEN_UPLOAD_FAILED',
        message: error.message || 'Failed to upload document via token'
      }
    });
  }
};
