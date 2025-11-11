-- ==========================================
-- MIGRATION: CREATE COMPLIANCE_ANALYSES TABLE
-- ==========================================
-- Created: 2025-11-11
-- Purpose: Add compliance_analyses table for CMVR document analysis
-- Stores AI-powered compliance analysis results with OCR caching
-- ==========================================

-- Create ENUM types for compliance analysis (if they don't exist)
DO $$ BEGIN
    CREATE TYPE analysis_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE compliance_rating AS ENUM ('FULLY_COMPLIANT', 'PARTIALLY_COMPLIANT', 'NON_COMPLIANT');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create compliance_analyses table
CREATE TABLE IF NOT EXISTS compliance_analyses (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL UNIQUE REFERENCES documents(id) ON DELETE CASCADE,
    document_name VARCHAR(255) NOT NULL,
    analysis_status analysis_status NOT NULL DEFAULT 'PENDING',
    compliance_percentage DECIMAL(5, 2),
    compliance_rating compliance_rating,
    total_items INTEGER,
    compliant_items INTEGER,
    non_compliant_items INTEGER,
    na_items INTEGER,
    applicable_items INTEGER,
    compliance_details JSONB,
    non_compliant_list JSONB,
    admin_adjusted BOOLEAN DEFAULT FALSE,
    admin_notes TEXT,
    analyzed_at TIMESTAMP,
    -- OCR caching columns (added for performance)
    extracted_text TEXT,
    ocr_confidence DECIMAL(5, 2),
    ocr_language VARCHAR(20),
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_compliance_analyses_document_id ON compliance_analyses(document_id);
CREATE INDEX IF NOT EXISTS idx_compliance_analyses_status ON compliance_analyses(analysis_status);
CREATE INDEX IF NOT EXISTS idx_compliance_analyses_rating ON compliance_analyses(compliance_rating);
CREATE INDEX IF NOT EXISTS idx_compliance_analyses_analyzed_at ON compliance_analyses(analyzed_at DESC);

-- Add comments
COMMENT ON TABLE compliance_analyses IS 'AI-powered CMVR compliance analysis results with OCR caching';
COMMENT ON COLUMN compliance_analyses.document_id IS 'References the CMVR document being analyzed';
COMMENT ON COLUMN compliance_analyses.analysis_status IS 'PENDING: queued, COMPLETED: analysis done, FAILED: error occurred';
COMMENT ON COLUMN compliance_analyses.compliance_percentage IS 'Overall compliance percentage (0-100)';
COMMENT ON COLUMN compliance_analyses.compliance_rating IS 'Rating based on percentage: >=90% FULLY, 70-89% PARTIALLY, <70% NON';
COMMENT ON COLUMN compliance_analyses.compliance_details IS 'JSON: Section-wise breakdown (ECC, EPEP, Water Quality, etc.)';
COMMENT ON COLUMN compliance_analyses.non_compliant_list IS 'JSON: Array of non-compliant items with severity and notes';
COMMENT ON COLUMN compliance_analyses.admin_adjusted IS 'TRUE if admin manually adjusted the percentage/rating';
COMMENT ON COLUMN compliance_analyses.extracted_text IS 'Full text extracted from PDF via OCR or text extraction (cached)';
COMMENT ON COLUMN compliance_analyses.ocr_confidence IS 'OCR confidence score (0-100) for quality assessment';
COMMENT ON COLUMN compliance_analyses.ocr_language IS 'Languages used for OCR (e.g., eng+fil for English+Filipino)';

