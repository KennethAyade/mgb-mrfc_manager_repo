/**
 * Script to create compliance_analyses table
 * Run with: npx ts-node src/scripts/create-compliance-table.ts
 */

import sequelize from '../config/database';

const createTableSQL = `
CREATE TABLE IF NOT EXISTS compliance_analyses (
  id BIGSERIAL PRIMARY KEY,
  document_id BIGINT NOT NULL UNIQUE REFERENCES documents(id) ON DELETE CASCADE,
  document_name VARCHAR(255) NOT NULL,
  analysis_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  compliance_percentage DECIMAL(5, 2),
  compliance_rating VARCHAR(30),
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
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_compliance_analyses_document_id ON compliance_analyses(document_id);
CREATE INDEX IF NOT EXISTS idx_compliance_analyses_analysis_status ON compliance_analyses(analysis_status);
CREATE INDEX IF NOT EXISTS idx_compliance_analyses_compliance_rating ON compliance_analyses(compliance_rating);

-- Create ENUMs if they don't exist
DO $$ BEGIN
  CREATE TYPE analysis_status_enum AS ENUM ('PENDING', 'COMPLETED', 'FAILED');
EXCEPTION
  WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
  CREATE TYPE compliance_rating_enum AS ENUM ('FULLY_COMPLIANT', 'PARTIALLY_COMPLIANT', 'NON_COMPLIANT');
EXCEPTION
  WHEN duplicate_object THEN null;
END $$;
`;

async function createTable() {
  try {
    console.log('Creating compliance_analyses table...');
    await sequelize.query(createTableSQL);
    console.log('✅ Table created successfully!');
    process.exit(0);
  } catch (error) {
    console.error('❌ Error creating table:', error);
    process.exit(1);
  }
}

createTable();

