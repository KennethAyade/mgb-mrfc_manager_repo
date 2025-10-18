/**
 * ================================================
 * COMPLIANCE DASHBOARD CONTROLLER
 * ================================================
 * Handles compliance tracking and reporting for MRFCs
 */

import { Request, Response } from 'express';
import { Op } from 'sequelize';
import { ComplianceLog, Mrfc, Quarter, User, Notification, AuditLog } from '../models';
import sequelize from '../config/database';

/**
 * Get compliance dashboard overview
 * GET /api/v1/compliance/dashboard
 */
export const getDashboard = async (req: Request, res: Response): Promise<void> => {
  try {
    const { quarter_id, status } = req.query;
    const currentUser = (req as any).user;

    // Build base filter
    const where: any = {};
    if (status) where.status = status;
    if (quarter_id) where.quarter_id = quarter_id;

    // Apply user MRFC access filter for USER role
    let mrfcFilter: any = {};
    if (currentUser?.role === 'USER') {
      const userMrfcIds = currentUser.mrfcAccess || [];
      mrfcFilter = { id: { [Op.in]: userMrfcIds } };
    }

    // Query compliance records
    const complianceRecords = await ComplianceLog.findAll({
      where,
      include: [
        {
          model: Mrfc,
          as: 'mrfc',
          where: mrfcFilter,
          attributes: ['id', 'mrfc_number', 'project_title']
        },
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['quarter_number', 'year']
        }
      ]
    });

    // Calculate summary statistics
    const total = complianceRecords.length;
    const compliant = complianceRecords.filter((c: any) => c.status === 'COMPLIANT').length;
    const nonCompliant = complianceRecords.filter((c: any) => c.status === 'NON_COMPLIANT').length;
    const pending = complianceRecords.filter((c: any) => c.status === 'PENDING').length;
    const rate = total > 0 ? (compliant / total) * 100 : 0;

    // Group by quarter
    const byQuarter = await ComplianceLog.findAll({
      attributes: [
        'quarter_id',
        [sequelize.fn('COUNT', sequelize.col('ComplianceLog.id')), 'total'],
        [
          sequelize.fn(
            'SUM',
            sequelize.literal("CASE WHEN status = 'COMPLIANT' THEN 1 ELSE 0 END")
          ),
          'compliant'
        ],
        [
          sequelize.fn(
            'SUM',
            sequelize.literal("CASE WHEN status = 'NON_COMPLIANT' THEN 1 ELSE 0 END")
          ),
          'non_compliant'
        ],
        [
          sequelize.fn(
            'SUM',
            sequelize.literal("CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END")
          ),
          'pending'
        ]
      ],
      include: [
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['quarter_number', 'year']
        }
      ],
      group: ['quarter_id', 'quarter.id'],
      raw: false
    });

    // Get recent updates
    const recentUpdates = await ComplianceLog.findAll({
      where,
      include: [
        {
          model: Mrfc,
          as: 'mrfc',
          where: mrfcFilter,
          attributes: ['id', 'mrfc_number', 'project_title']
        }
      ],
      order: [['updated_at', 'DESC']],
      limit: 10
    });

    res.json({
      success: true,
      data: {
        summary: {
          total_mrfcs: total,
          compliant,
          non_compliant: nonCompliant,
          pending,
          compliance_rate: parseFloat(rate.toFixed(2))
        },
        by_quarter: byQuarter,
        recent_updates: recentUpdates
      }
    });
  } catch (error: any) {
    console.error('Compliance dashboard error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'COMPLIANCE_DASHBOARD_FAILED',
        message: error.message || 'Failed to retrieve compliance dashboard'
      }
    });
  }
};

/**
 * Get compliance status for specific MRFC
 * GET /api/v1/compliance/mrfc/:id
 */
export const getMrfcCompliance = async (req: Request, res: Response): Promise<void> => {
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

    // Find MRFC
    const mrfc = await Mrfc.findByPk(mrfcId, {
      attributes: ['id', 'mrfc_number', 'project_title', 'status']
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

    // Query compliance history
    const complianceHistory = await ComplianceLog.findAll({
      where: { mrfc_id: mrfcId },
      include: [
        {
          model: Quarter,
          as: 'quarter',
          attributes: ['quarter_number', 'year']
        },
        {
          model: User,
          as: 'calculator',
          attributes: ['full_name']
        }
      ],
      order: [['compliance_date', 'DESC']]
    });

    // Calculate overall compliance
    const total = complianceHistory.length;
    const compliant = complianceHistory.filter((c: any) => c.status === 'COMPLIANT').length;
    const rate = total > 0 ? (compliant / total) * 100 : 0;

    res.json({
      success: true,
      data: {
        mrfc: {
          id: mrfc.id,
          mrfc_number: mrfc.mrfc_number,
          project_title: mrfc.project_title,
          current_status: mrfc.status
        },
        compliance_history: complianceHistory,
        overall_compliance: {
          total_quarters: total,
          compliant_quarters: compliant,
          compliance_rate: parseFloat(rate.toFixed(2))
        }
      }
    });
  } catch (error: any) {
    console.error('MRFC compliance history error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'COMPLIANCE_HISTORY_FAILED',
        message: error.message || 'Failed to retrieve compliance history'
      }
    });
  }
};

/**
 * Record compliance status
 * POST /api/v1/compliance
 */
export const recordCompliance = async (req: Request, res: Response): Promise<void> => {
  try {
    const {
      mrfc_id,
      quarter_id,
      status,
      compliance_date,
      requirements_met,
      requirements_pending,
      notes
    } = req.body;
    const currentUser = (req as any).user;

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

    // Check for duplicate
    const existing = await ComplianceLog.findOne({
      where: { mrfc_id, quarter_id }
    });

    if (existing) {
      res.status(409).json({
        success: false,
        error: {
          code: 'COMPLIANCE_EXISTS',
          message: 'Compliance record already exists for this MRFC and quarter'
        }
      });
      return;
    }

    // Create compliance record with transaction
    const compliance = await sequelize.transaction(async (t) => {
      // Auto-calculate compliance percentage
      const totalRequirements = (requirements_met?.length || 0) + (requirements_pending?.length || 0);
      const compliance_percentage =
        totalRequirements > 0 ? (requirements_met?.length / totalRequirements) * 100 : 0;

      const newCompliance = await ComplianceLog.create(
        {
          mrfc_id,
          quarter_id,
          status,
          compliance_date: compliance_date || new Date(),
          requirements_met: requirements_met || [],
          requirements_pending: requirements_pending || [],
          compliance_percentage: parseFloat(compliance_percentage.toFixed(2)),
          notes,
          calculated_by: currentUser?.userId
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'CREATE_COMPLIANCE',
          entity_type: 'COMPLIANCE',
          entity_id: newCompliance.id,
          details: { mrfc_id, status }
        },
        { transaction: t }
      );

      return newCompliance;
    });

    res.status(201).json({
      success: true,
      message: 'Compliance record created successfully',
      data: compliance
    });
  } catch (error: any) {
    console.error('Compliance recording error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'COMPLIANCE_RECORDING_FAILED',
        message: error.message || 'Failed to record compliance'
      }
    });
  }
};

/**
 * Update compliance record
 * PUT /api/v1/compliance/:id
 */
export const updateCompliance = async (req: Request, res: Response): Promise<void> => {
  try {
    const complianceId = parseInt(req.params.id);
    const currentUser = (req as any).user;
    const { status, requirements_met, requirements_pending, notes, compliance_date } = req.body;

    // Find compliance record
    const compliance = await ComplianceLog.findByPk(complianceId);
    if (!compliance) {
      res.status(404).json({
        success: false,
        error: {
          code: 'COMPLIANCE_NOT_FOUND',
          message: 'Compliance record not found'
        }
      });
      return;
    }

    // Update compliance with transaction
    await sequelize.transaction(async (t) => {
      // Recalculate compliance percentage if requirements changed
      let compliance_percentage = compliance.compliance_percentage;
      if (requirements_met || requirements_pending) {
        const totalRequirements =
          (requirements_met?.length || compliance.requirements_met?.length || 0) +
          (requirements_pending?.length || compliance.requirements_pending?.length || 0);
        compliance_percentage =
          totalRequirements > 0
            ? ((requirements_met?.length || compliance.requirements_met?.length || 0) /
                totalRequirements) *
              100
            : 0;
      }

      await compliance.update(
        {
          status,
          requirements_met,
          requirements_pending,
          compliance_percentage: parseFloat(compliance_percentage.toFixed(2)),
          notes,
          compliance_date,
          override_by: currentUser?.userId
        },
        { transaction: t }
      );

      // Create audit log
      await AuditLog.create(
        {
          user_id: currentUser?.userId,
          action: 'UPDATE_COMPLIANCE',
          entity_type: 'COMPLIANCE',
          entity_id: complianceId,
          details: { status, compliance_percentage }
        },
        { transaction: t }
      );
    });

    // Return updated compliance
    const updatedCompliance = await ComplianceLog.findByPk(complianceId);

    res.json({
      success: true,
      message: 'Compliance record updated successfully',
      data: updatedCompliance
    });
  } catch (error: any) {
    console.error('Compliance update error:', error);
    res.status(500).json({
      success: false,
      error: {
        code: 'COMPLIANCE_UPDATE_FAILED',
        message: error.message || 'Failed to update compliance'
      }
    });
  }
};
