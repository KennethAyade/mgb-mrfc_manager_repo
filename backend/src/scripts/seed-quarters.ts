/**
 * ================================================
 * QUARTER SEEDING SCRIPT
 * ================================================
 * Creates Quarter records for 2024 and 2025
 * Run: npx ts-node src/scripts/seed-quarters.ts
 */

import sequelize from '../config/database';
import { Quarter } from '../models';

const seedQuarters = async (): Promise<void> => {
  try {
    console.log('🌱 Starting quarter seeding...');

    // Connect to database
    await sequelize.authenticate();
    console.log('✅ Database connected');

    // Create quarters for 2024 and 2025
    const quarters = [
      // 2024
      { name: 'Q1 2024', year: 2024, quarter_number: 1, start_date: new Date('2024-01-01'), end_date: new Date('2024-03-31') },
      { name: 'Q2 2024', year: 2024, quarter_number: 2, start_date: new Date('2024-04-01'), end_date: new Date('2024-06-30') },
      { name: 'Q3 2024', year: 2024, quarter_number: 3, start_date: new Date('2024-07-01'), end_date: new Date('2024-09-30') },
      { name: 'Q4 2024', year: 2024, quarter_number: 4, start_date: new Date('2024-10-01'), end_date: new Date('2024-12-31') },
      
      // 2025
      { name: 'Q1 2025', year: 2025, quarter_number: 1, start_date: new Date('2025-01-01'), end_date: new Date('2025-03-31') },
      { name: 'Q2 2025', year: 2025, quarter_number: 2, start_date: new Date('2025-04-01'), end_date: new Date('2025-06-30') },
      { name: 'Q3 2025', year: 2025, quarter_number: 3, start_date: new Date('2025-07-01'), end_date: new Date('2025-09-30') },
      { name: 'Q4 2025', year: 2025, quarter_number: 4, start_date: new Date('2025-10-01'), end_date: new Date('2025-12-31') },
    ];

    console.log(`\n📦 Creating ${quarters.length} quarters...`);

    for (const quarter of quarters) {
      // Check if quarter already exists
      const existing = await Quarter.findOne({
        where: {
          year: quarter.year,
          quarter_number: quarter.quarter_number
        }
      });

      if (existing) {
        console.log(`⏭️  Skipping ${quarter.name} (already exists)`);
        continue;
      }

      // Create quarter
      await Quarter.create(quarter);
      console.log(`✅ Created ${quarter.name}`);
    }

    console.log('\n✅ Quarter seeding completed successfully!');
    console.log('\n📊 Summary:');
    const totalQuarters = await Quarter.count();
    console.log(`Total quarters in database: ${totalQuarters}`);

    process.exit(0);
  } catch (error) {
    console.error('❌ Error seeding quarters:', error);
    process.exit(1);
  }
};

// Run the seeding
seedQuarters();

