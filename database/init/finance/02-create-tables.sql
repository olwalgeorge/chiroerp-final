-- ChiroERP Finance Domain Tables
-- Phase 0: Schema definitions for domain entities
-- Phase 1: Flyway will manage migrations

-- NOTE: This is a bootstrap schema for Phase 0 development
-- In Phase 1, we'll use Flyway versioned migrations instead

SET search_path TO finance, public;

-- ============================================================================
-- GL Account - Chart of Accounts
-- ============================================================================

CREATE TABLE IF NOT EXISTS finance.gl_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL CHECK (account_type IN ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE')),
    balance_type VARCHAR(10) NOT NULL CHECK (balance_type IN ('DEBIT', 'CREDIT')),
    parent_account_id UUID REFERENCES finance.gl_accounts(id),
    currency_code CHAR(3) NOT NULL DEFAULT 'USD',
    is_control_account BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    cost_center_id UUID,
    profit_center_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT uk_gl_account_number UNIQUE (tenant_id, account_number),
    CONSTRAINT chk_account_number_format CHECK (account_number ~ '^[0-9]{4,10}(-[0-9]{3})?$'),
    CONSTRAINT chk_currency_code CHECK (LENGTH(currency_code) = 3)
);

-- Indexes for performance
CREATE INDEX idx_gl_accounts_tenant ON finance.gl_accounts(tenant_id);
CREATE INDEX idx_gl_accounts_type ON finance.gl_accounts(account_type) WHERE is_active = true;
CREATE INDEX idx_gl_accounts_parent ON finance.gl_accounts(parent_account_id) WHERE parent_account_id IS NOT NULL;
CREATE INDEX idx_gl_accounts_number ON finance.gl_accounts(tenant_id, account_number);

-- Audit trigger
CREATE TRIGGER trg_gl_accounts_updated_at
    BEFORE UPDATE ON finance.gl_accounts
    FOR EACH ROW
    EXECUTE FUNCTION finance.update_updated_at();

-- Tenant validation trigger
CREATE TRIGGER trg_gl_accounts_tenant
    BEFORE INSERT OR UPDATE ON finance.gl_accounts
    FOR EACH ROW
    EXECUTE FUNCTION finance.validate_tenant_id();

-- ============================================================================
-- Journal Entry - Double-Entry Transactions
-- ============================================================================

CREATE TABLE IF NOT EXISTS finance.journal_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    entry_number VARCHAR(50) NOT NULL,
    entry_date DATE NOT NULL,
    posting_date DATE NOT NULL,
    description TEXT NOT NULL,
    reference_type VARCHAR(50),
    reference_id UUID,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'POSTED', 'REVERSED')) DEFAULT 'DRAFT',
    currency_code CHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate DECIMAL(18, 6) NOT NULL DEFAULT 1.0,
    reversed_by UUID REFERENCES finance.journal_entries(id),
    reversal_entry_id UUID REFERENCES finance.journal_entries(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID NOT NULL,
    posted_at TIMESTAMP,
    posted_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT uk_journal_entry_number UNIQUE (tenant_id, entry_number),
    CONSTRAINT chk_posting_date CHECK (posting_date >= entry_date),
    CONSTRAINT chk_posted_fields CHECK (
        (status = 'POSTED' AND posted_at IS NOT NULL AND posted_by IS NOT NULL) OR
        (status != 'POSTED' AND posted_at IS NULL AND posted_by IS NULL)
    )
);

-- Indexes
CREATE INDEX idx_journal_entries_tenant ON finance.journal_entries(tenant_id);
CREATE INDEX idx_journal_entries_status ON finance.journal_entries(status);
CREATE INDEX idx_journal_entries_date ON finance.journal_entries(tenant_id, entry_date);
CREATE INDEX idx_journal_entries_posting_date ON finance.journal_entries(tenant_id, posting_date);
CREATE INDEX idx_journal_entries_reference ON finance.journal_entries(reference_type, reference_id);

-- Audit trigger
CREATE TRIGGER trg_journal_entries_updated_at
    BEFORE UPDATE ON finance.journal_entries
    FOR EACH ROW
    EXECUTE FUNCTION finance.update_updated_at();

-- Tenant validation trigger
CREATE TRIGGER trg_journal_entries_tenant
    BEFORE INSERT OR UPDATE ON finance.journal_entries
    FOR EACH ROW
    EXECUTE FUNCTION finance.validate_tenant_id();

-- ============================================================================
-- Journal Entry Lines - Individual debit/credit entries
-- ============================================================================

CREATE TABLE IF NOT EXISTS finance.journal_entry_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    journal_entry_id UUID NOT NULL REFERENCES finance.journal_entries(id) ON DELETE CASCADE,
    line_number INTEGER NOT NULL,
    account_id UUID NOT NULL REFERENCES finance.gl_accounts(id),
    debit_amount DECIMAL(18, 2),
    credit_amount DECIMAL(18, 2),
    description TEXT,
    cost_center_id UUID,
    profit_center_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    
    -- Constraints
    CONSTRAINT uk_journal_entry_line UNIQUE (journal_entry_id, line_number),
    CONSTRAINT chk_debit_or_credit CHECK (
        (debit_amount IS NOT NULL AND credit_amount IS NULL) OR
        (debit_amount IS NULL AND credit_amount IS NOT NULL)
    ),
    CONSTRAINT chk_positive_amounts CHECK (
        (debit_amount IS NULL OR debit_amount >= 0) AND
        (credit_amount IS NULL OR credit_amount >= 0)
    )
);

-- Indexes
CREATE INDEX idx_journal_entry_lines_tenant ON finance.journal_entry_lines(tenant_id);
CREATE INDEX idx_journal_entry_lines_entry ON finance.journal_entry_lines(journal_entry_id);
CREATE INDEX idx_journal_entry_lines_account ON finance.journal_entry_lines(account_id);
CREATE INDEX idx_journal_entry_lines_cost_center ON finance.journal_entry_lines(cost_center_id) WHERE cost_center_id IS NOT NULL;

-- Tenant validation trigger
CREATE TRIGGER trg_journal_entry_lines_tenant
    BEFORE INSERT OR UPDATE ON finance.journal_entry_lines
    FOR EACH ROW
    EXECUTE FUNCTION finance.validate_tenant_id();

-- ============================================================================
-- GL Balances - Materialized view for performance
-- ============================================================================

CREATE MATERIALIZED VIEW IF NOT EXISTS finance.gl_balances AS
SELECT
    jel.tenant_id,
    jel.account_id,
    gla.account_number,
    gla.account_name,
    gla.account_type,
    gla.balance_type,
    COUNT(DISTINCT jel.journal_entry_id) as transaction_count,
    COALESCE(SUM(jel.debit_amount), 0) as total_debits,
    COALESCE(SUM(jel.credit_amount), 0) as total_credits,
    CASE
        WHEN gla.balance_type = 'DEBIT' THEN COALESCE(SUM(jel.debit_amount), 0) - COALESCE(SUM(jel.credit_amount), 0)
        ELSE COALESCE(SUM(jel.credit_amount), 0) - COALESCE(SUM(jel.debit_amount), 0)
    END as current_balance,
    MAX(je.posting_date) as last_activity_date
FROM finance.journal_entry_lines jel
INNER JOIN finance.gl_accounts gla ON jel.account_id = gla.id
INNER JOIN finance.journal_entries je ON jel.journal_entry_id = je.id
WHERE je.status = 'POSTED'
GROUP BY jel.tenant_id, jel.account_id, gla.account_number, gla.account_name, gla.account_type, gla.balance_type;

-- Index on materialized view
CREATE UNIQUE INDEX idx_gl_balances_pk ON finance.gl_balances(tenant_id, account_id);
CREATE INDEX idx_gl_balances_type ON finance.gl_balances(account_type);

-- Log table creation
DO $$
BEGIN
    RAISE NOTICE 'Finance domain tables created successfully';
    RAISE NOTICE 'Tables: gl_accounts, journal_entries, journal_entry_lines';
    RAISE NOTICE 'Views: gl_balances (materialized)';
END
$$;
