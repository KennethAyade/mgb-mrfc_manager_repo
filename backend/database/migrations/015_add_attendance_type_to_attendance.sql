-- Migration: Add attendance_type to attendance table
-- Date: 2025-12-10
-- Description: Add ONSITE/ONLINE attendance type to support mobile app users
--              as recommended by MGB (users can now use the app on phones)

-- Create the enum type if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'attendance_type_enum') THEN
        CREATE TYPE attendance_type_enum AS ENUM ('ONSITE', 'ONLINE');
    END IF;
END $$;

-- Add the attendance_type column with default value 'ONSITE'
ALTER TABLE attendance
ADD COLUMN IF NOT EXISTS attendance_type attendance_type_enum DEFAULT 'ONSITE' NOT NULL;

-- Add comment for documentation
COMMENT ON COLUMN attendance.attendance_type IS 'Type of attendance: ONSITE (physically present) or ONLINE (remote via app/phone)';
