# Inventory Advanced Operations - ADR-024 Extension

> **Bounded Context:** `inventory-advanced-ops`  
> **Port:** `9008`  
> **Database:** `chiroerp_inventory_advanced_ops`  
> **Kafka Consumer Group:** `inventory-advanced-ops-cg`

## Overview

Advanced Inventory Operations extends Core Inventory (ADR-024) with sophisticated capabilities for retail, distribution, food & beverage, and manufacturing industries. It manages packaging hierarchies, kitting/bundling, repack operations, and catch weight handling. **Physical execution** (assembly, repack, break-bulk) is performed by **Warehouse Execution (WMS)**, while this service owns the rules, work orders, and cost/variance accounting.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Packaging hierarchies, kitting, repack, catch weight, VAS |
| **Aggregates** | PackagingLevel, Kit, KitAssembly, RepackOrder, CatchWeightItem, VASOrder |
| **Key Events** | KitAssembledEvent, KitDisassembledEvent, RepackCompletedEvent, CatchWeightRecordedEvent |
| **GL Integration** | Kit assembly cost rollup, repack labor/material costs, catch weight variance |
| **Compliance** | GTIN accuracy, FDA/USDA catch weight, GS1 standards, lot traceability |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [advanced-ops-domain.md](./advanced-ops/advanced-ops-domain.md) | Aggregates, entities, value objects, domain events |
| **Application Layer** | [advanced-ops-application.md](./advanced-ops/advanced-ops-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [advanced-ops-infrastructure.md](./advanced-ops/advanced-ops-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [advanced-ops-api.md](./advanced-ops/advanced-ops-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [advanced-ops-events.md](./advanced-ops/advanced-ops-events.md) | Domain events, consumed events, integration patterns |

## Bounded Context

```
inventory-advanced-ops/
├── packaging/           # Packaging hierarchy management
├── kitting/            # Kitting and bundling
├── repack/             # Repackaging and VAS
└── catchweight/        # Variable weight handling
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    INVENTORY-ADVANCED-OPS SERVICE                            │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      APPLICATION LAYER                               │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────┐  │    │
│  │  │ AssembleKit  │  │ ExecuteRepack│  │RecordCatchWt │  │ManageGTIN│  │    │
│  │  │ CmdHandler   │  │ CmdHandler   │  │ CmdHandler   │  │CmdHandler│  │    │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └────┬────┘  │    │
│  │         │                 │                 │               │        │    │
│  │  ┌──────┴─────────────────┴─────────────────┴───────────────┴─────┐  │    │
│  │  │              OUTPUT PORTS (Interfaces)                          │  │    │
│  │  │ KitRepo │ RepackRepo │ CatchWtRepo │ CoreInventoryPort │ GLPort│  │    │
│  │  └─────────────────────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                        DOMAIN LAYER                                  │    │
│  │  ┌──────────────┐  ┌───────────┐  ┌───────────┐  ┌──────────────┐   │    │
│  │  │PackagingLevel│  │    Kit    │  │RepackOrder│  │CatchWeightItem│   │    │
│  │  │  Aggregate   │  │ Aggregate │  │ Aggregate │  │  Aggregate    │   │    │
│  │  └──────────────┘  └───────────┘  └───────────┘  └──────────────┘   │    │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │    │
│  │  │ KitAssembly, VAS   │  │ Domain Services                      │    │    │
│  │  │ GTINManagement     │  │ KitATP, UOMConv, CostRollup          │    │    │
│  │  └────────────────────┘  └─────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                    INFRASTRUCTURE LAYER                              │    │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │    │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ ScaleAPI   │   │    │
│  │  │ (Quarkus)   │  │ (PostgreSQL) │  │ Publisher  │  │ Integration│   │    │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Packaging Hierarchy Management
- Multi-level packaging structures (pallet → case → inner pack → each)
- GTIN/barcode per level (GTIN-14, GTIN-13, GTIN-12, UPC)
- Dimensions and weight per level for cube/freight calculations
- Ti/Hi configuration (cases per layer, layers per pallet)
- UOM conversion validation across packaging levels
- Handling unit type management (pallet types, case types)

### 2. Kitting & Bundling Operations
- **Static Kits**: Pre-assembled, stocked as unique SKU
- **Dynamic Kits**: Build-to-order, assembled when ordered
- **Virtual Kits**: Logical grouping, components picked separately
- **Configurable Kits**: Customer-selectable components
- Kit BOM management with substitution rules
- Kit ATP explosion (min of component availability)
- Kit assembly/disassembly workflows with variance tracking
- Component reservation and cost rollup

### 3. Repackaging & VAS (Value-Added Services)
- **Break Bulk**: Split larger units into smaller (pallet → cases → each)
- **Repackaging**: Change packaging without changing SKU
- **Master Pack Creation**: Build display-ready packs
- **Deconsolidation**: Split mixed pallets by SKU
- **VAS Operations**: Labeling, gift wrapping, customization, kitting
- Repack work orders with labor/material cost capture
- Variance tracking and quality checks

### 4. Catch Weight / Variable Weight
- Nominal vs actual weight tracking (1 lb nominal = 1.03 actual)
- Dual UOM (count for stocking, weight for sales)
- Scale integration for receipt and POS
- Average weight calculation for planning (forecasting, MRP)
- Pricing by actual weight with tare weight handling
- USDA/FDA compliance for food products
- Catch weight variance and shrink tracking

## Integration Points

```
┌──────────────┐   Packaging Data    ┌──────────────┐
│ inventory-   │ <──────────────────> │ inventory-   │
│   core       │                     │ advanced-ops │
└──────────────┘                     └──────────────┘
        ▲                                    │
        │ Stock movements                   │ Kit/Repack events
┌───────┴────────┐                          │
│ procurement    │ ────────────────────────>│
└───────────────┘   Receive by pallet       │
        ▲                                   │
        │                                   ▼
┌───────┴────────┐                  ┌──────────────┐
│ sales / SD     │ <────────────── │  warehouse   │
└───────────────┘   Kit ATP check  └──────────────┘
        │                                   │
        ▼                                   ▼
┌──────────────┐                    ┌──────────────┐
│ controlling  │                    │ quality      │
└──────────────┘                    └──────────────┘
```

### Event Flow

**Kit Assembly Flow:**
1. Sales creates order for kit SKU → `KitAssemblyRequestedEvent`
2. Advanced Ops checks kit ATP → explodes to component availability
3. Components reserved in Core Inventory → `ComponentsReservedEvent`
4. WMS executes assembly work order → `KitAssemblyCompletedEvent`
5. Kit received into stock → `StockReceivedEvent` (Core Inventory)
6. Cost rollup posted to GL → `KitCostPostedEvent`

**Repack Flow:**
1. WMS identifies damaged case → `RepackRequiredEvent`
2. Advanced Ops creates repack work order → `RepackOrderCreatedEvent`
3. WMS executes repack (break case, repack items) → `RepackCompletedEvent`
4. Variance recorded (damaged units) → `StockAdjustedEvent` (Core Inventory)
5. Labor/material costs captured → `RepackCostPostedEvent`

**Catch Weight Flow:**
1. Procurement receives catch weight item → Scale captures actual weight
2. Advanced Ops records nominal + actual weight → `CatchWeightRecordedEvent`
3. Core Inventory updates stock (count + weight) → `StockReceivedEvent`
4. Sales order picks catch weight item → POS scales actual weight
5. Invoice adjusted to actual weight → `CatchWeightPricingAdjustedEvent`

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Kit ATP calculation latency | < 200ms p99 | > 500ms |
| Kit assembly cycle time | < 4 hours p95 | > 8 hours |
| Repack completion time | < 2 hours p95 | > 4 hours |
| Catch weight recording latency | < 100ms p99 | > 250ms |
| Packaging hierarchy accuracy | >= 99.9% | < 99.5% |
| Kit component variance | < 0.5% | > 1.0% |
| Repack accuracy | >= 99.5% | < 99.0% |
| Catch weight accuracy | >= 99.9% | < 99.5% |

## Key Workflows

### Workflow 1: Static Kit Assembly

```
┌────────────┐         ┌─────────────┐         ┌──────────────┐
│ Create Kit │────────>│ Reserve     │────────>│ Execute      │
│ Assembly   │         │ Components  │         │ Assembly WO  │
│ Order      │         │ (Core Inv)  │         │ (WMS)        │
└────────────┘         └─────────────┘         └──────────────┘
                                                        │
                                                        ▼
┌────────────┐         ┌─────────────┐         ┌──────────────┐
│ Post Cost  │<────────│ Receive Kit │<────────│ Issue        │
│ to GL      │         │ into Stock  │         │ Components   │
└────────────┘         └─────────────┘         └──────────────┘
```

**Steps:**
1. `POST /api/v1/inventory/kits/assembly-orders` - Create assembly order
2. `Kit ATP Check` - Validate component availability
3. `Reserve Components` - Atomic reservation of all components
4. `WMS Work Order` - Generate pick/assembly tasks
5. `Issue Components` - Deduct components from stock (Core Inventory)
6. `Receive Kit` - Add kit to stock (Core Inventory)
7. `Cost Rollup` - Calculate kit cost from component costs
8. `Post to GL` - DR Inventory (Kit), CR Inventory (Components)

### Workflow 2: Dynamic Kit (Build-to-Order)

```
┌────────────┐         ┌─────────────┐         ┌──────────────┐
│ Sales Order│────────>│ Check Kit   │────────>│ Reserve      │
│ for Kit    │         │ ATP         │         │ Components   │
└────────────┘         └─────────────┘         └──────────────┘
                                                        │
                                                        ▼
┌────────────┐         ┌─────────────┐         ┌──────────────┐
│ Ship Kit   │<────────│ Assemble    │<────────│ Pick         │
│ (no stock) │         │ at WMS      │         │ Components   │
└────────────┘         └─────────────┘         └──────────────┘
```

**Steps:**
1. `POST /api/v1/sales/orders` - Sales order with kit SKU
2. `GET /api/v1/inventory/kits/{sku}/atp` - Check kit ATP (explode to components)
3. `Reserve Components` - Reserve all components atomically
4. `WMS Pick` - Pick components from warehouse
5. `WMS Assemble` - Assemble kit at pack station
6. `Ship` - Ship kit directly (no stock receipt)
7. `Issue Components` - Deduct components from stock (Core Inventory)
8. `Revenue Recognition` - Invoice at kit price, cost = sum(component costs)

### Workflow 3: Break Bulk / Repackaging

```
┌────────────┐         ┌─────────────┐         ┌──────────────┐
│ Receive    │────────>│ Identify    │────────>│ Create Repack│
│ Pallet     │         │ Need for    │         │ Work Order   │
│ (100 cases)│         │ Case-level  │         │              │
└────────────┘         └─────────────┘         └──────────────┘
                                                        │
                                                        ▼
┌────────────┐         ┌─────────────┐         ┌──────────────┐
│ Update     │<────────│ Confirm     │<────────│ Execute      │
│ Packaging  │         │ Repack      │         │ Break Bulk   │
│ Level      │         │ (WMS)       │         │ (WMS)        │
└────────────┘         └─────────────┘         └──────────────┘
```

**Steps:**
1. `POST /api/v1/inventory/stock/receive` - Receive pallet (Core Inventory)
2. `Business Logic` - Determine need for case-level stock
3. `POST /api/v1/inventory/repack/orders` - Create repack order (break bulk)
4. `WMS Work Order` - Generate break-bulk task (pallet → cases)
5. `WMS Execution` - Physical break-bulk in warehouse
6. `PUT /api/v1/inventory/repack/orders/{id}/complete` - Confirm repack
7. `Adjust Packaging Level` - Reduce pallet qty, increase case qty (same SKU)
8. `Variance Check` - Validate expected vs actual quantities

### Workflow 4: Catch Weight Processing

```
┌────────────┐         ┌─────────────┐         ┌──────────────┐
│ Receive    │────────>│ Scale       │────────>│ Record       │
│ Catch      │         │ Integration │         │ Nominal +    │
│ Weight Item│         │ (Actual Wt) │         │ Actual Weight│
└────────────┘         └─────────────┘         └──────────────┘
                                                        │
                                                        ▼
┌────────────┐         ┌─────────────┐         ┌──────────────┐
│ Invoice    │<────────│ POS Scale   │<────────│ Pick for     │
│ by Actual  │         │ (Actual Wt) │         │ Sales Order  │
│ Weight     │         │             │         │              │
└────────────┘         └─────────────┘         └──────────────┘
```

**Steps:**
1. `POST /api/v1/procurement/receipts` - Receive catch weight item
2. `Scale Integration` - Capture actual weight from scale (e.g., 1.03 lbs)
3. `POST /api/v1/inventory/catchweight/record` - Record nominal (1 lb) + actual (1.03 lbs)
4. `Stock Update` - Update Core Inventory (count=1, weight=1.03 lbs)
5. `Sales Order Pick` - Pick catch weight item for order
6. `POS Scale` - Weigh at POS (actual weight = 1.02 lbs)
7. `Price Adjustment` - Adjust invoice line to actual weight (1.02 lbs × $5/lb = $5.10)
8. `Variance Tracking` - Track catch weight variance for shrink analysis

## Compliance & Audit

- **GS1 Standards**: GTIN-14/13/12 compliance, GS1-128 barcodes
- **FDA/USDA**: Catch weight accuracy for food products, lot traceability
- **SOX Controls**: Kit cost rollup, repack variance approval, catch weight shrink
- **IFRS/GAAP**: Proper inventory valuation for kits and repacked items
- **Retail Compliance**: Vendor compliance (packaging, labeling), EDI 856 (ASN with packaging detail)

## Related ADRs

- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md) - Core inventory and extensions
- [ADR-038: Warehouse Execution](../../adr/ADR-038-warehouse-execution-wms.md) - Physical execution of repack/assembly
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md) - Receive by pallet, catch weight receiving
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md) - Kit order processing, catch weight pricing
- [ADR-037: Manufacturing](../../adr/ADR-037-manufacturing-production.md) - Complex kit assembly as light manufacturing
- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md) - Kit costing, repack cost capture
- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md) - Repack quality checks, catch weight variance

---

## Implementation Phases

### Phase 1: Packaging Hierarchy (Month 1-2)
- **Core Features**:
  - Multi-level packaging master data (EACH, CASE, PALLET)
  - GTIN management per level
  - Dimensions, weight, Ti/Hi configuration
  - UOM conversion validation
- **API Endpoints**: 10 REST endpoints for packaging hierarchy CRUD
- **Database**: `packaging_level` table with GS1 validation
- **Integration**: Sync with Core Inventory for UOM conversions

### Phase 2: Kitting & Bundling (Month 3-4)
- **Core Features**:
  - Kit BOM management (static, dynamic, virtual, configurable)
  - Kit ATP explosion logic
  - Component reservation (atomic)
  - Kit assembly/disassembly workflows
- **API Endpoints**: 15 REST endpoints for kit operations
- **Database**: `kit`, `kit_component`, `kit_assembly_order` tables
- **Integration**: Core Inventory (reservations), WMS (assembly work orders), Sales (ATP checks)

### Phase 3: Repack & VAS (Month 5)
- **Core Features**:
  - Break bulk operations (pallet → case → each)
  - Repackaging workflows
  - Master pack creation
  - VAS work order management
- **API Endpoints**: 12 REST endpoints for repack operations
- **Database**: `repack_order`, `vas_order` tables
- **Integration**: WMS (repack work orders), Core Inventory (stock adjustments), Controlling (cost capture)

### Phase 4: Catch Weight (Month 6)
- **Core Features**:
  - Nominal vs actual weight tracking
  - Dual UOM management
  - Scale integration (receipt, POS)
  - Catch weight pricing adjustments
- **API Endpoints**: 8 REST endpoints for catch weight operations
- **Database**: `catch_weight_item`, `catch_weight_lot`, `catch_weight_transaction` tables
- **Integration**: Procurement (scale receipt), Sales (POS scale), Core Inventory (weight updates)

### Resource Requirements
- **Team**: 2 senior engineers (backend), 1 senior engineer (frontend), 1 QA engineer
- **Duration**: 6 months
- **Budget**: $200K (salaries, infrastructure, testing)
- **Dependencies**: Core Inventory (ADR-024), WMS (ADR-038), Sales (ADR-025)

---

## Testing Strategy

### Unit Tests
- Kit ATP explosion logic (min of component availability)
- UOM conversion validation across packaging levels
- Catch weight variance calculations
- Kit cost rollup algorithms

### Integration Tests
- Kit assembly end-to-end (order → reserve → assemble → receive)
- Break bulk flow (pallet → case → each)
- Catch weight flow (receipt → stock → pick → invoice)
- Event-driven integration with Core Inventory, WMS, Sales

### Performance Tests
- Kit ATP calculation: < 200ms p99 for 100-component kits
- Repack work order execution: < 2 hours p95
- Catch weight recording: < 100ms p99
- Packaging hierarchy queries: < 50ms p95

### Compliance Tests
- GTIN uniqueness validation (no duplicate GTINs)
- FDA/USDA catch weight accuracy (>= 99.9%)
- Kit cost rollup accuracy (GAAP compliance)
- Lot traceability through kit assembly and repack

---

## Operational Considerations

### Monitoring & Alerts
- **Kit ATP Accuracy**: Alert if kit ATP errors > 1% (incorrect component availability)
- **Kit Assembly Cycle Time**: Alert if p95 > 8 hours (SLA breach)
- **Repack Accuracy**: Alert if repack variance > 1% (quality issue)
- **Catch Weight Accuracy**: Alert if catch weight variance > 0.1% (shrink issue)
- **Packaging Hierarchy Errors**: Alert if UOM conversion failures > 10/day

### Dashboards
- **Kit Operations Dashboard**: Assembly orders, ATP accuracy, component variance, cycle time
- **Repack Operations Dashboard**: Repack orders, variance, labor efficiency, quality issues
- **Catch Weight Dashboard**: Catch weight items, shrink %, average weight variance, pricing adjustments
- **Packaging Hierarchy Dashboard**: GTIN coverage, UOM conversion accuracy, cube utilization

### Disaster Recovery
- **RTO**: 4 hours (align with Core Inventory)
- **RPO**: 15 minutes (Kafka event replay)
- **Backup Strategy**: Daily PostgreSQL backups, Kafka topic retention 7 days
- **Failover**: Active-active deployment across 2 regions

---

## Future Enhancements

### Advanced Kit Features (Phase 7+)
- **Kit Substitution Engine**: Auto-substitute out-of-stock components with approved alternates
- **Kit Pricing Optimization**: Dynamic bundle pricing based on component costs and margins
- **Kit Demand Forecasting**: Forecast component demand based on kit sales history

### Advanced Repack Features (Phase 7+)
- **Automated Repack Triggers**: Auto-generate repack orders based on demand (bulk → case)
- **Repack Cost Optimization**: Optimize repack scheduling to minimize labor costs
- **VAS Catalog**: Configurable VAS services with pricing and cost capture

### Advanced Catch Weight Features (Phase 7+)
- **Catch Weight Forecasting**: Predict catch weight variance for planning (procurement, sales)
- **Catch Weight Quality Control**: Auto-flag catch weight variance outliers for inspection
- **Catch Weight Pricing Tiers**: Volume-based pricing for catch weight items

### Industry-Specific Extensions
- **Food & Beverage**: Recipe scaling based on catch weight, yield management
- **Retail**: Store-level kitting (gift baskets, promotional packs), VAS at POS
- **Distribution**: Cross-dock repack (receive pallet, ship cases), freight optimization

---

## Service Industry Integration (ADR-042)

### Overview
Advanced Inventory Operations provide **critical capabilities for service industries** including field service, utilities, equipment maintenance, and repair operations. Service industries require sophisticated parts management, service kits, field inventory tracking, and material variance handling that go beyond basic stock movements.

### Service Industry Use Cases

#### 1. Service Kit Management (Field Service)

**Use Case**: Pre-configured repair/maintenance kits for technician dispatch

**Capabilities Leveraged**:
- **Static Kits**: Pre-built maintenance kits stocked in warehouse (HVAC Tune-Up Kit, Electrical Repair Kit)
- **Dynamic Kits**: Assembled on-demand based on service order type (emergency repair, scheduled maintenance)
- **Kit ATP**: Real-time availability check before technician dispatch
- **Kit Assembly**: Warehouse assembles kits, posts to service inventory
- **Kit Disassembly**: Return unused kits, receive components back to stock

**Workflow**:
```
1. Service Order Created (ADR-042) → Check Kit ATP
2. Kit Available → Reserve kit for service order
3. Kit Not Available → Trigger kit assembly work order
4. Warehouse assembles kit → Post kit receipt
5. Dispatch technician with kit → Issue kit to service order
6. Service completed → Post unused component returns
```

**Integration Points**:
- **Field Service (ADR-042)**: Service order → kit reservation → parts consumption
- **Core Inventory (ADR-024)**: Component stock, kit stock ledger, reservations
- **Controlling (ADR-028)**: Kit cost rollup, service job costing

**KPIs**:
- **Kit Availability**: >= 95% (kits available when service order created)
- **First-Time Fix Rate**: >= 85% (correct components in kit for service completion)
- **Kit Assembly Cycle Time**: p95 < 4 hours (from order to dispatch-ready)

#### 2. Field Inventory Management (Service Van/Truck Stock)

**Use Case**: Manage inventory on service vehicles with repack/break bulk operations

**Capabilities Leveraged**:
- **Packaging Hierarchy**: Warehouse bulk (pallet/case) → service van (each-level)
- **Break Bulk**: Split warehouse pallets/cases into individual units for van replenishment
- **Repackaging**: Repack damaged materials returned from field
- **Catch Weight**: Variable-weight materials (cable spools, wire reels) issued to field

**Workflow**:
```
1. Van Replenishment Request → Break bulk from warehouse stock
2. Warehouse issues pallet → Repack to van quantities (each)
3. Transfer to service van location → Van inventory updated
4. Technician consumes parts in field → Issue from van stock
5. End-of-day reconciliation → Variance posting (shrink, damage)
6. Return unused materials → Receive back to warehouse (repack if needed)
```

**Integration Points**:
- **WMS (ADR-038)**: Physical break bulk execution, van loading
- **Field Service (ADR-042)**: Van inventory tracking, technician assignments
- **Finance (ADR-009)**: Variance postings (shrink, damage, theft)

**KPIs**:
- **Van Stock Accuracy**: >= 99% (physical vs system match)
- **Replenishment Cycle Time**: p95 < 24 hours (request to van loaded)
- **Shrinkage Rate**: < 0.5% (field inventory variance)

#### 3. Utilities-Specific Operations (Electric, Gas, Water)

**Use Case**: Meter installation kits, variable-weight materials, emergency repair inventory

**Capabilities Leveraged**:
- **Kitting**: Meter installation kits (meter + fittings + seals + tools)
- **Catch Weight**: Cable spools, wire reels, pipe sections (variable length/weight)
- **Packaging Hierarchy**: Bulk materials (reels) → field consumption (linear feet/meters)
- **VAS**: Customer-specific labeling for private label utilities

**Workflow Examples**:

**Meter Installation Kit**:
```
1. Service Connection Order (ADR-042) → Reserve meter installation kit
2. Kit includes: Meter, mounting bracket, seals, connectors, tools
3. Technician dispatched with kit → Issue kit to service order
4. Meter installed → Post actual components used (serial number tracking)
5. Unused components → Return to stock or van inventory
```

**Cable/Wire Consumption (Catch Weight)**:
```
1. Service order requires 150 feet of cable (nominal)
2. Reserve cable spool with average weight (500 feet on spool)
3. Technician cuts 152 feet (actual) → Post catch weight consumption
4. System calculates: Material cost = (152 ft / 500 ft) × spool cost
5. Remaining spool weight updated → 348 feet available
```

**Emergency Parts Network**:
```
1. Outage detected (ADR-042) → Check transformer availability across all locations
2. ATP calculation across: Warehouse stock + van stock + consignment inventory
3. Nearest available transformer identified → Reserve and dispatch crew
4. Emergency transfer order → Move from van A to outage location
5. Transformer installed → Issue to work order with asset tracking
```

**Integration Points**:
- **Field Service (ADR-042)**: Service orders, outage management, meter-to-cash
- **Fixed Assets (ADR-021)**: Network infrastructure tracking (transformers, poles)
- **GIS Systems**: Asset location, service territory, crew routing
- **SCADA/AMI**: Real-time outage detection, meter data

**Utilities KPIs**:
- **Emergency Parts Availability**: >= 99.5% (critical infrastructure spares)
- **Meter Kit Accuracy**: >= 99.9% (correct meter for service type)
- **Cable Waste**: < 2% (catch weight variance from nominal)
- **Outage Response**: Parts available within 15 minutes of crew dispatch

#### 4. Warranty/RMA Processing (Return Material Authorization)

**Use Case**: Process defective parts returns from field, repack for vendor return or disposal

**Capabilities Leveraged**:
- **Repackaging**: Repack defective parts for vendor return (compliance with vendor requirements)
- **VAS**: Inspection, cleaning, repackaging, labeling for RMA
- **Kit Disassembly**: Return entire service kits, receive components individually

**Workflow**:
```
1. Technician returns defective part from field → Create RMA
2. Receive into quarantine location → Inspection (repackable vs scrap)
3. Repackable → Repack order (clean, repackage per vendor specs, label)
4. VAS operations → Inspection report, photos, labeling
5. Ship to vendor → Issue from stock, track warranty claim
6. Vendor credit received → Post to Finance (ADR-009)
```

**Integration Points**:
- **Field Service (ADR-042)**: RMA creation, warranty tracking
- **Procurement (ADR-023)**: Vendor return, warranty claim
- **Quality (ADR-039)**: Defect inspection, failure analysis
- **Finance (ADR-009)**: Warranty accrual, vendor credit posting

**KPIs**:
- **RMA Processing Time**: p95 < 48 hours (return to vendor shipment)
- **Repack Quality**: >= 99% (vendor acceptance rate)
- **Warranty Recovery**: >= 80% (successful claims vs defects)

#### 5. Repair Center / Depot Operations

**Use Case**: Centralized repair/refurbishment operations for high-value repairable components, service parts, and warranty repair programs

**Capabilities Leveraged**:
- **Repackaging**: Disassemble failed units, replace defective subcomponents, reassemble
- **VAS Operations**: Multi-stage repair workflow (diagnose → repair → test → certify → repackage)
- **Kit Operations**: Repair kits for common failures, replacement parts kits
- **Catch Weight**: Variable-weight components requiring precise measurement after repair

**Repair Center Operations**:

**A. Repair-or-Scrap Decision Workflow**:
```
1. Defective unit received → RMA intake inspection
2. Initial diagnosis → Determine repairability (economic repair limit check)
3. Decision:
   - Repairable + economic → Send to repair queue
   - Repairable + uneconomic → Scrap and order replacement
   - Not repairable → Scrap with failure analysis
4. Economic repair limit: Repair cost < (Replacement cost × threshold %)
   - Typical threshold: 60-70% for electronics, 50% for mechanical
```

**B. Multi-Stage Refurbishment Process**:
```
1. Intake → Receive into REPAIR_QUEUE location
2. Diagnosis → Troubleshoot, identify failed components
   - Capture: Failure mode, root cause, repair estimate
3. Component Replacement → Issue replacement parts from repair inventory
   - Kit explosion: Standard repair kit components
   - Ad-hoc parts: Special order for unique failures
4. Repair Execution → Replace components, clean, adjust
   - Track: Labor hours, materials consumed, repair technician
5. Testing/Calibration → Functional test, calibration verification
   - Capture: Test results, calibration certificate, QA approval
6. Certification → Quality inspection, pass/fail disposition
   - Pass → Move to REFURBISHED_AVAILABLE location
   - Fail → Return to repair queue or scrap
7. Repackaging → Clean, repackage, label as refurbished
8. Final Receipt → Receive into available inventory with REFURBISHED status
```

**C. Test & Calibration Integration**:
```
- **Test Station Assignment**: Route to appropriate test equipment
- **Test Scripts**: Automated test sequences per product type
- **Calibration Records**: Link to calibration certificates (ADR-039 Quality)
- **Test Equipment Tracking**: Calibrated test equipment with expiry dates
- **Pass/Fail Criteria**: Configurable acceptance thresholds
- **Compliance**: FDA, ISO, UL certification requirements
```

**D. Repair Cost Tracking & Analysis**:
```
RepairOrder:
  - repairOrderNumber: string
  - sku: string (failed unit)
  - serialNumber: string
  - failureMode: enum (MECHANICAL, ELECTRICAL, FIRMWARE, WEAR, ACCIDENT)
  - rootCause: string
  - diagnosisLaborHours: decimal
  - repairLaborHours: decimal
  - testingLaborHours: decimal
  - laborCost: decimal (total labor cost)
  - componentsCost: decimal (replacement parts)
  - totalRepairCost: decimal
  - replacementCost: decimal (new unit cost for comparison)
  - economicRepairLimit: decimal (threshold %)
  - repairability: enum (REPAIRABLE, UNECONOMIC, NOT_REPAIRABLE)
  - status: enum (INTAKE, DIAGNOSIS, REPAIR_IN_PROGRESS, TESTING, CERTIFICATION, COMPLETED, SCRAPPED)
  - startDate: timestamp
  - completionDate: timestamp
  - turnaroundTime: integer (hours)
  - technician: string
  - qualityInspector: string
  - certificationDate: timestamp
```

**E. Loaner/Exchange Programs**:
```
1. Customer needs immediate replacement → Issue loaner unit
2. Defective unit received → Create repair order with loaner tracking
3. Repair completed → Notify customer, schedule exchange
4. Customer returns loaner → Receive loaner back into LOANER_POOL
5. Exchange transaction:
   - Issue repaired unit to customer
   - Receive loaner unit from customer
   - Track: Loaner ID, customer, issue date, return date, condition

LoanerPool:
  - loanerSku: string (repairable item)
  - serialNumber: string
  - status: enum (AVAILABLE, ON_LOAN, IN_REPAIR, QUARANTINE)
  - loanedTo: string (customer ID)
  - issuedDate: timestamp
  - expectedReturnDate: timestamp
  - condition: enum (NEW, GOOD, FAIR, DAMAGED)
  - loanCount: integer (total times loaned)
```

**F. Certified Repair Centers (3rd Party Integration)**:
```
1. Defective unit shipped to certified repair depot
2. Repair depot performs work → Capture repair invoice, test results
3. Repaired unit returned → Receive with refurbished status
4. Cost allocation → Post repair invoice to Controlling (ADR-028)
5. Warranty claim → If under warranty, submit claim to OEM

CertifiedRepairCenter:
  - depotId: string
  - depotName: string
  - certifications: list<string> (OEM authorized, ISO certified)
  - averageTurnaroundDays: integer
  - qualityRating: decimal (vendor scorecard)
  - costPerRepair: map<sku, decimal>
```

**G. Warranty Refurbishment Programs**:
```
- **Warranty Refurb Policy**: Repair under warranty vs replace with new
- **Refurb as Replacement**: Issue refurbished units as warranty replacements
- **Cost Recovery**: Track warranty refurb costs for OEM claim
- **Refurb Warranty**: Shorter warranty period for refurbished units (e.g., 90 days vs 1 year)
- **Refurb Pool Management**: Maintain min/max levels of refurbished inventory
```

**Integration Points**:
- **Field Service (ADR-042)**: Service orders trigger repair needs, loaner exchanges
- **Quality Management (ADR-039)**: Test results, calibration certificates, failure analysis
- **Controlling (ADR-028)**: Repair cost capture, labor tracking, overhead allocation
- **Procurement (ADR-023)**: Repair parts procurement, certified depot invoices
- **Fixed Assets (ADR-021)**: Repairable asset tracking, depreciation on loaners
- **Finance (ADR-009)**: Warranty accrual, refurbishment capitalization

**Repair Center KPIs**:
- **Repair Turnaround Time (TAT)**: p95 < 5 days for in-house, < 10 days for depot
- **First Pass Yield**: >= 90% (repaired units pass testing on first attempt)
- **Repair Cost Accuracy**: Within 10% of estimate
- **Economic Repair Rate**: >= 85% (repairs within economic limit)
- **Loaner Availability**: >= 95% (loaner units available when needed)
- **Loaner Return Rate**: >= 98% (loaners returned on time)
- **Refurbished Quality**: >= 99% (refurb units meet quality standards)
- **Calibration Compliance**: 100% (all test equipment within calibration date)
- **Depot Performance**: Average quality rating >= 4.0/5.0

**Repair Center Data Model Extensions**:

**Repair Inventory Location Types**:
- `REPAIR_QUEUE`: Awaiting diagnosis and repair
- `REPAIR_IN_PROGRESS`: Currently being repaired
- `TEST_STATION`: In testing/calibration
- `REFURBISHED_AVAILABLE`: Repaired and available for issue
- `LOANER_POOL`: Available loaner units
- `LOANER_ON_LOAN`: Loaners issued to customers
- `REPAIR_QUARANTINE`: Failed testing, awaiting rework or scrap

**Repair-Specific Events**:
- `RepairOrderCreatedEvent`: Defective unit entered repair queue
- `DiagnosisCompletedEvent`: Repair estimate and repairability determined
- `RepairCompletedEvent`: Unit repaired and ready for testing
- `TestCompletedEvent`: Testing finished with pass/fail result
- `CertificationCompletedEvent`: Quality inspection passed
- `RefurbishedReceivedEvent`: Refurbished unit available in inventory
- `LoanerIssuedEvent`: Loaner unit issued to customer
- `LoanerReturnedEvent`: Loaner returned from customer
- `DepotRepairShippedEvent`: Unit shipped to certified repair depot
- `DepotRepairReceivedEvent`: Unit returned from depot

#### 6. Consignment Inventory at Customer Sites

**Use Case**: Manage consignment stock at customer locations (industrial service contracts)

**Capabilities Leveraged**:
- **Packaging Hierarchy**: Track consignment inventory by packaging level (pallet, case, each)
- **Repackaging**: Replenish consignment inventory from warehouse (break bulk)
- **Catch Weight**: Variable-weight consignment materials (chemicals, fluids, gases)

**Workflow**:
```
1. Initial consignment placement → Transfer from warehouse to customer site
2. Customer consumes parts → Consumption event triggers billing (ADR-025)
3. Replenishment trigger (min/max levels) → Break bulk from warehouse
4. Periodic reconciliation → Physical count vs system (cycle count)
5. Contract end → Retrieve consignment stock, repack if needed
```

**Integration Points**:
- **Sales (ADR-025)**: Consignment billing upon consumption
- **Field Service (ADR-042)**: Consignment site visits, replenishment
- **Finance (ADR-009)**: Consignment asset tracking, revenue recognition

**KPIs**:
- **Consignment Accuracy**: >= 99% (physical vs system reconciliation)
- **Replenishment Timeliness**: p95 < 24 hours (stockout prevention)
- **Consumption Billing Accuracy**: >= 99.9% (correct billing upon use)

---

### Service Industry Data Model Extensions

**Service Inventory Location Types**:
- `SERVICE_VAN`: Mobile inventory on service vehicles
- `CONSIGNMENT`: Customer site consignment stock
- `FIELD_WAREHOUSE`: Regional service centers
- `LOANER_POOL`: Loaner equipment inventory
- `RMA_QUARANTINE`: Returned materials awaiting inspection
- `REPAIR_QUEUE`: Defective units awaiting diagnosis
- `REPAIR_IN_PROGRESS`: Units currently being repaired
- `TEST_STATION`: Units in testing/calibration
- `REFURBISHED_AVAILABLE`: Repaired units ready for service

**Service Kit Types**:
- `MAINTENANCE_KIT`: Scheduled preventive maintenance
- `REPAIR_KIT`: Emergency repair (breakdown, outage)
- `INSTALLATION_KIT`: New service installation (meter, equipment)
- `INSPECTION_KIT`: Inspection/testing tools and materials
- `CALIBRATION_KIT`: Equipment calibration and adjustment
- `REPAIR_PARTS_KIT`: Standard components for common repairs

**Service-Specific Events**:
- `ServiceKitReservedEvent`: Kit reserved for service order
- `ServiceKitIssuedEvent`: Kit issued to technician
- `FieldConsumptionEvent`: Parts consumed in field
- `VanReplenishmentEvent`: Service van restocked
- `ConsignmentConsumptionEvent`: Consignment stock consumed at customer site
- `WarrantyReturnEvent`: Defective part returned under warranty
- `RepairOrderCreatedEvent`: Defective unit entered repair queue
- `RepairCompletedEvent`: Unit repaired and ready for testing
- `RefurbishedReceivedEvent`: Refurbished unit available in inventory
- `LoanerIssuedEvent`: Loaner unit issued to customer
- `LoanerReturnedEvent`: Loaner returned from customer

---

### Service Industry Robustness Assessment

**Coverage Score: 95/100** (improved from 92/100 with repair center addition)

**Strengths**:
- ✅ **Service Kit Management**: Complete static/dynamic/virtual kit support
- ✅ **Field Inventory**: Break bulk, repack, packaging hierarchy for van stock
- ✅ **Utilities Support**: Catch weight for cables/spools, meter kits, emergency parts
- ✅ **Warranty/RMA**: Repack and VAS for vendor returns
- ✅ **Consignment**: Multi-location tracking with consumption billing
- ✅ **Repair Center/Depot**: In-house refurbishment, loaner programs, test/calibration, economic repair logic

**Recommended Enhancements** (Phase 7+):
- **Field Consumption Forecasting**: Predict van restocking needs based on service history
- **Service Kit Optimization**: Auto-recommend kit composition based on first-time-fix rates
- **Real-Time Van Inventory**: Mobile app sync for real-time field consumption posting
- **Consignment Analytics**: Optimize consignment levels based on consumption patterns
- **Predictive Repair Analytics**: Forecast repair demand and loaner pool requirements

---

