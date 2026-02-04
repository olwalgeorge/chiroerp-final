# Inventory Core (MM-IM) - ADR-024

> **Bounded Context:** `inventory-core`
> **Port:** `9001`
> **Database:** `chiroerp_inventory_core`
> **Kafka Consumer Group:** `inventory-core-cg`

## Overview

Core Inventory owns the **stock ledger and valuation** for all locations, variants, and channels. It manages movement types, reservations, cycle counting, lot/serial tracking, and inventory visibility. It integrates with Procurement, Sales, Manufacturing, and Finance (GL) and provides authoritative inventory state to WMS.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Stock ledger, valuation, reservations, cycle counting, traceability |
| **Aggregates** | Item, ItemVariant, Location, StockLedger, StockLot, Reservation, CostLayer |
| **Key Events** | StockReceivedEvent, StockIssuedEvent, StockAdjustedEvent, ReservationCreatedEvent |
| **GL Integration** | Receipt (DR Inventory, CR GR/IR); Issue (DR COGS, CR Inventory) |
| **Compliance** | SOX controls, IFRS/GAAP valuation, audit trail |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [core-domain.md](./core/core-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [core-application.md](./core/core-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [core-infrastructure.md](./core/core-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [core-api.md](./core/core-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [core-events.md](./core/core-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |
| **Advanced Operations** | [inventory-advanced-ops.md](./inventory-advanced-ops.md) | Packaging hierarchies, kitting, repack, catch weight (ADR-024 extension) |

## Bounded Context

```
inventory-core/
├── core-domain/
├── core-application/
└── core-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          INVENTORY-CORE SERVICE                              │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      APPLICATION LAYER                               │    │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │    │
│  │  │ ReceiveStock     │  │ ReserveStock     │  │ PostCycleCount    │  │    │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │  │    │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │    │
│  │           │                    │                       │             │    │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │    │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │    │
│  │  │ StockLedgerRepo │ ReservationRepo │ GLPort │ WMSPort            │  │    │
│  │  └────────────────────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                        DOMAIN LAYER                                  │    │
│  │  ┌──────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │    │
│  │  │ Item     │  │ StockLedger  │  │ Reservation  │  │ CostLayer    │  │    │
│  │  │ Aggregate│  │ Aggregate    │  │ Aggregate    │  │ Aggregate    │  │    │
│  │  └──────────┘  └──────────────┘  └──────────────┘  └──────────────┘  │    │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │    │
│  │  │ Lot/Serial Tracking │  │ Domain Services                      │    │    │
│  │  │ & Cycle Counts      │  │ Valuation, Allocation, UoM Conv      │    │    │
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

### 1. Stock Ledger and Movements
- Movement types: receipt, issue, transfer, adjustment, shrinkage
- Statuses: available, reserved, quality hold, damaged, consigned, in-transit
- Full audit trail per movement

### 2. Valuation and Cost Layers
- FIFO, weighted average, standard cost
- Landed cost allocation and cost layers
- Period close revaluation support

### 3. Reservations and Allocation
- Channel-aware reservation rules
- Prevent oversell with real-time ATP
- Allocation priority by channel and SLA

### 4. Lot/Serial Traceability
- Lot/serial tracking with expiry (FEFO)
- Recall and traceability support
- Regulated goods compliance

### 5. Cycle Counting and Reconciliation
- Scheduled cycle counts and variance approval
- Auto-post adjustments to GL
- Accuracy KPIs and shrinkage reasons

## Integration Points

```
┌──────────────┐   GoodsReceived     ┌──────────────┐
│ procurement  │ ──────────────────> │ inventory-   │
└──────────────┘                     │   core       │
        ▲                             └──────────────┘
        │ SalesOrderAllocated                │
┌───────┴───────┐                           │ Stock events
│ sales / SD   │ ──────────────────────────┘
└──────────────┘                           │
        ▲                                  ▼
        │                      ┌──────────────────┐
┌───────┴────────┐             │ inventory-       │
│ manufacturing  │ <────────── │ warehouse (WMS)  │
└───────────────┘   Task confirm└──────────────────┘
        │                                  │
        ▼                                  ▼
┌──────────────┐                    ┌──────────────┐
│ finance / GL │                    │ analytics    │
└──────────────┘                    └──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Stock movement posting latency | < 150ms p99 | > 300ms |
| Reservation creation latency | < 100ms p99 | > 250ms |
| Stock sync to WMS | < 30s p95 | > 60s |
| ATP query response | < 200ms p95 | > 400ms |
| Inventory accuracy | >= 99.5% | < 99.0% |

## Compliance & Audit

- SOX controls for inventory valuation and adjustments
- IFRS/GAAP-compliant valuation methods
- Full traceability for regulated goods
- Segregation of duties for adjustments and approvals

## Related ADRs

- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md) - Core inventory decisions
- [ADR-038: Warehouse Execution](../../adr/ADR-038-warehouse-execution-wms.md) - WMS boundary
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md) - Goods receipt integration
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md) - Order allocation events
- [ADR-037: Manufacturing](../../adr/ADR-037-manufacturing-production.md) - Material issue/receipt
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka patterns
