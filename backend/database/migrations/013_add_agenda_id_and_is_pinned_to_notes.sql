-- Migration: Add agenda_id and is_pinned columns to notes table
-- Date: 2025-11-25
-- Description: Add agenda_id foreign key and is_pinned boolean flag to enable meeting-specific notes and pinned notes functionality

-- Add agenda_id column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'notes' AND column_name = 'agenda_id'
    ) THEN
        ALTER TABLE notes ADD COLUMN agenda_id BIGINT;
    END IF;
END $$;

-- Add foreign key constraint for agenda_id if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'notes_agenda_id_fkey' AND table_name = 'notes'
    ) THEN
        ALTER TABLE notes
        ADD CONSTRAINT notes_agenda_id_fkey
        FOREIGN KEY (agenda_id) REFERENCES agendas(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Add is_pinned column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'notes' AND column_name = 'is_pinned'
    ) THEN
        ALTER TABLE notes ADD COLUMN is_pinned BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

-- Create index on agenda_id for better query performance
CREATE INDEX IF NOT EXISTS idx_notes_agenda_id ON notes(agenda_id);

-- Add comments for documentation
COMMENT ON COLUMN notes.agenda_id IS 'Optional reference to specific meeting/agenda';
COMMENT ON COLUMN notes.is_pinned IS 'Flag to mark note as pinned for priority display';
