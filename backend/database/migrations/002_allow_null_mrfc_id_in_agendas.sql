-- Migration: Allow NULL mrfc_id in agendas table
-- Purpose: Support general meetings that aren't tied to specific MRFCs
-- Date: 2025-01-01
-- FIXED: Made idempotent (removed BEGIN/COMMIT, added IF NOT EXISTS)

-- Step 1: Drop the existing unique constraint (it doesn't allow multiple NULLs)
ALTER TABLE agendas
DROP CONSTRAINT IF EXISTS agendas_mrfc_id_quarter_id_key;

-- Step 2: Allow NULL values for mrfc_id (idempotent - safe to run multiple times)
DO $$
BEGIN
  ALTER TABLE agendas ALTER COLUMN mrfc_id DROP NOT NULL;
EXCEPTION
  WHEN invalid_table_definition THEN NULL;
END $$;

-- Step 3: Create a partial unique index that only applies when mrfc_id is NOT NULL
-- This allows multiple general meetings (NULL mrfc_id) per quarter
-- But still ensures only one meeting per MRFC per quarter
CREATE UNIQUE INDEX IF NOT EXISTS agendas_mrfc_quarter_unique
ON agendas (mrfc_id, quarter_id)
WHERE mrfc_id IS NOT NULL;

-- Step 4: Add a comment to explain the schema change
COMMENT ON COLUMN agendas.mrfc_id IS 'MRFC ID (NULL for general meetings not tied to specific MRFC)';
