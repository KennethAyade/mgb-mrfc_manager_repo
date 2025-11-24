-- Migration: Create voice_recordings table
-- Date: 2025-11-24
-- Description: Table for storing meeting voice recordings with title and description

-- Create voice_recordings table
CREATE TABLE IF NOT EXISTS voice_recordings (
    id BIGSERIAL PRIMARY KEY,
    agenda_id BIGINT NOT NULL REFERENCES agendas(id) ON DELETE CASCADE,
    recording_name VARCHAR(255) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_cloudinary_id VARCHAR(255),
    duration INTEGER,
    file_size BIGINT,
    recorded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_voice_recordings_agenda_id ON voice_recordings(agenda_id);
CREATE INDEX IF NOT EXISTS idx_voice_recordings_recorded_by ON voice_recordings(recorded_by);
CREATE INDEX IF NOT EXISTS idx_voice_recordings_recorded_at ON voice_recordings(recorded_at);

-- Add comments for documentation
COMMENT ON TABLE voice_recordings IS 'Stores audio recordings for meetings';
COMMENT ON COLUMN voice_recordings.duration IS 'Duration in seconds';
COMMENT ON COLUMN voice_recordings.file_size IS 'File size in bytes';
COMMENT ON COLUMN voice_recordings.file_cloudinary_id IS 'S3 key for the audio file';
