-- ChiroERP Pre-Implementation MVP bootstrap
-- Single PostgreSQL instance with schema-per-service isolation boundaries.
--
-- NOTE: Credentials here are development defaults. Use secrets/managed identities
-- in staging/production.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'svc_finance_gl') THEN
        CREATE ROLE svc_finance_gl LOGIN PASSWORD 'dev_password';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'svc_finance_ar') THEN
        CREATE ROLE svc_finance_ar LOGIN PASSWORD 'dev_password';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'svc_finance_ap') THEN
        CREATE ROLE svc_finance_ap LOGIN PASSWORD 'dev_password';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'svc_finance_assets') THEN
        CREATE ROLE svc_finance_assets LOGIN PASSWORD 'dev_password';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'svc_finance_tax') THEN
        CREATE ROLE svc_finance_tax LOGIN PASSWORD 'dev_password';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'svc_tenancy_core') THEN
        CREATE ROLE svc_tenancy_core LOGIN PASSWORD 'dev_password';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'svc_identity_core') THEN
        CREATE ROLE svc_identity_core LOGIN PASSWORD 'dev_password';
    END IF;
END
$$;

CREATE SCHEMA IF NOT EXISTS finance_gl AUTHORIZATION svc_finance_gl;
CREATE SCHEMA IF NOT EXISTS finance_ar AUTHORIZATION svc_finance_ar;
CREATE SCHEMA IF NOT EXISTS finance_ap AUTHORIZATION svc_finance_ap;
CREATE SCHEMA IF NOT EXISTS finance_assets AUTHORIZATION svc_finance_assets;
CREATE SCHEMA IF NOT EXISTS finance_tax AUTHORIZATION svc_finance_tax;
CREATE SCHEMA IF NOT EXISTS tenancy_core AUTHORIZATION svc_tenancy_core;
CREATE SCHEMA IF NOT EXISTS identity_core AUTHORIZATION svc_identity_core;

GRANT USAGE, CREATE ON SCHEMA finance_gl TO svc_finance_gl;
GRANT USAGE, CREATE ON SCHEMA finance_ar TO svc_finance_ar;
GRANT USAGE, CREATE ON SCHEMA finance_ap TO svc_finance_ap;
GRANT USAGE, CREATE ON SCHEMA finance_assets TO svc_finance_assets;
GRANT USAGE, CREATE ON SCHEMA finance_tax TO svc_finance_tax;
GRANT USAGE, CREATE ON SCHEMA tenancy_core TO svc_tenancy_core;
GRANT USAGE, CREATE ON SCHEMA identity_core TO svc_identity_core;

ALTER DEFAULT PRIVILEGES FOR ROLE svc_finance_gl IN SCHEMA finance_gl
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_finance_gl;
ALTER DEFAULT PRIVILEGES FOR ROLE svc_finance_ar IN SCHEMA finance_ar
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_finance_ar;
ALTER DEFAULT PRIVILEGES FOR ROLE svc_finance_ap IN SCHEMA finance_ap
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_finance_ap;
ALTER DEFAULT PRIVILEGES FOR ROLE svc_finance_assets IN SCHEMA finance_assets
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_finance_assets;
ALTER DEFAULT PRIVILEGES FOR ROLE svc_finance_tax IN SCHEMA finance_tax
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_finance_tax;
ALTER DEFAULT PRIVILEGES FOR ROLE svc_tenancy_core IN SCHEMA tenancy_core
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_tenancy_core;
ALTER DEFAULT PRIVILEGES FOR ROLE svc_identity_core IN SCHEMA identity_core
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO svc_identity_core;

-- Quick introspection view for local development
CREATE OR REPLACE VIEW public.mvp_service_schemas AS
SELECT nspname AS schema_name
FROM pg_namespace
WHERE nspname IN (
    'finance_gl',
    'finance_ar',
    'finance_ap',
    'finance_assets',
    'finance_tax',
    'tenancy_core',
    'identity_core'
)
ORDER BY nspname;
