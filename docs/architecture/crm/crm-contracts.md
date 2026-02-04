# CRM Contracts - ADR-043

> **Bounded Context:** `crm-contracts`
> **Port:** `9404`
> **Database:** `chiroerp_crm_contracts`
> **Kafka Consumer Group:** `crm-contracts-cg`

## Overview

Contracts manages **service entitlements, SLAs, renewals, and contract lifecycle** for customers. It validates coverage for Service Orders and provides renewal signals to Pipeline.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Contracts, entitlements, renewals |
| **Aggregates** | ServiceContract, Entitlement, SLA |
| **Key Events** | ContractActivatedEvent, ContractExpiredEvent, EntitlementConsumedEvent |
| **Integration** | Service Orders, Finance/AR, Pipeline |
| **Compliance** | Contract audit trail |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [contracts-domain.md](./contracts/contracts-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [contracts-application.md](./contracts/contracts-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [contracts-infrastructure.md](./contracts/contracts-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [contracts-api.md](./contracts/contracts-api.md) | Endpoints and DTOs |
| **Events & Integration** | [contracts-events.md](./contracts/contracts-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
crm-contracts/
|-- contracts-domain/
|-- contracts-application/
`-- contracts-infrastructure/
```

## Architecture Diagram

```
Contract -> Entitlement -> SLA -> Renew/Expire
```

## Key Business Capabilities

1. Contract lifecycle and renewal management.
2. Entitlement validation for service orders.
3. SLA definition and enforcement.
4. Renewal pipeline signals.

## Integration Points

- **Service Orders**: entitlement checks
- **Pipeline**: renewal opportunities
- **Finance/AR**: billing terms

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Entitlement lookup latency | < 200ms p95 | > 500ms |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Contract approvals and audit trail

## Related ADRs

- [ADR-043: CRM and Customer Management](../../adr/ADR-043-crm-customer-management.md)
- [ADR-042: Field Service Operations](../../adr/ADR-042-field-service-operations.md)
