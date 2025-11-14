-- =============================================
-- Migration 008: Add Agenda Item Proposal Workflow
-- =============================================
-- Date: November 14, 2025
-- Description: Add support for agenda item proposals with admin approval
-- 
-- Changes:
-- 1. Add status column (PROPOSED, APPROVED, DENIED)
-- 2. Add approval tracking fields (approved_by, approved_at)
-- 3. Add denial tracking fields (denied_by, denied_at, denial_remarks)
-- =============================================

-- Step 1: Create status ENUM
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'agenda_item_status') THEN
        CREATE TYPE agenda_item_status AS ENUM ('PROPOSED', 'APPROVED', 'DENIED');
        RAISE NOTICE 'Created agenda_item_status enum';
    ELSE
        RAISE NOTICE 'agenda_item_status enum already exists';
    END IF;
END $$;

-- Step 2: Add status column
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agenda_items' AND column_name = 'status'
    ) THEN
        ALTER TABLE agenda_items ADD COLUMN status agenda_item_status DEFAULT 'APPROVED';
        COMMENT ON COLUMN agenda_items.status IS 'PROPOSED: awaiting approval, APPROVED: approved by admin, DENIED: rejected';
        RAISE NOTICE 'Added status column';
    END IF;
END $$;

-- Step 3: Add approval/denial tracking columns
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agenda_items' AND column_name = 'approved_by'
    ) THEN
        ALTER TABLE agenda_items ADD COLUMN approved_by BIGINT REFERENCES users(id);
        COMMENT ON COLUMN agenda_items.approved_by IS 'Admin who approved this item';
        RAISE NOTICE 'Added approved_by column';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agenda_items' AND column_name = 'approved_at'
    ) THEN
        ALTER TABLE agenda_items ADD COLUMN approved_at TIMESTAMP;
        COMMENT ON COLUMN agenda_items.approved_at IS 'When item was approved';
        RAISE NOTICE 'Added approved_at column';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agenda_items' AND column_name = 'denied_by'
    ) THEN
        ALTER TABLE agenda_items ADD COLUMN denied_by BIGINT REFERENCES users(id);
        COMMENT ON COLUMN agenda_items.denied_by IS 'Admin who denied this item';
        RAISE NOTICE 'Added denied_by column';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agenda_items' AND column_name = 'denied_at'
    ) THEN
        ALTER TABLE agenda_items ADD COLUMN denied_at TIMESTAMP;
        COMMENT ON COLUMN agenda_items.denied_at IS 'When item was denied';
        RAISE NOTICE 'Added denied_at column';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agenda_items' AND column_name = 'denial_remarks'
    ) THEN
        ALTER TABLE agenda_items ADD COLUMN denial_remarks TEXT;
        COMMENT ON COLUMN agenda_items.denial_remarks IS 'Admin remarks for denial';
        RAISE NOTICE 'Added denial_remarks column';
    END IF;
END $$;

-- Step 4: Update existing agenda items to APPROVED status
UPDATE agenda_items SET status = 'APPROVED' WHERE status IS NULL;

-- Step 5: Create indexes
CREATE INDEX IF NOT EXISTS idx_agenda_items_status ON agenda_items(status);
CREATE INDEX IF NOT EXISTS idx_agenda_items_approved_by ON agenda_items(approved_by);

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Migration 008 completed successfully';
    RAISE NOTICE '   - Created agenda_item_status enum';
    RAISE NOTICE '   - Added 5 new columns to agenda_items';
    RAISE NOTICE '   - Updated existing items to APPROVED status';
    RAISE NOTICE '   - Created 2 new indexes';
END $$;

