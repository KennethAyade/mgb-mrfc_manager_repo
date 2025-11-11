/**
 * Script to run database schema
 * This creates all tables, indexes, and triggers
 */

const { Client } = require('pg');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

async function runSchema() {
  if (!process.env.DATABASE_URL) {
    console.error('❌ DATABASE_URL not found!');
    process.exit(1);
  }

  console.log('📡 Connecting to database...');
  console.log(`   URL: ${process.env.DATABASE_URL.substring(0, 30)}...`);

  const client = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: process.env.NODE_ENV === 'production' ? {
      rejectUnauthorized: false
    } : false
  });

  try {
    await client.connect();
    console.log('✅ Connected to database\n');

    // Read schema file
    const schemaPath = path.join(__dirname, '..', 'database', 'schema.sql');
    const schema = fs.readFileSync(schemaPath, 'utf8');

    console.log('🔨 Running database schema...');
    await client.query(schema);
    console.log('✅ Database schema executed successfully\n');

    // Verify tables were created
    const result = await client.query(`
      SELECT table_name
      FROM information_schema.tables
      WHERE table_schema = 'public'
      AND table_type = 'BASE TABLE'
      ORDER BY table_name;
    `);

    console.log('📊 Created tables:');
    result.rows.forEach(row => {
      console.log(`   ✓ ${row.table_name}`);
    });

    console.log(`\n✅ Total tables created: ${result.rows.length}`);

  } catch (error) {
    console.error('❌ Error running schema:');
    console.error(error);
    await client.end();
    process.exit(1);
  } finally {
    try {
      await client.end();
      console.log('\n🔌 Disconnected from database');
    } catch (e) {
      // Ignore disconnect errors
    }
  }
}

runSchema();
