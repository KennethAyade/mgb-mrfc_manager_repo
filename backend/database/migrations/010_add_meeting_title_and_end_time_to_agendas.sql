-- Migration 010: Add meeting_title and meeting_end_time to agendas table
-- This adds title and end time fields for better meeting management

-- Add Meeting Title
ALTER TABLE agendas
ADD COLUMN IF NOT EXISTS meeting_title VARCHAR(255);

-- Add Meeting End Time
ALTER TABLE agendas
ADD COLUMN IF NOT EXISTS meeting_end_time VARCHAR(10);

-- Add index for better search
CREATE INDEX IF NOT EXISTS idx_agendas_meeting_title ON agendas(meeting_title);

-- Add comments
COMMENT ON COLUMN agendas.meeting_title IS 'Title/name of the meeting';
COMMENT ON COLUMN agendas.meeting_time IS 'Meeting start time (e.g., 09:00 AM)';
COMMENT ON COLUMN agendas.meeting_end_time IS 'Meeting end time (e.g., 05:00 PM)';
