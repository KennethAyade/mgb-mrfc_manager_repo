/**
 * ================================================
 * AUTHENTICATION ROUTES
 * ================================================
 * Handles user registration, login, token refresh, and password management
 * Base path: /api/v1/auth
 *
 * ENDPOINTS:
 * POST   /auth/register           - User self-registration
 * POST   /auth/login              - User login
 * POST   /auth/refresh            - Refresh JWT token
 * POST   /auth/logout             - Logout user
 * POST   /auth/change-password    - Change password (authenticated)
 */

import { Router } from 'express';
import { validate, registerSchema, loginSchema, changePasswordSchema } from '../utils/validation';
import { authenticate } from '../middleware/auth';
import * as authController from '../controllers/auth.controller';

const router = Router();

/**
 * ================================================
 * POST /auth/register
 * ================================================
 * User self-registration endpoint
 * Creates new USER account (not ADMIN)
 *
 * REQUEST BODY:
 * {
 *   "username": "johndoe",
 *   "password": "SecurePass123!",
 *   "full_name": "John Doe",
 *   "email": "john@example.com"
 * }
 *
 * RESPONSE (201):
 * {
 *   "success": true,
 *   "message": "Registration successful. Please wait for admin approval.",
 *   "data": {
 *     "id": 5,
 *     "username": "johndoe",
 *     "full_name": "John Doe",
 *     "email": "john@example.com",
 *     "role": "USER",
 *     "is_active": true,
 *     "email_verified": false
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Validate input with Joi schema
 * 2. Check if username already exists (query users table)
 * 3. Check if email already exists (query users table)
 * 4. Hash password with bcrypt
 * 5. Create user record in database:
 *    - username, password_hash, full_name, email
 *    - role: 'USER' (hardcoded, no self-registration as ADMIN)
 *    - is_active: true (or false if email verification required)
 *    - email_verified: false
 * 6. Return user data (without password hash)
 * 7. Optional: Send verification email (future feature)
 *
 * ERROR CASES:
 * - 400: Validation error (missing fields, invalid email, etc.)
 * - 409: Username or email already exists
 * - 500: Database error or password hashing error
 */
router.post('/register', validate(registerSchema, 'body'), authController.register);

/**
 * ================================================
 * POST /auth/login
 * ================================================
 * User login endpoint
 * Returns JWT access token and refresh token
 *
 * REQUEST BODY:
 * {
 *   "username": "admin",
 *   "password": "admin123"
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "user": {
 *       "id": 1,
 *       "username": "admin",
 *       "full_name": "System Administrator",
 *       "email": "admin@mgb.gov.ph",
 *       "role": "SUPER_ADMIN"
 *     },
 *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *     "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *     "expiresIn": "24h"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Validate input with Joi schema
 * 2. Find user by username
 * 3. Check if user exists
 * 4. Check if user is active (is_active = true)
 * 5. Verify password with bcrypt
 * 6. For USER role: Load MRFC access list (query user_mrfc_access table)
 * 7. Generate JWT token with payload:
 *    {
 *      userId: user.id,
 *      username: user.username,
 *      role: user.role,
 *      email: user.email,
 *      mrfcAccess: [1, 2, 5] // Array of MRFC IDs (for USER role only)
 *    }
 * 8. Generate refresh token (same payload, longer expiry)
 * 9. Update user.last_login timestamp
 * 10. Return user data + tokens
 *
 * ERROR CASES:
 * - 400: Validation error
 * - 401: Invalid credentials (username not found or password wrong)
 * - 403: Account deactivated (is_active = false)
 * - 500: Database error
 *
 * SECURITY NOTES:
 * - Always use same error message for "user not found" and "wrong password"
 *   This prevents username enumeration attacks
 * - Never reveal which part failed (username or password)
 * - Rate limit this endpoint (implement with express-rate-limit)
 */
router.post('/login', validate(loginSchema, 'body'), authController.login);

/**
 * ================================================
 * POST /auth/refresh
 * ================================================
 * Refresh JWT access token using refresh token
 * Used when access token expires but refresh token is still valid
 *
 * REQUEST BODY:
 * {
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "data": {
 *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *     "expiresIn": "24h"
 *   }
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Extract refresh token from request body
 * 2. Verify refresh token (will throw if invalid/expired)
 * 3. Extract user data from token payload
 * 4. Verify user still exists and is active
 * 5. Generate new access token with same payload
 * 6. Return new token
 *
 * ERROR CASES:
 * - 400: No refresh token provided
 * - 401: Invalid or expired refresh token
 * - 403: User account deactivated
 * - 500: Server error
 */
router.post('/refresh', authController.refresh);

/**
 * ================================================
 * POST /auth/logout
 * ================================================
 * Logout user (invalidate tokens)
 * NOTE: JWTs are stateless, so true invalidation requires:
 * - Token blacklist (store invalidated tokens in database/Redis)
 * - OR short expiry times + refresh token rotation
 * For MVP, client-side token deletion is sufficient
 *
 * HEADERS:
 * Authorization: Bearer <token>
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Logged out successfully"
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Extract token from Authorization header
 * 2. (Optional) Add token to blacklist table
 * 3. Return success message
 * 4. Client must delete token from storage
 */
router.post('/logout', authenticate, authController.logout);

/**
 * ================================================
 * POST /auth/change-password
 * ================================================
 * Change user password (authenticated users only)
 *
 * HEADERS:
 * Authorization: Bearer <token>
 *
 * REQUEST BODY:
 * {
 *   "currentPassword": "oldPassword123",
 *   "newPassword": "newSecurePassword456!"
 * }
 *
 * RESPONSE (200):
 * {
 *   "success": true,
 *   "message": "Password changed successfully"
 * }
 *
 * IMPLEMENTATION STEPS:
 * 1. Get user ID from req.user (set by authenticate middleware)
 * 2. Find user in database
 * 3. Verify current password
 * 4. Validate new password strength
 * 5. Hash new password
 * 6. Update user password_hash in database
 * 7. (Optional) Invalidate all existing tokens (force re-login)
 * 8. Return success message
 *
 * ERROR CASES:
 * - 401: Not authenticated
 * - 400: Validation error (weak password)
 * - 403: Current password is incorrect
 * - 500: Database error
 */
router.post('/change-password', authenticate, validate(changePasswordSchema, 'body'), authController.changePassword);

export default router;
