/**
 * Run OCR Caching Migration
 * Adds extracted_text, ocr_confidence, and ocr_language columns
 */

import sequelize from '../config/database';
import { QueryInterface, DataTypes } from 'sequelize';

async function runMigration() {
  console.log('📝 Running OCR caching migration...\n');
  
  try {
    await sequelize.authenticate();
    console.log('✅ Database connected');
    
    const queryInterface: QueryInterface = sequelize.getQueryInterface();
    
    // Check if columns already exist
    const tableDescription = await queryInterface.describeTable('compliance_analyses');
    
    if (tableDescription.extracted_text) {
      console.log('⚠️  OCR caching columns already exist. Skipping migration.');
      return;
    }
    
    console.log('\n📊 Adding OCR caching columns...\n');
    
    // Add extracted_text column
    await queryInterface.addColumn('compliance_analyses', 'extracted_text', {
      type: DataTypes.TEXT,
      allowNull: true,
    });
    console.log('  ✅ Added extracted_text column');
    
    // Add ocr_confidence column
    await queryInterface.addColumn('compliance_analyses', 'ocr_confidence', {
      type: DataTypes.DECIMAL(5, 2),
      allowNull: true,
    });
    console.log('  ✅ Added ocr_confidence column');
    
    // Add ocr_language column
    await queryInterface.addColumn('compliance_analyses', 'ocr_language', {
      type: DataTypes.STRING(20),
      allowNull: true,
    });
    console.log('  ✅ Added ocr_language column');
    
    console.log('\n========================================');
    console.log('✅ OCR caching migration completed!');
    console.log('========================================\n');
    
  } catch (error: any) {
    console.error('\n❌ Migration failed:', error.message);
    throw error;
  } finally {
    await sequelize.close();
  }
}

runMigration();

