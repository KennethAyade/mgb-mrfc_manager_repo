/**
 * ================================================
 * USER MANAGEMENT CONTROLLER
 * ================================================
 * Handles user CRUD operations and status management
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { User, UserMrfcAccess, Mrfc, AuditLog } from '../models';
import { hashPassword } from '../utils/password';
import sequelize from '../config/database';

/**
 * List all users with pagination and filtering
 * GET /api/v1/users
 */
export const listUsers = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      page = '1',
      limit = '20',
      search,
      role,
      is_active,
      sort_by = 'created_at',
      sort_order = 'DESC'
    } = req.query;

    // Validate and parse parameters
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    // Build filter conditions
    const where: any = {};
    if (role) where.role = role;
    if (is_active !== undefined) where.is_active = is_active === 'true';

    // Search filter
    if (search) {
      where[Op.or] = [
        { username: { [Op.like]: `%${search}%` } },
        { full_name: { [Op.like]: `%${search}%` } },
        { email: { [Op.like]: `%${search}%` } }
      ];
    }

    // Query users (exclude password_hash)
    const { count, rows: users } = await User.findAndCountAll({
      where,
      attributes: { exclude: ['password_hash'] },
      limit: limitNum,
      offset,
      order: [[sort_by as string, sort_order as string]]
    });

    res.json({
      success: true,
      data: {
        users,
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
    console.error('User listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'USER_LISTING_FAILED',
        message: error.message || 'Failed to retrieve users'
      }
    });
  }
};

/**
 * Create new user account
 * POST /api/v1/users
 */
export const createUser = async (req: Request, res: Response): Promise<void> => {
  try {
    const { username, password, full_name, email, role, is_active, mrfc_access } = req.body;
    const currentUser = (req as any).user;

    // Authorization check: Only SUPER_ADMIN can create ADMIN accounts
    if (role === 'ADMIN' && currentUser?.role !== 'SUPER_ADMIN') {
      res.status(403).json({
        success: false,
        error: {
          code: 'INSUFFICIENT_PERMISSIONS',
          message: 'Only SUPER_ADMIN can create ADMIN accounts'
        }
      });
      return;
    }

    // Check if username exists
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

    // Check if email exists
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

    // Create user with transaction
    const user = await sequelize.transaction(async (t) => {
      const newUser = await User.create(
        {
          username,
          password_hash,
          full_name,
          email,
          role: role || 'USER',
          is_active: is_active !== false
        },
        { transaction: t }
      );

      // If USER role and mrfc_access provided, create access records
      if (newUser.role === 'USER' && mrfc_access?.length > 0) {
        const accessRecords = mrfc_access.map((mrfcId: number) => ({
          user_id: newUser.id,
          mrfc_id: mrfcId,
          granted_by: currentUser?.userId,
          is_active: true
        }));
        await UserMrfcAccess.bulkCreate(accessRecords, { transaction: t });
      }

      return newUser;
    });

    // Return user data (no password)
    res.status(201).json({
      success: true,
      message: 'User created successfully',
      data: {
        id: user.id,
        username: user.username,
        full_name: user.full_name,
        email: user.email,
        role: user.role,
        is_active: user.is_active,
        created_at: user.created_at
      }
    });
  } catch (error: any) {
    console.error('User creation error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'USER_CREATION_FAILED',
        message: error.message || 'Failed to create user'
      }
    });
  }
};

/**
 * Get user by ID
 * GET /api/v1/users/:id
 */
export const getUserById = async (req: Request, res: Response): Promise<void> => {
  try {
    const userId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Validate ID
    if (isNaN(userId)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_ID',
          message: 'Invalid user ID'
        }
      });
      return;
    }

    // Authorization check
    const isAdmin = currentUser?.role === 'ADMIN' || currentUser?.role === 'SUPER_ADMIN';
    const isOwnProfile = currentUser?.userId === userId;

    if (!isAdmin && !isOwnProfile) {
      res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'You can only view your own profile'
        }
      });
      return;
    }

    // Find user with MRFC access
    const user = await User.findByPk(userId, {
      attributes: { exclude: ['password_hash'] },
      include: [
        {
          model: UserMrfcAccess,
          as: 'mrfc_access',
          where: { is_active: true },
          required: false,
          include: [
            {
              model: Mrfc,
              as: 'mrfc',
              attributes: ['id', 'mrfc_number', 'project_title']
            }
          ]
        }
      ]
    });

    // Check if user exists
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

    res.json({
      success: true,
      data: user
    });
  } catch (error: any) {
    console.error('Get user error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_USER_FAILED',
        message: error.message || 'Failed to retrieve user'
      }
    });
  }
};

/**
 * Update user information
 * PUT /api/v1/users/:id
 */
export const updateUser = async (req: Request, res: Response): Promise<void> => {
  try {
    const userId = parseInt(req.params.id);
    const currentUser = (req as any).user;
    const { full_name, email, role, is_active, mrfc_access } = req.body;

    // Validate ID
    if (isNaN(userId)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_ID',
          message: 'Invalid user ID'
        }
      });
      return;
    }

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

    // Authorization check
    const isAdmin = currentUser?.role === 'ADMIN' || currentUser?.role === 'SUPER_ADMIN';
    const isOwnProfile = currentUser?.userId === userId;

    // USER can only update their own profile and limited fields
    if (!isAdmin && !isOwnProfile) {
      res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'You can only update your own profile'
        }
      });
      return;
    }

    // Build update object
    const updates: any = {};

    if (isAdmin) {
      // ADMIN can update all fields
      if (full_name) updates.full_name = full_name;
      if (email) updates.email = email;
      if (role) updates.role = role;
      if (is_active !== undefined) updates.is_active = is_active;
    } else {
      // USER can only update full_name and email
      if (full_name) updates.full_name = full_name;
      if (email) updates.email = email;
    }

    // Check if new email already exists
    if (email && email !== user.email) {
      const existingEmail = await User.findOne({ where: { email, id: { [Op.ne]: userId } } });
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
    }

    // Update user with transaction
    await sequelize.transaction(async (t) => {
      await user.update(updates, { transaction: t });

      // Update MRFC access if provided (ADMIN only)
      if (isAdmin && mrfc_access && user.role === 'USER') {
        // Remove existing access
        await UserMrfcAccess.destroy({
          where: { user_id: userId },
          transaction: t
        });

        // Create new access records
        if (mrfc_access.length > 0) {
          const accessRecords = mrfc_access.map((mrfcId: number) => ({
            user_id: userId,
            mrfc_id: mrfcId,
            granted_by: currentUser?.userId,
            is_active: true
          }));
          await UserMrfcAccess.bulkCreate(accessRecords, { transaction: t });
        }
      }
    });

    // Return updated user (exclude password)
    const updatedUser = await User.findByPk(userId, {
      attributes: { exclude: ['password_hash'] }
    });

    res.json({
      success: true,
      message: 'User updated successfully',
      data: updatedUser
    });
  } catch (error: any) {
    console.error('User update error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'USER_UPDATE_FAILED',
        message: error.message || 'Failed to update user'
      }
    });
  }
};

/**
 * Delete user (soft delete)
 * DELETE /api/v1/users/:id
 */
export const deleteUser = async (req: Request, res: Response): Promise<void> => {
  try {
    const userId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Prevent self-deletion
    if (currentUser?.userId === userId) {
      res.status(403).json({
        success: false,
        error: {
          code: 'CANNOT_DELETE_SELF',
          message: 'Cannot delete your own account'
        }
      });
      return;
    }

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

    // Soft delete with transaction
    await sequelize.transaction(async (t) => {
      await user.update(
        {
          is_active: false,
          deleted_at: new Date()
        },
        { transaction: t }
      );

      // Deactivate MRFC access
      await UserMrfcAccess.update(
        { is_active: false },
        { where: { user_id: userId }, transaction: t }
      );
    });

    res.json({
      success: true,
      message: 'User deleted successfully'
    });
  } catch (error: any) {
    console.error('User deletion error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'USER_DELETION_FAILED',
        message: error.message || 'Failed to delete user'
      }
    });
  }
};

/**
 * Toggle user account status (activate/deactivate)
 * PUT /api/v1/users/:id/toggle-status
 */
export const toggleUserStatus = async (req: Request, res: Response): Promise<void> => {
  try {
    const userId = parseInt(req.params.id);
    const currentUser = (req as any).user;
    const { is_active } = req.body;

    // Prevent self-status toggle
    if (currentUser?.userId === userId) {
      res.status(403).json({
        success: false,
        error: {
          code: 'CANNOT_TOGGLE_SELF',
          message: 'Cannot change your own account status'
        }
      });
      return;
    }

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

    // Update status
    await user.update({ is_active });

    const action = is_active ? 'activated' : 'deactivated';
    res.json({
      success: true,
      message: `User account ${action} successfully`,
      data: {
        id: user.id,
        username: user.username,
        is_active: user.is_active
      }
    });
  } catch (error: any) {
    console.error('Toggle user status error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'TOGGLE_STATUS_FAILED',
        message: error.message || 'Failed to toggle user status'
      }
    });
  }
};
