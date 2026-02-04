-- Journal Entry Tables Migration
-- Creates tables for journal entries and journal entry lines matching the refactored domain model (ADR-001)
-- Uses UUID primary keys, audit fields, and optimistic locking

-- Journal Entries Table
CREATE TABLE IF NOT EXISTS finance.journal_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_number VARCHAR(50) NOT NULL,
    tenant_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    entry_type VARCHAR(30) NOT NULL,
    document_date DATE NOT NULL,
    posting_date DATE NOT NULL,
    fiscal_year INTEGER NOT NULL,
    fiscal_period INTEGER NOT NULL,
    description TEXT NOT NULL,
    reference_type VARCHAR(50),
    reference_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    currency_code VARCHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate NUMERIC(18, 6) NOT NULL DEFAULT 1.0,
    reversed_by VARCHAR(255),
    reversal_entry_id VARCHAR(255),
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    posted_by VARCHAR(255),
    posted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_journal_entry_number UNIQUE (tenant_id, entry_number)
);

-- Journal Entry Lines Table
CREATE TABLE IF NOT EXISTS finance.journal_entry_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    debit_amount NUMERIC(18, 2),
    credit_amount NUMERIC(18, 2),
    description TEXT,
    cost_center VARCHAR(255),
    profit_center VARCHAR(255),
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_journal_entry FOREIGN KEY (journal_entry_id)
        REFERENCES finance.journal_entries(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_gl_account FOREIGN KEY (tenant_id, account_number)
        REFERENCES finance.gl_accounts(tenant_id, account_number)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_journal_entries_tenant ON finance.journal_entries(tenant_id);
CREATE INDEX IF NOT EXISTS idx_journal_entries_entry_number ON finance.journal_entries(entry_number);
CREATE INDEX IF NOT EXISTS idx_journal_entries_status ON finance.journal_entries(status);
CREATE INDEX IF NOT EXISTS idx_journal_entries_posting_date ON finance.journal_entries(posting_date);
CREATE INDEX IF NOT EXISTS idx_journal_entries_company_code ON finance.journal_entries(company_code);
CREATE INDEX IF NOT EXISTS idx_journal_entries_fiscal_period ON finance.journal_entries(fiscal_year, fiscal_period);
CREATE INDEX IF NOT EXISTS idx_journal_entries_created_by ON finance.journal_entries(created_by);
CREATE INDEX IF NOT EXISTS idx_journal_entries_reference_id ON finance.journal_entries(reference_id);

CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_journal_entry_id ON finance.journal_entry_lines(journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_account_number ON finance.journal_entry_lines(account_number);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_cost_center ON finance.journal_entry_lines(cost_center);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_profit_center ON finance.journal_entry_lines(profit_center);
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_tenant ON finance.journal_entry_lines(tenant_id);

-- Comments for documentation
COMMENT ON TABLE finance.journal_entries IS 'Journal entries for double-entry bookkeeping (UUID-based schema from ADR-001)';
COMMENT ON TABLE finance.journal_entry_lines IS 'Individual line items in journal entries (debits and credits)';

COMMENT ON COLUMN finance.journal_entries.id IS 'UUID primary key';
COMMENT ON COLUMN finance.journal_entries.entry_number IS 'Business key (e.g., JE-2026-001)';
COMMENT ON COLUMN finance.journal_entries.tenant_id IS 'Multi-tenancy isolation key';
COMMENT ON COLUMN finance.journal_entries.status IS 'DRAFT, POSTED, REVERSED, CANCELLED';
COMMENT ON COLUMN finance.journal_entries.reversal_entry_id IS 'Reference to the reversal entry ID if reversed';
COMMENT ON COLUMN finance.journal_entries.version IS 'Optimistic locking version for concurrent updates';
COMMENT ON COLUMN finance.journal_entries.reference_type IS 'Type of source document (e.g., INVOICE, PAYMENT)';
COMMENT ON COLUMN finance.journal_entries.reference_id IS 'ID of source document';

COMMENT ON COLUMN finance.journal_entry_lines.id IS 'UUID primary key';
COMMENT ON COLUMN finance.journal_entry_lines.journal_entry_id IS 'Foreign key to journal_entries table';
COMMENT ON COLUMN finance.journal_entry_lines.debit_amount IS 'Debit amount (nullable, must be 0 or NULL if credit_amount > 0)';
COMMENT ON COLUMN finance.journal_entry_lines.credit_amount IS 'Credit amount (nullable, must be 0 or NULL if debit_amount > 0)';
