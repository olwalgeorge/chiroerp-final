-- ChiroERP Finance GL Database Initialization
-- This script runs on first PostgreSQL container startup
-- Creates schema, extensions, and basic setup for General Ledger subdomain

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- For fuzzy text search
CREATE EXTENSION IF NOT EXISTS "btree_gist";  -- For advanced indexing

-- Create application schema for GL subdomain
CREATE SCHEMA IF NOT EXISTS finance_gl;

-- Set default schema search path
ALTER DATABASE finance_gl SET search_path TO finance_gl, public;

-- Create audit trigger function (used across all tables)
CREATE OR REPLACE FUNCTION finance_gl.update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create tenant_id validation function
CREATE OR REPLACE FUNCTION finance_gl.validate_tenant_id()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.tenant_id IS NULL THEN
        RAISE EXCEPTION 'tenant_id cannot be NULL';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Grant permissions to chiroerp user
GRANT ALL PRIVILEGES ON SCHEMA finance_gl TO chiroerp;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA finance_gl TO chiroerp;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA finance_gl TO chiroerp;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA finance_gl GRANT ALL ON TABLES TO chiroerp;
ALTER DEFAULT PRIVILEGES IN SCHEMA finance_gl GRANT ALL ON SEQUENCES TO chiroerp;

-- Create application user for runtime connections (Phase 1)
-- Password will be overridden by environment variables in production
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'finance_gl_app') THEN
        CREATE USER finance_gl_app WITH PASSWORD 'finance_gl_dev_password';
    END IF;
END
$$;

GRANT CONNECT ON DATABASE finance_gl TO finance_gl_app;
GRANT USAGE ON SCHEMA finance_gl TO finance_gl_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA finance_gl TO finance_gl_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA finance_gl TO finance_gl_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA finance_gl GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO finance_gl_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA finance_gl GRANT USAGE, SELECT ON SEQUENCES TO finance_gl_app;

-- Create readonly user for reporting/analytics (Phase 2)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'finance_gl_readonly') THEN
        CREATE USER finance_gl_readonly WITH PASSWORD 'finance_gl_readonly_password';
    END IF;
END
$$;

GRANT CONNECT ON DATABASE finance_gl TO finance_gl_readonly;
GRANT USAGE ON SCHEMA finance_gl TO finance_gl_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA finance_gl TO finance_gl_readonly;

ALTER DEFAULT PRIVILEGES IN SCHEMA finance_gl GRANT SELECT ON TABLES TO finance_gl_readonly;

-- Log initialization completion
DO $$
BEGIN
    RAISE NOTICE 'ChiroERP Finance GL database initialized successfully';
    RAISE NOTICE 'Schema: finance_gl';
    RAISE NOTICE 'Users: chiroerp (owner), finance_gl_app (runtime), finance_gl_readonly (reporting)';
    RAISE NOTICE 'Extensions: uuid-ossp, pg_trgm, btree_gist';
END
$$;
