# Inventory Warehouse Execution (WMS) - ADR-038

> **Bounded Context:** `inventory-warehouse`  
> **Port:** `9002`  
> **Database:** `chiroerp_inventory_warehouse`  
> **Kafka Consumer Group:** `inventory-warehouse-cg`

## Overview

Warehouse Execution provides **operational execution** for inbound, outbound, and internal warehouse activities. It owns wave planning, task management, directed putaway, pick optimization, replenishment, and labor tracking. It consumes reservations and inventory snapshots from Core Inventory and confirms goods movements back to the stock ledger. It also executes **kitting, repack, and break-bulk work orders** issued by Inventory Advanced Ops.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Wave planning, tasks, putaway, pick, replenishment, labor, kit/repack execution |
| **Aggregates** | WarehouseZone, Bin, PickWave, Task, ReplenishmentRule, LaborStandard |
| **Key Events** | WaveReleasedEvent, TaskCompletedEvent (incl. KIT_ASSEMBLY/REPACK), PutawayConfirmedEvent |
| **Integration** | Consumes reservations and publishes movement confirmations |
| **Compliance** | Traceability, safety and hazmat controls |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [warehouse-domain.md](./warehouse/warehouse-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [warehouse-application.md](./warehouse/warehouse-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [warehouse-infrastructure.md](./warehouse/warehouse-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [warehouse-api.md](./warehouse/warehouse-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [warehouse-events.md](./warehouse/warehouse-events.md) | Domain events, consumed events, Avro schemas, Kafka topics |

## Bounded Context

```
inventory-warehouse/
├── warehouse-domain/
├── warehouse-application/
└── warehouse-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       INVENTORY-WAREHOUSE SERVICE                            │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      APPLICATION LAYER                               │    │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │    │
│  │  │ ReleaseWave      │  │ CompleteTask     │  │ ConfirmPutaway    │  │    │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │  │    │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │    │
│  │           │                    │                       │             │    │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │    │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │    │
│  │  │ TaskRepo │ WaveRepo │ InventoryPort │ AutomationPort           │  │    │
│  │  └────────────────────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                        DOMAIN LAYER                                  │    │
│  │  ┌──────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │    │
│  │  │ PickWave │  │ Task         │  │ Bin          │  │ PutawayRule  │  │    │
│  │  │ Aggregate│  │ Aggregate    │  │ Aggregate    │  │ Aggregate    │  │    │
│  │  └──────────┘  └──────────────┘  └──────────────┘  └──────────────┘  │    │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │    │
│  │  │ Replenishment       │  │ Domain Services                      │    │    │
│  │  │ and Labor           │  │ Slotting, Wave Planning, Routing    │    │    │
│  │  └────────────────────┘  └─────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                    INFRASTRUCTURE LAYER                              │    │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │    │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ RF Devices │   │    │
│  │  │ (Quarkus)   │  │ (PostgreSQL) │  │ Publisher  │  │ / IoT      │   │    │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Wave and Task Management
- Wave planning with priorities and cut-off windows
- Task batching and interleaving for efficiency

### 2. Directed Putaway
- Rule-based putaway by zone, class, temperature, hazard
- Capacity checks and slotting recommendations

### 3. Pick Optimization
- Path optimization and cluster picking
- Pick-to-carton and multi-order strategies

### 4. Replenishment and Returns
- Min/max and demand-driven replenishment
- Returns disposition: restock, refurbish, quarantine, scrap

### 5. Labor and Productivity
- Engineered standards and labor KPIs
- Exception handling and audit trails

### 6. Kitting & Repack Execution (Advanced Ops)
- Execute kit assembly and break-bulk work orders
- Repack and VAS task execution with variance capture
- Confirm completion back to Advanced Ops and Core Inventory

## Integration Points

```
┌──────────────┐   Reservations     ┌──────────────────┐
│ inventory-   │ ─────────────────> │ inventory-       │
│   core       │                    │ warehouse (WMS)  │
└──────────────┘ <────────────────  └──────────────────┘
   Stock updates    Task confirmations        ▲
                                              │
                     ┌─────────────────┐      │
                     │ inventory-      │      │
                     │ advanced-ops    │──────┘  Kit/Repack work orders
                     └─────────────────┘
                         ┌──────────────┬──────────┬──────────────┐
                         │ procurement  │ sales / SD│ manufacturing │
                         └──────────────┴───────────┴──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Task confirmation to MM-IM | < 60s p95 | > 120s |
| Pick accuracy | >= 99.9% | < 99.5% |
| Order cycle time | < 4h p95 | > 6h |
| Putaway cycle time | < 30m p95 | > 60m |
| WMS availability | 99.9% | < 99.5% |

## Compliance & Audit

- Traceability for all picks and putaways
- Safety and hazmat segregation rules
- Audit trail for task execution and overrides

## Related ADRs

- [ADR-038: Warehouse Execution](../../adr/ADR-038-warehouse-execution-wms.md) - WMS boundary
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md) - Stock ledger ownership
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md) - Fulfillment integration
- [ADR-037: Manufacturing](../../adr/ADR-037-manufacturing-production.md) - Staging and receipt
- [ADR-016: Analytics](../../adr/ADR-016-analytics-reporting-architecture.md) - KPI reporting
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka patterns
