# Manufacturing MRP - ADR-037

> **Bounded Context:** `manufacturing-mrp`  
> **Port:** `9301`  
> **Database:** `chiroerp_manufacturing_mrp`  
> **Kafka Consumer Group:** `manufacturing-mrp-cg`

## Overview

MRP owns **net requirements planning** for materials and subassemblies. It converts demand signals into planned orders and purchase requisitions, and feeds Production Orders and Procurement with time-phased supply plans.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Demand, netting, planned orders, pegging |
| **Aggregates** | MRPPlan, PlannedOrder, DemandSignal |
| **Key Events** | PlannedOrderCreatedEvent, MRPRunCompletedEvent |
| **Integration** | Sales demand, Inventory availability, Procurement PRs |
| **Compliance** | Audit trail for planning changes |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [mrp-domain.md](./mrp/mrp-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [mrp-application.md](./mrp/mrp-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [mrp-infrastructure.md](./mrp/mrp-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [mrp-api.md](./mrp/mrp-api.md) | Endpoints and DTOs |
| **Events & Integration** | [mrp-events.md](./mrp/mrp-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
manufacturing-mrp/
|-- mrp-domain/
|-- mrp-application/
`-- mrp-infrastructure/
```

## Architecture Diagram

```
+--------------------------------------------------------------------------+
|                            MRP SERVICE                                    |
|--------------------------------------------------------------------------+
|  Demand Signals -> Netting Engine -> Planned Orders -> PR/Prod Orders     |
`--------------------------------------------------------------------------+
```

## Key Business Capabilities

1. Net requirements planning across BOM levels.
2. Time-phased pegging of demand to supply.
3. Planned order creation and release recommendations.
4. Exception messaging for shortages and delays.

## Integration Points

- **Sales**: MTO demand signals
- **Inventory**: on-hand and safety stock
- **Procurement**: planned orders to PR
- **Production Orders**: convert planned orders

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| MRP run duration | < 2h p95 (100k SKUs) | > 3h |
| Planned order creation | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Planning run audit trail
- Scenario approval for manual overrides

## Related ADRs

- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
