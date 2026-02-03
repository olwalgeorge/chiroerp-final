# Manufacturing BOM Management - ADR-037

> **Bounded Context:** `manufacturing-bom`  
> **Port:** `9304`  
> **Database:** `chiroerp_manufacturing_bom`  
> **Kafka Consumer Group:** `manufacturing-bom-cg`

## Overview

BOM Management owns **multi-level bill of materials and routing master data** with effectivity dates, alternates, and variants. It is the source of truth for MRP and Production Orders.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | BOMs, routings, operations, alternates |
| **Aggregates** | BOM, BOMItem, Routing, Operation |
| **Key Events** | BOMPublishedEvent, RoutingUpdatedEvent |
| **Integration** | MRP, Production Orders, Costing |
| **Compliance** | Change control and approvals |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [bom-domain.md](./bom/bom-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [bom-application.md](./bom/bom-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [bom-infrastructure.md](./bom/bom-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [bom-api.md](./bom/bom-api.md) | Endpoints and DTOs |
| **Events & Integration** | [bom-events.md](./bom/bom-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
manufacturing-bom/
|-- bom-domain/
|-- bom-application/
`-- bom-infrastructure/
```

## Architecture Diagram

```
BOM -> Routing -> Operation -> Effectivity -> Publish
```

## Key Business Capabilities

1. Multi-level BOMs with alternates and variants.
2. Routing definitions with work centers and times.
3. Engineering change control and approvals.
4. Effectivity date and revision management.

## Integration Points

- **MRP**: BOM explosion
- **Production Orders**: routing and operation plans
- **Costing**: standard cost rollup

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| BOM publish latency | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Change control for BOM revisions
- Approval workflow for routing updates

## Related ADRs

- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md)
