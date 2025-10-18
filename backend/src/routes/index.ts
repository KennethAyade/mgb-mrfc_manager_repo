/**
 * ================================================
 * MGB MRFC MANAGER - API ROUTES INDEX
 * ================================================
 * Central router that combines all route modules
 * Base path: /api/v1
 *
 * ROUTE STRUCTURE:
 * /api/v1/auth/*           - Authentication endpoints
 * /api/v1/users/*          - User management
 * /api/v1/mrfcs/*          - MRFC management
 * /api/v1/proponents/*     - Proponent management
 * /api/v1/quarters/*       - Quarter management
 * /api/v1/agendas/*        - Agenda management
 * /api/v1/documents/*      - Document management
 * /api/v1/attendance/*     - Attendance tracking
 * /api/v1/compliance/*     - Compliance dashboard
 * /api/v1/notes/*          - Personal notes
 * /api/v1/notifications/*  - Notifications
 * /api/v1/audit-logs/*     - Audit logs
 * /api/v1/statistics/*     - Statistics & analytics
 */

import { Router } from 'express';
import authRoutes from './auth.routes';
import userRoutes from './user.routes';
import mrfcRoutes from './mrfc.routes';
import proponentRoutes from './proponent.routes';
import quarterRoutes from './quarter.routes';
import agendaRoutes from './agenda.routes';
import documentRoutes from './document.routes';
import attendanceRoutes from './attendance.routes';
import complianceRoutes from './compliance.routes';
import noteRoutes from './note.routes';
import notificationRoutes from './notification.routes';
import auditLogRoutes from './auditLog.routes';
import statisticsRoutes from './statistics.routes';

const router = Router();

// Mount all route modules
router.use('/auth', authRoutes);
router.use('/users', userRoutes);
router.use('/mrfcs', mrfcRoutes);
router.use('/proponents', proponentRoutes);
router.use('/quarters', quarterRoutes);
router.use('/agendas', agendaRoutes);
router.use('/documents', documentRoutes);
router.use('/attendance', attendanceRoutes);
router.use('/compliance', complianceRoutes);
router.use('/notes', noteRoutes);
router.use('/notifications', notificationRoutes);
router.use('/audit-logs', auditLogRoutes);
router.use('/statistics', statisticsRoutes);

// Health check endpoint
router.get('/health', (req, res) => {
  res.json({
    success: true,
    message: 'MGB MRFC Manager API is running',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

export default router;
