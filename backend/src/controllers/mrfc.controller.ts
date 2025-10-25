/**
 * ================================================
 * MRFC MANAGEMENT CONTROLLER
 * ================================================
 * Handles MRFC (Municipal Resource and Finance Committee) operations
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { Mrfc, Proponent, UserMrfcAccess, User, AuditLog } from '../models';
import { AuditAction } from '../models/AuditLog';
import sequelize from '../config/database';

/**
 * List all MRFCs with pagination and filtering
 * GET /api/v1/mrfcs
 */
export const listMrfcs = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      page = '1',
      limit = '20',
      search = '',
      municipality = '',
      province = '',
      is_active = ''
    } = req.query;

    const pageNum = parseInt(page as string);
    const limitNum = parseInt(limit as string);
    const offset = (pageNum - 1) * limitNum;

    // Build where clause
    const where: any = {};

    if (search) {
      where[Op.or] = [
        { name: { [Op.iLike]: `%${search}%` } },
        { municipality: { [Op.iLike]: `%${search}%` } }
      ];
    }

    if (municipality) {
      where.municipality = { [Op.iLike]: `%${municipality}%` };
    }

    if (province) {
      where.province = { [Op.iLike]: `%${province}%` };
    }

    if (is_active !== '') {
      where.is_active = is_active === 'true';
    }

    const { count, rows } = await Mrfc.findAndCountAll({
      where,
      limit: limitNum,
      offset,
      order: [['created_at', 'DESC']],
      include: [
        {
          model: User,
          as: 'creator',
          attributes: ['id', 'username', 'full_name']
        }
      ]
    });

    res.json({
      success: true,
      data: {
        mrfcs: rows,
        pagination: {
          current_page: pageNum,
          total_pages: Math.ceil(count / limitNum),
          total_items: count,
          items_per_page: limitNum
        }
      }
    });
  } catch (error) {
    console.error('Error listing MRFCs:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to list MRFCs'
      }
    });
  }
};

/**
 * Get single MRFC by ID with full details
 * GET /api/v1/mrfcs/:id
 */
export const getMrfcById = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;

    const mrfc = await Mrfc.findByPk(id, {
      include: [
        {
          model: User,
          as: 'creator',
          attributes: ['id', 'username', 'full_name']
        },
        {
          model: Proponent,
          as: 'proponents'
        },
        {
          model: UserMrfcAccess,
          as: 'user_access',
          include: [
            {
              model: User,
              as: 'user',
              attributes: ['id', 'username', 'full_name', 'email']
            }
          ]
        }
      ]
    });

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

    res.json({
      success: true,
      data: mrfc
    });
  } catch (error) {
    console.error('Error fetching MRFC:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to fetch MRFC'
      }
    });
  }
};

/**
 * Create new MRFC
 * POST /api/v1/mrfcs
 */
export const createMrfc = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;
    const {
      name,
      municipality,
      province,
      region,
      contact_person,
      contact_number,
      email,
      address,
      assigned_user_ids
    } = req.body;

    // Check if MRFC with same name and municipality exists
    const existing = await Mrfc.findOne({
      where: {
        name,
        municipality
      }
    });

    if (existing) {
      res.status(409).json({
        success: false,
        error: {
          code: 'MRFC_EXISTS',
          message: 'MRFC with this name and municipality already exists'
        }
      });
      return;
    }

    // Create MRFC with transaction
    const mrfc = await sequelize.transaction(async (t) => {
      const newMrfc = await Mrfc.create(
        {
          name,
          municipality,
          province,
          region,
          contact_person,
          contact_number,
          email,
          address,
          is_active: true,
          created_by: currentUser?.userId
        },
        { transaction: t }
      );

      // Create user access records if provided
      if (assigned_user_ids?.length > 0) {
        const accessRecords = assigned_user_ids.map((userId: number) => ({
          user_id: userId,
          mrfc_id: newMrfc.id,
          granted_by: currentUser?.userId,
          is_active: true
        }));
        await UserMrfcAccess.bulkCreate(accessRecords, { transaction: t });
      }

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: AuditAction.CREATE,
          entity_type: 'mrfcs',
          entity_id: newMrfc.id,
          new_values: newMrfc.toJSON()
        },
        { transaction: t }
      );

      return newMrfc;
    });

    res.status(201).json({
      success: true,
      message: 'MRFC created successfully',
      data: mrfc
    });
  } catch (error) {
    console.error('Error creating MRFC:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to create MRFC'
      }
    });
  }
};

/**
 * Update MRFC
 * PUT /api/v1/mrfcs/:id
 */
export const updateMrfc = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;
    const { id } = req.params;
    const updateData = req.body;

    const mrfc = await Mrfc.findByPk(id);
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

    await sequelize.transaction(async (t) => {
      const oldValues = mrfc.toJSON();

      await mrfc.update(updateData, { transaction: t });

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: AuditAction.UPDATE,
          entity_type: 'mrfcs',
          entity_id: mrfc.id,
          old_values: oldValues,
          new_values: mrfc.toJSON()
        },
        { transaction: t }
      );
    });

    res.json({
      success: true,
      message: 'MRFC updated successfully',
      data: mrfc
    });
  } catch (error) {
    console.error('Error updating MRFC:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to update MRFC'
      }
    });
  }
};

/**
 * Soft delete MRFC (set is_active to false)
 * DELETE /api/v1/mrfcs/:id
 */
export const deleteMrfc = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;
    const { id } = req.params;

    const mrfc = await Mrfc.findByPk(id);
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

    await sequelize.transaction(async (t) => {
      const oldValues = mrfc.toJSON();

      await mrfc.update(
        { is_active: false },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: AuditAction.DELETE,
          entity_type: 'mrfcs',
          entity_id: mrfc.id,
          old_values: oldValues
        },
        { transaction: t }
      );
    });

    res.json({
      success: true,
      message: 'MRFC deleted successfully'
    });
  } catch (error) {
    console.error('Error deleting MRFC:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to delete MRFC'
      }
    });
  }
};
