/**
 * ================================================
 * AUDIT LOG CONTROLLER
 * ================================================
 * Handles audit log viewing and reporting (ADMIN only)
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { AuditLog, User } from '../models';

/**
 * List audit logs with pagination and filtering
 * GET /api/v1/audit-logs
 */
export const listAuditLogs = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      user_id,
      action,
      entity_type,
      entity_id,
      date_from,
      date_to,
      page = '1',
      limit = '50'
    } = req.query;

    // Build filter conditions
    const where: any = {};
    if (user_id) where.user_id = user_id;
    if (action) where.action = action;
    if (entity_type) where.entity_type = entity_type;
    if (entity_id) where.entity_id = entity_id;

    // Date range filter
    if (date_from || date_to) {
      where.created_at = {};
      if (date_from) where.created_at[Op.gte] = date_from;
      if (date_to) where.created_at[Op.lte] = date_to;
    }

    // Query audit logs with pagination
    const pageNum = Math.max(1, parseInt(page as string));
    const limitNum = Math.min(200, Math.max(1, parseInt(limit as string)));
    const offset = (pageNum - 1) * limitNum;

    const { count, rows: logs } = await AuditLog.findAndCountAll({
      where,
      include: [
        {
          model: User,
          as: 'user',
          attributes: ['id', 'username', 'full_name']
        }
      ],
      limit: limitNum,
      offset,
      order: [['created_at', 'DESC']]
    });

    res.json({
      success: true,
      data: {
        logs,
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
    console.error('Audit log listing error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AUDIT_LOG_LISTING_FAILED',
        message: error.message || 'Failed to retrieve audit logs'
      }
    });
  }
};

/**
 * Get audit log statistics
 * GET /api/v1/audit-logs/statistics
 */
export const getStatistics = async (req: Request, res: Response): Promise<void> => {
  try {
    const { date_from, date_to } = req.query;

    // Build filter
    const where: any = {};
    if (date_from || date_to) {
      where.created_at = {};
      if (date_from) where.created_at[Op.gte] = date_from;
      if (date_to) where.created_at[Op.lte] = date_to;
    }

    // Get total count
    const totalLogs = await AuditLog.count({ where });

    // Get counts by action
    const actionCounts = await AuditLog.findAll({
      where,
      attributes: [
        'action',
        [AuditLog.sequelize!.fn('COUNT', AuditLog.sequelize!.col('id')), 'count']
      ],
      group: ['action'],
      raw: true
    });

    // Get counts by entity type
    const entityTypeCounts = await AuditLog.findAll({
      where,
      attributes: [
        'entity_type',
        [AuditLog.sequelize!.fn('COUNT', AuditLog.sequelize!.col('id')), 'count']
      ],
      group: ['entity_type'],
      raw: true
    });

    // Get most active users
    const activeUsers = await AuditLog.findAll({
      where,
      attributes: [
        'user_id',
        [AuditLog.sequelize!.fn('COUNT', AuditLog.sequelize!.col('AuditLog.id')), 'action_count']
      ],
      include: [
        {
          model: User,
          as: 'user',
          attributes: ['username', 'full_name']
        }
      ],
      group: ['user_id', 'user.id'],
      order: [[AuditLog.sequelize!.literal('action_count'), 'DESC']],
      limit: 10,
      raw: false
    });

    res.json({
      success: true,
      data: {
        total_logs: totalLogs,
        by_action: actionCounts,
        by_entity_type: entityTypeCounts,
        most_active_users: activeUsers
      }
    });
  } catch (error: any) {
    console.error('Audit log statistics error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'AUDIT_LOG_STATISTICS_FAILED',
        message: error.message || 'Failed to retrieve audit log statistics'
      }
    });
  }
};
