/**
 * ================================================
 * MGB MRFC MANAGER - MAIN SERVER FILE
 * ================================================
 * Express.js server entry point
 * Initializes database, middleware, routes, and starts server
 */

import express, { Application, Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import morgan from 'morgan';
import dotenv from 'dotenv';
import rateLimit from 'express-rate-limit';
import { testConnection } from './config/database';
import routes from './routes';

// Load environment variables
dotenv.config();

const app: Application = express();
const PORT = parseInt(process.env.PORT || '3000', 10);
const API_VERSION = process.env.API_VERSION || 'v1';

/**
 * ===========================================
 * SECURITY MIDDLEWARE
 * ===========================================
 */

// Helmet: Sets various HTTP headers for security
app.use(helmet());

// CORS: Allow cross-origin requests from specified origins
const corsOptions = {
  origin: process.env.CORS_ORIGINS?.split(',') || ['http://localhost:3000', 'http://localhost:5173'],
  credentials: true,
  optionsSuccessStatus: 200
};
app.use(cors(corsOptions));

// Rate limiting: Prevent brute force attacks
const limiter = rateLimit({
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '900000'), // 15 minutes
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '100'), // Max 100 requests per window
  message: {
    success: false,
    error: {
      code: 'RATE_LIMIT_EXCEEDED',
      message: 'Too many requests, please try again later'
    }
  },
  standardHeaders: true,
  legacyHeaders: false
});
app.use('/api', limiter);

/**
 * ===========================================
 * GENERAL MIDDLEWARE
 * ===========================================
 */

// Body parsing
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Response compression
app.use(compression());

// Logging (only in development)
if (process.env.NODE_ENV === 'development') {
  app.use(morgan('dev'));
}

/**
 * ===========================================
 * ROUTES
 * ===========================================
 */

// Root endpoint
app.get('/', (_req: Request, res: Response) => {
  res.json({
    success: true,
    message: 'MGB MRFC Manager API',
    version: '1.0.0',
    documentation: '/api-docs',
    health: '/api/v1/health'
  });
});

// API routes
app.use(`/api/${API_VERSION}`, routes);

// 404 handler
app.use((req: Request, res: Response) => {
  res.status(404).json({
    success: false,
    error: {
      code: 'NOT_FOUND',
      message: `Route ${req.method} ${req.path} not found`
    }
  });
});

/**
 * ===========================================
 * ERROR HANDLING MIDDLEWARE
 * ===========================================
 */

// Global error handler
app.use((err: any, _req: Request, res: Response, _next: NextFunction) => {
  console.error('Global error handler:', err);

  // Sequelize validation errors
  if (err.name === 'SequelizeValidationError') {
    return res.status(400).json({
      success: false,
      error: {
        code: 'VALIDATION_ERROR',
        message: 'Database validation failed',
        details: err.errors.map((e: any) => ({
          field: e.path,
          message: e.message
        }))
      }
    });
  }

  // Sequelize unique constraint errors
  if (err.name === 'SequelizeUniqueConstraintError') {
    return res.status(409).json({
      success: false,
      error: {
        code: 'DUPLICATE_ENTRY',
        message: 'Record already exists',
        details: err.errors.map((e: any) => ({
          field: e.path,
          message: `${e.path} already exists`
        }))
      }
    });
  }

  // Default error response
  return res.status(err.status || 500).json({
    success: false,
    error: {
      code: err.code || 'INTERNAL_SERVER_ERROR',
      message: err.message || 'An unexpected error occurred'
    }
  });
});

/**
 * ===========================================
 * DATABASE INITIALIZATION
 * ===========================================
 */

/**
 * Initialize database connection and create super admin if not exists
 */
const initializeDatabase = async (): Promise<void> => {
  try {
    // Test database connection
    await testConnection();

    // Import models and utilities
    const { User } = await import('./models');
    const { UserRole } = await import('./models/User');
    const { hashPassword } = await import('./utils/password');

    // Create super admin user if not exists
    const superAdminUsername = process.env.SUPER_ADMIN_USERNAME || 'superadmin';
    const existingSuperAdmin = await User.findOne({ where: { username: superAdminUsername } });
    if (!existingSuperAdmin) {
      const password_hash = await hashPassword(process.env.SUPER_ADMIN_PASSWORD || 'Change@Me#2025');
      await User.create({
        username: superAdminUsername,
        password_hash,
        full_name: process.env.SUPER_ADMIN_FULL_NAME || 'System Administrator',
        email: process.env.SUPER_ADMIN_EMAIL || 'admin@mgb.gov.ph',
        role: UserRole.SUPER_ADMIN,
        is_active: true,
        email_verified: true
      });
      console.log('✅ Super admin user created');
    }

    console.log('✅ Database initialized successfully');
  } catch (error) {
    console.error('❌ Database initialization failed:', error);
    throw error;
  }
};

/**
 * ===========================================
 * START SERVER
 * ===========================================
 */

const startServer = async (): Promise<void> => {
  try {
    // Initialize database
    await initializeDatabase();

    // Start listening on all network interfaces (0.0.0.0)
    // This allows access from Android emulator (10.0.2.2)
    app.listen(PORT, '0.0.0.0', () => {
      console.log('\n================================================');
      console.log('🚀 MGB MRFC MANAGER API SERVER');
      console.log('================================================');
      console.log(`Environment: ${process.env.NODE_ENV || 'development'}`);
      console.log(`Server running on: http://0.0.0.0:${PORT}`);
      console.log(`Local access: http://localhost:${PORT}`);
      console.log(`Emulator access: http://10.0.2.2:${PORT}`);
      console.log(`API Base URL: http://localhost:${PORT}/api/${API_VERSION}`);
      console.log(`Health Check: http://localhost:${PORT}/api/${API_VERSION}/health`);
      console.log('================================================\n');
    });
  } catch (error) {
    console.error('❌ Failed to start server:', error);
    process.exit(1);
  }
};

// Start the server only if not in test mode
if (process.env.NODE_ENV !== 'test') {
  startServer();

  // Handle uncaught exceptions
  process.on('uncaughtException', (error) => {
    console.error('Uncaught Exception:', error);
    process.exit(1);
  });

  // Handle unhandled promise rejections
  process.on('unhandledRejection', (reason, promise) => {
    console.error('Unhandled Rejection at:', promise, 'reason:', reason);
    process.exit(1);
  });
}

export default app;
export { initializeDatabase };
