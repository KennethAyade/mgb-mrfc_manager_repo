-- ================================================
-- Migration 017: Add tablet_number to Attendance
-- ================================================
-- Date: December 11, 2025
-- Purpose: Add user-selectable tablet number (1-15) to attendance records
-- Replaces auto-computed chronological numbering with explicit selection

DO $$
BEGIN
    -- Add tablet_number column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'attendance' AND column_name = 'tablet_number'
    ) THEN
        ALTER TABLE attendance
        ADD COLUMN tablet_number INTEGER CHECK (tablet_number >= 1 AND tablet_number <= 15);

        RAISE NOTICE 'Added tablet_number column to attendance table';

        -- Backfill existing records with computed values based on chronological order
        -- This ensures existing data has valid tablet numbers before we require them
        WITH numbered AS (
            SELECT
                id,
                ROW_NUMBER() OVER (PARTITION BY agenda_id ORDER BY marked_at) as computed_num
            FROM attendance
        )
        UPDATE attendance a
        SET tablet_number = LEAST(n.computed_num, 15)  -- Cap at 15
        FROM numbered n
        WHERE a.id = n.id;

        RAISE NOTICE 'Backfilled tablet_number for % existing records', (SELECT COUNT(*) FROM attendance);
    ELSE
        RAISE NOTICE 'tablet_number column already exists, skipping';
    END IF;
END $$;

-- Add index for faster tablet number queries
CREATE INDEX IF NOT EXISTS idx_attendance_tablet_number
ON attendance(agenda_id, tablet_number)
WHERE tablet_number IS NOT NULL;

-- Comment for documentation
COMMENT ON COLUMN attendance.tablet_number IS
'User-selected tablet number (1-15) for physical tablet identification. Replaces auto-computed chronological numbering.';
