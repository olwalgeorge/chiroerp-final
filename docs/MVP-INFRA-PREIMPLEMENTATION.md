# ChiroERP MVP Infrastructure (Pre-Implementation)

This document defines the **authoritative local infrastructure baseline** for the current stage where application modules are scaffold placeholders and build logic is hardened.

## Scope

- Stage: **Pre-implementation MVP preparation**
- Code status: domain/application/infrastructure code is mostly placeholder
- Goal: provide a stable, production-adaptable local runtime foundation for incremental implementation

## Active Database Strategy

For local development, the stack uses:

1. **One PostgreSQL app instance** (`postgres-app`) for business services
2. **Schema-per-service boundary** for scaffolded modules
3. **One dedicated Temporal PostgreSQL instance** (`postgres-temporal`) for workflow engine internals

### Schema Mapping (Current MVP Modules)

| Bounded context / service | Schema | Suggested DB role |
|---|---|---|
| Finance GL | `finance_gl` | `svc_finance_gl` |
| Finance AR | `finance_ar` | `svc_finance_ar` |
| Finance AP | `finance_ap` | `svc_finance_ap` |
| Finance Assets | `finance_assets` | `svc_finance_assets` |
| Finance Tax | `finance_tax` | `svc_finance_tax` |
| Tenancy Core | `tenancy_core` | `svc_tenancy_core` |
| Identity Core | `identity_core` | `svc_identity_core` |

Bootstrap SQL is in `database/init/postgres-app/01-init-mvp-schemas.sql`.

## Infrastructure Stack

Defined in `docker-compose.yml`:

- PostgreSQL (`postgres-app`, `postgres-temporal`)
- Redpanda + Schema Registry (`redpanda`)
- Redis
- Temporal + Temporal UI
- Jaeger
- Prometheus
- Grafana (provisioned datasource/dashboards)
- pgAdmin

## Observability Provisioning

- Prometheus config: `config/prometheus/prometheus.yml`
- Grafana datasource: `config/grafana/provisioning/datasources/prometheus.yml`
- Grafana dashboard provider: `config/grafana/provisioning/dashboards/default.yml`
- Seed dashboard: `config/grafana/dashboards/chiroerp-mvp-overview.json`

Prometheus includes placeholder scrape targets for planned host-run module ports:

- `8071` tenancy-core
- `8072` identity-core
- `8081` finance-gl
- `8082` finance-ar
- `8083` finance-ap
- `8084` finance-assets
- `8085` finance-tax

## How to Run

```bash
docker compose up -d
docker compose ps
docker compose logs -f postgres-app
```

## Production Adaptation Path

When moving from pre-MVP to production-grade deployment:

1. Split schema-per-service into **database-per-context** for critical domains.
2. Replace default passwords with secrets manager / vault-backed credentials.
3. Enforce TLS/mTLS for DB, Kafka, Redis, and service ingress.
4. Add backup/restore + PITR policy for each data store.
5. Add HA topology (multi-AZ Postgres, Kafka replication, Redis failover).
6. Add formal SLOs, alerting rules, runbooks, and on-call workflows.

This keeps today’s dev environment fast while preserving a clean migration path toward SAP-grade operational rigor.
