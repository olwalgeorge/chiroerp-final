# ADR-024: Inventory Management (MM-IM)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Operations Team  
**Priority**: P1 (High)  
**Tier**: Core  
**Tags**: inventory, stock, valuation, warehousing, omnichannel

## Context
Inventory accuracy and valuation are critical for financial statements, order fulfillment, and customer experience. A SAP-grade ERP must support multi-store and multi-warehouse operations, POS synchronization, multi-UoM handling, multi-currency valuation, omnichannel fulfillment, and product variants while maintaining audit-ready stock and cost controls.

## Decision
Implement a **multi-entity Inventory Management** capability with a stock ledger, movement types, valuation engines, and omnichannel reservation/fulfillment controls.

### Scope
- Multi-store and multi-warehouse locations with optional bin-level tracking.
- POS and ecommerce synchronization with offline reconciliation.
- Multi-UoM conversions (purchase, stocking, and sales units).
- Multi-currency inventory valuation with FX revaluation.
- Product variants (size/color/style) and barcode support.
- Omnichannel allocation, reservation, and ATP (available-to-promise).

### Feature Tiering (Core vs Advanced)
**Core**
- Stock ledger, locations, FIFO/weighted-average valuation, basic reservations.
- Basic cycle counting and POS synchronization.

**Advanced**
- Lot/serial/FEFO, consignment, landed cost, multi-currency valuation.
- Advanced ATP, WMS execution integration boundary (ADR-038).

### Core Capabilities
- **Stock ledger** with movement types (receipt, issue, transfer, adjustment, shrinkage).
- **Valuation**: FIFO, weighted average, standard cost; LIFO as region-specific option (US GAAP only).
- **Lot/serial tracking** with expiry, FEFO, and traceability for regulated goods.
- **Reservations and allocations** with channel priority rules.
- **Cycle counting** and periodic reconciliation with GL.
- **Landed cost** allocation (freight, duties, insurance) and cost layers.
- **Stock status**: available, reserved, quality hold, damaged, consigned, in-transit.
- **Inventory visibility**: real-time by location, channel, and variant.

### Multi-Store and POS
- **Store locations** as first-class locations with local stock, transfers, and returns.
- **POS events**: sales, returns, voids, and offline postings reconciled into the stock ledger.
- **Store reconciliation**: daily POS vs stock variance with shrinkage reason codes.
- **Cash-and-carry** vs **ship-from-store** workflows supported.

### Multi-UoM (Units of Measure)
- **Base UoM** per SKU; purchase and sales UoM with conversion factors.
- **Rounding rules** per UoM (e.g., case to each, pallet to case).
- **UoM validation** to prevent fractional issues for discrete items.
- **Reporting** normalized to base UoM with conversion audit trail.

### Multi-Currency
- **Valuation currency** per company code and ledger.
- **Transaction currency** for receipts/issues with FX rates at posting time.
- **Revaluation** for inventory held in foreign currencies at period close.
- **Intercompany transfers** with FX differences captured to GL.

### Multi-Variant and Multichannel
- **Variant matrix**: attributes (size, color, style) define SKU variants.
- **Barcodes** per variant and per packaging level.
- **Channel-specific allocations** (POS, ecommerce, marketplace, wholesale).
- **Order orchestration** rules: ship-from-store, ship-from-warehouse, split shipment.

### Data Model (Conceptual)
- `Item`, `ItemVariant`, `UoM`, `UoMConversion`, `Location`, `Bin`, `StockLedger`, `StockLot`, `StockSerial`, `Reservation`, `TransferOrder`, `CycleCount`, `CostLayer`, `PriceCondition`.

### Key Workflows
- **Receive**: PO -> GR -> stock ledger -> valuation posting.
- **Transfer**: store-to-store or warehouse-to-store with in-transit status.
- **Pick/Pack/Ship**: reservation -> allocation -> issue -> shipment confirmation.
- **Return/RMA**: return authorization -> inspection -> restock or write-off.
- **Cycle Count**: scheduled counts -> variance approval -> adjustment posting.

### Integration Points
- **Procurement**: goods receipt, vendor returns, landed cost.
- **Sales/SD**: ATP, fulfillment, returns, channel allocations.
- **POS**: sales/return events, offline reconciliation.
- **Manufacturing**: material issue, production receipt.
- **Finance/GL**: inventory valuation and COGS postings.
- **Analytics**: stock turns, shrinkage, and service-level KPIs.

### WMS Integration Boundary
This ADR covers **ERP-grade inventory control** (SAP MM-IM equivalent). For **warehouse execution** (SAP EWM / Manhattan WMS equivalent), see ADR-038.

#### Scope Boundary
| Capability | ADR-024 (MM-IM) | ADR-038 (WES/WMS) |
|------------|-----------------|-------------------|
| Stock ledger & valuation | ✅ Owner | Consumes |
| Location/bin hierarchy | Basic (location + optional bin) | Full (zone/aisle/rack/bin) |
| Reservations & ATP | ✅ Owner | Consumes/extends |
| Lot/serial tracking | ✅ Owner | Consumes |
| Cycle counting | ✅ Owner | Executes |
| Wave planning | ❌ | ✅ Owner |
| Task management | ❌ | ✅ Owner |
| Pick optimization | ❌ | ✅ Owner |
| Putaway rules | ❌ | ✅ Owner |
| Labor management | ❌ | ✅ Owner |
| Slotting optimization | ❌ | ✅ Owner |
| Automation/IoT | ❌ | ✅ Owner |

#### Required Interfaces to WMS (ADR-038)
| Interface | Direction | Description |
|-----------|-----------|-------------|
| `InventorySnapshot` | MM-IM → WMS | Current stock by location/lot/serial |
| `ReservationEvent` | MM-IM → WMS | Allocation requests for fulfillment |
| `GoodsMovementConfirm` | WMS → MM-IM | Confirmed picks, putaways, transfers |
| `CycleCountResult` | WMS → MM-IM | Count results for variance processing |
| `ATPQuery` | WMS → MM-IM | Real-time availability check |
| `TaskComplete` | WMS → MM-IM | Execution confirmations for stock update |

#### Integration SLOs
| Metric | Target |
|--------|--------|
| Stock sync latency (MM-IM → WMS) | p95 < 30 seconds |
| Movement confirmation (WMS → MM-IM) | p95 < 60 seconds |
| ATP query response | p95 < 200ms |
| Reconciliation frequency | Every 4 hours minimum |
| Discrepancy threshold triggering alert | > 0.1% variance |

### Non-Functional Constraints
- **Accuracy**: 99.5% inventory accuracy target with cycle counting.
- **Latency**: POS stock updates within 60 seconds; offline reconciliation within 24 hours.
- **Idempotency**: all stock movements are idempotent and traceable.
- **Concurrency**: reservation engine prevents oversell.

## Alternatives Considered
- **Finance-only valuation**: rejected (no operational visibility).
- **Warehouse-only tracking**: rejected (no accounting controls).
- **External WMS only**: rejected (loss of ERP control).
- **Single-UoM only**: rejected (inadequate for wholesale and logistics).

## Consequences
### Positive
- Accurate stock visibility across stores, channels, and variants.
- Audit-ready valuation and reconciliation with GL.
- Omnichannel fulfillment with reservation control.

### Negative
- Increased complexity in inventory configuration and master data.
- Requires disciplined transaction capture across all channels.

### Neutral
- Advanced WMS features can be layered later via integration.

---

## Advanced Inventory Operations Extension

### Context
Retail, distribution, food & beverage, and manufacturing industries require sophisticated inventory operations beyond basic stock movements: **packaging hierarchies** (pallet → case → each), **kitting/bundling** (gift sets, promotional packs), **grouping/splitting/repackaging** (break bulk, master packs), and **catch weight handling** (variable weight items like meat, produce). These capabilities are essential for SAP-grade inventory management parity.

### Scope Extension
- **Packaging Hierarchies**: Multi-level packaging structures with nested UOM conversions and barcodes at each level.
- **Kitting & Bundling**: Sales kits, dynamic kitting, virtual kits, kit explosion for ATP.
- **Grouping/Splitting/Repackaging**: Break bulk, repack, master pack creation, deconsolidation, VAS.
- **Catch Weight**: Variable weight items with nominal vs actual weight tracking, dual UOM.

---

## 1. Packaging Hierarchy Management

### Overview
Enable multi-level packaging structures where a single SKU can be stocked, sold, and shipped in various packaging configurations (pallet → case → inner pack → each). Each level has its own GTIN/barcode, dimensions, weight, and UOM conversion.

### Core Capabilities

#### Packaging Levels
- **Base Unit (Each)**: Smallest saleable unit (1 bottle, 1 can, 1 widget).
- **Inner Pack**: Small bundle (6-pack of bottles, 12-pack of cans).
- **Case**: Shipping carton (4 inner packs = 24 bottles).
- **Master Case/Display**: Retail-ready display unit.
- **Pallet**: Distribution unit (40 cases = 960 bottles).
- **Custom Levels**: Configurable hierarchy (e.g., half-pallet, quarter-pallet).

#### Packaging Attributes per Level
- **GTIN/Barcode**: Unique identifier (GTIN-14 for case, GTIN-12/UPC for each).
- **Dimensions**: Length × Width × Height (for cube calculation, warehouse slotting).
- **Weight**: Gross weight including packaging, net weight of contents.
- **UOM Conversion**: How many base units in this level (case = 24 each).
- **Handling Unit Type**: Pallet type (40"×48" wood, 42"×42" plastic), case type (RSC, HSC).

#### Key Workflows
- **Receipt by Pallet**: Receive 10 pallets → auto-convert to 400 cases → 9,600 each (stock tracked at base unit).
- **Pick by Case**: Order 50 cases → allocate from pallet inventory → convert to 1,200 each.
- **Mixed Pallet Receiving**: Pallet contains multiple SKUs → deconsolidate to individual SKUs.
- **Cube/Weight Calculation**: Calculate total cube and weight for warehouse capacity planning and freight rating.

#### Data Model Extensions
```
PackagingLevel:
  - sku: string
  - level: enum (EACH, INNER_PACK, CASE, MASTER_CASE, PALLET, CUSTOM)
  - gtin: string (GTIN-14, GTIN-13, GTIN-12, UPC)
  - uomConversion: decimal (how many base units)
  - length: decimal (inches/cm)
  - width: decimal
  - height: decimal
  - grossWeight: decimal (lbs/kg)
  - netWeight: decimal
  - handlingUnitType: string (e.g., "40x48 wood pallet", "RSC case")
  - stackHeight: integer (max stack count for safety)
  - ti: integer (cases per layer)
  - hi: integer (layers per pallet)
```

#### Integration Points
- **Procurement (ADR-023)**: Purchase by pallet, receive and convert to base units.
- **Sales (ADR-025)**: Sell by case, allocate and convert to base units.
- **WMS (ADR-038)**: Slotting by packaging level, pick path optimization.
- **Shipping**: Calculate freight class, LTL vs FTL based on cube and weight.

#### KPIs / SLOs
- **Packaging hierarchy accuracy**: >= 99.9% (correct UOM conversions).
- **GTIN uniqueness**: 100% unique GTINs across all levels.
- **Cube calculation accuracy**: Within 2% of actual for freight rating.

---

## 2. Kitting & Bundling Operations

### Overview
Support sales kits (pre-configured bundles sold as single SKU), dynamic kitting (assembled on demand), virtual kits (logical grouping without physical assembly), and kit explosion for inventory allocation and ATP calculations.

### Kit Types

#### Static/Pre-Built Kits
- **Definition**: Physical kit assembled in advance, stocked as unique SKU.
- **Example**: "Holiday Gift Basket" (SKU: GIFT-001) = Wine (SKU: WINE-123) + Cheese (SKU: CHEESE-456) + Crackers (SKU: CRACK-789).
- **Inventory**: Kit has its own stock record; components consumed during kit assembly.
- **Use Case**: Gift sets, promotional bundles, starter packs.

#### Dynamic/Build-to-Order Kits
- **Definition**: Kit assembled at warehouse/store when ordered; no pre-built stock.
- **Example**: "Custom PC Build" assembled from CPU, RAM, GPU, case when customer orders.
- **Inventory**: No kit stock; ATP checked against component availability.
- **Use Case**: Configurable products, custom bundles, personalized gifts.

#### Virtual Kits
- **Definition**: Logical grouping for sales/marketing; components picked and shipped separately.
- **Example**: "Office Starter Pack" sold as one line item but fulfilled as separate shipments (desk, chair, monitor).
- **Inventory**: No kit stock; ATP is minimum of all component availability.
- **Use Case**: Multi-item promotions, cross-sell bundles, subscription boxes.

#### Configurable Kits
- **Definition**: Customer selects options from predefined choices.
- **Example**: "Build Your Own 6-Pack" (choose 6 beers from 20 options).
- **Inventory**: Dynamic ATP based on selected components.
- **Use Case**: Customizable bundles, personalization, variety packs.

### Core Capabilities

#### Kit Master Data
- **Kit BOM (Bill of Materials)**:
  - Kit SKU (parent)
  - Component SKUs (children) with quantities
  - Substitution rules (if component out of stock, use alternate)
  - Optional vs mandatory components
- **Kit Pricing**:
  - Bundle price (may be less than sum of components for promotions)
  - Component pricing passthrough vs fixed kit price
- **Kit Assembly Rules**:
  - Assemble in advance vs on-demand
  - Assembly location (warehouse, store, vendor)
  - Assembly lead time

#### Kit Explosion for ATP
- **ATP Calculation**: Calculate available-to-promise for kit based on component availability.
  - Example: Kit needs 1 × Wine, 1 × Cheese, 1 × Crackers
  - Wine: 100 available, Cheese: 50 available, Crackers: 75 available
  - **Kit ATP = min(100, 50, 75) = 50 kits**
- **Component Reservation**: When kit order placed, reserve all components atomically.
- **Partial Fulfillment**: Allow partial kit shipment if some components unavailable (with customer approval).

#### Kit Assembly Workflow
1. **Assembly Order Creation**: Generate work order to build kits.
2. **Component Allocation**: Reserve components from inventory.
3. **Component Issue**: Deduct components from stock.
4. **Kit Receipt**: Receive assembled kit into stock (for static kits).
5. **Cost Calculation**: Roll up component costs to kit cost.

#### Kit Disassembly Workflow
1. **Disassembly Order**: Break kit back into components (returns, overstock).
2. **Kit Issue**: Deduct kit from stock.
3. **Component Receipt**: Receive components back into inventory.
4. **Variance Handling**: Track damaged/missing components during disassembly.

### Data Model Extensions
```
Kit:
  - kitSku: string
  - kitType: enum (STATIC, DYNAMIC, VIRTUAL, CONFIGURABLE)
  - assemblyLocation: string (warehouse, store, vendor)
  - assemblyLeadTimeDays: integer
  - pricing: enum (BUNDLE_PRICE, COMPONENT_SUM)

KitComponent:
  - kitSku: string
  - componentSku: string
  - quantity: decimal
  - isOptional: boolean
  - substitutionGroup: string (allow substitutions within group)
  - sequence: integer (assembly order)

KitAssemblyOrder:
  - assemblyOrderNumber: string
  - kitSku: string
  - quantityToBuild: integer
  - status: enum (PLANNED, IN_PROGRESS, COMPLETED, CANCELLED)
  - componentAllocation: map<sku, quantity>
  - actualComponentsUsed: map<sku, quantity>
  - variance: map<sku, quantity>
```

### Integration Points
- **Sales (ADR-025)**: Kit pricing, kit order processing, ATP checks.
- **Manufacturing (ADR-037)**: Kit assembly as light manufacturing (for complex kits).
- **Inventory**: Component reservation, kit stock tracking.
- **Controlling (ADR-028)**: Kit costing, component cost rollup.

### KPIs / SLOs
- **Kit ATP accuracy**: >= 99% (correct component availability calculation).
- **Kit assembly cycle time**: p95 < 4 hours for dynamic kits.
- **Kit component variance**: < 0.5% (assembly accuracy).
- **Kit order fulfillment rate**: >= 95% (complete kit shipment).

---

## 3. Grouping, Splitting & Repackaging Operations

### Overview
Support inventory transformations where packaging changes but SKU remains same (break bulk), or where multiple items are combined/separated (master packs, deconsolidation). Essential for retail, distribution, and value-added services.

### Operation Types

#### Break Bulk (Splitting)
- **Definition**: Split larger packaging unit into smaller units.
- **Example**: Receive pallet (960 bottles) → break into 40 cases (24 bottles each).
- **Inventory Impact**: Reduce pallet-level stock, increase case-level stock (same SKU, different packaging level).
- **Use Cases**:
  - Distributor breaks pallets for smaller customers.
  - Retailer breaks cases to stock shelves (each-level).
  - Damaged outer packaging requires repack.

#### Repackaging
- **Definition**: Change packaging without changing SKU or quantity.
- **Example**: Case damaged, repack 24 bottles into new case.
- **Inventory Impact**: Adjust packaging level, maintain total quantity.
- **Use Cases**:
  - Damaged packaging replacement.
  - Rebranding (new label, same product).
  - Compliance (new regulatory labels).

#### Master Pack Creation (Grouping)
- **Definition**: Combine multiple units into display-ready master pack.
- **Example**: Combine 6 individual items into retail display pack.
- **Inventory Impact**: Reduce individual unit stock, increase master pack stock.
- **Use Cases**:
  - Retail display building (shippers, PDQs).
  - Promotional packs (multi-packs).
  - Vendor compliance (retailer requires specific pack size).

#### Deconsolidation
- **Definition**: Split mixed pallet/container into individual SKUs.
- **Example**: Import container with 50 SKUs → unload and put away by SKU.
- **Inventory Impact**: Receive mixed load, split into individual SKU locations.
- **Use Cases**:
  - Import receiving (mixed containers).
  - Cross-dock operations (inbound mixed, outbound sorted).
  - LTL freight receiving (multiple customers, multiple SKUs per pallet).

#### Value-Added Services (VAS)
- **Definition**: Enhance product before shipping (labeling, gift wrap, customization).
- **Example**: Add customer logo sticker, gift wrap, insert promotional materials.
- **Inventory Impact**: Move through VAS station, track labor/material costs.
- **Use Cases**:
  - Private label operations.
  - Gift wrapping services.
  - Product customization (engraving, monogramming).
  - Compliance labeling (add region-specific labels).

### Core Capabilities

#### Repack Work Orders
- **Work Order Creation**: Specify source packaging, target packaging, quantity.
- **Material Issue**: Deduct from source packaging level.
- **Material Receipt**: Receive into target packaging level.
- **Variance Tracking**: Track shrinkage, damage during repack.
- **Cost Capture**: Labor and material costs for repack operation.

#### VAS Work Orders
- **VAS Master Data**: Define VAS operations (label, wrap, assemble, customize).
- **VAS Routing**: Sequence of operations (e.g., label → inspect → wrap → pack).
- **Material Consumption**: Track labels, boxes, tape, labor used.
- **Status Tracking**: VAS-in-progress, VAS-completed, ready-to-ship.
- **Billing**: Charge customer for VAS services (per-unit fee).

#### Inventory Transformation Rules
- **Packaging Level Mapping**: Define valid transformations (pallet → case, case → each).
- **Conversion Validation**: Prevent invalid splits (can't split 24-unit case into 25 each).
- **Audit Trail**: Track all transformations with operator, timestamp, reason code.

### Data Model Extensions
```
RepackOrder:
  - repackOrderNumber: string
  - sku: string
  - sourcePackagingLevel: string (PALLET)
  - targetPackagingLevel: string (CASE)
  - sourceQuantity: integer (1 pallet)
  - targetQuantity: integer (40 cases)
  - reasonCode: enum (DAMAGED_PACKAGING, BREAK_BULK, COMPLIANCE, OTHER)
  - status: enum (PLANNED, IN_PROGRESS, COMPLETED)
  - variance: decimal (shrinkage/damage)
  - laborCost: decimal
  - materialCost: decimal

VASOrder:
  - vasOrderNumber: string
  - sku: string
  - quantity: integer
  - vasOperations: list<VASOperation>
  - status: enum (PENDING, IN_PROGRESS, COMPLETED)
  - startTime: timestamp
  - completionTime: timestamp
  - totalCost: decimal

VASOperation:
  - operationCode: string (LABEL, WRAP, CUSTOMIZE, INSPECT)
  - sequence: integer
  - laborMinutes: integer
  - materialConsumed: map<sku, quantity>
  - costPerUnit: decimal
```

### Integration Points
- **WMS (ADR-038)**: Repack task management, VAS workstation routing.
- **Controlling (ADR-028)**: Labor and material cost capture for repack/VAS.
- **Sales (ADR-025)**: VAS pricing and customer charges.
- **Quality (ADR-039)**: Inspection before/after repack, VAS quality checks.

### KPIs / SLOs
- **Repack accuracy**: >= 99.5% (correct quantity transformation).
- **Repack cycle time**: p95 < 2 hours for break bulk operations.
- **VAS cycle time**: p95 < 4 hours per order (depends on operation complexity).
- **Shrinkage during repack**: < 0.2% of quantity.

---

## 4. Catch Weight / Variable Weight Handling

### Overview
Handle products sold by weight where actual weight varies from nominal weight (meat, produce, cheese, seafood). Requires tracking both nominal (stocking) units and actual (catch) weights for accurate inventory valuation, pricing, and compliance.

### Core Concepts

#### Nominal vs Actual Weight
- **Nominal Weight**: Standard weight for stocking/planning (e.g., "1 lb package").
- **Actual Weight**: Real weight at receipt/sale (e.g., 1.03 lbs actual).
- **Variance**: Difference between nominal and actual (±3% typical for meat).

#### Dual UOM Tracking
- **Stocking UOM**: Count-based (25 steaks, 50 packages).
- **Sales UOM**: Weight-based (23.45 lbs actual weight).
- **Pricing**: Price per pound × actual weight = customer charge.

#### Average Weight
- **Statistical Average**: Calculate average actual weight over time for planning.
- **Example**: "1 lb packages" average 1.02 lbs over last 30 days.
- **Use Case**: ATP calculation, forecasting, procurement planning.

### Core Capabilities

#### Catch Weight Receipt
- **Receipt Process**:
  1. Scan package barcode (links to nominal weight).
  2. Weigh on scale (capture actual weight).
  3. Record: Item SKU, Lot number, Nominal qty (1 package), Actual weight (1.03 lbs).
- **Valuation**: Cost extended by actual weight (not nominal).
  - Cost per lb: $5.00/lb
  - Actual weight: 1.03 lbs
  - Item cost: $5.15 (not $5.00)

#### Catch Weight Inventory Tracking
- **Stock Ledger**: Track both count and weight.
  - On-hand: 100 packages (nominal 100 lbs, actual 103.2 lbs)
- **ATP Calculation**: Use average weight for planning.
  - Customer orders 50 lbs → allocate ~49 packages (avg 1.02 lbs each).
- **FIFO/FEFO**: Track by lot with actual weight for valuation.

#### Catch Weight Sales
- **Pricing Process**:
  1. Scan package at checkout (retrieve actual weight from receipt).
  2. Calculate price: $5.00/lb × 1.03 lbs = $5.15.
  3. Print label with actual weight and price.
- **Tare Weight**: Subtract packaging weight if needed (gross - tare = net).

#### Catch Weight Variance
- **Expected vs Actual**: Compare nominal weight to actual weight.
- **Variance Tracking**: Monitor by vendor, lot, product for quality control.
- **Example**: If nominal is 1 lb but average actual is 0.95 lb, flag vendor for short weight.

#### Compliance
- **USDA/FDA**: Label must show actual net weight, not average.
- **Weights & Measures**: Scale calibration, accuracy requirements.
- **Price Verification**: Actual weight must match POS price calculation.

### Data Model Extensions
```
CatchWeightItem:
  - sku: string
  - isCatchWeight: boolean
  - nominalWeight: decimal (1.0 lb)
  - nominalWeightUOM: string (LB, KG)
  - averageActualWeight: decimal (1.02 lb, rolling average)
  - varianceTolerance: decimal (±3%)

CatchWeightLot:
  - sku: string
  - lotNumber: string
  - packageCount: integer (100 packages)
  - nominalTotalWeight: decimal (100.0 lbs)
  - actualTotalWeight: decimal (103.2 lbs)
  - variancePercent: decimal (+3.2%)

CatchWeightTransaction:
  - transactionId: string
  - sku: string
  - lotNumber: string
  - packageNumber: string (unique package ID)
  - nominalWeight: decimal (1.0 lb)
  - actualWeight: decimal (1.03 lb)
  - tareWeight: decimal (0.02 lb, packaging)
  - netWeight: decimal (1.01 lb, actual - tare)
  - scaleId: string (for audit trail)
  - timestamp: timestamp
```

### Integration Points
- **Procurement (ADR-023)**: Receive catch weight items with scale integration.
- **Sales (ADR-025)**: Price by actual weight at checkout.
- **Finance (ADR-009)**: Valuation by actual weight for COGS.
- **Quality (ADR-039)**: Variance monitoring, vendor scorecards.
- **POS**: Scale integration for in-store weighing and pricing.

### KPIs / SLOs
- **Catch weight accuracy**: >= 99.9% (scale accuracy per Weights & Measures).
- **Variance within tolerance**: >= 95% of lots within ±3% of nominal.
- **Scale integration latency**: < 1 second per weigh operation.
- **Label generation**: < 3 seconds per catch weight package.

---

## 5. Repair & Refurbishment Inventory Management

### Overview
Support inventory movements and stock locations for repair operations. Repair workflows are owned by ADR-040 (Plant Maintenance); this section covers only the inventory management aspects.

### Repair Inventory Locations
- **REPAIR_QUEUE**: Defective units awaiting diagnosis (quarantine stock)
- **REPAIR_IN_PROGRESS**: Units currently being repaired (WIP stock)
- **TEST_STATION**: Units in testing/calibration (QC hold)
- **REFURBISHED_AVAILABLE**: Repaired units ready for issue (available stock)
- **REPAIR_QUARANTINE**: Units that failed testing, awaiting rework or scrap

### Loaner Pool Inventory Management
- **LOANER_POOL**: Available loaner units (special stock category)
- **LOANER_ON_LOAN**: Loaner units issued to customers (consignment-like tracking)
- **Stock Tracking**: Each loaner tracked by serial number with condition status
- **Valuation**: Loaner inventory valued at current book value (linked to Fixed Assets ADR-021)
- **Depreciation**: Loaner depreciation continues while on loan

### Inventory Movements for Repair Operations

#### Defective Unit Receipt
```
Movement Type: REPAIR_RECEIPT
From: Customer / Field Location / Service Van
To: REPAIR_QUEUE location
Valuation: No value change (internal movement)
Triggers: Repair work order creation (ADR-040)
```

#### Repair Parts Issue
```
Movement Type: REPAIR_ISSUE
From: Warehouse / Spare Parts location
To: REPAIR_IN_PROGRESS (consumed)
Valuation: Deduct from available stock, charge to repair work order
Cost Allocation: Component cost to repair order (ADR-040)
```

#### Refurbished Unit Receipt
```
Movement Type: REFURBISH_RECEIPT
From: REPAIR_IN_PROGRESS
To: REFURBISHED_AVAILABLE
Valuation: Original cost + repair costs = new refurbished cost basis
Stock Type: REFURBISHED (separate from NEW stock)
```

#### Loaner Issue
```
Movement Type: LOANER_ISSUE
From: LOANER_POOL
To: LOANER_ON_LOAN
Customer Link: Track which customer has loaner
Expected Return: Due date for loaner return
```

#### Loaner Return
```
Movement Type: LOANER_RETURN
From: LOANER_ON_LOAN
To: LOANER_POOL (if good condition) or REPAIR_QUEUE (if damaged)
Condition Check: Inspect and update loaner condition status
```

#### Scrap from Repair
```
Movement Type: REPAIR_SCRAP
From: REPAIR_QUEUE or REPAIR_IN_PROGRESS
To: Scrapped (remove from inventory)
Valuation: Write-off cost to scrap account
Reason Codes: NOT_REPAIRABLE, UNECONOMIC, BEYOND_REPAIR
```

### Refurbished Stock Management
- **Stock Type Differentiation**: NEW vs REFURBISHED stock (separate stock types)
- **Valuation Method**: Refurbished cost = Original cost + Repair costs
- **ATP Handling**: Refurbished stock available for allocation (configurable per SKU)
- **Warranty Tracking**: Different warranty terms for refurbished (shorter period)
- **Quality Certification**: Link to QA certification record (ADR-039)
- **Pricing**: Refurbished items may have different pricing (ADR-025)

### Spare Parts for Repair Operations
- **Spare Parts Catalog**: SKUs designated as repair parts
- **Reservation for Repair**: Reserve spare parts when repair work order created
- **Critical Spares**: Maintain safety stock for high-value repairable items
- **Reorder Point**: Trigger procurement when spare parts below minimum
- **Cost Allocation**: Spare parts issued charge to repair work order cost

### Data Model Extensions
```
InventoryLocation:
  - locationType: enum (...existing..., REPAIR_QUEUE, REPAIR_IN_PROGRESS, 
                        TEST_STATION, REFURBISHED_AVAILABLE, REPAIR_QUARANTINE,
                        LOANER_POOL, LOANER_ON_LOAN)

StockLedger:
  - stockType: enum (NEW, USED, REFURBISHED, LOANER, DAMAGED, SCRAP)
  - movementType: enum (...existing..., REPAIR_RECEIPT, REPAIR_ISSUE, 
                        REFURBISH_RECEIPT, LOANER_ISSUE, LOANER_RETURN, REPAIR_SCRAP)
  - linkedWorkOrder: string (repair work order number from ADR-040)
  - refurbishedCostBasis: decimal (original + repair costs)

LoanerInventory:
  - sku: string
  - serialNumber: string
  - stockLocation: enum (LOANER_POOL, LOANER_ON_LOAN)
  - condition: enum (NEW, GOOD, FAIR, DAMAGED)
  - loanCount: integer (total times loaned)
  - currentCustomer: string (if on loan)
  - issuedDate: timestamp (if on loan)
  - expectedReturnDate: timestamp (if on loan)
  - assetNumber: string (link to Fixed Assets ADR-021)
```

### Integration Points
- **Plant Maintenance (ADR-040)**: Repair work orders trigger inventory movements
- **Quality (ADR-039)**: QA certification before moving to REFURBISHED_AVAILABLE
- **Controlling (ADR-028)**: Repair costs allocated to work orders
- **Fixed Assets (ADR-021)**: Loaner asset depreciation and tracking
- **Procurement (ADR-023)**: Spare parts replenishment for repair operations
- **Sales (ADR-025)**: Refurbished stock available for sale (with different pricing)

### KPIs / SLOs
- **Loaner Inventory Accuracy**: >= 99% (loaner location and condition tracking)
- **Refurbished Stock Valuation Accuracy**: >= 99.5% (cost basis correctness)
- **Spare Parts Availability**: >= 98% (for critical repair parts)
- **Repair Location Cycle Count Frequency**: Weekly for LOANER_POOL, monthly for repair queues
- **Loaner Movement Latency**: < 2 minutes (loaner issue/return posting)

---

## Implementation Plan Updates

### Phase 6: Advanced Inventory Operations (7 months)
**Packaging Hierarchies** (2 months):
- Packaging level master data and UOM conversions.
- Multi-level barcode scanning and cube/weight calculations.
- Packaging hierarchy reporting and freight rating integration.

**Kitting & Bundling** (2 months):
- Kit BOM definition and ATP explosion logic.
- Static kit assembly/disassembly workflows.
- Dynamic kit fulfillment and virtual kit order processing.

**Grouping/Splitting/Repackaging** (1 month):
- Repack work orders and packaging transformation rules.
- Break bulk, master pack creation, deconsolidation workflows.
- VAS operations and cost capture.

**Catch Weight** (1 month):
- Catch weight item master data and lot tracking.
- Scale integration for receipt and sales.
- Actual weight pricing and variance reporting.

**Repair & Refurbishment Inventory** (1 month):
- Repair location types and stock movements.
- Loaner pool inventory management.
- Refurbished stock valuation and tracking.
- Integration with Plant Maintenance work orders (ADR-040).

**Total Duration**: 7 months (extended from 6 months, can run in parallel with WMS Phase 4).
- Scale integration for receipt and sales.
- Actual weight pricing and variance reporting.

**Total Duration**: 6 months (can run in parallel with WMS implementation Phase 4).

### Service Industry Use Cases
Advanced Inventory Operations provide **critical capabilities for service industries** including field service, utilities, equipment maintenance, and repair operations. For detailed service industry integration patterns, see:
- **Architecture Documentation**: `docs/architecture/inventory/inventory-advanced-ops.md` (Service Industry Integration section)
- **Related ADR**: ADR-042 Field Service Operations

**Key Service Capabilities**:
- **Service Kit Management**: Pre-configured repair/maintenance kits with static/dynamic/virtual kit support
- **Field Inventory**: Service van/truck stock management with break bulk and repack operations
- **Utilities Operations**: Meter installation kits, catch weight cables/spools, emergency parts network
- **Warranty/RMA**: Defective parts returns with repack and value-added services
- **Repair & Refurbishment Inventory**: Loaner pool management, refurbished stock tracking, repair location movements
- **Consignment Inventory**: Customer site stock tracking with consumption billing

**Service Industry KPIs**:
- Kit availability for service orders: >= 95%
- First-time-fix rate (correct parts in kit): >= 85%
- Van stock accuracy: >= 99%
- Emergency parts response: < 15 minutes
- Warranty recovery rate: >= 80%
- Loaner inventory accuracy: >= 99%
- Refurbished stock valuation accuracy: >= 99.5%

## Compliance
- **SOX**: valuation controls and reconciliation trails.
- **IFRS/GAAP**: approved valuation methods and cost layering.
- **GDPR**: minimize PII in inventory and POS data.
- **Regulated industries**: lot/serial traceability and retention.

## Implementation Plan
- Phase 1: Core stock ledger, locations, movement types, and basic valuation.
- Phase 2: Multi-store support, POS integration, and reservation engine.
- Phase 3: Multi-UoM, multi-currency, and landed cost allocations.
- Phase 4: Lot/serial, expiry/FEFO, and omnichannel orchestration.
- Phase 5: Advanced analytics (turns, shrinkage, ATP accuracy).

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-013: External Integration Patterns (B2B / EDI)
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-021: Fixed Asset Accounting (loaner asset depreciation)
- ADR-023: Procurement
- ADR-025: Sales & Distribution
- ADR-027: Master Data Governance (GTIN validation, kit BOM approval)
- ADR-028: Controlling / Management Accounting (repair cost allocation)
- ADR-038: Warehouse Execution System (WES/WMS)
- ADR-039: Quality Management (QA certification for refurbished stock)
- ADR-040: Plant Maintenance (repair work orders, loaner programs)
- ADR-042: Field Service Operations (service kits, field inventory, utilities)

### Internal Documentation
- `docs/inventory/inventory_requirements.md`
- `docs/architecture/inventory/inventory-advanced-ops.md` (Service Industry Integration)

### External References
- SAP MM-IM inventory management processes
- IFRS guidance on inventory valuation
