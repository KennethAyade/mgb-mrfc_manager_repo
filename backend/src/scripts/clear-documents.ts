/**
 * Clear all documents from database
 * Run: npm run ts-node src/scripts/clear-documents.ts
 */

import sequelize from '../config/database';
import { Document } from '../models';

async function clearDocuments() {
  try {
    console.log('🔄 Connecting to database...');
    await sequelize.authenticate();
    console.log('✅ Connected to database');

    console.log('📊 Checking current document count...');
    const countBefore = await Document.count();
    console.log(`   Found ${countBefore} documents`);

    if (countBefore === 0) {
      console.log('✅ No documents to delete');
      process.exit(0);
    }

    console.log('🗑️  Deleting all documents...');
    const deleted = await Document.destroy({
      where: {},
      force: true // Hard delete (not soft delete)
    });

    console.log(`✅ Deleted ${deleted} documents`);

    const countAfter = await Document.count();
    console.log(`📊 Documents remaining: ${countAfter}`);

    console.log('✅ Done! You can now re-upload files with proper public access.');

    process.exit(0);
  } catch (error) {
    console.error('❌ Error clearing documents:', error);
    process.exit(1);
  }
}

clearDocuments();
