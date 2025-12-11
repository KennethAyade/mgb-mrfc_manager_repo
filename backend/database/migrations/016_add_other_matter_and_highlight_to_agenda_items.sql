-- Migration: Add is_other_matter and is_highlighted fields to agenda_items
-- Purpose: Feature 2 (Other Matters tab) and Feature 7 (Highlight discussed items)
-- Date: 2025-12-10

-- Feature 2: Other Matters - Items added after main agenda is finalized
-- These show in a separate "Other Matters" tab instead of the main Agenda tab
ALTER TABLE agenda_items
ADD COLUMN IF NOT EXISTS is_other_matter BOOLEAN DEFAULT FALSE NOT NULL;

-- Feature 7: Highlight discussed items with green background
-- Admin can mark items as "discussed" and all users see the green highlight
ALTER TABLE agenda_items
ADD COLUMN IF NOT EXISTS is_highlighted BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE agenda_items
ADD COLUMN IF NOT EXISTS highlighted_by BIGINT REFERENCES users(id);

ALTER TABLE agenda_items
ADD COLUMN IF NOT EXISTS highlighted_at TIMESTAMP;

-- Add index for quick filtering of other matters
CREATE INDEX IF NOT EXISTS idx_agenda_items_is_other_matter ON agenda_items(is_other_matter);

-- Add index for quick filtering of highlighted items
CREATE INDEX IF NOT EXISTS idx_agenda_items_is_highlighted ON agenda_items(is_highlighted);
