-- ==========================================
-- MIGRATION: CREATE MEETING_MINUTES TABLE
-- ==========================================
-- Created: 2025-10-31
-- Purpose: Add meeting_minutes table for meeting summaries
-- Only meeting organizer (creator) can add/edit minutes
-- All users can view minutes
-- ==========================================

-- Create meeting_minutes table
CREATE TABLE IF NOT EXISTS meeting_minutes (
    id BIGSERIAL PRIMARY KEY,
    agenda_id BIGINT NOT NULL UNIQUE REFERENCES agendas(id) ON DELETE CASCADE,
    summary TEXT NOT NULL,
    decisions JSONB DEFAULT '[]'::jsonb,
    action_items JSONB DEFAULT '[]'::jsonb,
    attendees_summary TEXT,
    next_meeting_notes TEXT,
    is_final BOOLEAN DEFAULT FALSE,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_by_name VARCHAR(100) NOT NULL,
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_meeting_minutes_agenda_id ON meeting_minutes(agenda_id);
CREATE INDEX IF NOT EXISTS idx_meeting_minutes_created_by ON meeting_minutes(created_by);
CREATE INDEX IF NOT EXISTS idx_meeting_minutes_is_final ON meeting_minutes(is_final);

-- Add comments
COMMENT ON TABLE meeting_minutes IS 'Meeting minutes (summary, decisions, action items) - only organizer can edit';
COMMENT ON COLUMN meeting_minutes.agenda_id IS 'References the meeting (one-to-one relationship)';
COMMENT ON COLUMN meeting_minutes.summary IS 'Overall meeting summary/notes';
COMMENT ON COLUMN meeting_minutes.decisions IS 'JSON array of decisions made: [{"decision": "...", "status": "..."}]';
COMMENT ON COLUMN meeting_minutes.action_items IS 'JSON array of action items: [{"item": "...", "assigned_to": "...", "deadline": "..."}]';
COMMENT ON COLUMN meeting_minutes.attendees_summary IS 'Summary of who attended (complement to attendance table)';
COMMENT ON COLUMN meeting_minutes.next_meeting_notes IS 'Notes for next meeting';
COMMENT ON COLUMN meeting_minutes.is_final IS 'Whether minutes have been finalized/approved';
COMMENT ON COLUMN meeting_minutes.created_by IS 'Meeting organizer who created the minutes';
COMMENT ON COLUMN meeting_minutes.created_by_name IS 'Full name of organizer (denormalized)';
COMMENT ON COLUMN meeting_minutes.approved_by IS 'User who approved/finalized the minutes';
COMMENT ON COLUMN meeting_minutes.approved_at IS 'When the minutes were approved';

-- Success message
SELECT 'Meeting minutes table created successfully' AS status;
