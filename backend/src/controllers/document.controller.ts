/**
 * ================================================
 * DOCUMENT MANAGEMENT CONTROLLER
 * ================================================
 * Handles document upload, download, and management for MRFCs
 * Note: Actual file upload integration with Cloudinary should be added
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { Document, User, Mrfc, AuditLog } from '../models';
import sequelize from '../config/database';

/**
 * List documents with filtering
 * GET /api/v1/documents
 */
export const listDocuments = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      mrfc_id,
      file_type,
      uploaded_after,
      uploaded_before,
      page = '1',
      limit = '20'
    } = req.query;
    const currentUser = (req as any).user;

    // USER role must provide mrfc_id
    if (currentUser?.role === 'USER' && !mrfc_id) {
      res.status(400).json({
        success: false,
        error: {
          code: 'MRFC_ID_REQUIRED',
          message: 'mrfc_id parameter is required for USER role'
        }
      });
      return;
    }

    // Verify USER has access to requested MRFC
    if (currentUser?.role === 'USER' && mrfc_id) {
      const userMrfcIds = currentUser.mrfcAccess || [];
      if (!userMrfcIds.includes(parseInt(mrfc_id as string))) {
        res.status(403).json({
          success: false,
          error: {
            code: 'MRFC_ACCESS_DENIED',
            message: 'Access denied to this MRFC'
          }
        });
        return;
      }
    }

    // Build filters
    const where: any = {};
    if (mrfc_id) where.mrfc_id = mrfc_id;
    if (file_type) where.file_type = file_type;
    if (uploaded_after) where.uploaded_at = { [Op.gte]: uploaded_after };
    if (uploaded_before) where.uploaded_at = { ...where.uploaded_at, [Op.lte]: uploaded_before };

    // Query documents
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    const { count, rows: documents } = await Document.findAndCountAll({
      where,
      include: [
        {
          model: User,
          as: 'uploader',
          attributes: ['id', 'full_name']
        }
      ],
      limit: limitNum,
      offset,
      order: [['uploaded_at', 'DESC']]
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
 * Upload document
 * POST /api/v1/documents/upload
 * Note: This is a placeholder. Actual file upload with Cloudinary integration should be added
 */
export const uploadDocument = async (req: Request, res: Response): Promise<void> => {
  try {
    const { mrfc_id, description, filename, file_url } = req.body;
    const currentUser = (req as any).user;

    // Verify MRFC exists
    const mrfc = await Mrfc.findByPk(mrfc_id);
    if (!mrfc) {
      res.status(404).json({
        success: false,
        error: {
          code: 'MRFC_NOT_FOUND',
          message: 'MRFC not found'
        }
      });
      return;
    }

    // Authorization check for USER role
    if (currentUser?.role === 'USER') {
      const userMrfcIds = currentUser.mrfcAccess || [];
      if (!userMrfcIds.includes(mrfc_id)) {
        res.status(403).json({
          success: false,
          error: {
            code: 'MRFC_ACCESS_DENIED',
            message: 'Access denied'
          }
        });
        return;
      }
    }

    // Create document record
    // TODO: Integrate with Cloudinary for actual file upload
    const document = await sequelize.transaction(async (t) => {
      const newDocument = await Document.create(
        {
          mrfc_id,
          filename: filename || 'document.pdf',
          original_filename: filename || 'document.pdf',
          file_type: 'pdf',
          file_size: 0,
          file_path: file_url || '/uploads/placeholder.pdf',
          description,
          uploaded_by: currentUser?.userId
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'UPLOAD_DOCUMENT',
          entity_type: 'DOCUMENT',
          entity_id: newDocument.id,
          details: { filename: newDocument.original_filename, mrfc_id }
        },
        { transaction: t }
      );

      return newDocument;
    });

    res.status(201).json({
      success: true,
      message: 'Document uploaded successfully',
      data: document
    });
  } catch (error: any) {
    console.error('Document upload error:', error);
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

    // Find document
    const document = await Document.findByPk(documentId, {
      include: [
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'mrfc_number', 'project_title']
        },
        {
          model: User,
          as: 'uploader',
          attributes: ['id', 'full_name']
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

    // Authorization check for USER role
    if (currentUser?.role === 'USER') {
      const userMrfcIds = currentUser.mrfcAccess || [];
      if (!userMrfcIds.includes(document.mrfc_id)) {
        res.status(403).json({
          success: false,
          error: {
            code: 'MRFC_ACCESS_DENIED',
            message: 'Access denied'
          }
        });
        return;
      }
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
 * Note: This is a placeholder. Actual file download from Cloudinary should be implemented
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

    // Authorization check for USER role
    if (currentUser?.role === 'USER') {
      const userMrfcIds = currentUser.mrfcAccess || [];
      if (!userMrfcIds.includes(document.mrfc_id)) {
        res.status(403).json({
          success: false,
          error: {
            code: 'MRFC_ACCESS_DENIED',
            message: 'Access denied'
          }
        });
        return;
      }
    }

    // TODO: Implement actual file download from Cloudinary
    // For now, return document metadata
    res.json({
      success: true,
      message: 'Document download - Cloudinary integration pending',
      data: {
        file_path: document.file_path,
        filename: document.original_filename
      }
    });

    // Log download
    await AuditLog.create({
      user_id: currentUser?.userId,
      action: 'DOWNLOAD_DOCUMENT',
      entity_type: 'DOCUMENT',
      entity_id: documentId,
      details: { filename: document.original_filename }
    });
  } catch (error: any) {
    console.error('Document download error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DOCUMENT_DOWNLOAD_FAILED',
        message: error.message || 'Failed to download document'
      }
    });
  }
};

/**
 * Update document metadata
 * PUT /api/v1/documents/:id
 */
export const updateDocument = async (req: Request, res: Response): Promise<void> => {
  try {
    const documentId = parseInt(req.params.id);
    const currentUser = (req as any).user;
    const { description, original_filename } = req.body;

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

    // Update document
    await sequelize.transaction(async (t) => {
      await document.update({ description, original_filename }, { transaction: t });

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'UPDATE_DOCUMENT',
          entity_type: 'DOCUMENT',
          entity_id: documentId,
          details: { filename: document.original_filename }
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
      // TODO: Delete file from Cloudinary

      await document.destroy({ transaction: t });

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'DELETE_DOCUMENT',
          entity_type: 'DOCUMENT',
          entity_id: documentId,
          details: { filename: document.original_filename, mrfc_id: document.mrfc_id }
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
