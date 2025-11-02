/**
 * ================================================
 * DOCUMENT MANAGEMENT ROUTES
 * ================================================
 * Handles document upload, download, and management for proponent compliance documents
 * Base path: /api/v1/documents
 *
 * ENDPOINTS:
 * GET    /documents                        - List documents (filterable by proponent, quarter, category, status)
 * POST   /documents/upload                 - Upload document (requires multipart/form-data)
 * POST   /documents/generate-upload-token  - Generate upload token for proponent (ADMIN only)
 * POST   /documents/upload-via-token/:token - Upload via token (no auth required)
 * GET    /documents/:id                    - Get document metadata
 * GET    /documents/:id/download           - Get document download URL
 * PUT    /documents/:id                    - Update document status/remarks (approval workflow, ADMIN only)
 * DELETE /documents/:id                    - Delete document (ADMIN only)
 */

import { Router } from 'express';
import { authenticate, adminOnly } from '../middleware/auth';
import { uploadDocument as uploadMiddleware } from '../middleware/upload';
import * as documentController from '../controllers/document.controller';

const router = Router();

/**
 * ================================================
 * GET /documents
 * ================================================
 * List all documents with filtering
 *
 * QUERY PARAMETERS:
 * - proponent_id: number (filter by proponent)
 * - quarter_id: number (filter by quarter)
 * - category: string (MTF_REPORT, AEPEP, CMVR, SDMP, PRODUCTION, SAFETY, OTHER)
 * - status: string (PENDING, ACCEPTED, REJECTED)
 * - search: string (search by filename)
 * - page: number (default: 1)
 * - limit: number (default: 20, max: 100)
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/documents?proponent_id=5&quarter_id=2&category=MTF_REPORT&status=PENDING&page=1
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "documents": [
 *       {
 *         "id": 10,
 *         "proponent_id": 5,
 *         "quarter_id": 2,
 *         "file_name": "Cebu-MRFC_MTF-REPORT_Q1_2025.pdf",
 *         "original_name": "MTF Report January 2025.pdf",
 *         "file_type": "application/pdf",
 *         "file_size": 5242880,
 *         "category": "MTF_REPORT",
 *         "file_url": "https://res.cloudinary.com/...",
 *         "status": "PENDING",
 *         "upload_date": "2025-01-12T10:30:00Z",
 *         "proponent": {
 *           "id": 5,
 *           "name": "ABC Mining Corp",
 *           "mrfc": {
 *             "id": 2,
 *             "name": "Cebu MRFC",
 *             "municipality": "Cebu City"
 *           }
 *         },
 *         "quarter": {
 *           "id": 2,
 *           "name": "Q1 2025",
 *           "year": 2025,
 *           "quarter_number": 1
 *         },
 *         "uploader": {
 *           "id": 3,
 *           "full_name": "Jane Doe"
 *         }
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
 */
router.get('/', authenticate, documentController.listDocuments);

/**
 * ================================================
 * POST /documents/upload
 * ================================================
 * Upload compliance document for a proponent
 *
 * REQUEST (multipart/form-data):
 * - file: File (required, max 10MB, PDF/DOC/XLS/images)
 * - proponent_id: number (required)
 * - quarter_id: number (required)
 * - category: string (required: MTF_REPORT, AEPEP, CMVR, SDMP, PRODUCTION, SAFETY, OTHER)
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "Document uploaded successfully",
 *   "data": {
 *     "id": 10,
 *     "file_name": "Cebu-MRFC_MTF-REPORT_Q1_2025.pdf",
 *     "original_name": "MTF Report.pdf",
 *     "file_url": "https://res.cloudinary.com/...",
 *     "file_type": "application/pdf",
 *     "file_size": 5242880,
 *     "category": "MTF_REPORT",
 *     "status": "PENDING",
 *     "upload_date": "2025-01-15T10:00:00Z",
 *     "proponent_id": 5,
 *     "quarter_id": 2
 *   }
 * }
 *
 * BUSINESS RULES:
 * - File uploaded to Cloudinary
 * - Filename convention: {MRFCName}_{Category}_{Quarter}_{Year}.{ext}
 * - Status automatically set to PENDING (awaiting review)
 * - Max file size: 10MB (configured in upload middleware)
 */
router.post('/upload', authenticate, uploadMiddleware.single('file'), documentController.uploadDocument);

/**
 * ================================================
 * POST /documents/generate-upload-token
 * ================================================
 * Generate shareable upload token for proponents
 * ADMIN only
 *
 * REQUEST BODY:
 * {
 *   "proponent_id": 5,
 *   "quarter_id": 2,
 *   "category": "MTF_REPORT",
 *   "expires_in_hours": 48
 * }
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "Upload token generated successfully",
 *   "data": {
 *     "token": "eyJwcm9wb25lbnRfaWQiOjUsInF1YXJ0ZXJfaWQiOjI...",
 *     "upload_url": "http://localhost:5000/api/v1/documents/upload-via-token/eyJwcm9wb25...",
 *     "proponent_id": 5,
 *     "quarter_id": 2,
 *     "category": "MTF_REPORT",
 *     "expires_at": "2025-01-17T10:00:00Z",
 *     "expires_in_hours": 48
 *   }
 * }
 *
 * BUSINESS RULES:
 * - Token expires after specified hours (default: 48)
 * - Token can only be used for specified proponent, quarter, and category
 * - Shareable URL can be sent to proponent via email/SMS
 * - Proponent can upload without logging in
 */
router.post('/generate-upload-token', authenticate, adminOnly, documentController.generateUploadToken);

/**
 * ================================================
 * POST /documents/upload-via-token/:token
 * ================================================
 * Upload document using pre-generated token
 * NO AUTHENTICATION REQUIRED
 *
 * URL PARAMETERS:
 * - token: string (base64url encoded token)
 *
 * REQUEST (multipart/form-data):
 * - file: File (required)
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "Document uploaded successfully via token",
 *   "data": {
 *     "id": 10,
 *     "file_name": "Cebu-MRFC_MTF-REPORT_Q1_2025.pdf",
 *     "original_name": "MTF Report.pdf",
 *     "file_type": "application/pdf",
 *     "file_size": 5242880,
 *     "category": "MTF_REPORT",
 *     "status": "PENDING",
 *     "upload_date": "2025-01-15T10:00:00Z"
 *   }
 * }
 *
 * ERROR CASES:
 * - 400: Invalid token format
 * - 401: Token expired
 * - 404: Proponent or quarter not found
 */
router.post('/upload-via-token/:token', uploadMiddleware.single('file'), documentController.uploadViaToken);

/**
 * ================================================
 * GET /documents/:id
 * ================================================
 * Get document metadata by ID
 *
 * URL PARAMETERS:
 * - id: number (document ID)
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "id": 10,
 *     "proponent": {
 *       "id": 5,
 *       "name": "ABC Mining Corp",
 *       "mrfc": {
 *         "id": 2,
 *         "name": "Cebu MRFC"
 *       }
 *     },
 *     "quarter": {
 *       "id": 2,
 *       "name": "Q1 2025"
 *     },
 *     "file_name": "Cebu-MRFC_MTF-REPORT_Q1_2025.pdf",
 *     "original_name": "MTF Report.pdf",
 *     "file_type": "application/pdf",
 *     "file_size": 5242880,
 *     "category": "MTF_REPORT",
 *     "file_url": "https://res.cloudinary.com/...",
 *     "status": "PENDING",
 *     "upload_date": "2025-01-12T10:30:00Z",
 *     "uploader": {
 *       "id": 3,
 *       "full_name": "Jane Doe"
 *     },
 *     "reviewer": null,
 *     "reviewed_at": null,
 *     "remarks": null
 *   }
 * }
 */
router.get('/:id', authenticate, documentController.getDocumentById);

/**
 * ================================================
 * GET /documents/:id/download
 * ================================================
 * Get document download URL
 *
 * URL PARAMETERS:
 * - id: number (document ID)
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Document download URL retrieved",
 *   "data": {
 *     "file_url": "https://res.cloudinary.com/...",
 *     "file_name": "Cebu-MRFC_MTF-REPORT_Q1_2025.pdf",
 *     "original_name": "MTF Report.pdf",
 *     "file_type": "application/pdf",
 *     "file_size": 5242880
 *   }
 * }
 *
 * BUSINESS RULES:
 * - Returns Cloudinary URL for file download
 * - Logs download action in audit log
 * - Frontend can use URL to download file
 */
router.get('/:id/download', authenticate, documentController.downloadDocument);

/**
 * ================================================
 * PUT /documents/:id
 * ================================================
 * Update document status and remarks (approval workflow)
 * ADMIN only
 *
 * URL PARAMETERS:
 * - id: number (document ID)
 *
 * REQUEST BODY:
 * {
 *   "status": "ACCEPTED", // or "REJECTED" or "PENDING"
 *   "remarks": "Document approved after review"
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Document updated successfully",
 *   "data": {
 *     "id": 10,
 *     "status": "ACCEPTED",
 *     "reviewed_by": 1,
 *     "reviewed_at": "2025-01-15T10:00:00Z",
 *     "remarks": "Document approved after review"
 *   }
 * }
 *
 * BUSINESS RULES:
 * - Status changes: PENDING → ACCEPTED or REJECTED
 * - When accepting/rejecting, automatically records reviewer_id and reviewed_at
 * - Creates audit log entry (APPROVE_DOCUMENT or REJECT_DOCUMENT)
 * - Remarks field for approval/rejection notes
 */
router.put('/:id', authenticate, adminOnly, documentController.updateDocument);

/**
 * ================================================
 * DELETE /documents/:id
 * ================================================
 * Delete document (both database record and Cloudinary file)
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
 * BUSINESS RULES:
 * - Permanently deletes both database record and file from Cloudinary
 * - Cannot be undone
 * - Creates audit log entry
 * - If Cloudinary deletion fails, still deletes database record
 */
router.delete('/:id', authenticate, adminOnly, documentController.deleteDocument);

export default router;
