/**
 * Clear Compliance Analysis Cache
 * 
 * This script deletes all compliance analysis records from the database,
 * forcing the system to re-analyze PDFs on the next request.
 * 
 * Usage: npm run db:clear-compliance
 */

import sequelize from '../config/database';
import { ComplianceAnalysis } from '../models';

async function clearComplianceCache() {
  try {
    console.log('🗑️  Clearing compliance analysis cache...\n');

    // Connect to database
    await sequelize.authenticate();
    console.log('✅ Database connected');

    // Get count before deletion
    const countBefore = await ComplianceAnalysis.count();
    console.log(`📊 Found ${countBefore} cached analysis records\n`);

    if (countBefore === 0) {
      console.log('✅ Cache is already empty. Nothing to delete.\n');
      process.exit(0);
    }

    // Delete all compliance analyses
    const deleted = await ComplianceAnalysis.destroy({
      where: {},
      truncate: true
    });

    console.log(`✅ Successfully deleted ${deleted} compliance analysis records`);
    console.log('📝 Next CMVR analysis will perform fresh PDF scanning\n');

    process.exit(0);
  } catch (error: any) {
    console.error('❌ Error clearing compliance cache:', error.message);
    process.exit(1);
  }
}

clearComplianceCache();

