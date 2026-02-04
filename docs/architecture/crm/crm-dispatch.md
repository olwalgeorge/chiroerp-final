# CRM Dispatch - ADR-042

> **Bounded Context:** `crm-dispatch`
> **Port:** `9405`
> **Database:** `chiroerp_crm_dispatch`
> **Kafka Consumer Group:** `crm-dispatch-cg`

## Overview

Dispatch manages **technician scheduling, assignments, and route optimization hooks**. It orchestrates work assignments for Service Orders and records arrival and completion signals.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Scheduling, dispatch, routes |
| **Aggregates** | DispatchSchedule, Technician, WorkAssignment |
| **Key Events** | TechnicianAssignedEvent, DispatchConfirmedEvent, RouteOptimizedEvent |
| **Integration** | Service Orders, Inventory |
| **Compliance** | SLA reporting and audit trail |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [dispatch-domain.md](./dispatch/dispatch-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [dispatch-application.md](./dispatch/dispatch-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [dispatch-infrastructure.md](./dispatch/dispatch-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [dispatch-api.md](./dispatch/dispatch-api.md) | Endpoints and DTOs |
| **Events & Integration** | [dispatch-events.md](./dispatch/dispatch-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
crm-dispatch/
|-- dispatch-domain/
|-- dispatch-application/
`-- dispatch-infrastructure/
```

## Architecture Diagram

```
Schedule -> Assign -> Dispatch -> Arrive -> Complete
```

## Key Business Capabilities

1. Technician scheduling and assignment.
2. Route and travel time optimization hooks.
3. Dispatch updates and arrival confirmations.
4. SLA breach detection support.

## Integration Points

- **Service Orders**: assignments and status updates
- **Inventory**: parts availability

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Assignment latency | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Dispatch audit trail

## Related ADRs

- [ADR-042: Field Service Operations](../../adr/ADR-042-field-service-operations.md)
- [ADR-043: CRM and Customer Management](../../adr/ADR-043-crm-customer-management.md)
