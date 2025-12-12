/**
 * Database Schema Verification Script
 * Checks that all critical columns exist in the database
 * Exits with code 0 if all present, code 1 if any missing
 */

const { Client } = require('pg');
require('dotenv').config();

// Critical columns that must exist for the app to function
const CRITICAL_COLUMNS = [
  {
    table: 'attendance',
    column: 'attendance_type',
    migration: '015_add_attendance_type_to_attendance.sql',
    description: 'Attendance type (ONSITE/ONLINE)'
  },
  {
    table: 'attendance',
    column: 'tablet_number',
    migration: '017_add_tablet_number_to_attendance.sql',
    description: 'Tablet number (1-15)'
  },
  {
    table: 'agenda_items',
    column: 'is_other_matter',
    migration: '016_add_other_matter_and_highlight_to_agenda_items.sql',
    description: 'Marks item as "Other Matter"'
  },
  {
    table: 'agenda_items',
    column: 'is_highlighted',
    migration: '016_add_other_matter_and_highlight_to_agenda_items.sql',
    description: 'Marks item as highlighted/discussed'
  }
];

async function verifySchema() {
  console.log('🏥 Database Schema Health Check');
  console.log(`📅 Timestamp: ${new Date().toISOString()}`);
  console.log('═════════════════════════════════════════════════════════════\n');

  const client = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: {
      rejectUnauthorized: false
    }
  });

  try {
    console.log('📡 Connecting to database...');
    await client.connect();
    console.log('✅ Connected to database\n');

    console.log(`🔍 Checking ${CRITICAL_COLUMNS.length} critical columns...\n`);

    const missingColumns = [];
    const existingColumns = [];

    for (const { table, column, migration, description } of CRITICAL_COLUMNS) {
      console.log(`─────────────────────────────────────────────────────────────`);
      console.log(`🔎 Checking: ${table}.${column}`);
      console.log(`   📝 Description: ${description}`);
      console.log(`   📦 Migration file: ${migration}`);

      const query = `
        SELECT EXISTS (
          SELECT 1
          FROM information_schema.columns
          WHERE table_name = $1
          AND column_name = $2
        ) as column_exists;
      `;

      const result = await client.query(query, [table, column]);
      const exists = result.rows[0].column_exists;

      if (exists) {
        console.log(`   ✅ Status: EXISTS\n`);
        existingColumns.push({ table, column });
      } else {
        console.log(`   ❌ Status: MISSING\n`);
        missingColumns.push({ table, column, migration, description });
      }
    }

    console.log('═════════════════════════════════════════════════════════════');
    console.log('📊 HEALTH CHECK RESULTS');
    console.log('═════════════════════════════════════════════════════════════');
    console.log(`✅ Existing columns: ${existingColumns.length}/${CRITICAL_COLUMNS.length}`);
    if (existingColumns.length > 0) {
      existingColumns.forEach(({ table, column }) => {
        console.log(`   ✓ ${table}.${column}`);
      });
    }

    console.log(`\n❌ Missing columns: ${missingColumns.length}/${CRITICAL_COLUMNS.length}`);
    if (missingColumns.length > 0) {
      missingColumns.forEach(({ table, column, migration }) => {
        console.log(`   ✗ ${table}.${column} (from ${migration})`);
      });
    }
    console.log('═════════════════════════════════════════════════════════════\n');

    if (missingColumns.length > 0) {
      console.error('❌ HEALTH CHECK FAILED');
      console.error('The following critical columns are missing from the database:\n');

      missingColumns.forEach(({ table, column, migration, description }) => {
        console.error(`  ⚠️  ${table}.${column}`);
        console.error(`      Description: ${description}`);
        console.error(`      Required by: ${migration}\n`);
      });

      console.error('🔧 How to fix:');
      console.error('  1. Run: node scripts/migrate.js');
      console.error('  2. Check Railway logs for migration errors');
      console.error('  3. Verify migrations 015, 016, 017 executed successfully\n');

      console.error('⚠️  Server should NOT start without these columns.');
      console.error('    Missing columns will cause 500 errors in production.\n');

      process.exit(1);
    } else {
      console.log('✅ HEALTH CHECK PASSED');
      console.log('🎉 All critical columns exist in the database!');
      console.log('📍 Database schema is up to date and ready for production.\n');
      process.exit(0);
    }

  } catch (error) {
    console.error('═════════════════════════════════════════════════════════════');
    console.error('❌ HEALTH CHECK ERROR');
    console.error('═════════════════════════════════════════════════════════════');
    console.error(`💬 Error message: ${error.message}`);
    if (error.stack) {
      console.error(`📚 Stack trace:\n${error.stack}`);
    }
    console.error('═════════════════════════════════════════════════════════════\n');
    process.exit(1);
  } finally {
    await client.end();
    console.log('🔌 Disconnected from database');
    console.log(`📅 Finished at: ${new Date().toISOString()}\n`);
  }
}

verifySchema();
