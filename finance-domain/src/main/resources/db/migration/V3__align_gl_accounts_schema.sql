-- Align gl_accounts table schema with entity model from ADR-001
-- Fixes column type mismatches and length constraints

-- Drop foreign key constraint on parent_account_id before changing its type
ALTER TABLE finance.gl_accounts
    DROP CONSTRAINT IF EXISTS gl_accounts_parent_account_id_fkey;

-- Fix cost_center_id and profit_center_id types (UUID -> VARCHAR)
ALTER TABLE finance.gl_accounts
    ALTER COLUMN cost_center_id TYPE VARCHAR(255);

ALTER TABLE finance.gl_accounts
    ALTER COLUMN profit_center_id TYPE VARCHAR(255);

-- Fix parent_account_id type (UUID -> VARCHAR for account number reference)
-- Note: Existing UUID values will be lost; this should be run on fresh schema
ALTER TABLE finance.gl_accounts
    ALTER COLUMN parent_account_id TYPE VARCHAR(255);

-- Ensure currency_code is exactly 3 characters (CHAR(3) in init script, VARCHAR(3) in entity)
ALTER TABLE finance.gl_accounts
    ALTER COLUMN currency_code TYPE VARCHAR(3);

-- Comments
COMMENT ON COLUMN finance.gl_accounts.cost_center_id IS 'Default cost center for postings (varchar reference, not UUID FK)';
COMMENT ON COLUMN finance.gl_accounts.profit_center_id IS 'Default profit center for postings (varchar reference, not UUID FK)';
COMMENT ON COLUMN finance.gl_accounts.parent_account_id IS 'Parent account number for hierarchical chart of accounts (no FK - references account_number)';
