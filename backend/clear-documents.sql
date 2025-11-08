-- ================================================
-- CLEAR ALL DOCUMENTS
-- ================================================
-- This script deletes all documents from the database
-- Run this in your PostgreSQL client or pgAdmin
-- ================================================

-- Show current document count
SELECT 'Documents before deletion:' as message, COUNT(*) as count FROM documents;

-- Delete all documents
DELETE FROM documents;

-- Show result
SELECT 'Documents after deletion:' as message, COUNT(*) as count FROM documents;

-- Reset the sequence (optional)
ALTER SEQUENCE documents_id_seq RESTART WITH 1;

SELECT '✅ All documents cleared! You can now re-upload with proper public access.' as message;

