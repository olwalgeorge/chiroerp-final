# Inventory Domain Architecture

This directory contains hexagonal architecture specifications for Inventory subdomains.

## Subdomain Index

### Phase 1 Modules (Implemented)

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Core Inventory (MM-IM)** | ADR-024 | 9001 | Core | [inventory-core.md](./inventory-core.md) | Stock ledger, valuation, reservations, movement types |
| **Warehouse Execution (WMS)** | ADR-038 | 9002 | Add-on | [inventory-warehouse.md](./inventory-warehouse.md) | Wave planning, tasks, putaway, pick optimization, labor |
| **Cycle Counting** | ADR-024 | 9004 | Core | [inventory-counting.md](./inventory-counting.md) | ABC scheduling, count execution, variance approval |
| **Lot & Serial Tracking** | ADR-024 | 9007 | Core | [inventory-traceability.md](./inventory-traceability.md) | Lot/serial/expiry tracking, FEFO, recall management |
| **POS & Store Sync** | ADR-024 | 9003 | Core | [inventory-pos.md](./inventory-pos.md) | Store locations, POS events, offline reconciliation |

### Phase 2 Modules (Implemented)

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Valuation & Costing** | ADR-024 | 9005 | Core | [inventory-valuation.md](./inventory-valuation.md) | Cost layers, landed cost, FX revaluation |
| **ATP & Allocation** | ADR-024 | 9006 | Core | [inventory-atp.md](./inventory-atp.md) | ATP calculation, channel allocation, safety stock |

### Phase 3 Modules (Advanced Ops Extension)

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Advanced Inventory Ops** | ADR-024 | 9008 | Add-on | [inventory-advanced-ops.md](./inventory-advanced-ops.md) | Packaging hierarchies, kitting, repack, catch weight |

## Documentation Structure

### Complex Subdomains (with subfolders)
- **Core Inventory** (`/core/`) - 5 detailed files (domain, application, infrastructure, api, events)
- **Warehouse Execution** (`/warehouse/`) - 5 detailed files
- **Valuation & Costing** (`/valuation/`) - 5 detailed files
- **ATP & Allocation** (`/atp/`) - 5 detailed files
- **Advanced Inventory Ops** (`/advanced-ops/`) - 5 detailed files

### Simple Subdomains (inline documentation)
- **Cycle Counting** - Single file with complete specification
- **Lot & Serial Tracking** - Single file with compliance focus
- **POS & Store Sync** - Single file with omnichannel integration

## Port Allocation

| Port | Service | Database | Status |
|------|---------|----------|--------|
| 9001 | inventory-core | chiroerp_inventory_core | ✅ Implemented |
| 9002 | inventory-warehouse | chiroerp_inventory_warehouse | ✅ Implemented |
| 9003 | inventory-pos | chiroerp_inventory_core (shared) | ✅ Implemented |
| 9004 | inventory-counting | chiroerp_inventory_core (shared) | ✅ Implemented |
| 9005 | inventory-valuation | chiroerp_inventory_valuation | ✅ Implemented |
| 9006 | inventory-atp | chiroerp_inventory_atp | ✅ Implemented |
| 9007 | inventory-traceability | chiroerp_inventory_core (shared) | ✅ Implemented |
| 9008 | inventory-advanced-ops | chiroerp_inventory_advanced_ops | ✅ Implemented |

**Note:** Inline modules (counting, traceability, POS) share the `chiroerp_inventory_core` database and run as part of the Core service. Port numbers are logical identifiers for documentation purposes.

## Integration Map

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          INVENTORY DOMAIN                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌──────────────┐     Reservations      ┌──────────────┐                  │
│    │ inventory-   │ ────────────────────> │ inventory-   │                  │
│    │   core       │                       │ warehouse    │                  │
│    └──────────────┘ <──────────────────── └──────────────┘                  │
│            ▲           Confirmed moves             ▲                        │
│            │                                        │                        │
│            │                                        │                        │
│   ┌────────┴───────┐    ┌──────────────┐    ┌────────┴───────┐               │
│   │ procurement    │    │ sales / SD   │    │ manufacturing  │               │
│   └───────────────┘    └──────────────┘    └───────────────┘               │
│            ▲                    ▲                    ▲                     │
│            │                    │                    │                     │
│            └──────────────┬─────┴──────────────┬─────┘                     │
│                           ▼                    ▼                           │
│                      ┌──────────┐        ┌──────────┐                      │
│                      │ finance  │        │ analytics│                      │
│                      └──────────┘        └──────────┘                      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Integration Points

### Upstream Dependencies (Consume Events)
- **Procurement** → `GoodsReceivedEvent` → Stock receipt and valuation
- **Sales** → `SalesOrderAllocatedEvent` → Reservation creation
- **Manufacturing** → `ProductionReceiptEvent` → Finished goods receipt
- **Finance** → `FinancialPeriodClosedEvent` → Lock postings for period

### Downstream Consumers (Publish Events)
- **Finance/GL** ← `StockReceivedEvent`, `StockIssuedEvent`, `StockAdjustedEvent`
- **Sales** ← `ReservationCreatedEvent`, `StockAvailabilityChangedEvent`
- **WMS** ← `ReservationCreatedEvent`, `InventorySnapshotEvent`
- **Analytics** ← All inventory events for KPIs and reporting

## References

### Related ADRs
- [ADR-024: Inventory Management (MM-IM)](../../adr/ADR-024-inventory-management.md) - Core inventory architecture
- [ADR-038: Warehouse Execution System (WMS)](../../adr/ADR-038-warehouse-execution-wms.md) - WMS boundary and integration
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md) - GL integration patterns
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns

### Related Domains
- [Finance Architecture](../finance/README.md) - GL integration for valuation and COGS
- [Procurement Architecture](../procurement/README.md) - Goods receipt and purchase orders
- [Sales Architecture](../sales/README.md) - Order fulfillment and reservations

---

## Phase 2 Status: ✅ Complete

**7 modules implemented:**
- Core Inventory (MM-IM)
- Warehouse Execution (WMS)
- Cycle Counting
- Lot & Serial Tracking
- POS & Store Sync
- Valuation & Costing
- ATP & Allocation

**Total documentation:** 28 files (4 overview + 20 detailed subfolder files + 3 inline + 1 README)

**Next steps:**
- Integration: Build Procurement domain
- Integration: Build Sales/Distribution domain
