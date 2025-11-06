/**
 * Check what password is actually in the database for superadmin
 */

import { User } from '../src/models';
import sequelize from '../src/config/database';
import bcrypt from 'bcryptjs';
import dotenv from 'dotenv';

dotenv.config();

const checkPassword = async () => {
  try {
    await sequelize.authenticate();
    console.log('✅ Connected to database\n');

    const user = await User.findOne({ where: { username: 'superadmin' } });
    
    if (!user) {
      console.log('❌ Superadmin user not found!');
      process.exit(1);
    }

    console.log('='.repeat(60));
    console.log('SUPERADMIN USER FOUND');
    console.log('='.repeat(60));
    console.log(`Username: ${user.username}`);
    console.log(`Email: ${user.email}`);
    console.log(`Role: ${user.role}`);
    console.log(`Is Active: ${user.is_active}`);
    console.log(`Password Hash: ${user.password_hash.substring(0, 20)}...`);
    console.log('='.repeat(60));
    console.log('\nTESTING PASSWORDS:\n');

    // Test various password combinations
    const passwordsToTest = [
      'Change@Me#2025',
      'Change@Me',
      'admin123',
      'Admin123',
      process.env.SUPER_ADMIN_PASSWORD,
      process.env.SUPERADMIN_PASSWORD,
    ];

    for (const password of passwordsToTest) {
      if (!password) continue;
      
      const isMatch = await bcrypt.compare(password, user.password_hash);
      const icon = isMatch ? '✅' : '❌';
      const label = isMatch ? 'CORRECT' : 'wrong';
      console.log(`${icon} "${password}" → ${label}`);
      
      if (isMatch) {
        console.log('\n' + '='.repeat(60));
        console.log(`🎉 WORKING PASSWORD FOUND: "${password}"`);
        console.log('='.repeat(60));
      }
    }

    console.log('\n');
    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error);
    process.exit(1);
  }
};

checkPassword();



