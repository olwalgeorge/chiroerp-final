# Inventory ATP & Allocation - ADR-024

> **Bounded Context:** `inventory-atp`  
> **Port:** `9006`  
> **Database:** `chiroerp_inventory_atp`  
> **Kafka Consumer Group:** `inventory-atp-cg`

## Overview

ATP & Allocation provides **real-time availability** and **channel allocation** across omnichannel fulfillment. It enforces safety stock, prioritizes reservations, and exposes availability APIs for Sales, Ecommerce, and WMS.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | ATP calculation, allocations, reservation priority |
| **Aggregates** | AllocationPool, ChannelAllocation, ReservationRule, SafetyStock |
| **Key Events** | AvailabilityCalculatedEvent, AllocationCommittedEvent, ReservationDeniedEvent |
| **Integration** | Consumes stock/reservations from Core, serves Sales/WMS |
| **Compliance** | SLA prioritization and audit trail |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [atp-domain.md](./atp/atp-domain.md) | Aggregates, entities, value objects, domain events, exceptions, services |
| **Application Layer** | [atp-application.md](./atp/atp-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [atp-infrastructure.md](./atp/atp-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [atp-api.md](./atp/atp-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [atp-events.md](./atp/atp-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
inventory-atp/
├── atp-domain/
├── atp-application/
└── atp-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           INVENTORY-ATP SERVICE                              │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      APPLICATION LAYER                               │    │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │    │
│  │  │ CalculateATP     │  │ AllocateStock    │  │ ReleaseAllocation │  │    │
│  │  │ QueryHandler     │  │ CommandHandler   │  │ CommandHandler    │  │    │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │    │
│  │           │                    │                       │             │    │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │    │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │    │
│  │  │ AllocationRepo │ InventoryPort │ EventPublisherPort            │  │    │
│  │  └────────────────────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                        DOMAIN LAYER                                  │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │    │
│  │  │ Allocation   │  │ ChannelAlloc │  │ SafetyStock  │  │ Rules     │ │    │
│  │  │ Aggregate    │  │ Aggregate    │  │ Aggregate    │  │ Aggregate │ │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └───────────┘ │    │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │    │
│  │  │ ATP Engine          │  │ Domain Services                      │    │    │
│  │  │ Real-time calc      │  │ Priority, SLA, Channel Rules        │    │    │
│  │  └────────────────────┘  └─────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                    INFRASTRUCTURE LAYER                              │    │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │    │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ Cache      │   │    │
│  │  │ (Quarkus)   │  │ (PostgreSQL) │  │ Publisher  │  │ (Redis)    │   │    │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Real-Time ATP
- Availability by channel, location, and promise date
- SLA-aware allocation rules

### 2. Channel Allocation
- Allocation pools per channel (POS, Ecommerce, Wholesale)
- Priority and safety stock enforcement

### 3. Reservation Prioritization
- Backorder handling and allocation holds
- Reservation release on cancellation

### 4. Omnichannel Fulfillment
- Ship-from-store and cross-dock support
- Split-fulfillment recommendations

## Integration Points

```
┌──────────────┐   Stock/Reservations  ┌──────────────────┐
│ inventory-   │ ────────────────────> │ inventory-atp    │
│   core       │                       └──────────────────┘
└──────────────┘            ▲                 │
       ▲                    │ ATP responses   │ Allocation events
       │                    ▼                 ▼
┌──────────────┐        ┌──────────────┐   ┌──────────────┐
│ sales / SD   │        │ ecommerce    │   │ warehouse    │
└──────────────┘        └──────────────┘   └──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| ATP query response | < 200ms p95 | > 400ms |
| Allocation commit | < 150ms p99 | > 300ms |
| Availability cache freshness | < 10s | > 30s |
| API availability | 99.95% | < 99.90% |

## Compliance & Audit

- Audit trail for allocation overrides
- SLA-based priority enforcement
- Safe-stock guardrails

## Related ADRs

- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md)
- [ADR-017: Performance Standards](../../adr/ADR-017-performance-standards-monitoring.md)
