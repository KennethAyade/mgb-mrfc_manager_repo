/**
 * ================================================
 * MRFC MANAGEMENT CONTROLLER
 * ================================================
 * Handles MRFC (Mineral Resources and Foreign Capital) operations
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { Mrfc, Proponent, UserMrfcAccess, User, Document, Agenda, Quarter, AuditLog } from '../models';
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
      search,
      status,
      proponent_id,
      date_from,
      date_to,
      sort_by = 'date_received',
      sort_order = 'DESC'
    } = req.query;

    const currentUser = (req as any).user;

    // Validate and parse parameters
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    // Build filter conditions
    const where: any = {};
    if (status) where.status = status;
    if (proponent_id) where.proponent_id = proponent_id;
    if (date_from) where.date_received = { [Op.gte]: date_from };
    if (date_to) where.date_received = { ...where.date_received, [Op.lte]: date_to };

    // Apply user MRFC access filter for USER role
    if (currentUser?.role === 'USER') {
      const userMrfcIds = currentUser.mrfcAccess || [];
      where.id = { [Op.in]: userMrfcIds };
    }

    // Search filter (including proponent name)
    if (search) {
      where[Op.or] = [
        { mrfc_number: { [Op.like]: `%${search}%` } },
        { project_title: { [Op.like]: `%${search}%` } }
      ];
    }

    // Query MRFCs with proponent and assigned users
    const { count, rows: mrfcs } = await Mrfc.findAndCountAll({
      where,
      include: [
        {
          model: Proponent,
          as: 'proponents',
          attributes: ['id', 'company_name', 'contact_person']
        },
        {
          model: UserMrfcAccess,
          as: 'user_access',
          where: { is_active: true },
          required: false,
          include: [
            {
              model: User,
              as: 'user',
              attributes: ['id', 'full_name']
            }
          ]
        }
      ],
      limit: limitNum,
      offset,
      order: [[sort_by as string, sort_order as string]],
      distinct: true
    });

    res.json({
      success: true,
      data: {
        mrfcs,
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
    console.error('MRFC listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'MRFC_LISTING_FAILED',
        message: error.message || 'Failed to retrieve MRFCs'
      }
    });
  }
};

/**
 * Create new MRFC record
 * POST /api/v1/mrfcs
 */
export const createMrfc = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      mrfc_number,
      project_title,
      proponent_id,
      date_received,
      location,
      project_cost,
      project_description,
      mineral_type,
      area_hectares,
      status,
      assigned_user_ids
    } = req.body;
    const currentUser = (req as any).user;

    // Validate MRFC number format
    const mrfcNumberRegex = /^MRFC-\d{4}-\d{3}$/;
    if (!mrfcNumberRegex.test(mrfc_number)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_MRFC_NUMBER',
          message: 'MRFC number must follow format: MRFC-YYYY-XXX'
        }
      });
      return;
    }

    // Check if MRFC number exists
    const existing = await Mrfc.findOne({ where: { mrfc_number } });
    if (existing) {
      res.status(409).json({
        success: false,
        error: {
          code: 'MRFC_EXISTS',
          message: 'MRFC number already exists'
        }
      });
      return;
    }

    // Verify proponent exists
    const proponent = await Proponent.findByPk(proponent_id);
    if (!proponent) {
      res.status(404).json({
        success: false,
        error: {
          code: 'PROPONENT_NOT_FOUND',
          message: 'Proponent not found'
        }
      });
      return;
    }

    // Create MRFC with transaction
    const mrfc = await sequelize.transaction(async (t) => {
      const newMrfc = await Mrfc.create(
        {
          mrfc_number,
          project_title,
          proponent_id,
          date_received: date_received || new Date(),
          location,
          project_cost,
          project_description,
          mineral_type,
          area_hectares,
          status: status || 'PENDING',
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
          action: 'CREATE_MRFC',
          entity_type: 'MRFC',
          entity_id: newMrfc.id,
          details: { mrfc_number: newMrfc.mrfc_number }
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
  } catch (error: any) {
    console.error('MRFC creation error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'MRFC_CREATION_FAILED',
        message: error.message || 'Failed to create MRFC'
      }
    });
  }
};

/**
 * Get MRFC by ID
 * GET /api/v1/mrfcs/:id
 */
export const getMrfcById = async (req: Request, res: Response): Promise<void> => {
  try {
    const mrfcId = parseInt(req.params.id);

    // Validate ID
    if (isNaN(mrfcId)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_ID',
          message: 'Invalid MRFC ID'
        }
      });
      return;
    }

    // Find MRFC with full details
    const mrfc = await Mrfc.findByPk(mrfcId, {
      include: [
        {
          model: Proponent,
          as: 'proponents'
        },
        {
          model: UserMrfcAccess,
          as: 'user_access',
          where: { is_active: true },
          required: false,
          include: [
            {
              model: User,
              as: 'user',
              attributes: ['id', 'full_name', 'email']
            }
          ]
        },
        {
          model: Document,
          as: 'documents',
          attributes: ['id', 'filename', 'file_type', 'uploaded_at'],
          limit: 10
        },
        {
          model: Agenda,
          as: 'agendas',
          include: [
            {
              model: Quarter,
              as: 'quarter'
            }
          ],
          limit: 10
        }
      ]
    });

    // Check if MRFC exists
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
  } catch (error: any) {
    console.error('Get MRFC error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_MRFC_FAILED',
        message: error.message || 'Failed to retrieve MRFC'
      }
    });
  }
};

/**
 * Update MRFC information
 * PUT /api/v1/mrfcs/:id
 */
export const updateMrfc = async (req: Request, res: Response): Promise<void> => {
  try {
    const mrfcId = parseInt(req.params.id);
    const currentUser = (req as any).user;
    const { assigned_user_ids, proponent_id, ...updates } = req.body;

    // Find MRFC
    const mrfc = await Mrfc.findByPk(mrfcId);
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

    // Verify new proponent if changing
    if (proponent_id && proponent_id !== mrfc.proponent_id) {
      const proponent = await Proponent.findByPk(proponent_id);
      if (!proponent) {
        res.status(404).json({
          success: false,
          error: {
            code: 'PROPONENT_NOT_FOUND',
            message: 'Proponent not found'
          }
        });
        return;
      }
      updates.proponent_id = proponent_id;
    }

    // Update MRFC with transaction
    await sequelize.transaction(async (t) => {
      await mrfc.update(updates, { transaction: t });

      // Update user access if provided
      if (assigned_user_ids) {
        // Remove existing access
        await UserMrfcAccess.destroy({
          where: { mrfc_id: mrfcId },
          transaction: t
        });

        // Create new access records
        if (assigned_user_ids.length > 0) {
          const accessRecords = assigned_user_ids.map((userId: number) => ({
            user_id: userId,
            mrfc_id: mrfcId,
            granted_by: currentUser?.userId,
            is_active: true
          }));
          await UserMrfcAccess.bulkCreate(accessRecords, { transaction: t });
        }
      }

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'UPDATE_MRFC',
          entity_type: 'MRFC',
          entity_id: mrfcId,
          details: { mrfc_number: mrfc.mrfc_number, updates }
        },
        { transaction: t }
      );
    });

    // Return updated MRFC
    const updatedMrfc = await Mrfc.findByPk(mrfcId);

    res.json({
      success: true,
      message: 'MRFC updated successfully',
      data: updatedMrfc
    });
  } catch (error: any) {
    console.error('MRFC update error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'MRFC_UPDATE_FAILED',
        message: error.message || 'Failed to update MRFC'
      }
    });
  }
};

/**
 * Delete MRFC (soft delete)
 * DELETE /api/v1/mrfcs/:id
 */
export const deleteMrfc = async (req: Request, res: Response): Promise<void> => {
  try {
    const mrfcId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find MRFC
    const mrfc = await Mrfc.findByPk(mrfcId);
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

    // Soft delete with transaction
    await sequelize.transaction(async (t) => {
      await mrfc.update(
        {
          status: 'DELETED',
          deleted_at: new Date()
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'DELETE_MRFC',
          entity_type: 'MRFC',
          entity_id: mrfcId,
          details: { mrfc_number: mrfc.mrfc_number }
        },
        { transaction: t }
      );
    });

    res.json({
      success: true,
      message: 'MRFC deleted successfully'
    });
  } catch (error: any) {
    console.error('MRFC deletion error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'MRFC_DELETION_FAILED',
        message: error.message || 'Failed to delete MRFC'
      }
    });
  }
};
