/**
 * AUDIT LOG MIDDLEWARE
 * =====================
 * Automatically logs all CREATE, UPDATE, DELETE operations
 * Required for government accountability and compliance
 *
 * CRITICAL: All database changes must be audited
 * This middleware captures:
 * - Who made the change (user_id)
 * - What was changed (entity_type, entity_id)
 * - When it was changed (created_at)
 * - What changed (old_values, new_values)
 * - Where from (ip_address, user_agent)
 */

import { Request, Response, NextFunction } from 'express';

/**
 * Audit log data structure
 * This will be saved to the audit_logs table
 */
interface AuditLogEntry {
  userId: number | null;
  entityType: string;
  entityId: number | null;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  oldValues: any;
  newValues: any;
  ipAddress: string;
  userAgent: string;
}

/**
 * Store audit logs in memory until database models are ready
 * In production, this will write directly to database
 */
const auditLogsQueue: AuditLogEntry[] = [];

/**
 * Audit logging middleware
 * Wraps response.json() to capture successful operations
 *
 * HOW TO USE:
 * Apply this middleware AFTER authentication middleware
 * It will automatically log any successful CREATE/UPDATE/DELETE operations
 *
 * @example
 * router.post('/mrfcs', authenticate, auditLog, createMrfc);
 * router.put('/mrfcs/:id', authenticate, auditLog, updateMrfc);
 * router.delete('/mrfcs/:id', authenticate, auditLog, deleteMrfc);
 *
 * IMPLEMENTATION NOTES FOR LATER:
 * 1. In your controller, attach audit data to res.locals BEFORE sending response:
 *    res.locals.auditData = {
 *      entityType: 'mrfcs',
 *      entityId: mrfc.id,
 *      action: 'CREATE',
 *      oldValues: null,
 *      newValues: mrfc.toJSON()
 *    };
 *    return res.status(201).json({ success: true, data: mrfc });
 *
 * 2. This middleware will intercept the response and save audit log
 *
 * @param req Express request
 * @param res Express response
 * @param next Express next function
 */
export const auditLog = (req: Request, res: Response, next: NextFunction): void => {
  // Store original json function
  const originalJson = res.json.bind(res);

  // Override res.json to capture audit data
  res.json = function (data: any) {
    // Only log successful operations (2xx status codes)
    if (res.statusCode >= 200 && res.statusCode < 300) {
      // Check if controller attached audit data
      if (res.locals.auditData) {
        const auditEntry: AuditLogEntry = {
          userId: req.user?.userId || null,
          entityType: res.locals.auditData.entityType,
          entityId: res.locals.auditData.entityId,
          action: res.locals.auditData.action,
          oldValues: res.locals.auditData.oldValues,
          newValues: res.locals.auditData.newValues,
          ipAddress: getClientIp(req),
          userAgent: req.headers['user-agent'] || 'Unknown'
        };

        // TODO: When implementing, replace this with actual database insert
        // await AuditLog.create(auditEntry);
        auditLogsQueue.push(auditEntry);
        console.log('📝 Audit log created:', {
          action: auditEntry.action,
          entity: auditEntry.entityType,
          id: auditEntry.entityId,
          user: auditEntry.userId
        });
      }
    }

    // Call original json function
    return originalJson(data);
  };

  next();
};

/**
 * Helper function to extract client IP address
 * Handles proxy and load balancer scenarios
 *
 * @param req Express request
 * @returns Client IP address
 */
const getClientIp = (req: Request): string => {
  // Check for proxied IP (behind load balancer)
  const forwarded = req.headers['x-forwarded-for'];
  if (forwarded) {
    return typeof forwarded === 'string'
      ? forwarded.split(',')[0].trim()
      : forwarded[0];
  }

  // Check for real IP (some proxies use this)
  const realIp = req.headers['x-real-ip'];
  if (realIp) {
    return typeof realIp === 'string' ? realIp : realIp[0];
  }

  // Fall back to direct connection IP
  return req.socket.remoteAddress || 'Unknown';
};

/**
 * Helper function to create audit log entry
 * Use this in your controllers before sending response
 *
 * @example
 * // In controller after creating a record:
 * setAuditData(res, {
 *   entityType: 'proponents',
 *   entityId: newProponent.id,
 *   action: 'CREATE',
 *   oldValues: null,
 *   newValues: newProponent.toJSON()
 * });
 *
 * @example
 * // In controller after updating a record:
 * setAuditData(res, {
 *   entityType: 'documents',
 *   entityId: document.id,
 *   action: 'UPDATE',
 *   oldValues: { status: 'PENDING' },
 *   newValues: { status: 'ACCEPTED' }
 * });
 *
 * @example
 * // In controller after deleting a record:
 * setAuditData(res, {
 *   entityType: 'matters_arising',
 *   entityId: matterId,
 *   action: 'DELETE',
 *   oldValues: matter.toJSON(),
 *   newValues: null
 * });
 */
export const setAuditData = (
  res: Response,
  data: {
    entityType: string;
    entityId: number | null;
    action: 'CREATE' | 'UPDATE' | 'DELETE';
    oldValues: any;
    newValues: any;
  }
): void => {
  res.locals.auditData = data;
};

/**
 * Compliance override audit logger
 * Special audit log for when admins override compliance percentages
 * This is CRITICAL and must always be logged with justification
 *
 * @example
 * // In compliance controller:
 * await logComplianceOverride(req, {
 *   proponentId: 5,
 *   quarterId: 4,
 *   originalPct: 75.0,
 *   overridePct: 80.0,
 *   justification: 'Waived CMVR due to operational suspension'
 * });
 */
export const logComplianceOverride = async (
  req: Request,
  data: {
    proponentId: number;
    quarterId: number;
    originalPct: number;
    overridePct: number;
    justification: string;
  }
): Promise<void> => {
  const auditEntry: AuditLogEntry = {
    userId: req.user?.userId || null,
    entityType: 'compliance_override',
    entityId: data.proponentId,
    action: 'UPDATE',
    oldValues: {
      proponent_id: data.proponentId,
      quarter_id: data.quarterId,
      compliance_pct: data.originalPct
    },
    newValues: {
      proponent_id: data.proponentId,
      quarter_id: data.quarterId,
      compliance_pct: data.overridePct,
      override_justification: data.justification,
      override_by: req.user?.userId,
      override_at: new Date()
    },
    ipAddress: getClientIp(req),
    userAgent: req.headers['user-agent'] || 'Unknown'
  };

  // TODO: When implementing, save to audit_logs table
  auditLogsQueue.push(auditEntry);

  console.log('⚠️  COMPLIANCE OVERRIDE LOGGED:', {
    proponent: data.proponentId,
    quarter: data.quarterId,
    original: data.originalPct,
    override: data.overridePct,
    user: req.user?.userId,
    justification: data.justification
  });
};

/**
 * Get queued audit logs (for testing/debugging)
 * In production, this will query the database
 */
export const getAuditLogsQueue = (): AuditLogEntry[] => {
  return auditLogsQueue;
};

/**
 * Clear audit logs queue (for testing)
 */
export const clearAuditLogsQueue = (): void => {
  auditLogsQueue.length = 0;
};
