/**
 * Migration: Add Compliance Tracking Fields to MRFCs Table
 * Date: November 2, 2025
 * Purpose: Enable manual compliance percentage tracking and status management
 */

-- Add compliance tracking fields
ALTER TABLE mrfcs ADD COLUMN IF NOT EXISTS compliance_percentage DECIMAL(5,2) DEFAULT NULL;
ALTER TABLE mrfcs ADD COLUMN IF NOT EXISTS compliance_status VARCHAR(20) DEFAULT 'NOT_ASSESSED';
ALTER TABLE mrfcs ADD COLUMN IF NOT EXISTS compliance_updated_at TIMESTAMP NULL;
ALTER TABLE mrfcs ADD COLUMN IF NOT EXISTS compliance_updated_by BIGINT REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE mrfcs ADD COLUMN IF NOT EXISTS assigned_admin_id BIGINT REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE mrfcs ADD COLUMN IF NOT EXISTS mrfc_code VARCHAR(50) NULL;

-- Add constraint for compliance_percentage (must be between 0 and 100)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'check_compliance_range'
  ) THEN
    ALTER TABLE mrfcs ADD CONSTRAINT check_compliance_range
      CHECK (compliance_percentage IS NULL OR (compliance_percentage >= 0 AND compliance_percentage <= 100));
  END IF;
END $$;

-- Add constraint for compliance_status enum
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'check_compliance_status'
  ) THEN
    ALTER TABLE mrfcs ADD CONSTRAINT check_compliance_status
      CHECK (compliance_status IN ('COMPLIANT', 'NON_COMPLIANT', 'PARTIAL', 'NOT_ASSESSED'));
  END IF;
END $$;

-- Add unique constraint for mrfc_code (if not null)
CREATE UNIQUE INDEX IF NOT EXISTS idx_mrfcs_code_unique ON mrfcs(mrfc_code) WHERE mrfc_code IS NOT NULL;

-- Add indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_mrfcs_compliance_status ON mrfcs(compliance_status);
CREATE INDEX IF NOT EXISTS idx_mrfcs_assigned_admin ON mrfcs(assigned_admin_id);
CREATE INDEX IF NOT EXISTS idx_mrfcs_active_status ON mrfcs(is_active);

-- Add comments for documentation
COMMENT ON COLUMN mrfcs.compliance_percentage IS 'Manual compliance percentage (0-100) entered by admin';
COMMENT ON COLUMN mrfcs.compliance_status IS 'Current compliance status: COMPLIANT (>=80%), PARTIAL (40-79%), NON_COMPLIANT (<40%), NOT_ASSESSED';
COMMENT ON COLUMN mrfcs.compliance_updated_at IS 'Timestamp when compliance was last updated';
COMMENT ON COLUMN mrfcs.compliance_updated_by IS 'User ID who last updated compliance';
COMMENT ON COLUMN mrfcs.assigned_admin_id IS 'Admin user assigned to manage this MRFC';
COMMENT ON COLUMN mrfcs.mrfc_code IS 'Unique identifier code for the municipality/MRFC';
