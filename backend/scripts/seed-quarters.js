/**
 * Provision Q1-Q4 for the current reporting year.
 * Safe to run on every deployment; historical years are preserved.
 */

const { Sequelize, QueryTypes } = require('sequelize');
require('dotenv').config();

if (!process.env.DATABASE_URL) {
  console.error('DATABASE_URL is required to provision quarters.');
  process.exit(1);
}

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

const buildQuarters = (year, now = new Date()) => {
  const ranges = [
    ['01-01', '03-31'],
    ['04-01', '06-30'],
    ['07-01', '09-30'],
    ['10-01', '12-31']
  ];
  const currentQuarter = Math.floor(now.getUTCMonth() / 3) + 1;

  return ranges.map(([start, end], index) => ({
    name: `Q${index + 1} ${year}`,
    year,
    quarter_number: index + 1,
    start_date: `${year}-${start}`,
    end_date: `${year}-${end}`,
    is_current:
      year === now.getUTCFullYear() && index + 1 === currentQuarter
  }));
};

async function seedQuarters() {
  const now = new Date();
  const year = now.getUTCFullYear();
  const quarters = buildQuarters(year, now);
  let transaction;

  try {
    await sequelize.authenticate();
    transaction = await sequelize.transaction();
    console.log(`Provisioning reporting quarters for ${year}...`);

    await sequelize.query('UPDATE quarters SET is_current = FALSE', {
      transaction
    });

    for (const quarter of quarters) {
      await sequelize.query(
        `INSERT INTO quarters
          (name, year, quarter_number, start_date, end_date, is_current)
         VALUES
          (:name, :year, :quarter_number, :start_date, :end_date, :is_current)
         ON CONFLICT (year, quarter_number) DO UPDATE SET
          name = EXCLUDED.name,
          start_date = EXCLUDED.start_date,
          end_date = EXCLUDED.end_date,
          is_current = EXCLUDED.is_current`,
        { replacements: quarter, transaction }
      );
    }

    await transaction.commit();

    const provisioned = await sequelize.query(
      `SELECT id, name, year, quarter_number, start_date, end_date, is_current
       FROM quarters
       WHERE year = :year
       ORDER BY quarter_number ASC`,
      {
        replacements: { year },
        type: QueryTypes.SELECT
      }
    );

    if (provisioned.length !== 4) {
      throw new Error(
        `Expected 4 quarters for ${year}, found ${provisioned.length}`
      );
    }

    provisioned.forEach(quarter => {
      console.log(
        `  ${quarter.name}: ${quarter.start_date} to ${quarter.end_date}` +
          (quarter.is_current ? ' (CURRENT)' : '')
      );
    });
    console.log(`Quarter provisioning for ${year} completed.`);
  } catch (error) {
    if (transaction && !transaction.finished) {
      await transaction.rollback();
    }
    console.error('Quarter provisioning failed:', error);
    process.exitCode = 1;
  } finally {
    await sequelize.close();
  }
}

seedQuarters();
