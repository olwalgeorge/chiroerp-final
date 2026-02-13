CREATE TABLE IF NOT EXISTS identity_tenant_event_consumption (
    event_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    occurred_at TIMESTAMPTZ NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_identity_tenant_event_consumption_tenant_processed
    ON identity_tenant_event_consumption(tenant_id, processed_at DESC);

ALTER TABLE identity_tenant_event_consumption ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS p_identity_tenant_event_consumption_tenant_scope ON identity_tenant_event_consumption;
CREATE POLICY p_identity_tenant_event_consumption_tenant_scope
    ON identity_tenant_event_consumption
    USING (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
    )
    WITH CHECK (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
    );

