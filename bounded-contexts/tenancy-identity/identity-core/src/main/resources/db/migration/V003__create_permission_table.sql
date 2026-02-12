CREATE TABLE IF NOT EXISTS identity_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES identity_users(id) ON DELETE CASCADE,
    object_id VARCHAR(120) NOT NULL,
    actions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    constraints_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_identity_permissions_user_object UNIQUE (user_id, object_id)
);

CREATE INDEX IF NOT EXISTS idx_identity_permissions_user
    ON identity_permissions(user_id);

CREATE INDEX IF NOT EXISTS idx_identity_permissions_object
    ON identity_permissions(object_id);
