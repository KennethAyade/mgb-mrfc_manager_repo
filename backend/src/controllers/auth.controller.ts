/**
 * ================================================
 * AUTHENTICATION CONTROLLER
 * ================================================
 * Handles user registration, login, token refresh, and password management
 */

import { Request, Response } from 'express';
import { User, UserMrfcAccess } from '../models';
import { hashPassword, verifyPassword } from '../utils/password';
import { generateToken, generateRefreshToken, verifyRefreshToken } from '../utils/jwt';

/**
 * Register new user account
 * POST /api/v1/auth/register
 */
export const register = async (req: Request, res: Response): Promise<void> => {
  try {
    const { username, password, full_name, email } = req.body;

    // Check if username already exists
    const existingUser = await User.findOne({ where: { username } });
    if (existingUser) {
      res.status(409).json({
        success: false,
        error: {
          code: 'USERNAME_EXISTS',
          message: 'Username already taken'
        }
      });
      return;
    }

    // Check if email already exists
    const existingEmail = await User.findOne({ where: { email } });
    if (existingEmail) {
      res.status(409).json({
        success: false,
        error: {
          code: 'EMAIL_EXISTS',
          message: 'Email already registered'
        }
      });
      return;
    }

    // Hash password
    const password_hash = await hashPassword(password);

    // Create user (always USER role for self-registration)
    const user = await User.create({
      username,
      password_hash,
      full_name,
      email,
      role: 'USER',
      is_active: true,
      email_verified: false
    });

    // Return user data (no password)
    res.status(201).json({
      success: true,
      message: 'Registration successful. Please wait for admin approval.',
      data: {
        id: user.id,
        username: user.username,
        full_name: user.full_name,
        email: user.email,
        role: user.role,
        is_active: user.is_active,
        email_verified: user.email_verified
      }
    });
  } catch (error: any) {
    console.error('Registration error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'REGISTRATION_FAILED',
        message: error.message || 'Failed to register user'
      }
    });
  }
};

/**
 * User login
 * POST /api/v1/auth/login
 */
export const login = async (req: Request, res: Response): Promise<void> => {
  try {
    const { username, password } = req.body;

    // Find user by username
    const user = await User.findOne({ where: { username } });

    // Check if user exists
    if (!user) {
      res.status(401).json({
        success: false,
        error: {
          code: 'INVALID_CREDENTIALS',
          message: 'Invalid username or password'
        }
      });
      return;
    }

    // Check if account is active
    if (!user.is_active) {
      res.status(403).json({
        success: false,
        error: {
          code: 'ACCOUNT_DEACTIVATED',
          message: 'Your account has been deactivated'
        }
      });
      return;
    }

    // Verify password
    const isPasswordValid = await verifyPassword(password, user.password_hash);
    if (!isPasswordValid) {
      res.status(401).json({
        success: false,
        error: {
          code: 'INVALID_CREDENTIALS',
          message: 'Invalid username or password'
        }
      });
      return;
    }

    // Load MRFC access for USER role
    let mrfcAccess: number[] = [];
    if (user.role === 'USER') {
      const access = await UserMrfcAccess.findAll({
        where: { user_id: user.id, is_active: true },
        attributes: ['mrfc_id']
      });
      mrfcAccess = access.map(a => a.mrfc_id);
    }

    // Generate JWT tokens
    const payload = {
      userId: user.id,
      username: user.username,
      role: user.role as 'SUPER_ADMIN' | 'ADMIN' | 'USER',
      email: user.email,
      mrfcAccess: user.role === 'USER' ? mrfcAccess : undefined
    };
    const token = generateToken(payload);
    const refreshToken = generateRefreshToken(payload);

    // Update last login timestamp
    await user.update({ last_login: new Date() });

    // Return user data and tokens
    res.json({
      success: true,
      data: {
        user: {
          id: user.id,
          username: user.username,
          full_name: user.full_name,
          email: user.email,
          role: user.role
        },
        token,
        refreshToken,
        expiresIn: '24h'
      }
    });
  } catch (error: any) {
    console.error('Login error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'LOGIN_FAILED',
        message: 'Login failed. Please try again.'
      }
    });
  }
};

/**
 * Refresh access token
 * POST /api/v1/auth/refresh
 */
export const refresh = async (req: Request, res: Response): Promise<void> => {
  try {
    const { refreshToken } = req.body;

    // Validate refresh token provided
    if (!refreshToken) {
      res.status(400).json({
        success: false,
        error: {
          code: 'TOKEN_REQUIRED',
          message: 'Refresh token is required'
        }
      });
      return;
    }

    // Verify refresh token
    let payload;
    try {
      payload = verifyRefreshToken(refreshToken);
    } catch (error: any) {
      res.status(401).json({
        success: false,
        error: {
          code: 'INVALID_TOKEN',
          message: error.message || 'Invalid or expired refresh token'
        }
      });
      return;
    }

    // Verify user still exists and is active
    const user = await User.findByPk(payload.userId);
    if (!user) {
      res.status(401).json({
        success: false,
        error: {
          code: 'USER_NOT_FOUND',
          message: 'User not found'
        }
      });
      return;
    }

    if (!user.is_active) {
      res.status(403).json({
        success: false,
        error: {
          code: 'ACCOUNT_DEACTIVATED',
          message: 'Your account has been deactivated'
        }
      });
      return;
    }

    // Generate new access token with same payload
    const newToken = generateToken(payload);

    res.json({
      success: true,
      data: {
        token: newToken,
        expiresIn: '24h'
      }
    });
  } catch (error: any) {
    console.error('Token refresh error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'REFRESH_FAILED',
        message: error.message || 'Failed to refresh token'
      }
    });
  }
};

/**
 * Logout user
 * POST /api/v1/auth/logout
 */
export const logout = async (req: Request, res: Response): Promise<void> => {
  try {
    // For MVP: Client-side token deletion is sufficient
    // For production: Implement token blacklist using Redis or database

    res.json({
      success: true,
      message: 'Logged out successfully'
    });
  } catch (error: any) {
    console.error('Logout error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'LOGOUT_FAILED',
        message: 'Failed to logout'
      }
    });
  }
};

/**
 * Change user password
 * POST /api/v1/auth/change-password
 */
export const changePassword = async (req: Request, res: Response): Promise<void> => {
  try {
    const { currentPassword, newPassword } = req.body;
    const userId = (req as any).user?.userId;

    // Find user
    const user = await User.findByPk(userId);
    if (!user) {
      res.status(404).json({
        success: false,
        error: {
          code: 'USER_NOT_FOUND',
          message: 'User not found'
        }
      });
      return;
    }

    // Verify current password
    const isPasswordValid = await verifyPassword(currentPassword, user.password_hash);
    if (!isPasswordValid) {
      res.status(403).json({
        success: false,
        error: {
          code: 'INVALID_PASSWORD',
          message: 'Current password is incorrect'
        }
      });
      return;
    }

    // Hash new password
    const newPasswordHash = await hashPassword(newPassword);

    // Update password
    await user.update({ password_hash: newPasswordHash });

    res.json({
      success: true,
      message: 'Password changed successfully'
    });
  } catch (error: any) {
    console.error('Change password error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'PASSWORD_CHANGE_FAILED',
        message: error.message || 'Failed to change password'
      }
    });
  }
};
