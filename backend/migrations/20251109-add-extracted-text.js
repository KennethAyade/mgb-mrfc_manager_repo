'use strict';

/**
 * ================================================
 * MIGRATION: Add OCR Extracted Text Caching
 * ================================================
 * Adds columns to cache OCR results to avoid re-processing
 * - extracted_text: Full text extracted via OCR
 * - ocr_confidence: OCR confidence score (0-100)
 * - ocr_language: Languages used for OCR (e.g., 'eng+fil')
 */

module.exports = {
  up: async (queryInterface, Sequelize) => {
    console.log('📝 Adding OCR caching columns to compliance_analyses table...');
    
    await queryInterface.addColumn('compliance_analyses', 'extracted_text', {
      type: Sequelize.TEXT,
      allowNull: true,
      comment: 'Full text extracted from PDF via OCR or text extraction'
    });
    
    await queryInterface.addColumn('compliance_analyses', 'ocr_confidence', {
      type: Sequelize.DECIMAL(5, 2),
      allowNull: true,
      comment: 'OCR confidence score (0-100)'
    });
    
    await queryInterface.addColumn('compliance_analyses', 'ocr_language', {
      type: Sequelize.STRING(20),
      allowNull: true,
      comment: 'Languages used for OCR (e.g., eng+fil)'
    });
    
    console.log('✅ OCR caching columns added successfully');
  },

  down: async (queryInterface, Sequelize) => {
    console.log('🗑️  Removing OCR caching columns from compliance_analyses table...');
    
    await queryInterface.removeColumn('compliance_analyses', 'extracted_text');
    await queryInterface.removeColumn('compliance_analyses', 'ocr_confidence');
    await queryInterface.removeColumn('compliance_analyses', 'ocr_language');
    
    console.log('✅ OCR caching columns removed successfully');
  }
};

