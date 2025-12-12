/**
 * Script to run database migrations
 * This runs all SQL migration files in order
 * Enhanced with robust logging and error tracking
 */

const { Client } = require('pg');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

async function runMigrations() {
  console.log('🔄 Starting database migration process...');
  console.log(`📅 Timestamp: ${new Date().toISOString()}`);
  console.log(`🌍 Environment: ${process.env.NODE_ENV || 'development'}\n`);

  const client = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: {
      rejectUnauthorized: false
    }
  });

  // Track migration results
  const results = {
    applied: [],
    skipped: [],
    failed: []
  };

  try {
    console.log('📡 Connecting to database...');
    console.log(`🔗 Database URL: ${process.env.DATABASE_URL ? process.env.DATABASE_URL.replace(/:[^:@]+@/, ':****@') : 'NOT SET'}`);
    await client.connect();
    console.log('✅ Connected to database successfully\n');

    // Get all migration files
    const migrationsDir = path.join(__dirname, '..', 'database', 'migrations');
    console.log(`📁 Migrations directory: ${migrationsDir}`);

    if (!fs.existsSync(migrationsDir)) {
      console.error('❌ No migrations directory found');
      console.error(`   Expected path: ${migrationsDir}`);
      return;
    }

    const files = fs.readdirSync(migrationsDir)
      .filter(f => f.endsWith('.sql'))
      .sort(); // Sort to run in order

    if (files.length === 0) {
      console.log('⚠️  No migration files found');
      return;
    }

    console.log(`📦 Found ${files.length} migration file(s):`);
    files.forEach((file, idx) => {
      console.log(`   ${idx + 1}. ${file}`);
    });
    console.log('');

    // Run each migration in its own transaction
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      const migrationNum = i + 1;

      console.log(`─────────────────────────────────────────────────────────────`);
      console.log(`🔨 [${migrationNum}/${files.length}] Running migration: ${file}`);

      const migrationPath = path.join(migrationsDir, file);
      const migration = fs.readFileSync(migrationPath, 'utf8');
      const migrationLines = migration.split('\n').length;
      console.log(`   📄 File size: ${migration.length} bytes (${migrationLines} lines)`);

      try {
        const startTime = Date.now();

        await client.query('BEGIN');
        console.log(`   🔓 Transaction started`);

        await client.query(migration);
        console.log(`   ⚙️  Migration SQL executed`);

        await client.query('COMMIT');
        const duration = Date.now() - startTime;

        console.log(`   ✅ Transaction committed (${duration}ms)`);
        console.log(`✅ [${migrationNum}/${files.length}] COMPLETED: ${file}\n`);
        results.applied.push({ file, duration });

      } catch (error) {
        try {
          await client.query('ROLLBACK');
          console.log(`   ⏪ Transaction rolled back`);
        } catch (rollbackError) {
          console.error(`   ⚠️  Rollback failed:`, rollbackError.message);
        }

        // Check if this is an expected "already applied" error
        const isAlreadyApplied =
          error.message.includes('already exists') ||
          error.message.includes('duplicate') ||
          error.message.includes('does not exist') ||
          error.message.toLowerCase().includes('already exists');

        if (isAlreadyApplied) {
          console.log(`   ⚠️  Migration already applied (safe to skip)`);
          console.log(`   ℹ️  Reason: ${error.message}`);
          console.log(`⚠️  [${migrationNum}/${files.length}] SKIPPED: ${file}\n`);
          results.skipped.push({ file, reason: error.message });
        } else {
          console.error(`   ❌ CRITICAL ERROR in migration`);
          console.error(`   📝 Error type: ${error.name}`);
          console.error(`   💬 Error message: ${error.message}`);
          if (error.stack) {
            console.error(`   📚 Stack trace:\n${error.stack}`);
          }
          console.error(`❌ [${migrationNum}/${files.length}] FAILED: ${file}\n`);
          results.failed.push({ file, error: error.message });
          throw error;
        }
      }
    }

    console.log('═════════════════════════════════════════════════════════════');
    console.log('📊 MIGRATION SUMMARY');
    console.log('═════════════════════════════════════════════════════════════');
    console.log(`✅ Applied:  ${results.applied.length} migration(s)`);
    if (results.applied.length > 0) {
      results.applied.forEach(({ file, duration }) => {
        console.log(`   ✓ ${file} (${duration}ms)`);
      });
    }
    console.log(`⚠️  Skipped:  ${results.skipped.length} migration(s)`);
    if (results.skipped.length > 0) {
      results.skipped.forEach(({ file }) => {
        console.log(`   - ${file}`);
      });
    }
    console.log(`❌ Failed:   ${results.failed.length} migration(s)`);
    if (results.failed.length > 0) {
      results.failed.forEach(({ file, error }) => {
        console.log(`   ✗ ${file}: ${error}`);
      });
    }
    console.log('═════════════════════════════════════════════════════════════');

    if (results.failed.length === 0) {
      console.log('✅ All migrations completed successfully');
      console.log(`🎉 Database is up to date!\n`);
    } else {
      console.error('❌ Some migrations failed - database may be in inconsistent state');
      process.exit(1);
    }

  } catch (error) {
    console.error('═════════════════════════════════════════════════════════════');
    console.error('❌ FATAL ERROR: Migration process failed');
    console.error('═════════════════════════════════════════════════════════════');
    console.error(`💬 Error message: ${error.message}`);
    if (error.stack) {
      console.error(`📚 Stack trace:\n${error.stack}`);
    }
    console.error('═════════════════════════════════════════════════════════════');
    process.exit(1);
  } finally {
    await client.end();
    console.log('🔌 Disconnected from database');
    console.log(`📅 Finished at: ${new Date().toISOString()}\n`);
  }
}

runMigrations();
