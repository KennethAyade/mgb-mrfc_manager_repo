/**
 * Check documents in database and diagnose Cloudinary issues
 */

const { Sequelize, DataTypes } = require('sequelize');
require('dotenv').config({ path: './.env' });

// Database connection
const sequelize = process.env.DATABASE_URL
  ? new Sequelize(process.env.DATABASE_URL, {
      dialect: 'postgres',
      dialectOptions: {
        ssl: {
          require: true,
          rejectUnauthorized: false
        }
      },
      logging: false
    })
  : new Sequelize({
      database: process.env.DB_NAME || 'mgb_mrfc',
      username: process.env.DB_USER || 'postgres',
      password: process.env.DB_PASSWORD || '',
      host: process.env.DB_HOST || 'localhost',
      port: parseInt(process.env.DB_PORT || '5432'),
      dialect: 'postgres',
      logging: false
    });

// Document model
const Document = sequelize.define('Document', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  file_name: DataTypes.STRING,
  original_name: DataTypes.STRING,
  file_url: DataTypes.STRING,
  file_cloudinary_id: DataTypes.STRING,
  category: DataTypes.STRING,
  created_at: DataTypes.DATE
}, {
  tableName: 'documents',
  timestamps: false
});

async function checkDocuments() {
  console.log('\n=====================================================');
  console.log('  Document Database Diagnostics');
  console.log('=====================================================\n');

  try {
    // Test connection
    await sequelize.authenticate();
    console.log('✅ Database connection successful\n');

    // Get document count
    const count = await Document.count();
    console.log(`📊 Total documents: ${count}\n`);

    if (count === 0) {
      console.log('✅ No documents in database');
      console.log('You can upload new documents via the app.\n');
      return;
    }

    // Get recent documents
    const documents = await Document.findAll({
      limit: 10,
      order: [['created_at', 'DESC']]
    });

    console.log('Recent documents:');
    console.log('================\n');

    documents.forEach((doc, index) => {
      console.log(`${index + 1}. ${doc.original_name || doc.file_name}`);
      console.log(`   Category: ${doc.category}`);
      console.log(`   Cloudinary ID: ${doc.file_cloudinary_id || 'N/A'}`);
      console.log(`   URL: ${doc.file_url?.substring(0, 80)}...`);
      console.log(`   Created: ${doc.created_at}`);
      console.log('');
    });

    console.log('\n=====================================================');
    console.log('⚠️  DIAGNOSIS:');
    console.log('=====================================================');
    console.log('');
    console.log('If documents show 401 errors when accessed:');
    console.log('');
    console.log('CAUSE:');
    console.log('  These files were uploaded with AUTHENTICATED access mode');
    console.log('  before the code fix was applied.');
    console.log('');
    console.log('SOLUTION:');
    console.log('  1. Clear old documents: node clear-documents-db.js');
    console.log('  2. Re-upload via Android app');
    console.log('  3. New uploads will have PUBLIC access');
    console.log('');

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await sequelize.close();
  }
}

checkDocuments();

