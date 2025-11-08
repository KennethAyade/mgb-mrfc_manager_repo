/**
 * ================================================
 * FIX CLOUDINARY ACCESS MODE
 * ================================================
 * This script deletes all existing documents from Cloudinary
 * and clears document records from the database so they can be
 * re-uploaded with proper public access.
 * 
 * Run: npx ts-node src/scripts/fix-cloudinary-access.ts
 */

import sequelize from '../config/database';
import { deleteFromCloudinary } from '../config/cloudinary';
import { Document } from '../models/Document';

const fixCloudinaryAccess = async (): Promise<void> => {
  try {
    console.log('🔧 Starting Cloudinary access fix...');

    // Connect to database
    await sequelize.authenticate();
    console.log('✅ Database connected');

    // Get all documents
    const documents = await Document.findAll();
    console.log(`\n📋 Found ${documents.length} documents to process`);

    if (documents.length === 0) {
      console.log('ℹ️  No documents to process');
      process.exit(0);
    }

    let successCount = 0;
    let errorCount = 0;

    // Delete each document from Cloudinary and database
    for (const doc of documents) {
      try {
        console.log(`\n🗑️  Processing: ${doc.fileName}`);
        
        // Delete from Cloudinary if publicId exists
        if (doc.fileCloudinaryId) {
          try {
            // For PDFs, use 'raw' resource type
            await deleteFromCloudinary(doc.fileCloudinaryId, 'raw');
            console.log(`  ✅ Deleted from Cloudinary: ${doc.fileCloudinaryId}`);
          } catch (cloudinaryError: any) {
            console.log(`  ⚠️  Cloudinary deletion failed (file may not exist): ${cloudinaryError.message}`);
          }
        }

        // Delete from database
        await doc.destroy();
        console.log(`  ✅ Deleted from database: ${doc.id}`);
        
        successCount++;
      } catch (error: any) {
        console.error(`  ❌ Error processing document ${doc.id}:`, error.message);
        errorCount++;
      }
    }

    console.log('\n' + '='.repeat(50));
    console.log('📊 SUMMARY:');
    console.log(`  ✅ Successfully processed: ${successCount}`);
    console.log(`  ❌ Errors: ${errorCount}`);
    console.log('='.repeat(50));

    console.log('\n✅ All documents cleared!');
    console.log('📝 Users can now re-upload documents with proper public access.');
    
    process.exit(0);
  } catch (error: any) {
    console.error('❌ Error fixing Cloudinary access:', error);
    process.exit(1);
  }
};

// Run the fix
fixCloudinaryAccess();

