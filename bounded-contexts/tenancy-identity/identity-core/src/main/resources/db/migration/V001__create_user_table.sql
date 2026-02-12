CREATE TABLE IF NOT EXISTS identity_users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    email VARCHAR(320) NOT NULL,
    phone_number VARCHAR(60) NULL,
    locale VARCHAR(20) NOT NULL DEFAULT 'en-US',
    time_zone_id VARCHAR(80) NOT NULL DEFAULT 'UTC',
    password_hash VARCHAR(255) NOT NULL,
    password_version INTEGER NOT NULL DEFAULT 1,
    password_changed_at TIMESTAMPTZ NOT NULL,
    password_history_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    password_expires_at TIMESTAMPTZ NULL,
    status VARCHAR(20) NOT NULL,
    mfa_methods_json JSONB NULL,
    mfa_shared_secret VARCHAR(255) NULL,
    mfa_backup_codes_json JSONB NULL,
    mfa_enrolled_at TIMESTAMPTZ NULL,
    mfa_verified_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMPTZ NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_identity_users_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX IF NOT EXISTS idx_identity_users_tenant_status_created
    ON identity_users(tenant_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_identity_users_tenant_last_login
    ON identity_users(tenant_id, last_login_at DESC);
