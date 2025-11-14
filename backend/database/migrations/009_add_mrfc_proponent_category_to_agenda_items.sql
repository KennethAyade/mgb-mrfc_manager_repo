-- Migration 009: Add MRFC, Proponent, and File Category fields to agenda_items
-- This allows agenda items to be linked to specific MRFCs, Proponents, and File Categories
-- for better organization and quick access to related documents

-- Add MRFC reference
ALTER TABLE agenda_items
ADD COLUMN IF NOT EXISTS mrfc_id BIGINT REFERENCES mrfcs(id) ON DELETE CASCADE;

-- Add Proponent reference
ALTER TABLE agenda_items
ADD COLUMN IF NOT EXISTS proponent_id BIGINT REFERENCES proponents(id) ON DELETE CASCADE;

-- Add File Category (matches document categories)
ALTER TABLE agenda_items
ADD COLUMN IF NOT EXISTS file_category VARCHAR(50);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_agenda_items_mrfc_id ON agenda_items(mrfc_id);
CREATE INDEX IF NOT EXISTS idx_agenda_items_proponent_id ON agenda_items(proponent_id);
CREATE INDEX IF NOT EXISTS idx_agenda_items_file_category ON agenda_items(file_category);

-- Add comment explaining file_category values
COMMENT ON COLUMN agenda_items.file_category IS 'Document category: CMVR, RESEARCH_ACCOMPLISHMENTS, SDMP, PRODUCTION_REPORT, SAFETY_REPORT, MTF_REPORT, AEPEP, OTHER';
