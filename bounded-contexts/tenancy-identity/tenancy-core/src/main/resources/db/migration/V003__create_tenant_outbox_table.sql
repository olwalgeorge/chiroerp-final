CREATE TABLE IF NOT EXISTS tenant_outbox (
    event_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    payload JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ NULL,
    publish_attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_error TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_tenant_outbox_pending
    ON tenant_outbox(next_attempt_at, created_at)
    WHERE published_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_tenant_outbox_tenant_created
    ON tenant_outbox(tenant_id, created_at DESC);
