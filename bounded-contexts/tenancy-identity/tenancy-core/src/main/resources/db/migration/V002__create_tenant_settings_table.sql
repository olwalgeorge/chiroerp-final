CREATE TABLE IF NOT EXISTS tenant_settings (
    tenant_id UUID PRIMARY KEY REFERENCES tenants(id) ON DELETE CASCADE,
    locale VARCHAR(20) NOT NULL DEFAULT 'en_US',
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    feature_flags JSONB NOT NULL DEFAULT '{}'::jsonb,
    custom_config JSONB NOT NULL DEFAULT '{}'::jsonb,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tenant_settings_feature_flags
    ON tenant_settings USING GIN(feature_flags);
