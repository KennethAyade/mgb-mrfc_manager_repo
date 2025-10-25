/**
 * AUTHENTICATION MIDDLEWARE
 * =========================
 * Middleware to protect routes and verify JWT tokens
 * Also handles role-based authorization
 */

import { Request, Response, NextFunction } from 'express';
import { verifyToken, extractTokenFromHeader, JwtPayload } from '../utils/jwt';

/**
 * Extend Express Request type to include user data
 * This allows us to access req.user in protected routes
 */
declare global {
  namespace Express {
    interface Request {
      user?: JwtPayload;
    }
  }
}

/**
 * Authentication middleware
 * Verifies JWT token and attaches user data to request
 *
 * Usage in routes:
 * router.get('/protected', authenticate, (req, res) => {
 *   console.log(req.user); // User data from token
 * });
 *
 * @param req Express request
 * @param res Express response
 * @param next Express next function
 */
export const authenticate = async (
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    // Extract token from Authorization header
    const token = extractTokenFromHeader(req.headers.authorization);

    if (!token) {
      res.status(401).json({
        success: false,
        error: {
          code: 'NO_TOKEN',
          message: 'No authentication token provided'
        }
      });
      return;
    }

    // Verify and decode token
    const decoded = verifyToken(token);

    // Attach user data to request
    req.user = decoded;

    next();
  } catch (error: any) {
    res.status(401).json({
      success: false,
      error: {
        code: 'INVALID_TOKEN',
        message: error.message || 'Invalid or expired token'
      }
    });
  }
};

/**
 * Role-based authorization middleware
 * Checks if authenticated user has required role
 *
 * Usage:
 * router.delete('/users/:id', authenticate, authorize(['SUPER_ADMIN', 'ADMIN']), deleteUser);
 *
 * @param allowedRoles Array of roles that can access the route
 * @returns Middleware function
 *
 * Role hierarchy:
 * - SUPER_ADMIN: Can create other admins, full system access
 * - ADMIN: Full CRUD access to all resources
 * - USER: Read-only access to assigned MRFCs
 */
export const authorize = (allowedRoles: Array<'SUPER_ADMIN' | 'ADMIN' | 'USER'>) => {
  return (req: Request, res: Response, next: NextFunction): void => {
    if (!req.user) {
      res.status(401).json({
        success: false,
        error: {
          code: 'UNAUTHENTICATED',
          message: 'Authentication required'
        }
      });
      return;
    }

    if (!allowedRoles.includes(req.user.role)) {
      res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Insufficient permissions to access this resource'
        }
      });
      return;
    }

    next();
  };
};

/**
 * Admin-only middleware
 * Shortcut for routes that require ADMIN or SUPER_ADMIN role
 *
 * Usage:
 * router.post('/mrfcs', authenticate, adminOnly, createMrfc);
 */
export const adminOnly = authorize(['SUPER_ADMIN', 'ADMIN']);

/**
 * Super admin-only middleware
 * For critical operations like creating other admins
 *
 * Usage:
 * router.post('/users/admin', authenticate, superAdminOnly, createAdmin);
 */
export const superAdminOnly = authorize(['SUPER_ADMIN']);

/**
 * MRFC Access Control Middleware
 * Verifies that a USER has access to specific MRFC
 * ADMIN and SUPER_ADMIN have access to all MRFCs
 *
 * Usage:
 * router.get('/mrfcs/:mrfcId/details', authenticate, checkMrfcAccess, getMrfcDetails);
 *
 * How it works:
 * 1. If user is ADMIN or SUPER_ADMIN: Allow (they have access to all MRFCs)
 * 2. If user is USER: Check if req.params.mrfcId is in their mrfcAccess array
 * 3. If not in array: Deny with 403 Forbidden
 *
 * @param req Express request
 * @param res Express response
 * @param next Express next function
 */
export const checkMrfcAccess = (
  req: Request,
  res: Response,
  next: NextFunction
): void => {
  if (!req.user) {
    res.status(401).json({
      success: false,
      error: {
        code: 'UNAUTHENTICATED',
        message: 'Authentication required'
      }
    });
    return;
  }

  // Admins have access to all MRFCs
  if (req.user.role === 'ADMIN' || req.user.role === 'SUPER_ADMIN') {
    next();
    return;
  }

  // For regular users, check MRFC access
  const mrfcId = parseInt(req.params.mrfcId);
  const userMrfcAccess = req.user.mrfcAccess || [];

  if (!userMrfcAccess.includes(mrfcId)) {
    res.status(403).json({
      success: false,
      error: {
        code: 'MRFC_ACCESS_DENIED',
        message: 'You do not have access to this MRFC'
      }
    });
    return;
  }

  next();
};

/**
 * Optional authentication middleware
 * Attempts to authenticate user, but doesn't fail if no token provided
 * Useful for routes that behave differently for authenticated vs anonymous users
 *
 * Usage:
 * router.get('/public-data', optionalAuth, getPublicData);
 * // req.user will be undefined if not authenticated
 *
 * @param req Express request
 * @param res Express response
 * @param next Express next function
 */
export const optionalAuth = async (
  req: Request,
  _res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const token = extractTokenFromHeader(req.headers.authorization);

    if (token) {
      const decoded = verifyToken(token);
      req.user = decoded;
    }

    next();
  } catch (error) {
    // Silently fail - user is not authenticated but route continues
    next();
  }
};
