# Manufacturing Production Orders - ADR-037

> **Bounded Context:** `manufacturing-production`  
> **Port:** `9302`  
> **Database:** `chiroerp_manufacturing_production`  
> **Kafka Consumer Group:** `manufacturing-production-cg`

## Overview

Production Orders own **execution control** from order release through component issue, operation confirmation, and finished goods receipt. It is the transactional backbone for WIP and shop floor integration.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Production orders, confirmations, WIP |
| **Aggregates** | ProductionOrder, OperationConfirmation, MaterialIssue |
| **Key Events** | ProductionOrderReleasedEvent, MaterialIssuedEvent, ProductionConfirmedEvent, ProductionReceiptPostedEvent |
| **Integration** | Inventory issues/receipts, Costing, Shop Floor |
| **Compliance** | SOX controls for WIP and postings |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [production-domain.md](./production/production-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [production-application.md](./production/production-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [production-infrastructure.md](./production/production-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [production-api.md](./production/production-api.md) | Endpoints and DTOs |
| **Events & Integration** | [production-events.md](./production/production-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
manufacturing-production/
|-- production-domain/
|-- production-application/
`-- production-infrastructure/
```

## Architecture Diagram

```
Planned Order -> Production Order -> Issue -> Confirm -> Receipt -> Close
```

## Key Business Capabilities

1. Release and dispatch production orders.
2. Component issue and backflush support.
3. Operation confirmations with labor and machine time.
4. WIP accumulation and receipt posting.

## Integration Points

- **MRP**: planned order conversion
- **Inventory**: material issues and receipts
- **Shop Floor**: dispatch and confirmations
- **Costing/CO**: WIP and variance postings

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Order release latency | < 2s p95 | > 5s |
| Confirmation latency | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Controlled postings for WIP and variances
- Approval workflow for order changes

## Related ADRs

- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md)
