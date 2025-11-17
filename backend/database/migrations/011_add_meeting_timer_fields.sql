-- Migration 011: Add Meeting Timer Fields
-- Add fields to track actual meeting start/end times and duration

-- Add actual_start_time column
ALTER TABLE agendas
ADD COLUMN IF NOT EXISTS actual_start_time TIMESTAMP;

-- Add actual_end_time column
ALTER TABLE agendas
ADD COLUMN IF NOT EXISTS actual_end_time TIMESTAMP;

-- Add duration_minutes column (calculated from actual times)
ALTER TABLE agendas
ADD COLUMN IF NOT EXISTS duration_minutes INTEGER;

-- Add started_by column (admin who started the meeting)
ALTER TABLE agendas
ADD COLUMN IF NOT EXISTS started_by BIGINT REFERENCES users(id);

-- Add ended_by column (admin who ended the meeting)
ALTER TABLE agendas
ADD COLUMN IF NOT EXISTS ended_by BIGINT REFERENCES users(id);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_agendas_actual_start_time ON agendas(actual_start_time);
CREATE INDEX IF NOT EXISTS idx_agendas_actual_end_time ON agendas(actual_end_time);
CREATE INDEX IF NOT EXISTS idx_agendas_started_by ON agendas(started_by);
CREATE INDEX IF NOT EXISTS idx_agendas_ended_by ON agendas(ended_by);

-- Add comments
COMMENT ON COLUMN agendas.actual_start_time IS 'Actual time the meeting was started (real-time timer)';
COMMENT ON COLUMN agendas.actual_end_time IS 'Actual time the meeting was ended';
COMMENT ON COLUMN agendas.duration_minutes IS 'Actual meeting duration in minutes';
COMMENT ON COLUMN agendas.started_by IS 'Admin/Super Admin who started the meeting';
COMMENT ON COLUMN agendas.ended_by IS 'Admin/Super Admin who ended the meeting';
