/**
 * ================================================
 * AGENDA MANAGEMENT CONTROLLER
 * ================================================
 * Handles agenda items for quarterly meetings
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { Agenda, Mrfc, Quarter, MatterArising, AuditLog } from '../models';
import sequelize from '../config/database';

/**
 * List all agendas with pagination and filtering
 * GET /api/v1/agendas
 */
export const listAgendas = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      page = '1',
      limit = '20',
      quarter_id,
      mrfc_id,
      status,
      sort_by = 'created_at',
      sort_order = 'DESC'
    } = req.query;

    const currentUser = (req as any).user;

    // Validate and parse parameters
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    // Build filter conditions
    const where: any = {};
    if (quarter_id) where.quarter_id = quarter_id;
    if (mrfc_id) where.mrfc_id = mrfc_id;
    if (status) where.status = status;

    // Apply user MRFC access filter for USER role
    if (currentUser?.role === 'USER') {
      const userMrfcIds = currentUser.mrfcAccess || [];
      where.mrfc_id = { [Op.in]: userMrfcIds };
    }

    // Query agendas
    const { count, rows: agendas } = await Agenda.findAndCountAll({
      where,
      include: [
        {
          model: Mrfc,
          as: 'mrfc',
          attributes: ['id', 'mrfc_number', 'project_title']
        },
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['id', 'quarter_number', 'year', 'meeting_date']
        }
      ],
      limit: limitNum,
      offset,
      order: [[sort_by as string, sort_order as string]]
    });

    res.json({
      success: true,
      data: {
        agendas,
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
    console.error('Agenda listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AGENDA_LISTING_FAILED',
        message: error.message || 'Failed to retrieve agendas'
      }
    });
  }
};

/**
 * Create new agenda
 * POST /api/v1/agendas
 */
export const createAgenda = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      quarter_id,
      mrfc_id,
      agenda_number,
      title,
      description,
      status,
      decision,
      conditions
    } = req.body;
    const currentUser = (req as any).user;

    // Verify quarter exists
    const quarter = await Quarter.findByPk(quarter_id);
    if (!quarter) {
      res.status(404).json({
        success: false,
        error: {
          code: 'QUARTER_NOT_FOUND',
          message: 'Quarter not found'
        }
      });
      return;
    }

    // Verify MRFC exists
    const mrfc = await Mrfc.findByPk(mrfc_id);
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

    // Create agenda with transaction
    const agenda = await sequelize.transaction(async (t) => {
      const newAgenda = await Agenda.create(
        {
          quarter_id,
          mrfc_id,
          agenda_number,
          title,
          description,
          status: status || 'SCHEDULED',
          decision,
          conditions,
          created_by: currentUser?.userId
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'CREATE_AGENDA',
          entity_type: 'AGENDA',
          entity_id: newAgenda.id,
          details: { agenda_number: newAgenda.agenda_number, mrfc_id }
        },
        { transaction: t }
      );

      return newAgenda;
    });

    res.status(201).json({
      success: true,
      message: 'Agenda created successfully',
      data: {
        ...agenda.toJSON(),
        id: Number(agenda.id),
        quarter_id: Number(agenda.quarter_id),
        mrfc_id: Number(agenda.mrfc_id),
        created_by: agenda.created_by ? Number(agenda.created_by) : null
      }
    });
  } catch (error: any) {
    console.error('Agenda creation error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AGENDA_CREATION_FAILED',
        message: error.message || 'Failed to create agenda'
      }
    });
  }
};

/**
 * Get agenda by ID
 * GET /api/v1/agendas/:id
 */
export const getAgendaById = async (req: Request, res: Response): Promise<void> => {
  try {
    const agendaId = parseInt(req.params.id);

    // Validate ID
    if (isNaN(agendaId)) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_ID',
          message: 'Invalid agenda ID'
        }
      });
      return;
    }

    // Find agenda with full details
    const agenda = await Agenda.findByPk(agendaId, {
      include: [
        {
          model: Mrfc,
          as: 'mrfc'
        },
        {
          model: Quarter,
          as: 'quarter'
        },
        {
          model: MatterArising,
          as: 'matters_arising'
        }
      ]
    });

    // Check if agenda exists
    if (!agenda) {
      res.status(404).json({
        success: false,
        error: {
          code: 'AGENDA_NOT_FOUND',
          message: 'Agenda not found'
        }
      });
      return;
    }

    res.json({
      success: true,
      data: agenda
    });
  } catch (error: any) {
    console.error('Get agenda error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'GET_AGENDA_FAILED',
        message: error.message || 'Failed to retrieve agenda'
      }
    });
  }
};

/**
 * Update agenda
 * PUT /api/v1/agendas/:id
 */
export const updateAgenda = async (req: Request, res: Response): Promise<void> => {
  try {
    const agendaId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find agenda
    const agenda = await Agenda.findByPk(agendaId);
    if (!agenda) {
      res.status(404).json({
        success: false,
        error: {
          code: 'AGENDA_NOT_FOUND',
          message: 'Agenda not found'
        }
      });
      return;
    }

    // Update agenda with transaction
    await sequelize.transaction(async (t) => {
      await agenda.update(req.body, { transaction: t });

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'UPDATE_AGENDA',
          entity_type: 'AGENDA',
          entity_id: agendaId,
          details: { agenda_number: agenda.agenda_number, updates: req.body }
        },
        { transaction: t }
      );
    });

    // Return updated agenda
    const updatedAgenda = await Agenda.findByPk(agendaId);

    res.json({
      success: true,
      message: 'Agenda updated successfully',
      data: updatedAgenda
    });
  } catch (error: any) {
    console.error('Agenda update error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AGENDA_UPDATE_FAILED',
        message: error.message || 'Failed to update agenda'
      }
    });
  }
};

/**
 * Delete agenda
 * DELETE /api/v1/agendas/:id
 */
export const deleteAgenda = async (req: Request, res: Response): Promise<void> => {
  try {
    const agendaId = parseInt(req.params.id);
    const currentUser = (req as any).user;

    // Find agenda
    const agenda = await Agenda.findByPk(agendaId);
    if (!agenda) {
      res.status(404).json({
        success: false,
        error: {
          code: 'AGENDA_NOT_FOUND',
          message: 'Agenda not found'
        }
      });
      return;
    }

    // Delete with transaction
    await sequelize.transaction(async (t) => {
      await agenda.destroy({ transaction: t });

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'DELETE_AGENDA',
          entity_type: 'AGENDA',
          entity_id: agendaId,
          details: { agenda_number: agenda.agenda_number }
        },
        { transaction: t }
      );
    });

    res.json({
      success: true,
      message: 'Agenda deleted successfully'
    });
  } catch (error: any) {
    console.error('Agenda deletion error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AGENDA_DELETION_FAILED',
        message: error.message || 'Failed to delete agenda'
      }
    });
  }
};
