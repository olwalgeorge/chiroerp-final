-- Journal Entry Tables Migration
-- Creates tables for journal entries and journal entry lines

-- Journal Entries Table
CREATE TABLE IF NOT EXISTS finance.journal_entries (
    entry_id VARCHAR(50) PRIMARY KEY,
    entry_number VARCHAR(20) NOT NULL UNIQUE,
    tenant_id UUID NOT NULL,
    company_code VARCHAR(20) NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    document_date DATE NOT NULL,
    posting_date DATE NOT NULL,
    fiscal_year INTEGER NOT NULL,
    fiscal_period INTEGER NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate NUMERIC(18, 6) NOT NULL DEFAULT 1.0,
    description VARCHAR(500) NOT NULL,
    reference VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    posted_by VARCHAR(100),
    posted_at TIMESTAMP WITH TIME ZONE,
    reversed_by VARCHAR(100),
    reversal_entry_id VARCHAR(50),
    CONSTRAINT fk_reversal_entry FOREIGN KEY (reversal_entry_id) 
        REFERENCES finance.journal_entries(entry_id)
);

-- Journal Entry Lines Table
CREATE TABLE IF NOT EXISTS finance.journal_entry_lines (
    line_id VARCHAR(50) PRIMARY KEY,
    entry_id VARCHAR(50) NOT NULL,
    line_number INTEGER NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    debit_amount NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
    credit_amount NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
    cost_center VARCHAR(20),
    profit_center VARCHAR(20),
    business_area VARCHAR(20),
    text VARCHAR(200),
    assignment VARCHAR(100),
    tax_code VARCHAR(20),
    quantity NUMERIC(18, 4),
    uom VARCHAR(10),
    CONSTRAINT fk_journal_entry FOREIGN KEY (entry_id) 
        REFERENCES finance.journal_entries(entry_id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_gl_account FOREIGN KEY (account_number) 
        REFERENCES finance.gl_accounts(account_number)
);

-- Indexes for performance
CREATE INDEX idx_journal_entries_tenant ON finance.journal_entries(tenant_id);
CREATE INDEX idx_journal_entries_entry_number ON finance.journal_entries(entry_number);
CREATE INDEX idx_journal_entries_status ON finance.journal_entries(status);
CREATE INDEX idx_journal_entries_posting_date ON finance.journal_entries(posting_date);
CREATE INDEX idx_journal_entries_company_code ON finance.journal_entries(company_code);
CREATE INDEX idx_journal_entries_fiscal_period ON finance.journal_entries(fiscal_year, fiscal_period);
CREATE INDEX idx_journal_entries_created_by ON finance.journal_entries(created_by);
CREATE INDEX idx_journal_entries_reference ON finance.journal_entries(reference);

CREATE INDEX idx_journal_entry_lines_entry_id ON finance.journal_entry_lines(entry_id);
CREATE INDEX idx_journal_entry_lines_account_number ON finance.journal_entry_lines(account_number);
CREATE INDEX idx_journal_entry_lines_cost_center ON finance.journal_entry_lines(cost_center);
CREATE INDEX idx_journal_entry_lines_profit_center ON finance.journal_entry_lines(profit_center);

-- Comments for documentation
COMMENT ON TABLE finance.journal_entries IS 'Journal entries for double-entry bookkeeping';
COMMENT ON TABLE finance.journal_entry_lines IS 'Individual line items in journal entries (debits and credits)';

COMMENT ON COLUMN finance.journal_entries.entry_id IS 'Unique identifier for the journal entry';
COMMENT ON COLUMN finance.journal_entries.entry_number IS 'Business key (e.g., JE-2026-001)';
COMMENT ON COLUMN finance.journal_entries.tenant_id IS 'Multi-tenancy isolation key';
COMMENT ON COLUMN finance.journal_entries.status IS 'DRAFT, PENDING_APPROVAL, POSTED, REVERSED, CANCELLED';
COMMENT ON COLUMN finance.journal_entries.reversal_entry_id IS 'Self-reference to the reversal entry if reversed';

COMMENT ON COLUMN finance.journal_entry_lines.debit_amount IS 'Debit amount (must be 0 if credit_amount > 0)';
COMMENT ON COLUMN finance.journal_entry_lines.credit_amount IS 'Credit amount (must be 0 if debit_amount > 0)';
