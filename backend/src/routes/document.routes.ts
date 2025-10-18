/**
 * ================================================
 * DOCUMENT MANAGEMENT ROUTES
 * ================================================
 * Handles document upload, download, and management for MRFCs
 * Base path: /api/v1/documents
 *
 * ENDPOINTS:
 * GET    /documents              - List documents (filterable by MRFC)
 * POST   /documents/upload       - Upload document (requires multipart/form-data)
 * GET    /documents/:id          - Get document metadata
 * GET    /documents/:id/download - Download document file
 * PUT    /documents/:id          - Update document metadata (ADMIN only)
 * DELETE /documents/:id          - Delete document (ADMIN only)
 */

import { Router, Request, Response } from 'express';
import { authenticate, adminOnly, checkMrfcAccess } from '../middleware/auth';

const router = Router();

/**
 * ================================================
 * GET /documents
 * ================================================
 * List all documents with filtering
 * USER: Can only see documents for MRFCs they have access to
 * ADMIN: Can see all documents
 *
 * QUERY PARAMETERS:
 * - mrfc_id: number (filter by MRFC, required for USER role)
 * - file_type: string (filter by file type, e.g., 'pdf', 'docx')
 * - uploaded_after: ISO date (filter by upload date >= date)
 * - uploaded_before: ISO date (filter by upload date <= date)
 * - page: number (default: 1)
 * - limit: number (default: 20, max: 100)
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/documents?mrfc_id=25&file_type=pdf&page=1
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "documents": [
 *       {
 *         "id": 10,
 *         "mrfc_id": 25,
 *         "filename": "environmental_impact_assessment.pdf",
 *         "original_filename": "EIA Report 2025.pdf",
 *         "file_type": "pdf",
 *         "file_size": 5242880,
 *         "file_path": "/uploads/mrfc-25/environmental_impact_assessment.pdf",
 *         "description": "Environmental Impact Assessment Report",
 *         "uploaded_by": {
 *           "id": 3,
 *           "full_name": "Jane Doe"
 *         },
 *         "uploaded_at": "2025-01-12T10:30:00Z"
 *       }
 *     ],
 *     "pagination": {
 *       "page": 1,
 *       "limit": 20,
 *       "total": 8,
 *       "totalPages": 1,
 *       "hasNext": false,
 *       "hasPrev": false
 *     }
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse and validate query parameters
 * 2. Authorization check:
 *    - If USER: Require mrfc_id and verify access
 *    - If ADMIN: mrfc_id optional
 * 3. Build WHERE clause for filters
 * 4. Calculate pagination
 * 5. Query documents table with JOIN to users (uploader info)
 * 6. Return documents array with pagination
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: USER accessing MRFC without access
 * - 400: Invalid query parameters or missing mrfc_id for USER
 * - 500: Database error
 */
router.get('/', authenticate, async (req: Request, res: Response) => {
  try {
    // TODO: IMPLEMENT DOCUMENT LISTING LOGIC
    // Step 1: Parse query parameters
    // const { mrfc_id, file_type, uploaded_after, uploaded_before, page = 1, limit = 20 } = req.query;

    // Step 2: USER role validation
    // if (req.user?.role === 'USER') {
    //   if (!mrfc_id) {
    //     return res.status(400).json({
    //       success: false,
    //       error: {
    //         code: 'MRFC_ID_REQUIRED',
    //         message: 'mrfc_id parameter is required for USER role'
    //       }
    //     });
    //   }
    //   const userMrfcIds = req.user.mrfcAccess || [];
    //   if (!userMrfcIds.includes(parseInt(mrfc_id as string))) {
    //     return res.status(403).json({
    //       success: false,
    //       error: { code: 'MRFC_ACCESS_DENIED', message: 'Access denied to this MRFC' }
    //     });
    //   }
    // }

    // Step 3: Build filters
    // const where: any = {};
    // if (mrfc_id) where.mrfc_id = mrfc_id;
    // if (file_type) where.file_type = file_type;
    // if (uploaded_after) where.uploaded_at = { [Op.gte]: uploaded_after };
    // if (uploaded_before) where.uploaded_at = { ...where.uploaded_at, [Op.lte]: uploaded_before };

    // Step 4: Query documents
    // const pageNum = Math.max(1, parseInt(page as string));
    // const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    // const offset = (pageNum - 1) * limitNum;
    //
    // const { count, rows: documents } = await Document.findAndCountAll({
    //   where,
    //   include: [{
    //     model: User,
    //     as: 'uploaded_by',
    //     attributes: ['id', 'full_name']
    //   }],
    //   limit: limitNum,
    //   offset,
    //   order: [['uploaded_at', 'DESC']]
    // });

    // Step 5: Return results
    // return res.json({
    //   success: true,
    //   data: {
    //     documents,
    //     pagination: {
    //       page: pageNum,
    //       limit: limitNum,
    //       total: count,
    //       totalPages: Math.ceil(count / limitNum),
    //       hasNext: pageNum * limitNum < count,
    //       hasPrev: pageNum > 1
    //     }
    //   }
    // });

    res.status(501).json({
      success: false,
      error: {
        code: 'NOT_IMPLEMENTED',
        message: 'Document listing endpoint not yet implemented. See comments in document.routes.ts for implementation details.'
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
});

/**
 * ================================================
 * POST /documents/upload
 * ================================================
 * Upload document file for an MRFC
 * USER: Can upload to MRFCs they have access to
 * ADMIN: Can upload to any MRFC
 *
 * REQUEST (multipart/form-data):
 * - file: File (required, max 50MB)
 * - mrfc_id: number (required)
 * - description: string (optional)
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "Document uploaded successfully",
 *   "data": {
 *     "id": 10,
 *     "mrfc_id": 25,
 *     "filename": "environmental_impact_assessment_1642156800000.pdf",
 *     "original_filename": "EIA Report 2025.pdf",
 *     "file_type": "pdf",
 *     "file_size": 5242880,
 *     "file_path": "/uploads/mrfc-25/environmental_impact_assessment_1642156800000.pdf",
 *     "description": "Environmental Impact Assessment Report",
 *     "uploaded_at": "2025-01-15T10:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Use multer middleware to handle file upload
 * 2. Validate file exists and meets requirements:
 *    - Max size: 50MB
 *    - Allowed types: pdf, doc, docx, xls, xlsx, jpg, png
 * 3. Verify mrfc_id exists
 * 4. Authorization check:
 *    - If USER: Verify MRFC access
 *    - If ADMIN: Allow
 * 5. Generate unique filename (timestamp + original name)
 * 6. Save file to disk (e.g., /uploads/mrfc-{id}/)
 * 7. Create document record in database
 * 8. Create audit log entry
 * 9. Return document metadata
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: USER uploading to inaccessible MRFC
 * - 400: No file provided, file too large, or invalid file type
 * - 404: MRFC not found
 * - 500: File system error or database error
 *
 * BUSINESS RULES:
 * - Max file size: 50MB (configurable)
 * - Allowed file types: PDF, Word, Excel, images
 * - Files stored in /uploads/mrfc-{id}/ directory
 * - Filenames sanitized and made unique
 * - Virus scanning recommended (future enhancement)
 */
router.post('/upload', authenticate, async (req: Request, res: Response) => {
  try {
    // TODO: IMPLEMENT DOCUMENT UPLOAD LOGIC
    // Step 1: Configure multer middleware
    // const upload = multer({
    //   storage: multer.diskStorage({
    //     destination: (req, file, cb) => {
    //       const mrfcId = req.body.mrfc_id;
    //       const dir = path.join(__dirname, '../../uploads', `mrfc-${mrfcId}`);
    //       fs.mkdirSync(dir, { recursive: true });
    //       cb(null, dir);
    //     },
    //     filename: (req, file, cb) => {
    //       const uniqueName = `${Date.now()}_${file.originalname}`;
    //       cb(null, uniqueName);
    //     }
    //   }),
    //   limits: { fileSize: 50 * 1024 * 1024 }, // 50MB
    //   fileFilter: (req, file, cb) => {
    //     const allowedTypes = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'jpg', 'png'];
    //     const ext = path.extname(file.originalname).toLowerCase().slice(1);
    //     if (allowedTypes.includes(ext)) {
    //       cb(null, true);
    //     } else {
    //       cb(new Error('Invalid file type'));
    //     }
    //   }
    // });

    // Step 2: Verify MRFC exists and check access
    // const mrfc = await MRFC.findByPk(req.body.mrfc_id);
    // if (!mrfc) {
    //   return res.status(404).json({
    //     success: false,
    //     error: { code: 'MRFC_NOT_FOUND', message: 'MRFC not found' }
    //   });
    // }

    // Step 3: Authorization check
    // if (req.user?.role === 'USER') {
    //   const userMrfcIds = req.user.mrfcAccess || [];
    //   if (!userMrfcIds.includes(mrfc.id)) {
    //     return res.status(403).json({
    //       success: false,
    //       error: { code: 'MRFC_ACCESS_DENIED', message: 'Access denied' }
    //     });
    //   }
    // }

    // Step 4: Create document record
    // const document = await Document.create({
    //   mrfc_id: req.body.mrfc_id,
    //   filename: req.file.filename,
    //   original_filename: req.file.originalname,
    //   file_type: path.extname(req.file.originalname).slice(1),
    //   file_size: req.file.size,
    //   file_path: req.file.path,
    //   description: req.body.description,
    //   uploaded_by: req.user?.userId
    // });

    // Step 5: Create audit log
    // await AuditLog.create({
    //   user_id: req.user?.userId,
    //   action: 'UPLOAD_DOCUMENT',
    //   entity_type: 'DOCUMENT',
    //   entity_id: document.id,
    //   details: { filename: document.original_filename, mrfc_id: document.mrfc_id }
    // });

    // Step 6: Return document metadata
    // return res.status(201).json({
    //   success: true,
    //   message: 'Document uploaded successfully',
    //   data: document
    // });

    res.status(501).json({
      success: false,
      error: {
        code: 'NOT_IMPLEMENTED',
        message: 'Document upload endpoint not yet implemented. See comments in document.routes.ts for implementation details.'
      }
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
});

/**
 * ================================================
 * GET /documents/:id
 * ================================================
 * Get document metadata by ID
 * USER: Must have access to the MRFC
 * ADMIN: Can access any document
 *
 * URL PARAMETERS:
 * - id: number (document ID)
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "id": 10,
 *     "mrfc_id": 25,
 *     "mrfc": {
 *       "mrfc_number": "MRFC-2025-001",
 *       "project_title": "Gold Mining Project"
 *     },
 *     "filename": "environmental_impact_assessment.pdf",
 *     "original_filename": "EIA Report 2025.pdf",
 *     "file_type": "pdf",
 *     "file_size": 5242880,
 *     "description": "Environmental Impact Assessment Report",
 *     "uploaded_by": {
 *       "id": 3,
 *       "full_name": "Jane Doe"
 *     },
 *     "uploaded_at": "2025-01-12T10:30:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse document ID from URL params
 * 2. Find document with MRFC details
 * 3. If not found: Return 404
 * 4. Authorization check:
 *    - If USER: Verify MRFC access
 *    - If ADMIN: Allow
 * 5. Return document metadata (not the file itself)
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: USER accessing document for inaccessible MRFC
 * - 404: Document not found
 * - 500: Database error
 */
router.get('/:id', authenticate, async (req: Request, res: Response) => {
  try {
    // TODO: IMPLEMENT GET DOCUMENT METADATA LOGIC
    // See comments above for implementation steps

    res.status(501).json({
      success: false,
      error: {
        code: 'NOT_IMPLEMENTED',
        message: 'Get document metadata endpoint not yet implemented. See comments in document.routes.ts for implementation details.'
      }
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
});

/**
 * ================================================
 * GET /documents/:id/download
 * ================================================
 * Download document file
 * USER: Must have access to the MRFC
 * ADMIN: Can download any document
 *
 * URL PARAMETERS:
 * - id: number (document ID)
 *
 * RESPONSE (200):
 * - Content-Type: application/octet-stream (or specific MIME type)
 * - Content-Disposition: attachment; filename="original_filename.pdf"
 * - Binary file data
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse document ID from URL params
 * 2. Find document in database
 * 3. If not found: Return 404
 * 4. Authorization check:
 *    - If USER: Verify MRFC access
 *    - If ADMIN: Allow
 * 5. Check if file exists on disk
 * 6. Set appropriate headers (Content-Type, Content-Disposition)
 * 7. Stream file to response
 * 8. Log download in audit log
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: USER downloading document for inaccessible MRFC
 * - 404: Document not found or file not found on disk
 * - 500: File system error
 */
router.get('/:id/download', authenticate, async (req: Request, res: Response) => {
  try {
    // TODO: IMPLEMENT DOCUMENT DOWNLOAD LOGIC
    // Step 1: Find document
    // const document = await Document.findByPk(req.params.id, {
    //   include: [{ model: MRFC, as: 'mrfc' }]
    // });
    // if (!document) {
    //   return res.status(404).json({
    //     success: false,
    //     error: { code: 'DOCUMENT_NOT_FOUND', message: 'Document not found' }
    //   });
    // }

    // Step 2: Authorization check
    // if (req.user?.role === 'USER') {
    //   const userMrfcIds = req.user.mrfcAccess || [];
    //   if (!userMrfcIds.includes(document.mrfc_id)) {
    //     return res.status(403).json({
    //       success: false,
    //       error: { code: 'MRFC_ACCESS_DENIED', message: 'Access denied' }
    //     });
    //   }
    // }

    // Step 3: Check file exists
    // if (!fs.existsSync(document.file_path)) {
    //   return res.status(404).json({
    //     success: false,
    //     error: { code: 'FILE_NOT_FOUND', message: 'File not found on server' }
    //   });
    // }

    // Step 4: Set headers and stream file
    // const mimeType = mime.lookup(document.file_type) || 'application/octet-stream';
    // res.setHeader('Content-Type', mimeType);
    // res.setHeader('Content-Disposition', `attachment; filename="${document.original_filename}"`);
    // res.setHeader('Content-Length', document.file_size);
    //
    // const fileStream = fs.createReadStream(document.file_path);
    // fileStream.pipe(res);

    // Step 5: Log download
    // await AuditLog.create({
    //   user_id: req.user?.userId,
    //   action: 'DOWNLOAD_DOCUMENT',
    //   entity_type: 'DOCUMENT',
    //   entity_id: document.id,
    //   details: { filename: document.original_filename }
    // });

    res.status(501).json({
      success: false,
      error: {
        code: 'NOT_IMPLEMENTED',
        message: 'Document download endpoint not yet implemented. See comments in document.routes.ts for implementation details.'
      }
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
});

/**
 * ================================================
 * PUT /documents/:id
 * ================================================
 * Update document metadata (not the file itself)
 * ADMIN only
 *
 * URL PARAMETERS:
 * - id: number (document ID)
 *
 * REQUEST BODY:
 * {
 *   "description": "Updated description",
 *   "original_filename": "Updated Filename.pdf"
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Document updated successfully",
 *   "data": {
 *     "id": 10,
 *     "description": "Updated description",
 *     "updated_at": "2025-01-15T10:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse document ID
 * 2. Find document in database
 * 3. If not found: Return 404
 * 4. Update allowed fields (description, original_filename)
 * 5. Create audit log entry
 * 6. Return updated document
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin
 * - 404: Document not found
 * - 400: Validation error
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Cannot update: mrfc_id, filename, file_path, file_size, file_type
 * - Can update: description, original_filename
 * - To replace file: Delete old document and upload new one
 */
router.put('/:id', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    // TODO: IMPLEMENT DOCUMENT UPDATE LOGIC
    // See comments above for implementation steps

    res.status(501).json({
      success: false,
      error: {
        code: 'NOT_IMPLEMENTED',
        message: 'Document update endpoint not yet implemented. See comments in document.routes.ts for implementation details.'
      }
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
});

/**
 * ================================================
 * DELETE /documents/:id
 * ================================================
 * Delete document (both record and file)
 * ADMIN only
 *
 * URL PARAMETERS:
 * - id: number (document ID)
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Document deleted successfully"
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse document ID
 * 2. Find document in database
 * 3. If not found: Return 404
 * 4. Delete file from disk
 * 5. Delete document record from database
 * 6. Create audit log entry
 * 7. Return success message
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin
 * - 404: Document not found
 * - 500: File system error or database error
 *
 * BUSINESS RULES:
 * - Permanently deletes both database record and file
 * - Cannot be undone
 * - If file doesn't exist on disk, still delete database record
 * - Audit log must record deletion
 */
router.delete('/:id', authenticate, adminOnly, async (req: Request, res: Response) => {
  try {
    // TODO: IMPLEMENT DOCUMENT DELETION LOGIC
    // Step 1: Find document
    // const document = await Document.findByPk(req.params.id);
    // if (!document) {
    //   return res.status(404).json({
    //     success: false,
    //     error: { code: 'DOCUMENT_NOT_FOUND', message: 'Document not found' }
    //   });
    // }

    // Step 2: Delete file from disk
    // if (fs.existsSync(document.file_path)) {
    //   fs.unlinkSync(document.file_path);
    // }

    // Step 3: Delete database record
    // await document.destroy();

    // Step 4: Create audit log
    // await AuditLog.create({
    //   user_id: req.user?.userId,
    //   action: 'DELETE_DOCUMENT',
    //   entity_type: 'DOCUMENT',
    //   entity_id: document.id,
    //   details: { filename: document.original_filename, mrfc_id: document.mrfc_id }
    // });

    // Step 5: Return success
    // return res.json({
    //   success: true,
    //   message: 'Document deleted successfully'
    // });

    res.status(501).json({
      success: false,
      error: {
        code: 'NOT_IMPLEMENTED',
        message: 'Document deletion endpoint not yet implemented. See comments in document.routes.ts for implementation details.'
      }
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
});

export default router;
