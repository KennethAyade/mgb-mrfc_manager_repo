INSERT INTO quarters (name, year, quarter_number, start_date, end_date, is_current)
VALUES
  ('Q1 2026', 2026, 1, '2026-01-01', '2026-03-31', FALSE),
  ('Q2 2026', 2026, 2, '2026-04-01', '2026-06-30', FALSE),
  ('Q3 2026', 2026, 3, '2026-07-01', '2026-09-30', FALSE),
  ('Q4 2026', 2026, 4, '2026-10-01', '2026-12-31', FALSE)
ON CONFLICT (year, quarter_number) DO UPDATE SET
  name = EXCLUDED.name,
  start_date = EXCLUDED.start_date,
  end_date = EXCLUDED.end_date;

UPDATE quarters
SET is_current = (
  year = EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER
  AND quarter_number = EXTRACT(QUARTER FROM CURRENT_DATE)::INTEGER
);

