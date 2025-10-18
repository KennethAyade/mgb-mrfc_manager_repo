/**
 * ================================================
 * STATISTICS CONTROLLER
 * ================================================
 * Handles statistics and reports for the dashboard
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import {
  Mrfc,
  User,
  Proponent,
  Quarter,
  Agenda,
  Document,
  ComplianceLog,
  AuditLog
} from '../models';
import sequelize from '../config/database';

/**
 * Get dashboard statistics
 * GET /api/v1/statistics/dashboard
 */
export const getDashboard = async (req: Request, res: Response): Promise<void> => {
  try {
    const currentUser = (req as any).user;

    // Apply MRFC access filter for USER role
    let mrfcWhere: any = {};
    if (currentUser?.role === 'USER') {
      const userMrfcIds = currentUser.mrfcAccess || [];
      mrfcWhere = { id: { [Op.in]: userMrfcIds } };
    }

    // Count statistics
    const totalMrfcs = await Mrfc.count({ where: mrfcWhere });
    const totalProponents = await Proponent.count({ where: { is_active: true } });
    const totalUsers = await User.count({ where: { is_active: true } });
    const currentQuarters = await Quarter.count({
      where: {
        is_active: true,
        end_date: { [Op.gte]: new Date() }
      }
    });

    // MRFC status breakdown
    const mrfcsByStatus = await Mrfc.findAll({
      where: mrfcWhere,
      attributes: [
        'status',
        [sequelize.fn('COUNT', sequelize.col('id')), 'count']
      ],
      group: ['status'],
      raw: true
    });

    // Recent MRFCs
    const recentMrfcs = await Mrfc.findAll({
      where: mrfcWhere,
      include: [
        {
          model: Proponent,
          as: 'proponents',
          attributes: ['company_name']
        }
      ],
      order: [['created_at', 'DESC']],
      limit: 5
    });

    // Compliance summary
    const complianceStats = await ComplianceLog.findAll({
      attributes: [
        'status',
        [sequelize.fn('COUNT', sequelize.col('id')), 'count']
      ],
      group: ['status'],
      raw: true
    });

    // Recent activity (audit logs)
    const recentActivity = await AuditLog.findAll({
      include: [
        {
          model: User,
          as: 'user',
          attributes: ['username', 'full_name']
        }
      ],
      order: [['created_at', 'DESC']],
      limit: 10
    });

    res.json({
      success: true,
      data: {
        summary: {
          total_mrfcs: totalMrfcs,
          total_proponents: totalProponents,
          total_users: totalUsers,
          current_quarters: currentQuarters
        },
        mrfcs_by_status: mrfcsByStatus,
        recent_mrfcs: recentMrfcs,
        compliance_stats: complianceStats,
        recent_activity: recentActivity
      }
    });
  } catch (error: any) {
    console.error('Dashboard statistics error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'DASHBOARD_STATISTICS_FAILED',
        message: error.message || 'Failed to retrieve dashboard statistics'
      }
    });
  }
};

/**
 * Generate reports
 * GET /api/v1/statistics/reports
 */
export const generateReports = async (req: Request, res: Response): Promise<void> => {
  try {
    const { report_type, date_from, date_to, quarter_id } = req.query;
    const currentUser = (req as any).user;

    // Apply MRFC access filter for USER role
    let mrfcWhere: any = {};
    if (currentUser?.role === 'USER') {
      const userMrfcIds = currentUser.mrfcAccess || [];
      mrfcWhere = { id: { [Op.in]: userMrfcIds } };
    }

    let reportData: any = {};

    switch (report_type) {
      case 'mrfc_summary':
        // MRFC summary report
        const mrfcs = await Mrfc.findAll({
          where: {
            ...mrfcWhere,
            ...(date_from && { date_received: { [Op.gte]: date_from } }),
            ...(date_to && { date_received: { [Op.lte]: date_to } })
          },
          include: [
            {
              model: Proponent,
              as: 'proponents',
              attributes: ['company_name', 'contact_person']
            }
          ],
          order: [['date_received', 'DESC']]
        });

        reportData = {
          title: 'MRFC Summary Report',
          total_count: mrfcs.length,
          mrfcs
        };
        break;

      case 'compliance_report':
        // Compliance report
        const whereClause: any = {};
        if (quarter_id) whereClause.quarter_id = quarter_id;
        if (date_from) whereClause.compliance_date = { [Op.gte]: date_from };
        if (date_to)
          whereClause.compliance_date = {
            ...whereClause.compliance_date,
            [Op.lte]: date_to
          };

        const complianceLogs = await ComplianceLog.findAll({
          where: whereClause,
          include: [
            {
              model: Mrfc,
              as: 'mrfc',
              where: mrfcWhere,
              attributes: ['mrfc_number', 'project_title']
            },
            {
              model: Quarter,
              as: 'quarter',
              attributes: ['quarter_number', 'year']
            }
          ],
          order: [['compliance_date', 'DESC']]
        });

        const avgCompliance =
          complianceLogs.length > 0
            ? complianceLogs.reduce((sum: number, log: any) => sum + log.compliance_percentage, 0) /
              complianceLogs.length
            : 0;

        reportData = {
          title: 'Compliance Report',
          total_records: complianceLogs.length,
          average_compliance: parseFloat(avgCompliance.toFixed(2)),
          compliance_logs: complianceLogs
        };
        break;

      case 'quarterly_summary':
        // Quarterly summary report
        if (!quarter_id) {
          res.status(400).json({
            success: false,
            error: {
              code: 'QUARTER_ID_REQUIRED',
              message: 'quarter_id is required for quarterly summary report'
            }
          });
          return;
        }

        const quarter = await Quarter.findByPk(quarter_id as string);
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

        const agendas = await Agenda.findAll({
          where: { quarter_id },
          include: [
            {
              model: Mrfc,
              as: 'mrfc',
              where: mrfcWhere,
              attributes: ['mrfc_number', 'project_title']
            }
          ]
        });

        const documents = await Document.count({
          where: { quarter_id }
        });

        reportData = {
          title: `Quarter ${quarter.quarter_number} ${quarter.year} Summary`,
          quarter: {
            quarter_number: quarter.quarter_number,
            year: quarter.year,
            meeting_date: quarter.meeting_date
          },
          total_agendas: agendas.length,
          total_documents: documents,
          agendas
        };
        break;

      default:
        res.status(400).json({
          success: false,
          error: {
            code: 'INVALID_REPORT_TYPE',
            message:
              'Invalid report type. Valid types: mrfc_summary, compliance_report, quarterly_summary'
          }
        });
        return;
    }

    res.json({
      success: true,
      data: reportData
    });
  } catch (error: any) {
    console.error('Report generation error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'REPORT_GENERATION_FAILED',
        message: error.message || 'Failed to generate report'
      }
    });
  }
};

/**
 * Get user activity statistics
 * GET /api/v1/statistics/user-activity
 */
export const getUserActivity = async (req: Request, res: Response): Promise<void> => {
  try {
    const { user_id, date_from, date_to } = req.query;

    // Build filter
    const where: any = {};
    if (user_id) where.user_id = user_id;
    if (date_from || date_to) {
      where.created_at = {};
      if (date_from) where.created_at[Op.gte] = date_from;
      if (date_to) where.created_at[Op.lte] = date_to;
    }

    // Get activity count by action
    const activityByAction = await AuditLog.findAll({
      where,
      attributes: [
        'action',
        [sequelize.fn('COUNT', sequelize.col('id')), 'count']
      ],
      group: ['action'],
      raw: true
    });

    // Get total activity count
    const totalActivity = await AuditLog.count({ where });

    // Get recent activity
    const recentActivity = await AuditLog.findAll({
      where,
      include: [
        {
          model: User,
          as: 'user',
          attributes: ['username', 'full_name']
        }
      ],
      order: [['created_at', 'DESC']],
      limit: 20
    });

    res.json({
      success: true,
      data: {
        total_activity: totalActivity,
        activity_by_action: activityByAction,
        recent_activity: recentActivity
      }
    });
  } catch (error: any) {
    console.error('User activity statistics error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'USER_ACTIVITY_FAILED',
        message: error.message || 'Failed to retrieve user activity statistics'
      }
    });
  }
};
