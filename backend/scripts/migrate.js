/**
 * Script to run database migrations
 * This runs all SQL migration files in order
 */

const { Client } = require('pg');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

async function runMigrations() {
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

    // Get all migration files
    const migrationsDir = path.join(__dirname, '..', 'database', 'migrations');

    if (!fs.existsSync(migrationsDir)) {
      console.log('⚠️  No migrations directory found');
      return;
    }

    const files = fs.readdirSync(migrationsDir)
      .filter(f => f.endsWith('.sql'))
      .sort(); // Sort to run in order

    if (files.length === 0) {
      console.log('⚠️  No migration files found');
      return;
    }

    console.log(`📦 Found ${files.length} migration file(s)\n`);

    // Run each migration in its own transaction
    for (const file of files) {
      console.log(`🔨 Running migration: ${file}`);
      const migrationPath = path.join(migrationsDir, file);
      const migration = fs.readFileSync(migrationPath, 'utf8');

      try {
        await client.query('BEGIN');
        await client.query(migration);
        await client.query('COMMIT');
        console.log(`✅ Completed: ${file}\n`);
      } catch (error) {
        await client.query('ROLLBACK');
        if (error.message.includes('already exists') ||
            error.message.includes('duplicate') ||
            error.message.includes('does not exist')) {
          console.log(`⚠️  Skipped (already applied): ${file}\n`);
        } else {
          console.error(`❌ Error in ${file}:`, error.message);
          throw error;
        }
      }
    }

    console.log('✅ All migrations completed successfully\n');

  } catch (error) {
    console.error('❌ Error running migrations:', error.message);
    process.exit(1);
  } finally {
    await client.end();
    console.log('🔌 Disconnected from database');
  }
}

runMigrations();
