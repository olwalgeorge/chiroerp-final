# Plant Maintenance Preventive Maintenance - ADR-040

> **Bounded Context:** `maintenance-preventive`  
> **Port:** `9603`  
> **Database:** `chiroerp_maintenance_preventive`  
> **Kafka Consumer Group:** `maintenance-preventive-cg`

## Overview

Preventive Maintenance owns **maintenance plans, schedules, and triggers** for time-based and counter-based maintenance. It generates work orders and ensures compliance with maintenance cycles.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Maintenance plans, schedules, triggers |
| **Aggregates** | MaintenancePlan, MaintenanceSchedule, Counter |
| **Key Events** | MaintenancePlanCreatedEvent, MaintenanceScheduleGeneratedEvent |
| **Integration** | Work Orders, Equipment Master |
| **Compliance** | PM compliance metrics |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [preventive-domain.md](./preventive/preventive-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [preventive-application.md](./preventive/preventive-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [preventive-infrastructure.md](./preventive/preventive-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [preventive-api.md](./preventive/preventive-api.md) | Endpoints and DTOs |
| **Events & Integration** | [preventive-events.md](./preventive/preventive-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
maintenance-preventive/
|-- preventive-domain/
|-- preventive-application/
`-- preventive-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                      PREVENTIVE MAINTENANCE                            |
|-----------------------------------------------------------------------|
|  Plans + Counters -> Schedules -> Work Orders -> Compliance            |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Time-based and counter-based maintenance plans.
2. Schedule generation with call horizons.
3. Automatic work order creation.
4. PM compliance tracking.

## Integration Points

- **Equipment Master**: plan linkage to equipment.
- **Work Orders**: auto-creation of PM orders.
- **Manufacturing**: downtime coordination.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Schedule generation | < 5s p95 | > 10s |
| Work order trigger | < 2s p95 | > 5s |
| API availability | 99.7% | < 99.5% |

## Compliance & Audit

- Plan changes require approval.
- Maintenance compliance reports retained.

## Related ADRs

- [ADR-040: Plant Maintenance](../../adr/ADR-040-plant-maintenance.md)
- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
