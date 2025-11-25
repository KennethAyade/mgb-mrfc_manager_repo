-- ==========================================
-- MGB MRFC MANAGER - DATABASE SCHEMA
-- ==========================================
-- PostgreSQL Schema for MRFC Compliance Tracking System
-- Version: 1.0
-- Date: October 18, 2025
-- ==========================================

-- Drop existing types if they exist (for clean re-runs)
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS proponent_status CASCADE;
DROP TYPE IF EXISTS matter_status CASCADE;
DROP TYPE IF EXISTS document_category CASCADE;
DROP TYPE IF EXISTS document_status CASCADE;
DROP TYPE IF EXISTS notification_type CASCADE;
DROP TYPE IF EXISTS audit_action CASCADE;
DROP TYPE IF EXISTS agenda_status CASCADE;
DROP TYPE IF EXISTS compliance_status CASCADE;

-- Create ENUM types
CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'ADMIN', 'USER');
CREATE TYPE proponent_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE matter_status AS ENUM ('PENDING', 'IN_PROGRESS', 'RESOLVED');
CREATE TYPE document_category AS ENUM ('MTF_REPORT', 'AEPEP', 'CMVR', 'SDMP', 'PRODUCTION', 'SAFETY', 'OTHER');
CREATE TYPE document_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');
CREATE TYPE notification_type AS ENUM ('MEETING', 'COMPLIANCE', 'ALERT', 'GENERAL');
CREATE TYPE audit_action AS ENUM ('CREATE', 'UPDATE', 'DELETE');
CREATE TYPE agenda_status AS ENUM ('DRAFT', 'PUBLISHED', 'COMPLETED', 'CANCELLED');
CREATE TYPE compliance_status AS ENUM ('COMPLIANT', 'PARTIALLY_COMPLIANT', 'NON_COMPLIANT');

-- ==========================================
-- TABLE 1: USERS
-- ==========================================
-- Stores user accounts with role-based access control
-- Roles: SUPER_ADMIN (can create admins), ADMIN (full access), USER (read-only)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role user_role NOT NULL DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'User accounts with role-based authentication';
COMMENT ON COLUMN users.role IS 'SUPER_ADMIN: creates admins, ADMIN: full access, USER: read-only';
COMMENT ON COLUMN users.is_active IS 'Soft delete flag - inactive users cannot login';
COMMENT ON COLUMN users.email_verified IS 'Email verification status for self-registration';

-- ==========================================
-- TABLE 2: MRFCS
-- ==========================================
-- Municipal Resource and Finance Committees
CREATE TABLE mrfcs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    municipality VARCHAR(100) NOT NULL,
    province VARCHAR(100),
    region VARCHAR(50),
    contact_person VARCHAR(100),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mrfcs IS 'Municipal Resource and Finance Committees';
COMMENT ON COLUMN mrfcs.created_by IS 'Admin user who created this MRFC';

-- ==========================================
-- TABLE 3: PROPONENTS
-- ==========================================
-- Mining and quarrying companies under MRFC oversight
CREATE TABLE proponents (
    id BIGSERIAL PRIMARY KEY,
    mrfc_id BIGINT NOT NULL REFERENCES mrfcs(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    permit_number VARCHAR(50),
    permit_type VARCHAR(50),
    status proponent_status DEFAULT 'ACTIVE',
    contact_person VARCHAR(100),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE proponents IS 'Mining/quarrying companies monitored by MRFCs';
COMMENT ON COLUMN proponents.permit_type IS 'e.g., Quarry Permit, Mining Permit, etc.';
COMMENT ON COLUMN proponents.status IS 'ACTIVE: operating, INACTIVE: not operating, SUSPENDED: permit suspended';

-- ==========================================
-- TABLE 4: QUARTERS
-- ==========================================
-- Quarter periods for reporting (Q1-Q4 per year)
CREATE TABLE quarters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE,
    year INTEGER NOT NULL,
    quarter_number INTEGER NOT NULL CHECK (quarter_number BETWEEN 1 AND 4),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_current BOOLEAN DEFAULT FALSE,
    UNIQUE(year, quarter_number)
);

COMMENT ON TABLE quarters IS 'Quarter periods for compliance reporting';
COMMENT ON COLUMN quarters.name IS 'e.g., "Q1 2025", "Q2 2025"';
COMMENT ON COLUMN quarters.is_current IS 'Only one quarter should be current at a time';

-- ==========================================
-- TABLE 5: AGENDAS
-- ==========================================
-- MRFC meeting agendas (one per MRFC per quarter)
CREATE TABLE agendas (
    id BIGSERIAL PRIMARY KEY,
    mrfc_id BIGINT NOT NULL REFERENCES mrfcs(id) ON DELETE CASCADE,
    quarter_id BIGINT NOT NULL REFERENCES quarters(id),
    meeting_date DATE NOT NULL,
    meeting_time TIME,
    location TEXT,
    status agenda_status DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(mrfc_id, quarter_id)
);

COMMENT ON TABLE agendas IS 'Meeting agendas - one per MRFC per quarter';
COMMENT ON COLUMN agendas.status IS 'DRAFT: being prepared, PUBLISHED: visible to users, COMPLETED: meeting done, CANCELLED: meeting cancelled';
COMMENT ON CONSTRAINT agendas_mrfc_id_quarter_id_key ON agendas IS 'Ensures one agenda per MRFC per quarter';

-- ==========================================
-- TABLE 6: MATTERS ARISING
-- ==========================================
-- Unresolved issues from previous meetings
CREATE TABLE matters_arising (
    id BIGSERIAL PRIMARY KEY,
    agenda_id BIGINT NOT NULL REFERENCES agendas(id) ON DELETE CASCADE,
    issue TEXT NOT NULL,
    status matter_status DEFAULT 'PENDING',
    assigned_to VARCHAR(100),
    date_raised DATE NOT NULL,
    date_resolved DATE,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE matters_arising IS 'Unresolved issues requiring follow-up';
COMMENT ON COLUMN matters_arising.status IS 'PENDING: not started, IN_PROGRESS: being worked on, RESOLVED: completed';
COMMENT ON COLUMN matters_arising.assigned_to IS 'Person responsible for resolving the issue';

-- ==========================================
-- TABLE 7: ATTENDANCE
-- ==========================================
-- Meeting attendance records with photo documentation
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    agenda_id BIGINT NOT NULL REFERENCES agendas(id) ON DELETE CASCADE,
    proponent_id BIGINT NOT NULL REFERENCES proponents(id) ON DELETE CASCADE,
    is_present BOOLEAN DEFAULT FALSE,
    photo_url TEXT,
    photo_cloudinary_id VARCHAR(255),
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    marked_by BIGINT REFERENCES users(id),
    remarks TEXT,
    UNIQUE(agenda_id, proponent_id)
);

COMMENT ON TABLE attendance IS 'Meeting attendance records with photo proof';
COMMENT ON COLUMN attendance.photo_url IS 'Cloudinary URL of attendance photo';
COMMENT ON COLUMN attendance.photo_cloudinary_id IS 'Cloudinary public_id for deletion';
COMMENT ON CONSTRAINT attendance_agenda_id_proponent_id_key ON attendance IS 'One attendance record per proponent per meeting';

-- ==========================================
-- TABLE 8: DOCUMENTS
-- ==========================================
-- Compliance documents uploaded by proponents
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    proponent_id BIGINT NOT NULL REFERENCES proponents(id) ON DELETE CASCADE,
    quarter_id BIGINT NOT NULL REFERENCES quarters(id),
    uploaded_by BIGINT REFERENCES users(id),
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    category document_category NOT NULL,
    file_url TEXT NOT NULL,
    file_cloudinary_id VARCHAR(255),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status document_status DEFAULT 'PENDING',
    reviewed_by BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE documents IS 'Compliance documents (PDF, DOC, XLS, etc.)';
COMMENT ON COLUMN documents.category IS 'MTF_REPORT, AEPEP, CMVR, SDMP, PRODUCTION, SAFETY, OTHER';
COMMENT ON COLUMN documents.status IS 'PENDING: awaiting review, ACCEPTED: approved, REJECTED: not accepted';
COMMENT ON COLUMN documents.file_cloudinary_id IS 'Cloudinary public_id for deletion';

-- ==========================================
-- TABLE 9: VOICE RECORDINGS
-- ==========================================
-- Meeting audio recordings
CREATE TABLE voice_recordings (
    id BIGSERIAL PRIMARY KEY,
    agenda_id BIGINT NOT NULL REFERENCES agendas(id) ON DELETE CASCADE,
    recording_name VARCHAR(255) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_cloudinary_id VARCHAR(255),
    duration INTEGER,
    file_size BIGINT,
    recorded_by BIGINT REFERENCES users(id),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE voice_recordings IS 'Meeting audio recordings';
COMMENT ON COLUMN voice_recordings.duration IS 'Duration in seconds';
COMMENT ON COLUMN voice_recordings.file_cloudinary_id IS 'Cloudinary public_id for deletion';

-- ==========================================
-- TABLE 10: NOTES
-- ==========================================
-- Personal notes created by users
CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mrfc_id BIGINT REFERENCES mrfcs(id) ON DELETE CASCADE,
    quarter_id BIGINT REFERENCES quarters(id),
    agenda_id BIGINT REFERENCES agendas(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    is_pinned BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE notes IS 'Personal notes - private to each user';
COMMENT ON COLUMN notes.mrfc_id IS 'Optional: link note to specific MRFC';
COMMENT ON COLUMN notes.quarter_id IS 'Optional: link note to specific quarter';
COMMENT ON COLUMN notes.agenda_id IS 'Optional: link note to specific meeting/agenda';
COMMENT ON COLUMN notes.is_pinned IS 'Flag to mark note as pinned for priority display';

-- ==========================================
-- TABLE 11: NOTIFICATIONS
-- ==========================================
-- User notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP
);

COMMENT ON TABLE notifications IS 'User notifications (in-app)';
COMMENT ON COLUMN notifications.type IS 'MEETING: meeting alerts, COMPLIANCE: compliance reminders, ALERT: urgent notices, GENERAL: general info';
COMMENT ON COLUMN notifications.user_id IS 'NULL means broadcast to all users';

-- ==========================================
-- TABLE 12: USER MRFC ACCESS
-- ==========================================
-- Many-to-many relationship: users can access multiple MRFCs
CREATE TABLE user_mrfc_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mrfc_id BIGINT NOT NULL REFERENCES mrfcs(id) ON DELETE CASCADE,
    granted_by BIGINT REFERENCES users(id) ON DELETE CASCADE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE(user_id, mrfc_id)
);

COMMENT ON TABLE user_mrfc_access IS 'User access permissions to specific MRFCs';
COMMENT ON COLUMN user_mrfc_access.granted_by IS 'Admin who granted this access';
COMMENT ON CONSTRAINT user_mrfc_access_user_id_mrfc_id_key ON user_mrfc_access IS 'One access record per user per MRFC';

-- ==========================================
-- TABLE 13: COMPLIANCE LOGS
-- ==========================================
-- Historical compliance tracking
CREATE TABLE compliance_logs (
    id BIGSERIAL PRIMARY KEY,
    proponent_id BIGINT NOT NULL REFERENCES proponents(id) ON DELETE CASCADE,
    quarter_id BIGINT NOT NULL REFERENCES quarters(id),
    compliance_pct DECIMAL(5,2) NOT NULL,
    documents_submitted INTEGER NOT NULL,
    documents_required INTEGER NOT NULL,
    status compliance_status NOT NULL,
    override_pct DECIMAL(5,2),
    override_justification TEXT,
    override_by BIGINT REFERENCES users(id),
    override_at TIMESTAMP,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    calculated_by BIGINT REFERENCES users(id)
);

COMMENT ON TABLE compliance_logs IS 'Historical compliance tracking per proponent per quarter';
COMMENT ON COLUMN compliance_logs.compliance_pct IS 'Auto-calculated compliance percentage';
COMMENT ON COLUMN compliance_logs.override_pct IS 'Admin-overridden percentage (if different)';
COMMENT ON COLUMN compliance_logs.override_justification IS 'Required justification for overrides';
COMMENT ON COLUMN compliance_logs.status IS 'COMPLIANT: >=80%, PARTIALLY_COMPLIANT: 50-79%, NON_COMPLIANT: <50%';

-- ==========================================
-- TABLE 14: AUDIT LOGS
-- ==========================================
-- System audit trail for all CREATE/UPDATE/DELETE actions
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action audit_action NOT NULL,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE audit_logs IS 'Complete audit trail - cannot be deleted by users';
COMMENT ON COLUMN audit_logs.entity_type IS 'Table name (e.g., "proponents", "documents")';
COMMENT ON COLUMN audit_logs.old_values IS 'JSON snapshot of record before change';
COMMENT ON COLUMN audit_logs.new_values IS 'JSON snapshot of record after change';

-- ==========================================
-- CREATE INDEXES FOR PERFORMANCE
-- ==========================================
-- FIXED: Added IF NOT EXISTS to all indexes for idempotent schema execution

-- User indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- MRFC indexes
CREATE INDEX IF NOT EXISTS idx_mrfcs_municipality ON mrfcs(municipality);
CREATE INDEX IF NOT EXISTS idx_mrfcs_province ON mrfcs(province);
CREATE INDEX IF NOT EXISTS idx_mrfcs_is_active ON mrfcs(is_active);

-- Proponent indexes
CREATE INDEX IF NOT EXISTS idx_proponents_mrfc ON proponents(mrfc_id);
CREATE INDEX IF NOT EXISTS idx_proponents_status ON proponents(status);
CREATE INDEX IF NOT EXISTS idx_proponents_permit_number ON proponents(permit_number);

-- Quarter indexes
CREATE INDEX IF NOT EXISTS idx_quarters_year ON quarters(year);
CREATE INDEX IF NOT EXISTS idx_quarters_is_current ON quarters(is_current);

-- Agenda indexes
CREATE INDEX IF NOT EXISTS idx_agendas_mrfc ON agendas(mrfc_id);
CREATE INDEX IF NOT EXISTS idx_agendas_quarter ON agendas(quarter_id);
CREATE INDEX IF NOT EXISTS idx_agendas_meeting_date ON agendas(meeting_date);
CREATE INDEX IF NOT EXISTS idx_agendas_status ON agendas(status);

-- Matters arising indexes
CREATE INDEX IF NOT EXISTS idx_matters_arising_agenda ON matters_arising(agenda_id);
CREATE INDEX IF NOT EXISTS idx_matters_arising_status ON matters_arising(status);

-- Attendance indexes
CREATE INDEX IF NOT EXISTS idx_attendance_agenda ON attendance(agenda_id);
CREATE INDEX IF NOT EXISTS idx_attendance_proponent ON attendance(proponent_id);
CREATE INDEX IF NOT EXISTS idx_attendance_is_present ON attendance(is_present);

-- Document indexes
CREATE INDEX IF NOT EXISTS idx_documents_proponent ON documents(proponent_id);
CREATE INDEX IF NOT EXISTS idx_documents_quarter ON documents(quarter_id);
CREATE INDEX IF NOT EXISTS idx_documents_category ON documents(category);
CREATE INDEX IF NOT EXISTS idx_documents_status ON documents(status);
CREATE INDEX IF NOT EXISTS idx_documents_upload_date ON documents(upload_date);

-- Voice recording indexes
CREATE INDEX IF NOT EXISTS idx_voice_recordings_agenda ON voice_recordings(agenda_id);

-- Notes indexes
CREATE INDEX IF NOT EXISTS idx_notes_user ON notes(user_id);
CREATE INDEX IF NOT EXISTS idx_notes_mrfc ON notes(mrfc_id);
CREATE INDEX IF NOT EXISTS idx_notes_quarter ON notes(quarter_id);
CREATE INDEX IF NOT EXISTS idx_notes_agenda ON notes(agenda_id);

-- Notification indexes
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

-- User MRFC access indexes
CREATE INDEX IF NOT EXISTS idx_user_mrfc_access_user ON user_mrfc_access(user_id);
CREATE INDEX IF NOT EXISTS idx_user_mrfc_access_mrfc ON user_mrfc_access(mrfc_id);

-- Compliance log indexes
CREATE INDEX IF NOT EXISTS idx_compliance_logs_proponent ON compliance_logs(proponent_id);
CREATE INDEX IF NOT EXISTS idx_compliance_logs_quarter ON compliance_logs(quarter_id);
CREATE INDEX IF NOT EXISTS idx_compliance_logs_status ON compliance_logs(status);
CREATE INDEX IF NOT EXISTS idx_compliance_logs_calculated_at ON compliance_logs(calculated_at);

-- Audit log indexes
CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);

-- ==========================================
-- CREATE TRIGGERS FOR UPDATED_AT
-- ==========================================
-- FIXED: Added DROP TRIGGER IF EXISTS for idempotent schema execution

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to all tables with updated_at column
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_mrfcs_updated_at ON mrfcs;
CREATE TRIGGER update_mrfcs_updated_at BEFORE UPDATE ON mrfcs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_proponents_updated_at ON proponents;
CREATE TRIGGER update_proponents_updated_at BEFORE UPDATE ON proponents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_agendas_updated_at ON agendas;
CREATE TRIGGER update_agendas_updated_at BEFORE UPDATE ON agendas
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_matters_arising_updated_at ON matters_arising;
CREATE TRIGGER update_matters_arising_updated_at BEFORE UPDATE ON matters_arising
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_documents_updated_at ON documents;
CREATE TRIGGER update_documents_updated_at BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_notes_updated_at ON notes;
CREATE TRIGGER update_notes_updated_at BEFORE UPDATE ON notes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ==========================================
-- INSERT DEFAULT DATA
-- ==========================================
-- FIXED: Added ON CONFLICT DO NOTHING for idempotent schema execution

-- Insert default quarters for 2025
INSERT INTO quarters (name, year, quarter_number, start_date, end_date, is_current) VALUES
('Q1 2025', 2025, 1, '2025-01-01', '2025-03-31', FALSE),
('Q2 2025', 2025, 2, '2025-04-01', '2025-06-30', FALSE),
('Q3 2025', 2025, 3, '2025-07-01', '2025-09-30', FALSE),
('Q4 2025', 2025, 4, '2025-10-01', '2025-12-31', TRUE)
ON CONFLICT (name) DO NOTHING;

-- Note: Super admin user will be created by the application on first run
-- See: src/server.ts -> initializeDatabase()

-- ==========================================
-- SCHEMA COMPLETE
-- ==========================================
-- Total tables: 14
-- Total indexes: 40+
-- Total triggers: 7
-- ==========================================
