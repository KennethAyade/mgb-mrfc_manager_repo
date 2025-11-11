/**
 * Railway Start Script
 * Ensures database is migrated and seeded before starting the server
 */

const { execSync } = require('child_process');
const path = require('path');

console.log('🚂 Railway Start Script - MGB MRFC Manager');
console.log('============================================\n');

function runCommand(command, description) {
  console.log(`📍 ${description}...`);
  try {
    execSync(command, {
      stdio: 'inherit',
      cwd: path.join(__dirname, '..')
    });
    console.log(`✅ ${description} - SUCCESS\n`);
    return true;
  } catch (error) {
    console.error(`❌ ${description} - FAILED`);
    console.error(error.message);
    return false;
  }
}

// Step 1: Check database connection
console.log('🔍 Step 1: Testing database connection...');
if (!process.env.DATABASE_URL) {
  console.error('❌ DATABASE_URL not found in environment variables!');
  console.error('Please add PostgreSQL plugin to Railway project.');
  process.exit(1);
}
console.log('✅ DATABASE_URL found\n');

// Step 2: Run main schema (create all base tables)
console.log('📍 Step 2: Creating database schema (tables, types, indexes)...');
try {
  execSync('node scripts/run-schema.js', {
    stdio: 'inherit',
    cwd: path.join(__dirname, '..')
  });
  console.log('✅ Step 2: Creating database schema - SUCCESS\n');
} catch (error) {
  // Schema creation failed - this is critical, don't continue
  console.error('❌ CRITICAL: Schema creation failed!');
  console.error('Cannot start server without database tables.');
  console.error('\nPlease check:');
  console.error('1. DATABASE_URL is correct');
  console.error('2. Database is accessible');
  console.error('3. schema.sql file exists');
  process.exit(1);
}

// Step 3: Run migrations (additional table modifications)
const migrationSuccess = runCommand(
  'node scripts/migrate.js',
  'Step 3: Running database migrations'
);

if (!migrationSuccess) {
  console.error('⚠️  Warning: Migration failed, but continuing...');
  console.error('This is normal if migrations already applied.');
}

// Step 4: Seed quarters (CRITICAL)
const seedSuccess = runCommand(
  'node scripts/seed-quarters.js',
  'Step 4: Seeding quarters (Q1-Q4 2025)'
);

if (!seedSuccess) {
  console.error('⚠️  Warning: Quarters seeding failed!');
  console.error('File upload feature may not work correctly.');
}

// Step 5: Start the server
console.log('🚀 Step 5: Starting server...');
console.log('============================================\n');

try {
  require('../dist/server.js');
} catch (error) {
  console.error('❌ Failed to start server!');
  console.error(error);
  process.exit(1);
}

