# CRM Service Orders - ADR-042

> **Bounded Context:** `crm-service-orders`
> **Port:** `9403`
> **Database:** `chiroerp_crm_service_orders`
> **Kafka Consumer Group:** `crm-service-orders-cg`

## Overview

Service Orders manages **field service work orders, SLAs, and service billing readiness**. It coordinates with Dispatch, Parts Consumption, Contracts, and Finance/AR.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Service orders, SLAs, service billing |
| **Aggregates** | ServiceOrder, WorkOrder, SLA |
| **Key Events** | ServiceOrderCreatedEvent, ServiceOrderScheduledEvent, ServiceOrderCompletedEvent |
| **Integration** | Dispatch, Inventory, Finance/AR |
| **Compliance** | SLA audit trail |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [service-orders-domain.md](./service-orders/service-orders-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [service-orders-application.md](./service-orders/service-orders-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [service-orders-infrastructure.md](./service-orders/service-orders-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [service-orders-api.md](./service-orders/service-orders-api.md) | Endpoints and DTOs |
| **Events & Integration** | [service-orders-events.md](./service-orders/service-orders-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
crm-service-orders/
|-- service-orders-domain/
|-- service-orders-application/
`-- service-orders-infrastructure/
```

## Architecture Diagram

```
Request -> Schedule -> Dispatch -> Complete -> Bill
```

## Key Business Capabilities

1. Service order lifecycle and SLA tracking.
2. Scheduling and dispatch integration.
3. Parts usage and billing readiness.
4. Customer notifications and status updates.

## Integration Points

- **Dispatch**: scheduling and assignment
- **Inventory**: parts usage
- **Finance/AR**: service billing
- **Contracts**: entitlement validation

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Service order creation | < 500ms p95 | > 1s |
| Dispatch assignment | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- SLA breach tracking
- Audit trail for billing approvals

## Related ADRs

- [ADR-042: Field Service Operations](../../adr/ADR-042-field-service-operations.md)
- [ADR-043: CRM and Customer Management](../../adr/ADR-043-crm-customer-management.md)
