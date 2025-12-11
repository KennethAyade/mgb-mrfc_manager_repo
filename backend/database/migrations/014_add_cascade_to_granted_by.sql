-- Migration: Add CASCADE delete to user_mrfc_access.granted_by foreign key
-- Date: 2025-11-25
-- Description: Allow user deletion by cascading deletes to access grant records

-- Drop existing constraint if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'user_mrfc_access_granted_by_fkey'
        AND table_name = 'user_mrfc_access'
    ) THEN
        ALTER TABLE user_mrfc_access
        DROP CONSTRAINT user_mrfc_access_granted_by_fkey;
    END IF;
END $$;

-- Add new constraint with CASCADE
ALTER TABLE user_mrfc_access
ADD CONSTRAINT user_mrfc_access_granted_by_fkey
FOREIGN KEY (granted_by)
REFERENCES users(id)
ON DELETE CASCADE;

COMMENT ON CONSTRAINT user_mrfc_access_granted_by_fkey
ON user_mrfc_access IS 'Cascade delete access grants when granter is deleted';
