/**
 * ================================================
 * MRFC MANAGEMENT ROUTES
 * ================================================
 * Handles MRFC (Mineral Resources and Foreign Capital) operations
 * Base path: /api/v1/mrfcs
 *
 * ENDPOINTS:
 * GET    /mrfcs              - List all MRFCs (paginated, filterable)
 * POST   /mrfcs              - Create new MRFC (ADMIN only)
 * GET    /mrfcs/:id          - Get MRFC details by ID
 * PUT    /mrfcs/:id          - Update MRFC information (ADMIN only)
 * DELETE /mrfcs/:id          - Delete MRFC (ADMIN only)
 */

import { Router } from 'express';
import { authenticate, adminOnly, checkMrfcAccess } from '../middleware/auth';
import * as mrfcController from '../controllers/mrfc.controller';

const router = Router();

/**
 * ================================================
 * GET /mrfcs
 * ================================================
 * List all MRFCs with pagination and filtering
 * ADMIN: Can see all MRFCs
 * USER: Can only see MRFCs they have access to
 *
 * QUERY PARAMETERS:
 * - page: number (default: 1)
 * - limit: number (default: 20, max: 100)
 * - search: string (search by MRFC number, project title, or proponent name)
 * - status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'UNDER_REVIEW'
 * - proponent_id: number (filter by proponent)
 * - date_from: ISO date (filter by date_received >= date_from)
 * - date_to: ISO date (filter by date_received <= date_to)
 * - sort_by: 'date_received' | 'mrfc_number' | 'project_title' (default: 'date_received')
 * - sort_order: 'ASC' | 'DESC' (default: 'DESC')
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/mrfcs?page=1&limit=20&status=PENDING&search=mining&sort_by=date_received
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "mrfcs": [
 *       {
 *         "id": 1,
 *         "mrfc_number": "MRFC-2025-001",
 *         "project_title": "Gold Mining Exploration Project",
 *         "proponent": {
 *           "id": 5,
 *           "company_name": "ABC Mining Corp.",
 *           "contact_person": "John Smith"
 *         },
 *         "status": "PENDING",
 *         "date_received": "2025-01-10T00:00:00Z",
 *         "location": "Benguet Province",
 *         "project_cost": 50000000,
 *         "assigned_users": [
 *           { "id": 3, "full_name": "Jane Doe" }
 *         ],
 *         "created_at": "2025-01-10T08:00:00Z"
 *       }
 *     ],
 *     "pagination": {
 *       "page": 1,
 *       "limit": 20,
 *       "total": 150,
 *       "totalPages": 8,
 *       "hasNext": true,
 *       "hasPrev": false
 *     }
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse and validate query parameters
 * 2. Build WHERE clause for filters (status, proponent_id, date range, search)
 * 3. Authorization check:
 *    - If USER: Add filter WHERE id IN (user's mrfc_access array)
 *    - If ADMIN: No additional filter
 * 4. For search: Use LIKE query on mrfc_number, project_title, proponent name
 * 5. Calculate pagination (offset = (page - 1) * limit)
 * 6. Query mrfcs table with JOIN to proponents table
 * 7. Include count of assigned users for each MRFC
 * 8. Get total count for pagination metadata
 * 9. Return MRFCs array with pagination info
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 400: Invalid query parameters
 * - 500: Database error
 */
router.get('/', authenticate, mrfcController.listMrfcs);

/**
 * ================================================
 * POST /mrfcs
 * ================================================
 * Create new MRFC record
 * ADMIN only
 *
 * REQUEST BODY:
 * {
 *   "mrfc_number": "MRFC-2025-001",
 *   "project_title": "Gold Mining Exploration Project",
 *   "proponent_id": 5,
 *   "date_received": "2025-01-10",
 *   "location": "Benguet Province",
 *   "project_cost": 50000000,
 *   "project_description": "Detailed description of the project...",
 *   "mineral_type": "Gold",
 *   "area_hectares": 500,
 *   "status": "PENDING",
 *   "assigned_user_ids": [3, 7]  // Optional: Array of user IDs to grant access
 * }
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "MRFC created successfully",
 *   "data": {
 *     "id": 25,
 *     "mrfc_number": "MRFC-2025-001",
 *     "project_title": "Gold Mining Exploration Project",
 *     "proponent_id": 5,
 *     "status": "PENDING",
 *     "date_received": "2025-01-10T00:00:00Z",
 *     "created_at": "2025-01-15T09:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Validate input with Joi schema
 * 2. Check if mrfc_number already exists (must be unique)
 * 3. Verify proponent_id exists in proponents table
 * 4. Create MRFC record in database
 * 5. If assigned_user_ids provided:
 *    - Validate all user IDs exist
 *    - Create user_mrfc_access records for each user
 * 6. Create audit log entry
 * 7. Return created MRFC data
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin
 * - 400: Validation error
 * - 409: MRFC number already exists
 * - 404: Proponent not found
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - MRFC number must be unique (format: MRFC-YYYY-XXX)
 * - Proponent must exist
 * - Default status is PENDING if not specified
 * - project_cost must be positive number
 * - date_received defaults to current date if not provided
 */
router.post('/', authenticate, adminOnly, mrfcController.createMrfc);

/**
 * ================================================
 * GET /mrfcs/:id
 * ================================================
 * Get detailed MRFC information by ID
 * USER: Must have access to the specific MRFC
 * ADMIN: Can access any MRFC
 *
 * URL PARAMETERS:
 * - id: number (MRFC ID)
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/mrfcs/25
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "id": 25,
 *     "mrfc_number": "MRFC-2025-001",
 *     "project_title": "Gold Mining Exploration Project",
 *     "proponent": {
 *       "id": 5,
 *       "company_name": "ABC Mining Corp.",
 *       "contact_person": "John Smith",
 *       "contact_email": "john@abcmining.com",
 *       "contact_phone": "+63 912 345 6789"
 *     },
 *     "date_received": "2025-01-10T00:00:00Z",
 *     "location": "Benguet Province",
 *     "project_cost": 50000000,
 *     "project_description": "Detailed description...",
 *     "mineral_type": "Gold",
 *     "area_hectares": 500,
 *     "status": "PENDING",
 *     "assigned_users": [
 *       { "id": 3, "full_name": "Jane Doe", "email": "jane@mgb.gov.ph" }
 *     ],
 *     "documents": [
 *       { "id": 10, "filename": "environmental_study.pdf", "uploaded_at": "2025-01-12T00:00:00Z" }
 *     ],
 *     "agendas": [
 *       { "id": 5, "quarter_id": 1, "agenda_number": "Item 5", "status": "SCHEDULED" }
 *     ],
 *     "created_at": "2025-01-10T08:00:00Z",
 *     "updated_at": "2025-01-12T14:30:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse MRFC ID from URL params
 * 2. Authorization check using checkMrfcAccess middleware
 * 3. Query mrfcs table with full details
 * 4. Include associated data:
 *    - Proponent information
 *    - Assigned users
 *    - Documents (count or list)
 *    - Agendas (current quarter)
 *    - Compliance status
 * 5. If not found: Return 404
 * 6. Return complete MRFC data
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: User does not have access to this MRFC
 * - 404: MRFC not found
 * - 500: Database error
 */
router.get('/:id', authenticate, checkMrfcAccess, mrfcController.getMrfcById);

/**
 * ================================================
 * PUT /mrfcs/:id
 * ================================================
 * Update MRFC information
 * ADMIN only
 *
 * URL PARAMETERS:
 * - id: number (MRFC ID)
 *
 * REQUEST BODY:
 * {
 *   "project_title": "Updated Project Title",
 *   "location": "Nueva Vizcaya",
 *   "status": "UNDER_REVIEW",
 *   "project_cost": 75000000,
 *   "assigned_user_ids": [3, 7, 9]
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "MRFC updated successfully",
 *   "data": {
 *     "id": 25,
 *     "mrfc_number": "MRFC-2025-001",
 *     "project_title": "Updated Project Title",
 *     "status": "UNDER_REVIEW",
 *     "updated_at": "2025-01-15T10:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse MRFC ID from URL params
 * 2. Find MRFC in database
 * 3. If not found: Return 404
 * 4. Validate update fields
 * 5. If updating proponent_id: Verify new proponent exists
 * 6. Update MRFC record
 * 7. If assigned_user_ids provided:
 *    - Remove existing user_mrfc_access records
 *    - Create new user_mrfc_access records
 * 8. Create audit log entry
 * 9. Return updated MRFC data
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin
 * - 404: MRFC or proponent not found
 * - 400: Validation error
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Cannot update mrfc_number after creation
 * - Cannot update date_received (historical record)
 * - Status transitions must be valid (e.g., APPROVED cannot go back to PENDING)
 * - Updating assigned users replaces entire list
 */
router.put('/:id', authenticate, adminOnly, mrfcController.updateMrfc);

/**
 * ================================================
 * DELETE /mrfcs/:id
 * ================================================
 * Delete MRFC record
 * ADMIN only - use with caution
 *
 * URL PARAMETERS:
 * - id: number (MRFC ID)
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "MRFC deleted successfully"
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse MRFC ID from URL params
 * 2. Find MRFC in database
 * 3. If not found: Return 404
 * 4. Check for related records (agendas, documents, attendance)
 * 5. Decide: Soft delete (recommended) or hard delete
 * 6. If soft delete:
 *    - Set status = 'DELETED' and deleted_at = NOW
 *    - Keep all related records for audit trail
 * 7. If hard delete:
 *    - Delete cascade: documents, agendas, attendance, notes, user_access
 *    - Delete MRFC record
 * 8. Create audit log entry
 * 9. Return success message
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin
 * - 404: MRFC not found
 * - 409: Cannot delete MRFC with active agendas (optional check)
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Soft delete preferred over hard delete for audit trail
 * - Cannot delete MRFCs with approved agendas (optional business rule)
 * - All related documents should be archived, not deleted
 * - Audit log must record deletion
 */
router.delete('/:id', authenticate, adminOnly, mrfcController.deleteMrfc);

export default router;
