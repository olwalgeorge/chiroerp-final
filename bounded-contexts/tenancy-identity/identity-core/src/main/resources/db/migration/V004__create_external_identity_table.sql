CREATE TABLE IF NOT EXISTS identity_external_identities (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES identity_users(id) ON DELETE CASCADE,
    provider VARCHAR(20) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    claims_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_identity_external_identity_user_provider_subject UNIQUE (user_id, provider, subject)
);

CREATE INDEX IF NOT EXISTS idx_identity_external_identities_user
    ON identity_external_identities(user_id);

CREATE INDEX IF NOT EXISTS idx_identity_external_identities_provider_subject
    ON identity_external_identities(provider, subject);
