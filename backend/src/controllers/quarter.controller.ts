/**
 * ================================================
 * QUARTER MANAGEMENT CONTROLLER
 * ================================================
 * Handles quarterly meeting periods management
 */

import { Request, Response } from 'express';
import { Quarter, Agenda, AuditLog } from '../models';
import sequelize from '../config/database';

/**
 * List all quarters
 * GET /api/v1/quarters
 */
export const listQuarters = async (req: Request, res: Response): Promise<void> => {
  try {
    const { year, is_active, sort_order = 'DESC' } = req.query;

    // Build filter conditions
    const where: any = {};
    if (year) where.year = parseInt(year as string);
    if (is_active !== undefined) where.is_active = is_active === 'true';

    // Query quarters with agenda count
    const quarters = await Quarter.findAll({
      where,
      attributes: {
        include: [[sequelize.fn('COUNT', sequelize.col('agendas.id')), 'agenda_count']]
      },
      include: [
        {
          model: Agenda,
          as: 'agendas',
          attributes: [],
          required: false
        }
      ],
      group: ['Quarter.id'],
      order: [
        ['year', sort_order],
        ['quarter_number', sort_order]
      ]
    });

    // Add dynamic status based on dates
    const today = new Date();
    const enrichedQuarters = quarters.map((q: any) => {
      let status = 'COMPLETED';
      const meetingDate = new Date(q.meeting_date);
      const startDate = new Date(q.start_date);
      const endDate = new Date(q.end_date);

      if (meetingDate > today) {
        status = 'UPCOMING';
      } else if (startDate <= today && today <= endDate) {
        status = 'IN_PROGRESS';
      }

      return {
        ...q.toJSON(),
        status
      };
    });

    res.json({
      success: true,
      data: {
        quarters: enrichedQuarters
      }
    });
  } catch (error: any) {
    console.error('Quarter listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'QUARTER_LISTING_FAILED',
        message: error.message || 'Failed to retrieve quarters'
      }
    });
  }
};

/**
 * Create new quarter
 * POST /api/v1/quarters
 */
export const createQuarter = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      quarter_number,
      year,
      start_date,
      end_date,
      meeting_date,
      meeting_location,
      is_active
    } = req.body;
    const currentUser = (req as any).user;

    // Validate quarter_number
    if (quarter_number < 1 || quarter_number > 4) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_QUARTER_NUMBER',
          message: 'Quarter number must be between 1 and 4'
        }
      });
      return;
    }

    // Check if quarter exists
    const existing = await Quarter.findOne({
      where: { quarter_number, year }
    });
    if (existing) {
      res.status(409).json({
        success: false,
        error: {
          code: 'QUARTER_EXISTS',
          message: `Quarter ${quarter_number} for year ${year} already exists`
        }
      });
      return;
    }

    // Validate date range
    const startDate = new Date(start_date);
    const endDate = new Date(end_date);
    if (startDate >= endDate) {
      res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_DATE_RANGE',
          message: 'Start date must be before end date'
        }
      });
      return;
    }

    // Create quarter with transaction
    const quarter = await sequelize.transaction(async (t) => {
      const newQuarter = await Quarter.create(
        {
          quarter_number,
          year,
          start_date,
          end_date,
          meeting_date,
          meeting_location,
          is_active: is_active !== false,
          created_by: currentUser?.userId
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'CREATE_QUARTER',
          entity_type: 'QUARTER',
          entity_id: newQuarter.id,
          details: { quarter_number: newQuarter.quarter_number, year: newQuarter.year }
        },
        { transaction: t }
      );

      return newQuarter;
    });

    res.status(201).json({
      success: true,
      message: 'Quarter created successfully',
      data: quarter
    });
  } catch (error: any) {
    console.error('Quarter creation error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'QUARTER_CREATION_FAILED',
        message: error.message || 'Failed to create quarter'
      }
    });
  }
};
