/**
 * Check if super admin user exists
 */

const { Client } = require('pg');
require('dotenv').config();

async function checkUser() {
  const client = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: {
      rejectUnauthorized: false
    }
  });

  try {
    await client.connect();
    console.log('✅ Connected to database\n');

    const result = await client.query('SELECT * FROM users WHERE username = $1', ['superadmin']);

    if (result.rows.length > 0) {
      console.log('✅ Super admin user found:');
      console.log(JSON.stringify(result.rows[0], null, 2));
    } else {
      console.log('❌ Super admin user not found');
    }

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await client.end();
  }
}

checkUser();
