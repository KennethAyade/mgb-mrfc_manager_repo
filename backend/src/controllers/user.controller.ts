/**
 * ================================================
 * USER MANAGEMENT CONTROLLER
 * ================================================
 * Handles user CRUD operations, status management, and MRFC access
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { User, UserMrfcAccess, Mrfc, AuditLog } from '../models';
import { UserRole } from '../models/User';
import { AuditAction } from '../models/AuditLog';
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
      search = '',
      role = '',
      is_active = '',
      sort_by = 'created_at',
      sort_order = 'DESC'
    } = req.query;

    const pageNum = parseInt(page as string);
    const limitNum = Math.min(parseInt(limit as string), 100);
    const offset = (pageNum - 1) * limitNum;

    // Build WHERE clause
    const where: any = {};

    if (search) {
      where[Op.or] = [
        { username: { [Op.iLike]: `%${search}%` } },
        { full_name: { [Op.iLike]: `%${search}%` } },
        { email: { [Op.iLike]: `%${search}%` } }
      ];
    }

    if (role) {
      where.role = role;
    }

    if (is_active !== '') {
      where.is_active = is_active === 'true';
    }

    const { count, rows } = await User.findAndCountAll({
      where,
      limit: limitNum,
      offset,
      order: [[sort_by as string, sort_order as string]],
      attributes: { exclude: ['password_hash'] }
    });

    res.json({
      success: true,
      data: {
        users: rows,
        pagination: {
          page: pageNum,
          limit: limitNum,
          total: count,
          totalPages: Math.ceil(count / limitNum),
          hasNext: pageNum < Math.ceil(count / limitNum),
          hasPrev: pageNum > 1
        }
      }
    });
  } catch (error) {
    console.error('Error listing users:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to list users'
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
    const { id } = req.params;

    const user = await User.findByPk(id, {
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
              attributes: ['id', 'name', 'municipality']
            }
          ]
        }
      ]
    });

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
  } catch (error) {
    console.error('Error fetching user:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to fetch user'
      }
    });
  }
};

/**
 * Create new user
 * POST /api/v1/users
 */
export const createUser = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;
    const {
      username,
      password,
      full_name,
      email,
      role
    } = req.body;

    // Check permissions - only SUPER_ADMIN can create ADMIN users
    if (role === UserRole.ADMIN && currentUser.role !== UserRole.SUPER_ADMIN) {
      res.status(403).json({
        success: false,
        error: {
          code: 'INSUFFICIENT_PERMISSIONS',
          message: 'Only SUPER_ADMIN can create ADMIN users'
        }
      });
      return;
    }

    // Cannot create SUPER_ADMIN users
    if (role === UserRole.SUPER_ADMIN) {
      res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Cannot create SUPER_ADMIN users via API'
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
          role: role || UserRole.USER,
          is_active: true,
          email_verified: true
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser.userId,
          action: AuditAction.CREATE,
          entity_type: 'users',
          entity_id: newUser.id,
          new_values: {
            username: newUser.username,
            full_name: newUser.full_name,
            email: newUser.email,
            role: newUser.role
          }
        },
        { transaction: t }
      );

      return newUser;
    });

    // Return user without password
    const { password_hash: _, ...userData } = user.toJSON();

    res.status(201).json({
      success: true,
      message: 'User created successfully',
      data: userData
    });
  } catch (error) {
    console.error('Error creating user:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to create user'
      }
    });
  }
};

/**
 * Update user
 * PUT /api/v1/users/:id
 */
export const updateUser = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;
    const { id } = req.params;
    const updateData = req.body;

    // Remove fields that shouldn't be updated directly
    delete updateData.password_hash;
    delete updateData.id;
    delete updateData.created_at;

    const user = await User.findByPk(id);
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

    // Check permissions for role changes
    if (updateData.role) {
      // Only SUPER_ADMIN can change roles
      if (currentUser.role !== UserRole.SUPER_ADMIN) {
        res.status(403).json({
          success: false,
          error: {
            code: 'INSUFFICIENT_PERMISSIONS',
            message: 'Only SUPER_ADMIN can change user roles'
          }
        });
        return;
      }

      // Cannot change SUPER_ADMIN role
      if (user.role === UserRole.SUPER_ADMIN || updateData.role === UserRole.SUPER_ADMIN) {
        res.status(403).json({
          success: false,
          error: {
            code: 'FORBIDDEN',
            message: 'Cannot modify SUPER_ADMIN role'
          }
        });
        return;
      }
    }

    await sequelize.transaction(async (t) => {
      const { password_hash: _, ...oldValues } = user.toJSON();

      await user.update(updateData, { transaction: t });

      const { password_hash: __, ...newValues } = user.toJSON();

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser.userId,
          action: AuditAction.UPDATE,
          entity_type: 'users',
          entity_id: user.id,
          old_values: oldValues,
          new_values: newValues
        },
        { transaction: t }
      );
    });

    const { password_hash: _, ...userData } = user.toJSON();

    res.json({
      success: true,
      message: 'User updated successfully',
      data: userData
    });
  } catch (error) {
    console.error('Error updating user:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to update user'
      }
    });
  }
};

/**
 * Soft delete user
 * DELETE /api/v1/users/:id
 */
export const deleteUser = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;
    const { id } = req.params;

    const user = await User.findByPk(id);
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

    // Cannot delete SUPER_ADMIN
    if (user.role === UserRole.SUPER_ADMIN) {
      res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Cannot delete SUPER_ADMIN user'
        }
      });
      return;
    }

    // Cannot delete self
    if (user.id.toString() === currentUser.userId.toString()) {
      res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: 'Cannot delete your own account'
        }
      });
      return;
    }

    await sequelize.transaction(async (t) => {
      const { password_hash: _, ...oldValues } = user.toJSON();

      // Soft delete
      await user.update(
        { is_active: false },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser.userId,
          action: AuditAction.DELETE,
          entity_type: 'users',
          entity_id: user.id,
          old_values: oldValues
        },
        { transaction: t }
      );
    });

    res.json({
      success: true,
      message: 'User deleted successfully'
    });
  } catch (error) {
    console.error('Error deleting user:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to delete user'
      }
    });
  }
};

/**
 * Toggle user active status
 * PUT /api/v1/users/:id/toggle-status
 */
export const toggleUserStatus = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;
    const { id } = req.params;

    const user = await User.findByPk(id);
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

    // Prevent deactivating own account
    if (user.id === currentUser?.userId) {
      res.status(400).json({
        success: false,
        error: {
          code: 'CANNOT_DEACTIVATE_SELF',
          message: 'Cannot deactivate your own account'
        }
      });
      return;
    }

    await sequelize.transaction(async (t) => {
      const oldValues = { is_active: user.is_active };
      const newStatus = !user.is_active;

      await user.update(
        { is_active: newStatus },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: AuditAction.UPDATE,
          entity_type: 'users',
          entity_id: user.id,
          old_values: oldValues,
          new_values: { is_active: newStatus }
        },
        { transaction: t }
      );
    });

    res.json({
      success: true,
      message: `User ${user.is_active ? 'activated' : 'deactivated'} successfully`,
      data: {
        id: Number(user.id),
        username: user.username,
        is_active: user.is_active
      }
    });
  } catch (error) {
    console.error('Error toggling user status:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to toggle user status'
      }
    });
  }
};

/**
 * Grant MRFC access to user
 * POST /api/v1/users/:id/grant-mrfc-access
 */
export const grantMrfcAccess = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;
    const { id } = req.params;
    const { mrfc_ids } = req.body;

    if (!Array.isArray(mrfc_ids)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_INPUT',
          message: 'mrfc_ids must be an array'
        }
      });
      return;
    }

    const user = await User.findByPk(id);
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

    // If mrfc_ids is not empty, verify all MRFCs exist
    if (mrfc_ids.length > 0) {
      const mrfcs = await Mrfc.findAll({
        where: {
          id: { [Op.in]: mrfc_ids },
          is_active: true
        }
      });

      if (mrfcs.length !== mrfc_ids.length) {
        res.status(404).json({
          success: false,
          error: {
            code: 'MRFC_NOT_FOUND',
            message: 'One or more MRFCs not found or inactive'
          }
        });
        return;
      }
    }

    await sequelize.transaction(async (t) => {
      // First, delete all existing access records for this user (force hard delete)
      const deletedCount = await UserMrfcAccess.destroy({
        where: { user_id: user.id },
        transaction: t,
        force: true // Ensure hard delete even if paranoid mode is enabled
      });
      console.log(`Deleted ${deletedCount} existing MRFC access records for user ${user.id}`);

      // Create new access records (if any)
      if (mrfc_ids.length > 0) {
        const accessRecords = mrfc_ids.map((mrfc_id: number) => ({
          user_id: user.id,
          mrfc_id,
          granted_by: currentUser.userId,
          is_active: true
        }));

        const created = await UserMrfcAccess.bulkCreate(accessRecords, {
          transaction: t,
          ignoreDuplicates: true // Ignore if duplicate constraint exists
        });
        console.log(`Created ${created.length} new MRFC access records for user ${user.id}`);
      }

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser.userId,
          action: AuditAction.CREATE,
          entity_type: 'user_mrfc_access',
          entity_id: user.id,
          new_values: {
            user_id: user.id,
            mrfc_ids,
            granted_by: currentUser.userId
          }
        },
        { transaction: t }
      );
    });

    // Fetch updated user with MRFC access
    const updatedUser = await User.findByPk(id, {
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
              attributes: ['id', 'name', 'municipality']
            }
          ]
        }
      ]
    });

    res.json({
      success: true,
      message: 'MRFC access granted successfully',
      data: updatedUser
    });
  } catch (error) {
    console.error('Error granting MRFC access:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to grant MRFC access'
      }
    });
  }
};
