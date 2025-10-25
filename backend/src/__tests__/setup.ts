/**
 * ================================================
 * TEST SETUP UTILITIES
 * ================================================
 * Database setup, teardown, and cleanup utilities for tests
 */

import sequelize from '../config/database';
import { User } from '../models';
import { UserRole } from '../models/User';
import { hashPassword } from '../utils/password';

/**
 * Initialize test database connection
 */
export const setupTestDatabase = async (): Promise<void> => {
  try {
    await sequelize.authenticate();
    console.log('✅ Test database connection established');
  } catch (error) {
    console.error('❌ Unable to connect to test database:', error);
    throw error;
  }
};

/**
 * Clean up test database after tests
 */
export const teardownTestDatabase = async (): Promise<void> => {
  try {
    await sequelize.close();
    console.log('✅ Test database connection closed');
  } catch (error) {
    console.error('❌ Error closing test database:', error);
    throw error;
  }
};

/**
 * Create test users for authentication
 */
export const createTestUsers = async () => {
  // Try to find existing test users first
  let existingSuperAdmin = await User.findOne({ where: { username: 'testsuperadmin' } });
  let existingAdmin = await User.findOne({ where: { username: 'testadmin' } });
  let existingUser = await User.findOne({ where: { username: 'testuser' } });

  // If they exist but are inactive, reactivate them
  if (existingSuperAdmin) {
    await existingSuperAdmin.update({ is_active: true });
  }
  if (existingAdmin) {
    await existingAdmin.update({ is_active: true });
  }
  if (existingUser) {
    await existingUser.update({ is_active: true });
  }

  // If they all exist and are now active, return them
  if (existingSuperAdmin && existingAdmin && existingUser) {
    return {
      superAdmin: existingSuperAdmin,
      admin: existingAdmin,
      user: existingUser
    };
  }

  // Delete existing test users if any (in case of partial data)
  await User.destroy({
    where: {
      username: ['testsuperadmin', 'testadmin', 'testuser']
    },
    force: true
  }).catch(() => {});

  // Create super admin
  const superAdminPassword = await hashPassword('SuperAdmin123!');
  const superAdmin = await User.create({
    username: 'testsuperadmin',
    password_hash: superAdminPassword,
    full_name: 'Test Super Admin',
    email: 'superadmin@test.com',
    role: UserRole.SUPER_ADMIN,
    is_active: true,
    email_verified: true
  });

  // Create admin
  const adminPassword = await hashPassword('Admin123!');
  const admin = await User.create({
    username: 'testadmin',
    password_hash: adminPassword,
    full_name: 'Test Admin',
    email: 'admin@test.com',
    role: UserRole.ADMIN,
    is_active: true,
    email_verified: true
  });

  // Create regular user
  const userPassword = await hashPassword('User123!');
  const user = await User.create({
    username: 'testuser',
    password_hash: userPassword,
    full_name: 'Test User',
    email: 'user@test.com',
    role: UserRole.USER,
    is_active: true,
    email_verified: true
  });

  return { superAdmin, admin, user };
};

/**
 * Clean up test data
 */
export const cleanupTestData = async (): Promise<void> => {
  try {
    // Don't delete test users as they may be referenced by audit logs
    // Just deactivate them or skip cleanup
    console.log('✅ Test cleanup completed (users kept for audit trail)');
  } catch (error) {
    console.error('Error cleaning up test data:', error);
  }
};

