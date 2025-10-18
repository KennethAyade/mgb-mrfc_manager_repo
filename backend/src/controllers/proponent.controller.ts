/**
 * ================================================
 * PROPONENT MANAGEMENT CONTROLLER
 * ================================================
 * Handles proponent (mining company) CRUD operations
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { Proponent, Mrfc, AuditLog } from '../models';
import sequelize from '../config/database';

/**
 * List all proponents with pagination and filtering
 * GET /api/v1/proponents
 */
export const listProponents = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      page = '1',
      limit = '20',
      search,
      is_active,
      sort_by = 'company_name',
      sort_order = 'ASC'
    } = req.query;

    // Validate and parse parameters
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    // Build filter conditions
    const where: any = {};
    if (is_active !== undefined) where.is_active = is_active === 'true';

    // Search filter
    if (search) {
      where[Op.or] = [
        { company_name: { [Op.like]: `%${search}%` } },
        { contact_person: { [Op.like]: `%${search}%` } },
        { contact_email: { [Op.like]: `%${search}%` } }
      ];
    }

    // Query proponents with MRFC count
    const { count, rows: proponents } = await Proponent.findAndCountAll({
      where,
      attributes: {
        include: [[sequelize.fn('COUNT', sequelize.col('mrfcs.id')), 'mrfc_count']]
      },
      include: [
        {
          model: Mrfc,
          as: 'mrfcs',
          attributes: [],
          required: false
        }
      ],
      group: ['Proponent.id'],
      limit: limitNum,
      offset,
      order: [[sort_by as string, sort_order as string]],
      subQuery: false
    });

    res.json({
      success: true,
      data: {
        proponents,
        pagination: {
          page: pageNum,
          limit: limitNum,
          total: Array.isArray(count) ? count.length : count,
          totalPages: Math.ceil((Array.isArray(count) ? count.length : count) / limitNum),
          hasNext: pageNum * limitNum < (Array.isArray(count) ? count.length : count),
          hasPrev: pageNum > 1
        }
      }
    });
  } catch (error: any) {
    console.error('Proponent listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'PROPONENT_LISTING_FAILED',
        message: error.message || 'Failed to retrieve proponents'
      }
    });
  }
};

/**
 * Create new proponent
 * POST /api/v1/proponents
 */
export const createProponent = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      company_name,
      contact_person,
      contact_email,
      contact_phone,
      address,
      tin,
      business_permit,
      is_active
    } = req.body;
    const currentUser = (req as any).user;

    // Check if company name exists
    const existing = await Proponent.findOne({ where: { company_name } });
    if (existing) {
      res.status(409).json({
        success: false,
        error: {
          code: 'COMPANY_EXISTS',
          message: 'Company name already exists'
        }
      });
      return;
    }

    // Check if email exists
    const existingEmail = await Proponent.findOne({ where: { contact_email } });
    if (existingEmail) {
      res.status(409).json({
        success: false,
        error: {
          code: 'EMAIL_EXISTS',
          message: 'Contact email already registered'
        }
      });
      return;
    }

    // Create proponent with transaction
    const proponent = await sequelize.transaction(async (t) => {
      const newProponent = await Proponent.create(
        {
          company_name,
          contact_person,
          contact_email,
          contact_phone,
          address,
          tin,
          business_permit,
          is_active: is_active !== false,
          created_by: currentUser?.userId
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'CREATE_PROPONENT',
          entity_type: 'PROPONENT',
          entity_id: newProponent.id,
          details: { company_name: newProponent.company_name }
        },
        { transaction: t }
      );

      return newProponent;
    });

    res.status(201).json({
      success: true,
      message: 'Proponent created successfully',
      data: proponent
    });
  } catch (error: any) {
    console.error('Proponent creation error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'PROPONENT_CREATION_FAILED',
        message: error.message || 'Failed to create proponent'
      }
    });
  }
};

/**
 * Get proponent by ID
 * GET /api/v1/proponents/:id
 */
export const getProponentById = async (req: Request, res: Response): Promise<void> => {
  try {
    const proponentId = parseInt(req.params.id);

    // Validate ID
    if (isNaN(proponentId)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_ID',
          message: 'Invalid proponent ID'
        }
      });
      return;
    }

    // Find proponent with MRFCs
    const proponent = await Proponent.findByPk(proponentId, {
      include: [
        {
          model: Mrfc,
          as: 'mrfcs',
          attributes: ['id', 'mrfc_number', 'project_title', 'status', 'date_received']
        }
      ]
    });

    // Check if exists
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

    // Calculate statistics
    const mrfcs = proponent.mrfcs || [];
    const statistics = {
      total_mrfcs: mrfcs.length,
      pending_mrfcs: mrfcs.filter((m: any) => m.status === 'PENDING').length,
      approved_mrfcs: mrfcs.filter((m: any) => m.status === 'APPROVED').length,
      rejected_mrfcs: mrfcs.filter((m: any) => m.status === 'REJECTED').length
    };

    res.json({
      success: true,
      data: {
        ...proponent.toJSON(),
        statistics
      }
    });
  } catch (error: any) {
    console.error('Get proponent error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_PROPONENT_FAILED',
        message: error.message || 'Failed to retrieve proponent'
      }
    });
  }
};

/**
 * Update proponent information
 * PUT /api/v1/proponents/:id
 */
export const updateProponent = async (req: Request, res: Response): Promise<void> => {
  try {
    const proponentId = parseInt(req.params.id);
    const currentUser = (req as any).user;
    const {
      company_name,
      contact_person,
      contact_email,
      contact_phone,
      address,
      tin,
      business_permit,
      is_active
    } = req.body;

    // Find proponent
    const proponent = await Proponent.findByPk(proponentId);
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

    // Check if new company name already exists
    if (company_name && company_name !== proponent.company_name) {
      const existing = await Proponent.findOne({
        where: { company_name, id: { [Op.ne]: proponentId } }
      });
      if (existing) {
        res.status(409).json({
          success: false,
          error: {
            code: 'COMPANY_EXISTS',
            message: 'Company name already exists'
          }
        });
        return;
      }
    }

    // Check if new email already exists
    if (contact_email && contact_email !== proponent.contact_email) {
      const existingEmail = await Proponent.findOne({
        where: { contact_email, id: { [Op.ne]: proponentId } }
      });
      if (existingEmail) {
        res.status(409).json({
          success: false,
          error: {
            code: 'EMAIL_EXISTS',
            message: 'Contact email already registered'
          }
        });
        return;
      }
    }

    // Update proponent with transaction
    await sequelize.transaction(async (t) => {
      await proponent.update(
        {
          company_name,
          contact_person,
          contact_email,
          contact_phone,
          address,
          tin,
          business_permit,
          is_active
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'UPDATE_PROPONENT',
          entity_type: 'PROPONENT',
          entity_id: proponentId,
          details: { company_name: proponent.company_name }
        },
        { transaction: t }
      );
    });

    // Return updated proponent
    const updatedProponent = await Proponent.findByPk(proponentId);

    res.json({
      success: true,
      message: 'Proponent updated successfully',
      data: updatedProponent
    });
  } catch (error: any) {
    console.error('Proponent update error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'PROPONENT_UPDATE_FAILED',
        message: error.message || 'Failed to update proponent'
      }
    });
  }
};

/**
 * Delete proponent (soft delete)
 * DELETE /api/v1/proponents/:id
 */
export const deleteProponent = async (req: Request, res: Response): Promise<void> => {
  try {
    const proponentId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find proponent
    const proponent = await Proponent.findByPk(proponentId);
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

    // Check for associated MRFCs
    const mrfcCount = await Mrfc.count({ where: { proponent_id: proponentId } });
    if (mrfcCount > 0) {
      res.status(409).json({
        success: false,
        error: {
          code: 'HAS_ASSOCIATED_MRFCS',
          message: `Cannot delete proponent with ${mrfcCount} associated MRFCs`
        }
      });
      return;
    }

    // Soft delete with transaction
    await sequelize.transaction(async (t) => {
      await proponent.update(
        {
          is_active: false,
          deleted_at: new Date()
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'DELETE_PROPONENT',
          entity_type: 'PROPONENT',
          entity_id: proponentId,
          details: { company_name: proponent.company_name }
        },
        { transaction: t }
      );
    });

    res.json({
      success: true,
      message: 'Proponent deleted successfully'
    });
  } catch (error: any) {
    console.error('Proponent deletion error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'PROPONENT_DELETION_FAILED',
        message: error.message || 'Failed to delete proponent'
      }
    });
  }
};
