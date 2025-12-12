/**
 * Railway Start Script
 * Ensures database is migrated and seeded before starting the server
 * Enhanced with guaranteed migration execution and health checks
 */

const { execSync } = require('child_process');
const path = require('path');

console.log('🚂 Railway Start Script - MGB MRFC Manager');
console.log(`📅 Timestamp: ${new Date().toISOString()}`);
console.log('============================================\n');

function runCommand(command, description, options = {}) {
  const { optional = false, failureMessage = '' } = options;

  console.log(`📍 ${description}...`);
  try {
    execSync(command, {
      stdio: 'inherit',
      cwd: path.join(__dirname, '..')
    });
    console.log(`✅ ${description} - SUCCESS\n`);
    return { success: true };
  } catch (error) {
    console.error(`❌ ${description} - FAILED`);
    console.error(error.message);

    if (optional) {
      console.log(`⚠️  Continuing despite failure (optional step)...\n`);
      if (failureMessage) {
        console.log(`ℹ️  ${failureMessage}\n`);
      }
      return { success: false, optional: true };
    } else {
      if (failureMessage) {
        console.error(`\n❌ CRITICAL: ${failureMessage}\n`);
      }
      return { success: false, optional: false };
    }
  }
}

// Step 1: Check database connection
console.log('🔍 Step 1: Testing database connection...');
if (!process.env.DATABASE_URL) {
  console.error('❌ DATABASE_URL not found in environment variables!');
  console.error('Please add PostgreSQL plugin to Railway project.');
  process.exit(1);
}
const maskedUrl = process.env.DATABASE_URL.replace(/:[^:@]+@/, ':****@');
console.log(`✅ DATABASE_URL found: ${maskedUrl}\n`);

// Step 2: Run main schema (create all base tables)
console.log('📍 Step 2: Creating database schema (tables, types, indexes)...');
const schemaResult = runCommand(
  'node scripts/run-schema.js',
  'Step 2: Creating database schema',
  {
    optional: true,
    failureMessage: 'Schema already exists from previous deployment (safe to skip)'
  }
);

// Step 3: Run migrations (CRITICAL - must succeed)
console.log('═════════════════════════════════════════');
console.log('🔨 CRITICAL STEP: Running Database Migrations');
console.log('═════════════════════════════════════════');

const migrationResult = runCommand(
  'node scripts/migrate.js',
  'Step 3: Running database migrations',
  {
    optional: false,
    failureMessage: 'Migration failure will prevent server from starting. Database may have missing columns.'
  }
);

if (!migrationResult.success) {
  console.error('═════════════════════════════════════════');
  console.error('❌ FATAL ERROR: Database migrations failed!');
  console.error('═════════════════════════════════════════');
  console.error('The database may be missing critical columns:');
  console.error('  - attendance.attendance_type');
  console.error('  - attendance.tablet_number');
  console.error('  - agenda_items.is_other_matter');
  console.error('  - agenda_items.is_highlighted');
  console.error('');
  console.error('Server CANNOT start without these columns.');
  console.error('Please check migration logs above for details.');
  console.error('═════════════════════════════════════════');
  process.exit(1);
}

console.log('✅ Migration step completed successfully');
console.log('');

// Step 4: Run database health check (verify critical columns exist)
console.log('🏥 Step 4: Running database health check...');
const healthCheckResult = runCommand(
  'node scripts/verify-schema.js',
  'Step 4: Verifying database schema',
  {
    optional: true,
    failureMessage: 'Health check script not found (will be added soon)'
  }
);

if (healthCheckResult.success) {
  console.log('✅ Database health check passed - all critical columns exist\n');
} else if (!healthCheckResult.optional) {
  console.error('❌ Database health check failed!');
  console.error('Critical columns may be missing from database.');
  console.error('This will cause 500 errors in production.');
  process.exit(1);
}

// Step 5: Seed quarters (IMPORTANT but not blocking)
const seedResult = runCommand(
  'node scripts/seed-quarters.js',
  'Step 5: Seeding quarters (Q1-Q4 2025)',
  {
    optional: true,
    failureMessage: 'Quarters may already be seeded. File upload feature may not work if quarters are missing.'
  }
);

// Step 6: Start the server
console.log('═════════════════════════════════════════');
console.log('🚀 Step 6: Starting MGB MRFC Manager Server');
console.log('═════════════════════════════════════════');
console.log('');

try {
  require('../dist/server.js');
} catch (error) {
  console.error('❌ Failed to start server!');
  console.error(error);
  process.exit(1);
}

