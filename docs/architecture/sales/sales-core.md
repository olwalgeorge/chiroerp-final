# Sales Core - ADR-025

> **Bounded Context:** `sales-core`  
> **Port:** `9201`  
> **Database:** `chiroerp_sales_core`  
> **Kafka Consumer Group:** `sales-core-cg`

## Overview

Sales Core owns **order lifecycle management** from quote to order, allocation, and billing readiness. It enforces order approvals, change control, and publishes fulfillment and billing events to Inventory and Finance/AR.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Quotes, sales orders, order lifecycle, approvals |
| **Aggregates** | SalesOrder, Quote, OrderLine, DeliveryPlan |
| **Key Events** | SalesOrderCreatedEvent, SalesOrderAllocatedEvent, SalesOrderFulfilledEvent |
| **GL Integration** | Fulfillment events to AR for billing |
| **Compliance** | SOX approvals, audit trail, pricing override controls |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [core-domain.md](./core/core-domain.md) | Aggregates, entities, value objects, domain events, exceptions, services |
| **Application Layer** | [core-application.md](./core/core-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [core-infrastructure.md](./core/core-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [core-api.md](./core/core-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [core-events.md](./core/core-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |

## Bounded Context

```
sales-core/
├── core-domain/
├── core-application/
└── core-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SALES-CORE SERVICE                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      APPLICATION LAYER                               │    │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │    │
│  │  │ CreateOrder      │  │ AllocateOrder    │  │ MarkFulfilled     │  │    │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │  │    │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │    │
│  │           │                    │                       │             │    │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │    │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │    │
│  │  │ OrderRepo │ PricingPort │ InventoryPort │ EventPublisherPort    │  │    │
│  │  └────────────────────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                        DOMAIN LAYER                                  │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │    │
│  │  │ SalesOrder   │  │ Quote        │  │ OrderLine    │  │ Delivery  │ │    │
│  │  │ Aggregate    │  │ Aggregate    │  │ Entity       │  │ Plan Agg  │ │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └───────────┘ │    │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │    │
│  │  │ Order Policy        │  │ Domain Services                      │    │    │
│  │  │ Credit/Pricing       │  │ Allocation, Change Control          │    │    │
│  │  └────────────────────┘  └─────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                    INFRASTRUCTURE LAYER                              │    │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │    │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ Scheduler  │   │    │
│  │  │ (Quarkus)   │  │ (PostgreSQL) │  │ Publisher  │  │ (Batch)    │   │    │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Order Lifecycle
- Quote → order → allocation → shipment → fulfillment
- Change control and audit trail

### 2. Pricing Integration
- Price evaluation and override approvals
- Promotion and discount validation

### 3. Billing Readiness
- SalesOrderFulfilledEvent for AR billing
- Revenue recognition linkage

## Integration Points

```
┌──────────────┐   Allocation     ┌──────────────┐
│ sales-core   │ ───────────────> │ inventory    │
└──────────────┘                  └──────────────┘
       │ SalesOrderFulfilled
       ▼
┌──────────────┐
│ finance / AR │
└──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Order submission latency | < 2s p95 | > 4s |
| Allocation latency | < 1s p95 | > 2s |
| Sales order fulfilled event latency | < 500ms p99 | > 1s |
| API availability | 99.95% | < 99.90% |

## Compliance & Audit

- SOX approvals for pricing overrides
- Audit trail for order changes and cancellations
- Segregation of duties for approvals

## Related ADRs

- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md)
- [ADR-022: Revenue Recognition](../../adr/ADR-022-revenue-recognition.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
