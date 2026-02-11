CREATE TABLE IF NOT EXISTS identity_outbox (
    event_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    payload JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
    published_at TIMESTAMPTZ NULL,
    publish_attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_error TEXT NULL,
    CONSTRAINT chk_identity_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'DEAD'))
);

CREATE INDEX IF NOT EXISTS idx_identity_outbox_status_pending
    ON identity_outbox(next_attempt_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_identity_outbox_dead
    ON identity_outbox(created_at)
    WHERE status = 'DEAD';

CREATE INDEX IF NOT EXISTS idx_identity_outbox_tenant_created
    ON identity_outbox(tenant_id, created_at DESC);

COMMENT ON COLUMN identity_outbox.status IS 'PENDING = awaiting dispatch, PUBLISHED = successfully sent, DEAD = max attempts exceeded (requires manual intervention)';
