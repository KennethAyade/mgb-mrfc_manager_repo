/**
 * ================================================
 * PROPONENT CONTROLLER
 * ================================================
 * Handles business logic for proponent (mining company) operations
 */

import { Request, Response } from 'express';
import { Op, Sequelize } from 'sequelize';
import Proponent, { ProponentStatus } from '../models/Proponent';
import Mrfc from '../models/Mrfc';
import Joi from 'joi';

/**
 * LIST ALL PROPONENTS
 * GET /api/v1/proponents
 */
export const listProponents = async (req: Request, res: Response): Promise<void> => {
  try {
    // Parse and validate query parameters
    const page = Math.max(1, parseInt(req.query.page as string) || 1);
    const limit = Math.min(100, Math.max(1, parseInt(req.query.limit as string) || 20));
    const search = req.query.search as string || '';
    const mrfcId = req.query.mrfc_id ? parseInt(req.query.mrfc_id as string) : undefined;
    const isActive = req.query.is_active === 'true' ? true : req.query.is_active === 'false' ? false : undefined;
    const sortBy = (req.query.sort_by as string) || 'company_name';
    const sortOrder = (req.query.sort_order as string)?.toUpperCase() === 'DESC' ? 'DESC' : 'ASC';

    // Build WHERE clause
    const where: any = {};

    // Filter by MRFC
    if (mrfcId) {
      where.mrfc_id = mrfcId;
    }

    // Filter by status
    if (isActive !== undefined) {
      where.status = isActive ? ProponentStatus.ACTIVE : { [Op.ne]: ProponentStatus.ACTIVE };
    }

    // Search filter
    if (search.trim()) {
      where[Op.or] = [
        { company_name: { [Op.iLike]: `%${search}%` } },
        { name: { [Op.iLike]: `%${search}%` } },
        { contact_person: { [Op.iLike]: `%${search}%` } },
        { email: { [Op.iLike]: `%${search}%` } }
      ];
    }

    // Calculate pagination
    const offset = (page - 1) * limit;

    // Determine sort field
    const orderField = sortBy === 'created_at' ? 'created_at' : 
                       sortBy === 'contact_person' ? 'contact_person' :
                       'company_name';

    // Fetch proponents with pagination
    const { count, rows: proponents } = await Proponent.findAndCountAll({
      where,
      limit,
      offset,
      order: [[orderField, sortOrder]],
      include: [
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'name', 'municipality']
        }
      ]
    });

    // Calculate pagination metadata
    const totalPages = Math.ceil(count / limit);

    res.status(200).json({
      success: true,
      data: {
        proponents: proponents.map(p => ({
          id: p.id,
          mrfc_id: p.mrfc_id,
          name: p.name,
          company_name: p.company_name,
          permit_number: p.permit_number,
          permit_type: p.permit_type,
          status: p.status,
          contact_person: p.contact_person,
          contact_number: p.contact_number,
          email: p.email,
          address: p.address,
          created_at: p.created_at,
          updated_at: p.updated_at
        })),
        pagination: {
          page,
          limit,
          total: count,
          totalPages,
          hasNext: page < totalPages,
          hasPrev: page > 1
        }
      }
    });
  } catch (error: any) {
    console.error('Error listing proponents:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to fetch proponents',
        details: process.env.NODE_ENV === 'development' ? error.message : undefined
      }
    });
  }
};

/**
 * GET PROPONENT BY ID
 * GET /api/v1/proponents/:id
 */
export const getProponentById = async (req: Request, res: Response): Promise<void> => {
  try {
    const proponentId = parseInt(req.params.id);

    if (isNaN(proponentId)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_ID',
          message: 'Invalid proponent ID format'
        }
      });
      return;
    }

    const proponent = await Proponent.findByPk(proponentId, {
      include: [
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'name', 'municipality', 'contact_person', 'contact_number']
        }
      ]
    });

    if (!proponent) {
      res.status(404).json({
        success: false,
        error: {
          code: 'NOT_FOUND',
          message: 'Proponent not found'
        }
      });
      return;
    }

    res.status(200).json({
      success: true,
      data: {
        id: proponent.id,
        mrfc_id: proponent.mrfc_id,
        name: proponent.name,
        company_name: proponent.company_name,
        permit_number: proponent.permit_number,
        permit_type: proponent.permit_type,
        status: proponent.status,
        contact_person: proponent.contact_person,
        contact_number: proponent.contact_number,
        email: proponent.email,
        address: proponent.address,
        created_at: proponent.created_at,
        updated_at: proponent.updated_at
      }
    });
  } catch (error: any) {
    console.error('Error fetching proponent:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to fetch proponent details',
        details: process.env.NODE_ENV === 'development' ? error.message : undefined
      }
    });
  }
};

/**
 * CREATE PROPONENT
 * POST /api/v1/proponents
 */
export const createProponent = async (req: Request, res: Response): Promise<void> => {
  try {
    // Validation schema
    const schema = Joi.object({
      mrfc_id: Joi.number().integer().positive().required(),
      name: Joi.string().max(200).required(),
      company_name: Joi.string().max(200).required(),
      permit_number: Joi.string().max(50).optional().allow(null, ''),
      permit_type: Joi.string().max(50).optional().allow(null, ''),
      status: Joi.string().valid(...Object.values(ProponentStatus)).default(ProponentStatus.ACTIVE),
      contact_person: Joi.string().max(100).optional().allow(null, ''),
      contact_number: Joi.string().max(20).optional().allow(null, ''),
      email: Joi.string().email().max(100).optional().allow(null, ''),
      address: Joi.string().optional().allow(null, '')
    });

    const { error, value } = schema.validate(req.body);

    if (error) {
      res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'Invalid input data',
          details: error.details.map(d => d.message)
        }
      });
      return;
    }

    // Check if MRFC exists
    const mrfc = await Mrfc.findByPk(value.mrfc_id);
    if (!mrfc) {
      res.status(404).json({
        success: false,
        error: {
          code: 'MRFC_NOT_FOUND',
          message: 'The specified MRFC does not exist'
        }
      });
      return;
    }

    // Check for duplicate company name
    const existingCompany = await Proponent.findOne({
      where: { company_name: value.company_name }
    });

    if (existingCompany) {
      res.status(409).json({
        success: false,
        error: {
          code: 'DUPLICATE_COMPANY',
          message: 'A proponent with this company name already exists'
        }
      });
      return;
    }

    // Check for duplicate email if provided
    if (value.email) {
      const existingEmail = await Proponent.findOne({
        where: { email: value.email }
      });

      if (existingEmail) {
        res.status(409).json({
          success: false,
          error: {
            code: 'DUPLICATE_EMAIL',
            message: 'A proponent with this email already exists'
          }
        });
        return;
      }
    }

    // Create proponent
    const proponent = await Proponent.create({
      mrfc_id: value.mrfc_id,
      name: value.name,
      company_name: value.company_name,
      permit_number: value.permit_number || null,
      permit_type: value.permit_type || null,
      status: value.status || ProponentStatus.ACTIVE,
      contact_person: value.contact_person || null,
      contact_number: value.contact_number || null,
      email: value.email || null,
      address: value.address || null
    });

    res.status(201).json({
      success: true,
      message: 'Proponent created successfully',
      data: {
        id: proponent.id,
        mrfc_id: proponent.mrfc_id,
        name: proponent.name,
        company_name: proponent.company_name,
        permit_number: proponent.permit_number,
        permit_type: proponent.permit_type,
        status: proponent.status,
        contact_person: proponent.contact_person,
        contact_number: proponent.contact_number,
        email: proponent.email,
        address: proponent.address,
        created_at: proponent.created_at,
        updated_at: proponent.updated_at
      }
    });
  } catch (error: any) {
    console.error('Error creating proponent:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to create proponent',
        details: process.env.NODE_ENV === 'development' ? error.message : undefined
      }
    });
  }
};

/**
 * UPDATE PROPONENT
 * PUT /api/v1/proponents/:id
 */
export const updateProponent = async (req: Request, res: Response): Promise<void> => {
  try {
    const proponentId = parseInt(req.params.id);

    if (isNaN(proponentId)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_ID',
          message: 'Invalid proponent ID format'
        }
      });
      return;
    }

    // Validation schema
    const schema = Joi.object({
      name: Joi.string().max(200).optional(),
      company_name: Joi.string().max(200).optional(),
      permit_number: Joi.string().max(50).optional().allow(null, ''),
      permit_type: Joi.string().max(50).optional().allow(null, ''),
      status: Joi.string().valid(...Object.values(ProponentStatus)).optional(),
      contact_person: Joi.string().max(100).optional().allow(null, ''),
      contact_number: Joi.string().max(20).optional().allow(null, ''),
      email: Joi.string().email().max(100).optional().allow(null, ''),
      address: Joi.string().optional().allow(null, '')
    });

    const { error, value } = schema.validate(req.body);

    if (error) {
      res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'Invalid input data',
          details: error.details.map(d => d.message)
        }
      });
      return;
    }

    // Find proponent
    const proponent = await Proponent.findByPk(proponentId);

    if (!proponent) {
      res.status(404).json({
        success: false,
        error: {
          code: 'NOT_FOUND',
          message: 'Proponent not found'
        }
      });
      return;
    }

    // Check for duplicate company name if updating
    if (value.company_name && value.company_name !== proponent.company_name) {
      const existingCompany = await Proponent.findOne({
        where: { 
          company_name: value.company_name,
          id: { [Op.ne]: proponentId }
        }
      });

      if (existingCompany) {
        res.status(409).json({
          success: false,
          error: {
            code: 'DUPLICATE_COMPANY',
            message: 'A proponent with this company name already exists'
          }
        });
        return;
      }
    }

    // Check for duplicate email if updating
    if (value.email && value.email !== proponent.email) {
      const existingEmail = await Proponent.findOne({
        where: { 
          email: value.email,
          id: { [Op.ne]: proponentId }
        }
      });

      if (existingEmail) {
        res.status(409).json({
          success: false,
          error: {
            code: 'DUPLICATE_EMAIL',
            message: 'A proponent with this email already exists'
          }
        });
        return;
      }
    }

    // Update proponent
    await proponent.update(value);

    res.status(200).json({
      success: true,
      message: 'Proponent updated successfully',
      data: {
        id: proponent.id,
        mrfc_id: proponent.mrfc_id,
        name: proponent.name,
        company_name: proponent.company_name,
        permit_number: proponent.permit_number,
        permit_type: proponent.permit_type,
        status: proponent.status,
        contact_person: proponent.contact_person,
        contact_number: proponent.contact_number,
        email: proponent.email,
        address: proponent.address,
        created_at: proponent.created_at,
        updated_at: proponent.updated_at
      }
    });
  } catch (error: any) {
    console.error('Error updating proponent:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to update proponent',
        details: process.env.NODE_ENV === 'development' ? error.message : undefined
      }
    });
  }
};

/**
 * DELETE PROPONENT
 * DELETE /api/v1/proponents/:id
 */
export const deleteProponent = async (req: Request, res: Response): Promise<void> => {
  try {
    const proponentId = parseInt(req.params.id);

    if (isNaN(proponentId)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_ID',
          message: 'Invalid proponent ID format'
        }
      });
      return;
    }

    // Find proponent
    const proponent = await Proponent.findByPk(proponentId);

    if (!proponent) {
      res.status(404).json({
        success: false,
        error: {
          code: 'NOT_FOUND',
          message: 'Proponent not found'
        }
      });
      return;
    }

    // Soft delete: Set status to INACTIVE
    await proponent.update({ status: ProponentStatus.INACTIVE });

    res.status(200).json({
      success: true,
      message: 'Proponent deleted successfully (set to INACTIVE)'
    });
  } catch (error: any) {
    console.error('Error deleting proponent:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'SERVER_ERROR',
        message: 'Failed to delete proponent',
        details: process.env.NODE_ENV === 'development' ? error.message : undefined
      }
    });
  }
};
