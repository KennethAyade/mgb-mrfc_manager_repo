/**
 * SEED QUARTERS FOR 2025
 * ======================
 * Creates Q1-Q4 quarters for the year 2025
 */

const { Sequelize } = require('sequelize');
require('dotenv').config();

// Create database connection using DATABASE_URL
const sequelize = new Sequelize(process.env.DATABASE_URL, {
  dialect: 'postgres',
  logging: false,
  dialectOptions: {
    ssl: {
      require: true,
      rejectUnauthorized: false
    }
  }
});

const quarters2025 = [
  {
    name: 'Q1-2025',
    year: 2025,
    quarter_number: 1,
    start_date: '2025-01-01',
    end_date: '2025-03-31',
    is_current: false
  },
  {
    name: 'Q2-2025',
    year: 2025,
    quarter_number: 2,
    start_date: '2025-04-01',
    end_date: '2025-06-30',
    is_current: false
  },
  {
    name: 'Q3-2025',
    year: 2025,
    quarter_number: 3,
    start_date: '2025-07-01',
    end_date: '2025-09-30',
    is_current: false
  },
  {
    name: 'Q4-2025',
    year: 2025,
    quarter_number: 4,
    start_date: '2025-10-01',
    end_date: '2025-12-31',
    is_current: true // Q4 is current (we're in November 2025)
  }
];

async function seedQuarters() {
  try {
    console.log('🌱 Starting quarter seed...\n');

    // Test connection
    await sequelize.authenticate();
    console.log('✅ Database connection established\n');

    // Check if quarters already exist
    const [existingQuarters] = await sequelize.query(
      'SELECT id, name, year, quarter_number FROM quarters WHERE year = 2025'
    );

    if (existingQuarters.length > 0) {
      console.log('ℹ️  Quarters for 2025 already exist:');
      existingQuarters.forEach(q => {
        console.log(`   - ${q.name} (ID: ${q.id})`);
      });
      console.log('\n⚠️  Skipping seed to avoid duplicates.');
      console.log('   To re-seed, delete existing quarters first:\n');
      console.log('   DELETE FROM quarters WHERE year = 2025;\n');
      await sequelize.close();
      return;
    }

    // Insert quarters
    console.log('📝 Inserting quarters for 2025...\n');

    for (const quarter of quarters2025) {
      const [result] = await sequelize.query(
        `INSERT INTO quarters (name, year, quarter_number, start_date, end_date, is_current)
         VALUES (:name, :year, :quarter_number, :start_date, :end_date, :is_current)
         RETURNING id`,
        {
          replacements: quarter
        }
      );

      console.log(`   ✅ Created ${quarter.name} (ID: ${result[0].id})`);
    }

    // Verify insertion
    const [insertedQuarters] = await sequelize.query(
      'SELECT id, name, quarter_number, start_date, end_date, is_current FROM quarters WHERE year = 2025 ORDER BY quarter_number'
    );

    console.log('\n✅ Successfully seeded quarters!\n');
    console.log('📊 Summary:');
    insertedQuarters.forEach(q => {
      const current = q.is_current ? ' (CURRENT)' : '';
      console.log(`   - ID ${q.id}: ${q.name} | ${q.start_date} to ${q.end_date}${current}`);
    });

    await sequelize.close();
    console.log('\n✅ Database connection closed');
  } catch (error) {
    console.error('\n❌ Error seeding quarters:', error.message);
    console.error(error);
    await sequelize.close();
    process.exit(1);
  }
}

// Run the seed
seedQuarters();
