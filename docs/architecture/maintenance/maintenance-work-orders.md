# Plant Maintenance Work Orders - ADR-040

> **Bounded Context:** `maintenance-work-orders`  
> **Port:** `9602`  
> **Database:** `chiroerp_maintenance_work_orders`  
> **Kafka Consumer Group:** `maintenance-work-orders-cg`

## Overview

Work Orders manage **planning, execution, and settlement** of maintenance activities. They capture tasks, labor, parts, and confirmations, then post actual costs to Controlling and Finance.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Work orders, task lists, confirmations |
| **Aggregates** | WorkOrder, WorkOrderOperation, MaintenanceTask |
| **Key Events** | WorkOrderCreatedEvent, WorkOrderCompletedEvent, MaintenanceCostPostedEvent |
| **Integration** | Inventory, Procurement, Finance/CO |
| **Compliance** | OSHA/LOTO tracking |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [work-orders-domain.md](./work-orders/work-orders-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [work-orders-application.md](./work-orders/work-orders-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [work-orders-infrastructure.md](./work-orders/work-orders-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [work-orders-api.md](./work-orders/work-orders-api.md) | Endpoints and DTOs |
| **Events & Integration** | [work-orders-events.md](./work-orders/work-orders-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
maintenance-work-orders/
|-- work-orders-domain/
|-- work-orders-application/
`-- work-orders-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                        WORK ORDER MANAGEMENT                            |
|-----------------------------------------------------------------------|
|  Plan -> Schedule -> Execute -> Confirm -> Close -> Cost Settlement    |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Work order creation and classification.
2. Task lists with labor, tools, and parts.
3. Execution confirmations and time tracking.
4. Cost settlement to cost centers and assets.

## Integration Points

- **Inventory**: reserve and issue spare parts.
- **Procurement**: external services and parts sourcing.
- **Finance/CO**: cost posting and budgets.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Work order creation | < 2s p95 | > 5s |
| Confirmation posting | < 1s p95 | > 3s |
| API availability | 99.7% | < 99.5% |

## Compliance & Audit

- LOTO steps logged for safety compliance.
- Work order history retained for audits.

## Related ADRs

- [ADR-040: Plant Maintenance](../../adr/ADR-040-plant-maintenance.md)
- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
