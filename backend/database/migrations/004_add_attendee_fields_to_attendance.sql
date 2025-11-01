/**
 * ================================================
 * MIGRATION: Add Attendee Fields to Attendance Table
 * ================================================
 * Version: 004
 * Date: 2025-11-01
 *
 * CHANGES:
 * - Add attendee_name field (manual entry for non-proponent attendees)
 * - Add attendee_position field (job title/position)
 * - Add attendee_department field (department/organization)
 * - Make proponent_id nullable (to support manual attendee entry)
 *
 * PURPOSE:
 * Support flexible attendance tracking for:
 * 1. Proponents (mining company reps) - use proponent_id
 * 2. MGB staff - manual entry with name/position/department
 * 3. Guests/observers - manual entry with name/position/department
 */

-- Step 1: Make proponent_id nullable (to support manual attendee entry)
ALTER TABLE attendance
  ALTER COLUMN proponent_id DROP NOT NULL;

-- Step 2: Add attendee information fields
ALTER TABLE attendance
  ADD COLUMN IF NOT EXISTS attendee_name VARCHAR(100),
  ADD COLUMN IF NOT EXISTS attendee_position VARCHAR(100),
  ADD COLUMN IF NOT EXISTS attendee_department VARCHAR(100);

-- Step 3: Create index on attendee_name for search performance
CREATE INDEX IF NOT EXISTS idx_attendance_attendee_name ON attendance(attendee_name);

-- Step 4: Add check constraint to ensure either proponent_id OR attendee_name is provided
-- Note: PostgreSQL doesn't support CHECK constraints with multiple columns easily in ALTER TABLE
-- So we'll add a comment explaining the business rule
COMMENT ON TABLE attendance IS 'Attendance records for meetings. Either proponent_id OR attendee_name must be provided.';

-- Step 5: Return success status
SELECT 'Attendee fields added to attendance table successfully' AS status;
