/**
 * Reset super admin password
 */

const { Client } = require('pg');
const bcrypt = require('bcryptjs');
require('dotenv').config();

async function resetSuperAdmin() {
  const client = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: {
      rejectUnauthorized: false
    }
  });

  try {
    await client.connect();
    console.log('✅ Connected to database\n');

    // Hardcode password since env var has issues with special chars
    const password = 'Change@Me#2025';
    console.log('Hashing password:', password);

    const password_hash = await bcrypt.hash(password, 10);
    console.log('New hash:', password_hash);

    // Update super admin password
    await client.query(
      'UPDATE users SET password_hash = $1 WHERE username = $2',
      [password_hash, 'superadmin']
    );

    console.log('\n✅ Super admin password reset successfully');

    // Verify
    const result = await client.query('SELECT username, role FROM users WHERE username = $1', ['superadmin']);
    console.log('User:', result.rows[0]);

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await client.end();
  }
}

resetSuperAdmin();
