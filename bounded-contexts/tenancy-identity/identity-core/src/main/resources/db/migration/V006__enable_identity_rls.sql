-- ADR-005 defense-in-depth: DB-level tenant isolation policies.
-- Policy is permissive when app.current_tenant_id is unset so existing
-- system/background flows continue to work during incremental rollout.

ALTER TABLE identity_users ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS p_identity_users_tenant_scope ON identity_users;
CREATE POLICY p_identity_users_tenant_scope
    ON identity_users
    USING (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
    )
    WITH CHECK (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
    );

ALTER TABLE identity_user_roles ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS p_identity_user_roles_tenant_scope ON identity_user_roles;
CREATE POLICY p_identity_user_roles_tenant_scope
    ON identity_user_roles
    USING (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR EXISTS (
            SELECT 1
            FROM identity_users u
            WHERE u.id = identity_user_roles.user_id
              AND u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        )
    )
    WITH CHECK (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR EXISTS (
            SELECT 1
            FROM identity_users u
            WHERE u.id = identity_user_roles.user_id
              AND u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        )
    );

ALTER TABLE identity_permissions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS p_identity_permissions_tenant_scope ON identity_permissions;
CREATE POLICY p_identity_permissions_tenant_scope
    ON identity_permissions
    USING (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR EXISTS (
            SELECT 1
            FROM identity_users u
            WHERE u.id = identity_permissions.user_id
              AND u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        )
    )
    WITH CHECK (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR EXISTS (
            SELECT 1
            FROM identity_users u
            WHERE u.id = identity_permissions.user_id
              AND u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        )
    );

ALTER TABLE identity_external_identities ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS p_identity_external_identities_tenant_scope ON identity_external_identities;
CREATE POLICY p_identity_external_identities_tenant_scope
    ON identity_external_identities
    USING (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR EXISTS (
            SELECT 1
            FROM identity_users u
            WHERE u.id = identity_external_identities.user_id
              AND u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        )
    )
    WITH CHECK (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR EXISTS (
            SELECT 1
            FROM identity_users u
            WHERE u.id = identity_external_identities.user_id
              AND u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
        )
    );

ALTER TABLE identity_outbox ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS p_identity_outbox_tenant_scope ON identity_outbox;
CREATE POLICY p_identity_outbox_tenant_scope
    ON identity_outbox
    USING (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
    )
    WITH CHECK (
        NULLIF(current_setting('app.current_tenant_id', true), '') IS NULL
        OR tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
    );
