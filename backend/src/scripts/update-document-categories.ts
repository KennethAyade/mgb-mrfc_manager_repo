/**
 * ================================================
 * UPDATE DOCUMENT CATEGORIES ENUM
 * ================================================
 * Adds new document categories to the PostgreSQL enum
 * Run: npx ts-node src/scripts/update-document-categories.ts
 */

import sequelize from '../config/database';

const updateDocumentCategories = async (): Promise<void> => {
  try {
    console.log('🔧 Starting document categories enum update...');

    // Connect to database
    await sequelize.authenticate();
    console.log('✅ Database connected');

    // Get current enum values
    const [currentValues] = await sequelize.query(`
      SELECT enumlabel 
      FROM pg_enum 
      WHERE enumtypid = (
        SELECT oid 
        FROM pg_type 
        WHERE typname = 'enum_documents_category'
      );
    `);

    console.log('\n📋 Current enum values:');
    console.log(currentValues);

    // New categories to add
    const newCategories = ['NTE_DISBURSEMENT', 'OMVR', 'RESEARCH_ACCOMPLISHMENTS'];
    
    console.log('\n➕ Adding new categories...');
    
    for (const category of newCategories) {
      try {
        // Check if category already exists
        const exists = (currentValues as any[]).some(
          (row: any) => row.enumlabel === category
        );
        
        if (exists) {
          console.log(`⏭️  ${category} already exists`);
          continue;
        }

        // Add new enum value
        await sequelize.query(`
          ALTER TYPE enum_documents_category ADD VALUE IF NOT EXISTS '${category}';
        `);
        console.log(`✅ Added ${category}`);
      } catch (error: any) {
        // Ignore if value already exists
        if (error.message.includes('already exists')) {
          console.log(`⏭️  ${category} already exists`);
        } else {
          throw error;
        }
      }
    }

    // Verify final enum values
    const [finalValues] = await sequelize.query(`
      SELECT enumlabel 
      FROM pg_enum 
      WHERE enumtypid = (
        SELECT oid 
        FROM pg_type 
        WHERE typname = 'enum_documents_category'
      )
      ORDER BY enumsortorder;
    `);

    console.log('\n📊 Final enum values:');
    console.log(finalValues);

    console.log('\n✅ Document categories enum updated successfully!');
    process.exit(0);
  } catch (error) {
    console.error('❌ Error updating document categories:', error);
    process.exit(1);
  }
};

// Run the update
updateDocumentCategories();

