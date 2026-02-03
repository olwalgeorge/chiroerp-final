-- Add missing columns to gl_accounts for domain model compatibility
-- Migration to align database schema with domain model

ALTER TABLE finance.gl_accounts
ADD COLUMN IF NOT EXISTS is_posting_allowed BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN IF NOT EXISTS company_code VARCHAR(20) NOT NULL DEFAULT '1000',
ADD COLUMN IF NOT EXISTS description TEXT;

-- Add comment
COMMENT ON COLUMN finance.gl_accounts.is_posting_allowed IS 'Indicates if direct postings are allowed (some accounts are totals-only)';
COMMENT ON COLUMN finance.gl_accounts.company_code IS 'Company code for multi-company scenarios';
COMMENT ON COLUMN finance.gl_accounts.description IS 'Additional description for the account';
