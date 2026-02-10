-- V004: Add status column for terminal failure policy (dead-letter)
-- Finding #2: Enables max-attempts limit and DEAD state for poison events

-- Add status column with PENDING/PUBLISHED/DEAD states
ALTER TABLE tenant_outbox
    ADD COLUMN IF NOT EXISTS status VARCHAR(12) NOT NULL DEFAULT 'PENDING';

-- Migrate existing rows: set status based on published_at
UPDATE tenant_outbox SET status = 'PUBLISHED' WHERE published_at IS NOT NULL AND status = 'PENDING';

-- Create index for status-based queries (pending entries ready for relay)
CREATE INDEX IF NOT EXISTS idx_tenant_outbox_status_pending
    ON tenant_outbox(next_attempt_at, created_at)
    WHERE status = 'PENDING';

-- Add index for dead-letter monitoring/alerting
CREATE INDEX IF NOT EXISTS idx_tenant_outbox_dead
    ON tenant_outbox(created_at)
    WHERE status = 'DEAD';

COMMENT ON COLUMN tenant_outbox.status IS 'PENDING = awaiting dispatch, PUBLISHED = successfully sent, DEAD = max attempts exceeded (requires manual intervention)';
