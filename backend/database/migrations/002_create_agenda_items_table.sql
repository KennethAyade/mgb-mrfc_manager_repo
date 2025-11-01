-- ==========================================
-- MIGRATION: CREATE AGENDA_ITEMS TABLE
-- ==========================================
-- Created: 2025-10-31
-- Purpose: Add agenda_items table for meeting discussion topics
-- All users can add agenda items, auto-tagged with contributor info
-- ==========================================

-- Create agenda_items table
CREATE TABLE IF NOT EXISTS agenda_items (
    id BIGSERIAL PRIMARY KEY,
    agenda_id BIGINT NOT NULL REFERENCES agendas(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    added_by BIGINT NOT NULL REFERENCES users(id),
    added_by_name VARCHAR(100) NOT NULL,
    added_by_username VARCHAR(50) NOT NULL,
    order_index INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_agenda_items_agenda_id ON agenda_items(agenda_id);
CREATE INDEX IF NOT EXISTS idx_agenda_items_added_by ON agenda_items(added_by);
CREATE INDEX IF NOT EXISTS idx_agenda_items_created_at ON agenda_items(created_at DESC);

-- Add comments
COMMENT ON TABLE agenda_items IS 'Meeting agenda items (discussion topics) added by any user';
COMMENT ON COLUMN agenda_items.agenda_id IS 'References the meeting (agendas table)';
COMMENT ON COLUMN agenda_items.title IS 'Brief title of the agenda item';
COMMENT ON COLUMN agenda_items.description IS 'Detailed description of the agenda item';
COMMENT ON COLUMN agenda_items.added_by IS 'User ID of the person who added this item';
COMMENT ON COLUMN agenda_items.added_by_name IS 'Full name of contributor (denormalized for display)';
COMMENT ON COLUMN agenda_items.added_by_username IS 'Username of contributor (denormalized for display)';
COMMENT ON COLUMN agenda_items.order_index IS 'Display order (lower numbers first)';

-- Success message
SELECT 'Agenda items table created successfully' AS status;
