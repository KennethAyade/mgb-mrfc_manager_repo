/**
 * ================================================
 * USER MANAGEMENT ROUTES
 * ================================================
 * Handles user CRUD operations and status management
 * Base path: /api/v1/users
 *
 * ENDPOINTS:
 * GET    /users              - List all users (paginated, filterable)
 * POST   /users              - Create new user (ADMIN only)
 * GET    /users/:id          - Get user details by ID
 * PUT    /users/:id          - Update user information
 * DELETE /users/:id          - Delete user (soft delete)
 * PUT    /users/:id/toggle-status - Activate/deactivate user account
 */

import { Router } from 'express';
import { authenticate, adminOnly } from '../middleware/auth';
import * as userController from '../controllers/user.controller';

const router = Router();

/**
 * ================================================
 * GET /users
 * ================================================
 * List all users with pagination and filtering
 * ADMIN and SUPER_ADMIN only
 *
 * QUERY PARAMETERS:
 * - page: number (default: 1)
 * - limit: number (default: 20, max: 100)
 * - search: string (search by username, full_name, or email)
 * - role: 'SUPER_ADMIN' | 'ADMIN' | 'USER' (filter by role)
 * - is_active: boolean (filter by active status)
 * - sort_by: 'created_at' | 'username' | 'last_login' (default: 'created_at')
 * - sort_order: 'ASC' | 'DESC' (default: 'DESC')
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/users?page=1&limit=20&role=USER&search=john&sort_by=username
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "users": [
 *       {
 *         "id": 5,
 *         "username": "johndoe",
 *         "full_name": "John Doe",
 *         "email": "john@example.com",
 *         "role": "USER",
 *         "is_active": true,
 *         "email_verified": true,
 *         "last_login": "2025-01-15T08:30:00Z",
 *         "created_at": "2025-01-10T10:00:00Z"
 *       }
 *     ],
 *     "pagination": {
 *       "page": 1,
 *       "limit": 20,
 *       "total": 45,
 *       "totalPages": 3,
 *       "hasNext": true,
 *       "hasPrev": false
 *     }
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse and validate query parameters
 * 2. Build WHERE clause for filters (role, is_active, search)
 * 3. For search: Use LIKE query on username, full_name, email
 * 4. Calculate pagination (offset = (page - 1) * limit)
 * 5. Query users table with filters, sorting, and pagination
 * 6. Exclude password_hash from results
 * 7. Get total count for pagination metadata
 * 8. Return users array with pagination info
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin (USER role cannot access)
 * - 400: Invalid query parameters
 * - 500: Database error
 */
router.get('/', authenticate, adminOnly, userController.listUsers);

/**
 * ================================================
 * POST /users
 * ================================================
 * Create new user account
 * ADMIN can create USER accounts
 * SUPER_ADMIN can create USER and ADMIN accounts
 *
 * REQUEST BODY:
 * {
 *   "username": "newuser",
 *   "password": "SecurePass123!",
 *   "full_name": "New User",
 *   "email": "newuser@example.com",
 *   "role": "USER",
 *   "is_active": true,
 *   "mrfc_access": [1, 3, 5]  // Optional: Array of MRFC IDs (for USER role only)
 * }
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "User created successfully",
 *   "data": {
 *     "id": 10,
 *     "username": "newuser",
 *     "full_name": "New User",
 *     "email": "newuser@example.com",
 *     "role": "USER",
 *     "is_active": true,
 *     "created_at": "2025-01-15T09:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Validate input with Joi schema
 * 2. Authorization check:
 *    - If creating ADMIN: Require SUPER_ADMIN role
 *    - If creating USER: Allow ADMIN or SUPER_ADMIN
 * 3. Check if username already exists
 * 4. Check if email already exists
 * 5. Hash password with bcrypt
 * 6. Create user record in users table
 * 7. If role is USER and mrfc_access provided:
 *    - Insert records into user_mrfc_access table
 * 8. Return user data (without password)
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Insufficient permissions (e.g., ADMIN trying to create ADMIN)
 * - 400: Validation error
 * - 409: Username or email already exists
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Only SUPER_ADMIN can create ADMIN accounts
 * - Username must be unique and 3-50 characters
 * - Email must be valid and unique
 * - Password must meet complexity requirements
 * - Default role is USER if not specified
 * - New accounts are active by default (is_active: true)
 */
router.post('/', authenticate, adminOnly, userController.createUser);

/**
 * ================================================
 * GET /users/:id
 * ================================================
 * Get detailed user information by ID
 * ADMIN can view any user
 * USER can only view their own profile
 *
 * URL PARAMETERS:
 * - id: number (user ID)
 *
 * EXAMPLE REQUEST:
 * GET /api/v1/users/5
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "id": 5,
 *     "username": "johndoe",
 *     "full_name": "John Doe",
 *     "email": "john@example.com",
 *     "role": "USER",
 *     "is_active": true,
 *     "email_verified": true,
 *     "last_login": "2025-01-15T08:30:00Z",
 *     "created_at": "2025-01-10T10:00:00Z",
 *     "updated_at": "2025-01-12T14:20:00Z",
 *     "mrfc_access": [
 *       {
 *         "mrfc_id": 1,
 *         "mrfc_name": "MRFC-2025-001",
 *         "granted_at": "2025-01-10T10:00:00Z",
 *         "granted_by": "admin"
 *       }
 *     ]
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse user ID from URL params
 * 2. Authorization check:
 *    - If ADMIN/SUPER_ADMIN: Allow access to any user
 *    - If USER: Only allow if ID matches their own (req.user.userId === parseInt(id))
 * 3. Query users table by ID
 * 4. If user not found: Return 404
 * 5. Exclude password_hash from result
 * 6. If user role is USER: Join with user_mrfc_access and mrfcs tables
 * 7. Return user data with MRFC access list
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: USER trying to access another user's profile
 * - 404: User not found
 * - 500: Database error
 */
router.get('/:id', authenticate, userController.getUserById);

/**
 * ================================================
 * PUT /users/:id
 * ================================================
 * Update user information
 * ADMIN can update any user
 * USER can only update their own basic info (not role/status)
 *
 * URL PARAMETERS:
 * - id: number (user ID)
 *
 * REQUEST BODY:
 * {
 *   "full_name": "Updated Name",
 *   "email": "updated@example.com",
 *   "role": "ADMIN",  // ADMIN only
 *   "is_active": false,  // ADMIN only
 *   "mrfc_access": [1, 2, 3]  // ADMIN only, for USER role
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "User updated successfully",
 *   "data": {
 *     "id": 5,
 *     "username": "johndoe",
 *     "full_name": "Updated Name",
 *     "email": "updated@example.com",
 *     "role": "USER",
 *     "is_active": true,
 *     "updated_at": "2025-01-15T10:00:00Z"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse user ID from URL params
 * 2. Find user in database
 * 3. If not found: Return 404
 * 4. Authorization check:
 *    - ADMIN: Can update all fields
 *    - USER: Can only update own profile and only full_name, email
 * 5. Validate which fields can be updated based on role
 * 6. If updating email: Check if new email already exists
 * 7. Update user record
 * 8. If mrfc_access provided and user is USER role:
 *    - Delete existing access records
 *    - Create new access records
 * 9. Return updated user data
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: USER trying to update another user or restricted fields
 * - 404: User not found
 * - 400: Validation error
 * - 409: Email already exists
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Username cannot be changed after creation
 * - USER can only update: full_name, email
 * - ADMIN can update: full_name, email, role, is_active, mrfc_access
 * - Cannot change own role to prevent privilege escalation
 * - Cannot deactivate own account
 */
router.put('/:id', authenticate, adminOnly, userController.updateUser);

/**
 * ================================================
 * DELETE /users/:id
 * ================================================
 * Delete user account (soft delete)
 * ADMIN only - cannot delete own account
 *
 * URL PARAMETERS:
 * - id: number (user ID)
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "User deleted successfully"
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse user ID from URL params
 * 2. Check if trying to delete own account (prevent self-deletion)
 * 3. Find user in database
 * 4. If not found: Return 404
 * 5. Soft delete (set is_active = false and deleted_at = NOW)
 *    OR hard delete based on business requirements
 * 6. Cascade: Deactivate all user_mrfc_access records
 * 7. Cascade: Anonymize or delete related data (notes, attendance, etc.)
 * 8. Return success message
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin or trying to delete own account
 * - 404: User not found
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Cannot delete own account (use separate deactivation flow)
 * - Soft delete preferred over hard delete for audit trail
 * - Related data handling:
 *   - MRFCs: Keep (only remove access)
 *   - Attendance records: Keep for historical accuracy
 *   - Notes: Keep but mark as deleted
 *   - Audit logs: Always keep
 */
router.delete('/:id', authenticate, adminOnly, userController.deleteUser);

/**
 * ================================================
 * PUT /users/:id/toggle-status
 * ================================================
 * Activate or deactivate user account
 * ADMIN only - cannot toggle own status
 *
 * URL PARAMETERS:
 * - id: number (user ID)
 *
 * EXAMPLE REQUEST:
 * PUT /api/v1/users/5/toggle-status
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "User activated successfully",
 *   "data": {
 *     "id": 5,
 *     "username": "johndoe",
 *     "is_active": true
 *   }
 * }
 */
router.put('/:id/toggle-status', authenticate, adminOnly, userController.toggleUserStatus);

/**
 * ================================================
 * PUT /users/:id/toggle-status
 * ================================================
 * Activate or deactivate user account
 * ADMIN only - cannot toggle own status
 *
 * URL PARAMETERS:
 * - id: number (user ID)
 *
 * REQUEST BODY:
 * {
 *   "is_active": false
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "User account deactivated successfully",
 *   "data": {
 *     "id": 5,
 *     "username": "johndoe",
 *     "is_active": false
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Parse user ID from URL params
 * 2. Check if trying to toggle own status (prevent self-lockout)
 * 3. Find user in database
 * 4. If not found: Return 404
 * 5. Update is_active field
 * 6. If deactivating: Invalidate all user's active sessions/tokens (optional)
 * 7. Return updated status
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 403: Not admin or trying to toggle own status
 * - 404: User not found
 * - 400: Validation error
 * - 500: Database error
 *
 * BUSINESS RULES:
 * - Cannot toggle own account status (prevent accidental lockout)
 * - Deactivated users cannot login
 * - Existing sessions may remain valid until token expiry
 * - Reactivating user restores all previous permissions and MRFC access
 */
// Toggle status can be done via PUT /users/:id with {"is_active": true/false}

/**
 * ================================================
 * POST /users/:id/grant-mrfc-access
 * ================================================
 * Grant MRFC access to a user
 * ADMIN only
 */
router.post('/:id/grant-mrfc-access', authenticate, adminOnly, userController.grantMrfcAccess);

export default router;
