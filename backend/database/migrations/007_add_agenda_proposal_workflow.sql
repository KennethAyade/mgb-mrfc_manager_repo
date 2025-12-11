-- =============================================
-- Migration 007: Add Agenda Proposal Workflow
-- =============================================
-- Date: November 14, 2025
-- Description: Add support for agenda proposals with admin approval/denial
-- 
-- Changes:
-- 1. Add 'PROPOSED' status to agenda_status ENUM
-- 2. Add proposal tracking fields (proposed_by, proposed_at)
-- 3. Add denial tracking fields (denied_by, denied_at, denial_remarks)
-- 4. Add approval tracking fields (approved_by, approved_at)
-- =============================================

-- Step 1: Add 'PROPOSED' to agenda_status ENUM
DO $$
BEGIN
    -- Check if PROPOSED value already exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum 
        WHERE enumlabel = 'PROPOSED' 
        AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'agenda_status')
    ) THEN
        ALTER TYPE agenda_status ADD VALUE 'PROPOSED';
        RAISE NOTICE 'Added PROPOSED to agenda_status enum';
    ELSE
        RAISE NOTICE 'PROPOSED already exists in agenda_status enum';
    END IF;
END $$;

-- Step 2: Add proposal tracking columns
DO $$
BEGIN
    -- proposed_by column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendas' AND column_name = 'proposed_by'
    ) THEN
        ALTER TABLE agendas ADD COLUMN proposed_by BIGINT REFERENCES users(id);
        COMMENT ON COLUMN agendas.proposed_by IS 'User who proposed this agenda (for USER role)';
        RAISE NOTICE 'Added proposed_by column';
    END IF;

    -- proposed_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendas' AND column_name = 'proposed_at'
    ) THEN
        ALTER TABLE agendas ADD COLUMN proposed_at TIMESTAMP;
        COMMENT ON COLUMN agendas.proposed_at IS 'Timestamp when agenda was proposed';
        RAISE NOTICE 'Added proposed_at column';
    END IF;

    -- approved_by column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendas' AND column_name = 'approved_by'
    ) THEN
        ALTER TABLE agendas ADD COLUMN approved_by BIGINT REFERENCES users(id);
        COMMENT ON COLUMN agendas.approved_by IS 'Admin who approved this proposed agenda';
        RAISE NOTICE 'Added approved_by column';
    END IF;

    -- approved_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendas' AND column_name = 'approved_at'
    ) THEN
        ALTER TABLE agendas ADD COLUMN approved_at TIMESTAMP;
        COMMENT ON COLUMN agendas.approved_at IS 'Timestamp when agenda was approved';
        RAISE NOTICE 'Added approved_at column';
    END IF;

    -- denied_by column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendas' AND column_name = 'denied_by'
    ) THEN
        ALTER TABLE agendas ADD COLUMN denied_by BIGINT REFERENCES users(id);
        COMMENT ON COLUMN agendas.denied_by IS 'Admin who denied this proposed agenda';
        RAISE NOTICE 'Added denied_by column';
    END IF;

    -- denied_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendas' AND column_name = 'denied_at'
    ) THEN
        ALTER TABLE agendas ADD COLUMN denied_at TIMESTAMP;
        COMMENT ON COLUMN agendas.denied_at IS 'Timestamp when agenda was denied';
        RAISE NOTICE 'Added denied_at column';
    END IF;

    -- denial_remarks column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agendas' AND column_name = 'denial_remarks'
    ) THEN
        ALTER TABLE agendas ADD COLUMN denial_remarks TEXT;
        COMMENT ON COLUMN agendas.denial_remarks IS 'Admin remarks explaining why proposal was denied';
        RAISE NOTICE 'Added denial_remarks column';
    END IF;
END $$;

-- Step 3: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_agendas_proposed_by ON agendas(proposed_by);
CREATE INDEX IF NOT EXISTS idx_agendas_approved_by ON agendas(approved_by);
CREATE INDEX IF NOT EXISTS idx_agendas_denied_by ON agendas(denied_by);

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Migration 007 completed successfully';
    RAISE NOTICE '   - PROPOSED status added to agenda_status enum';
    RAISE NOTICE '   - Added 7 new columns for proposal workflow';
    RAISE NOTICE '   - Created 3 new indexes';
END $$;

