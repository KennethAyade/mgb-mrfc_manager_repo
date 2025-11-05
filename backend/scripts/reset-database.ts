/**
 * ================================================
 * DATABASE RESET SCRIPT
 * ================================================
 * Drops all tables and recreates them from scratch
 * Keeps only the superadmin user from .env
 * 
 * Usage: npm run db:reset
 */

import sequelize from '../src/config/database';
import { User, Mrfc, Proponent, Agenda, Quarter, Attendance, MeetingMinutes, Note, AgendaItem, MatterArising, UserMrfcAccess, AuditLog } from '../src/models';
import { UserRole } from '../src/models/User';
import bcrypt from 'bcryptjs';

const resetDatabase = async () => {
  try {
    console.log('================================================');
    console.log('DATABASE RESET - STARTING');
    console.log('================================================\n');

    // Step 1: Connect to database
    console.log('📡 Connecting to database...');
    await sequelize.authenticate();
    console.log('✅ Database connected\n');

    // Step 2: Drop all tables (force sync)
    console.log('🗑️  Dropping all tables...');
    await sequelize.sync({ force: true });
    console.log('✅ All tables dropped and recreated\n');

    // Step 3: Create superadmin user from .env
    console.log('👤 Creating superadmin user...');
    
    const superadminUsername = process.env.SUPER_ADMIN_USERNAME || process.env.SUPERADMIN_USERNAME || 'superadmin';
    const superadminPassword = process.env.SUPER_ADMIN_PASSWORD || process.env.SUPERADMIN_PASSWORD || 'Change@Me#2025';
    const superadminEmail = process.env.SUPER_ADMIN_EMAIL || process.env.SUPERADMIN_EMAIL || 'superadmin@mgb.gov.ph';
    const superadminFullName = process.env.SUPER_ADMIN_FULL_NAME || process.env.SUPERADMIN_FULLNAME || 'Super Administrator';

    const hashedPassword = await bcrypt.hash(superadminPassword, 10);

    await User.create({
      username: superadminUsername,
      password_hash: hashedPassword,
      full_name: superadminFullName,
      email: superadminEmail,
      role: UserRole.SUPER_ADMIN,
      is_active: true,
      email_verified: true
    });

    console.log('✅ Superadmin user created');
    console.log(`   Username: ${superadminUsername}`);
    console.log(`   Password: ${superadminPassword}`);
    console.log(`   Email: ${superadminEmail}\n`);

    // Step 4: Summary
    console.log('================================================');
    console.log('DATABASE RESET - COMPLETED');
    console.log('================================================');
    console.log('✅ All data cleared');
    console.log('✅ Fresh tables created');
    console.log('✅ Superadmin user ready');
    console.log('\n📝 You can now login with superadmin credentials');
    console.log('================================================\n');

    process.exit(0);
  } catch (error) {
    console.error('❌ Database reset failed:', error);
    process.exit(1);
  }
};

// Run the reset
resetDatabase();

