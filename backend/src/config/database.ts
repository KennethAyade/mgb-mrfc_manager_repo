/**
 * DATABASE CONFIGURATION
 * ======================
 * Sequelize ORM configuration for PostgreSQL connection
 * Supports both Supabase and Neon database providers
 */

import { Sequelize } from 'sequelize';
import dotenv from 'dotenv';

dotenv.config();

/**
 * Database connection configuration
 * Uses DATABASE_URL if provided, otherwise constructs from individual env vars
 */
const sequelize = process.env.DATABASE_URL
  ? new Sequelize(process.env.DATABASE_URL, {
      dialect: 'postgres',
      dialectOptions: {
        ssl: {
          require: true,
          rejectUnauthorized: false
        }
      },
      logging: process.env.NODE_ENV === 'development' ? console.log : false,
      pool: {
        max: 5,
        min: 0,
        acquire: 30000,
        idle: 10000
      }
    })
  : new Sequelize({
      database: process.env.DB_NAME || 'mgb_mrfc',
      username: process.env.DB_USER || 'postgres',
      password: process.env.DB_PASSWORD || '',
      host: process.env.DB_HOST || 'localhost',
      port: parseInt(process.env.DB_PORT || '5432'),
      dialect: 'postgres',
      dialectOptions: {
        ssl: process.env.NODE_ENV === 'production' ? {
          require: true,
          rejectUnauthorized: false
        } : false
      },
      logging: process.env.NODE_ENV === 'development' ? console.log : false,
      pool: {
        max: 5,
        min: 0,
        acquire: 30000,
        idle: 10000
      }
    });

/**
 * Test database connection
 * @returns Promise that resolves if connection successful
 */
export const testConnection = async (): Promise<void> => {
  try {
    await sequelize.authenticate();
    console.log('✅ Database connection established successfully');
  } catch (error) {
    console.error('❌ Unable to connect to database:', error);
    throw error;
  }
};

/**
 * Sync database models
 * WARNING: Only use in development! In production, use migrations
 * @param force If true, drops all tables and recreates them
 */
export const syncDatabase = async (force: boolean = false): Promise<void> => {
  try {
    await sequelize.sync({ force });
    console.log(`✅ Database synced ${force ? '(force mode - all tables dropped)' : ''}`);
  } catch (error) {
    console.error('❌ Database sync failed:', error);
    throw error;
  }
};

export default sequelize;
