import { Transaction } from 'sequelize';
import sequelize from '../config/database';
import { Quarter } from '../models';

export interface QuarterDefinition {
  name: string;
  year: number;
  quarter_number: number;
  start_date: Date;
  end_date: Date;
  is_current: boolean;
}

const QUARTER_RANGES = [
  { startMonth: 0, startDay: 1, endMonth: 2, endDay: 31 },
  { startMonth: 3, startDay: 1, endMonth: 5, endDay: 30 },
  { startMonth: 6, startDay: 1, endMonth: 8, endDay: 30 },
  { startMonth: 9, startDay: 1, endMonth: 11, endDay: 31 }
];

export const getQuarterNumber = (date: Date): number =>
  Math.floor(date.getUTCMonth() / 3) + 1;

export const buildQuarterDefinitions = (
  year: number,
  now: Date = new Date()
): QuarterDefinition[] => {
  if (!Number.isInteger(year) || year < 2000 || year > 2200) {
    throw new Error(`Invalid reporting year: ${year}`);
  }

  const currentYear = now.getUTCFullYear();
  const currentQuarter = getQuarterNumber(now);

  return QUARTER_RANGES.map((range, index) => {
    const quarterNumber = index + 1;
    return {
      name: `Q${quarterNumber} ${year}`,
      year,
      quarter_number: quarterNumber,
      start_date: new Date(Date.UTC(year, range.startMonth, range.startDay)),
      end_date: new Date(Date.UTC(year, range.endMonth, range.endDay)),
      is_current: year === currentYear && quarterNumber === currentQuarter
    };
  });
};

export const provisionQuartersForYear = async (
  year: number,
  now: Date = new Date(),
  transaction?: Transaction
): Promise<Quarter[]> => {
  const definitions = buildQuarterDefinitions(year, now);

  const provision = async (t: Transaction): Promise<Quarter[]> => {
    await Quarter.update({ is_current: false }, { where: {}, transaction: t });

    for (const definition of definitions) {
      const [quarter] = await Quarter.findOrCreate({
        where: {
          year: definition.year,
          quarter_number: definition.quarter_number
        },
        defaults: definition,
        transaction: t
      });

      await quarter.update(
        {
          name: definition.name,
          start_date: definition.start_date,
          end_date: definition.end_date,
          is_current: definition.is_current
        },
        { transaction: t }
      );
    }

    return Quarter.findAll({
      where: { year },
      order: [['quarter_number', 'ASC']],
      transaction: t
    });
  };

  return transaction ? provision(transaction) : sequelize.transaction(provision);
};

