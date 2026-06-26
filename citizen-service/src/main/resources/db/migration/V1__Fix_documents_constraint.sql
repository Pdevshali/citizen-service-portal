-- Fix check constraint on documents table to include CERTIFICATE type
ALTER TABLE documents DROP CONSTRAINT IF EXISTS documents_document_type_check;

ALTER TABLE documents
ADD CONSTRAINT documents_document_type_check
CHECK (document_type IN ('AADHAAR', 'PAN', 'PASSPORT', 'CERTIFICATE'));

