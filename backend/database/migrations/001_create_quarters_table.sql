-- Migration: Create quarters table
-- Description: Creates the quarters table for Q1-Q4 quarter tracking

CREATE TABLE IF NOT EXISTS quarters (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    year INTEGER NOT NULL,
    quarter_number INTEGER NOT NULL CHECK (quarter_number BETWEEN 1 AND 4),
    is_current BOOLEAN DEFAULT false,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_year_quarter UNIQUE (year, quarter_number)
);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_quarters_year ON quarters(year);
CREATE INDEX IF NOT EXISTS idx_quarters_current ON quarters(is_current);

-- Insert comment
COMMENT ON TABLE quarters IS 'Stores quarterly periods for document organization';

