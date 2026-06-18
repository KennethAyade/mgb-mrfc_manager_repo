import {
  buildQuarterDefinitions,
  getQuarterNumber
} from '../../services/quarterProvisioning.service';

describe('quarter provisioning definitions', () => {
  it('creates exactly four standard quarters for 2026', () => {
    const quarters = buildQuarterDefinitions(
      2026,
      new Date('2026-06-18T00:00:00Z')
    );

    expect(quarters).toHaveLength(4);
    expect(quarters.map(q => q.name)).toEqual([
      'Q1 2026',
      'Q2 2026',
      'Q3 2026',
      'Q4 2026'
    ]);
    expect(quarters[0].start_date.toISOString()).toContain('2026-01-01');
    expect(quarters[3].end_date.toISOString()).toContain('2026-12-31');
  });

  it('marks only the actual current quarter', () => {
    const quarters = buildQuarterDefinitions(
      2026,
      new Date('2026-06-18T00:00:00Z')
    );

    expect(getQuarterNumber(new Date('2026-06-18T00:00:00Z'))).toBe(2);
    expect(quarters.filter(q => q.is_current).map(q => q.quarter_number)).toEqual([2]);
  });

  it('does not mark a historical year current', () => {
    const quarters = buildQuarterDefinitions(
      2025,
      new Date('2026-06-18T00:00:00Z')
    );

    expect(quarters.some(q => q.is_current)).toBe(false);
  });
});
