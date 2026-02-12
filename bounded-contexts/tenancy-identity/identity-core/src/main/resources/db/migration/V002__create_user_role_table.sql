CREATE TABLE IF NOT EXISTS identity_user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES identity_users(id) ON DELETE CASCADE,
    role_code VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    sod_group VARCHAR(80) NULL,
    permissions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_identity_user_roles_user_code UNIQUE (user_id, role_code)
);

CREATE INDEX IF NOT EXISTS idx_identity_user_roles_user
    ON identity_user_roles(user_id);

CREATE INDEX IF NOT EXISTS idx_identity_user_roles_code
    ON identity_user_roles(role_code);
