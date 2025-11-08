/**
 * Clear documents from database
 * This removes document records but leaves files in Cloudinary
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
  file_name: DataTypes.STRING
}, {
  tableName: 'documents',
  timestamps: false
});

async function clearDocuments() {
  console.log('\n=====================================================');
  console.log('  Clear Documents from Database');
  console.log('=====================================================\n');

  try {
    // Test connection
    await sequelize.authenticate();
    console.log('✅ Database connection successful\n');

    // Get document count
    const count = await Document.count();
    console.log(`📊 Current documents: ${count}\n`);

    if (count === 0) {
      console.log('✅ Database is already clean');
      return;
    }

    // Delete all documents
    console.log('🗑️  Clearing documents...');
    const deleted = await Document.destroy({ where: {}, truncate: true });
    console.log(`✅ Cleared ${count} document records\n`);

    console.log('=====================================================');
    console.log('  NEXT STEPS');
    console.log('=====================================================\n');
    console.log('1. Make sure backend is running:');
    console.log('   cd backend && npm run dev\n');
    console.log('2. Open your Android app\n');
    console.log('3. Upload test documents\n');
    console.log('4. New uploads will have PUBLIC access (no 401 errors!)\n');

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await sequelize.close();
  }
}

clearDocuments();

