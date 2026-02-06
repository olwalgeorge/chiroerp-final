# ADR-037: Manufacturing & Production (PP)

**Status**: Accepted (Planned - Blueprint Defined)
**Date**: 2026-02-01
**Updated**: 2026-02-06 - Clarified process manufacturing extension scope and implementation status
**Deciders**: Architecture Team, Operations Team
**Priority**: P2 (Medium)
**Tier**: Add-on
**Tags**: manufacturing, production, mrp, bom, routing

## Context
Manufacturing requires coordinated planning, execution, and costing. This ADR defines Production Planning (PP) capabilities to support BOMs, routings, MRP, work centers, production orders, WIP, and product costing integration for make-to-stock and make-to-order operations.

## Decision
Implement a **Manufacturing & Production (PP)** capability that covers BOM/routing, MRP, production order execution, and WIP tracking integrated with inventory, procurement, sales, and controlling.

### Bounded Context Architecture

```
manufacturing/                          # Manufacturing Bounded Context (ADR-037)
├── manufacturing-shared/               # Shared identifiers (ADR-006 compliant)
├── manufacturing-mrp/                  # MRP Planning (Port 9351)
├── manufacturing-production/           # Production Orders (Port 9352)
├── manufacturing-shopfloor/            # Shop Floor Execution (Port 9353)
├── manufacturing-bom/                  # BOM Management (Port 9354)
├── manufacturing-costing/              # Product Costing (Port 9355)
├── manufacturing-capacity/             # Capacity Planning (Port 9356)
├── manufacturing-subcontracting/       # Subcontracting (Port 9357)
└── manufacturing-analytics/            # Manufacturing Analytics (Port 9358)
```

**Package Structure**: `com.chiroerp.manufacturing.*`
**Port Range**: 9351-9359 (avoids conflict with Commerce 9301-9309)

### Scope
- Bill of Materials (BOM) and routing management.
- Material Requirements Planning (MRP).
- Work centers and capacity planning.
- Production orders and shop floor execution.
- WIP, scrap, and yield tracking.
- Make-to-stock (MTS) and make-to-order (MTO).

### Core Capabilities
- **BOM management**: multi-level BOMs, alternates, effectivity dates.
- **Routing**: operations, work centers, setup/run times.
- **MRP**: net requirements, planned orders, and purchase requisitions.
- **Production orders**: release, issue components, confirm operations, receipt.
- **WIP tracking**: cost accumulation and variance analysis.
- **Backflushing**: automatic component consumption.
- **Subcontracting**: external operations and component provisioning.
- **Quality integration**: inspection lots and nonconformance handling.

### Data Model (Conceptual)
- `BOM`, `BOMItem`, `Routing`, `Operation`, `WorkCenter`, `ProductionOrder`, `MRPPlan`, `PlannedOrder`, `WIPBalance`, `ScrapRecord`.

### Key Workflows
- **MRP run**: demand -> net requirements -> planned orders.
- **Order execution**: release -> issue -> confirm -> goods receipt.
- **MTO**: sales order -> planned order -> production order -> delivery.
- **Subcontracting**: send components -> receive finished goods.
- **Close**: settle WIP -> post variances -> update CO.

### Integration Points
- **Inventory (MM-IM)**: component issues and finished goods receipts.
- **Procurement (MM-PUR)**: planned orders to purchase requisitions.
- **Controlling (CO)**: product costing and variance analysis.
- **Sales (SD)**: MTO demand and delivery schedules.
- **Fixed Assets (FI-AA)**: production equipment depreciation.
- **Analytics**: production KPIs, yield, and throughput.

### Non-Functional Constraints
- **Accuracy**: production postings reconcile to inventory and CO.
- **Performance**: MRP run p95 < 2 hours for 100k SKUs.
- **Traceability**: full lot/serial trace for regulated goods.

### KPIs and SLOs
- **MRP completion**: p95 < 2 hours for 100k SKUs.
- **Production order cycle time**: p95 within planned routing time + 10%.
- **WIP variance**: < 0.5% at period close.
- **Yield accuracy**: >= 98% for standard processes.
- **On-time production completion**: >= 95%.

## Alternatives Considered
- **External MES only**: rejected (loss of ERP cost control).
- **Manual production tracking**: rejected (no traceability).
- **Inventory-only approach**: rejected (no planning or WIP).

## Consequences
### Positive
- End-to-end manufacturing planning and execution.
- Accurate product costing and variance reporting.
- Improved inventory visibility and supply chain alignment.

### Negative
- Requires disciplined master data (BOMs/routings).
- Implementation complexity for shop floor integration.

### Neutral
- Advanced MES integration can be phased.

---

## Process Manufacturing Extension (Chemical, Food, Pharma Industries)

### Context
Process manufacturing industries (chemicals, pharmaceuticals, food & beverage, oil refining) require formula-based production, batch genealogy, co-products/by-products handling, and continuous production scheduling—distinct from discrete manufacturing's BOM-based assembly.

### Scope Extension
- **Recipe Management**: Master recipes, formula versioning, ingredient scaling.
- **Process Orders**: Formula-based orders with phase management (mixing, heating, cooling, curing).
- **Batch Genealogy**: Forward and backward tracing (where-used, lot traceability).
- **Co-products & By-products**: Joint costing, valuation, yield allocation.
- **Process Instructions**: SOPs, critical control points (HACCP), in-process checks.
- **Yield Optimization**: Actual vs theoretical yield tracking, variance analysis.
- **Campaign Management**: Multi-batch campaigns for efficiency (pharma, chemicals).
- **Continuous Production**: Flow-based scheduling vs discrete orders.

### Additional Capabilities
- **Master Recipes**:
  - Formula composition (ingredients, quantities, tolerances).
  - Process phases (sequence, duration, temperature, pressure).
  - Scaling factors for different batch sizes.
  - Version control and approval workflows.

- **Process Orders**:
  - Formula-driven material consumption (not BOM-based).
  - Phase execution tracking (start, complete, hold, skip).
  - In-process quality checks and holds.
  - Dynamic yield calculations.

- **Batch Genealogy**:
  - Lot-to-lot traceability (forward: which finished goods contain input lot X).
  - Backward tracing (which raw material lots went into finished lot Y).
  - Genealogy trees for recall management.

- **Co-products & By-products**:
  - Joint production from single process order.
  - Cost allocation methods (sales value, physical units, NRV).
  - By-product revenue or disposal cost tracking.

- **Process Control**:
  - SOP integration (work instructions, critical parameters).
  - HACCP critical control points.
  - Environmental controls (temperature, pH, viscosity).

- **Campaign Management**:
  - Multi-batch production runs for equipment efficiency.
  - Cleaning validation between campaigns.
  - Campaign costing and variance.

### Data Model Extensions
- `MasterRecipe`: product, version, formula items, process phases, scaling rules.
- `FormulaItem`: ingredient, quantity, tolerance range, phase assignment.
- `ProcessOrder`: recipe reference, batch size, actual yields (co-products, by-products).
- `ProcessPhase`: sequence, operation type, duration, control parameters, status.
- `BatchGenealogy`: batch number, parent lots, child lots, traceability links.
- `CoProductOutput`: output material, quantity, valuation method.

### Integration Points
- **Inventory/MM-IM**: Lot-tracked material consumption, co-product receipts.
- **Quality/QM**: In-process inspection, batch release, stability testing.
- **Controlling/CO**: Process costing, co-product valuation, yield variance.
- **Compliance**: FDA 21 CFR Part 11 (pharma), HACCP (food), GMP (manufacturing).

### KPIs / SLOs
- **Recipe versioning**: Approval cycle < 2 business days.
- **Batch genealogy**: Traceability query response < 5 seconds for 1M lots.
- **Yield accuracy**: Actual vs theoretical yield variance < 1.0%.
- **Co-product valuation**: Posting accuracy >= 99.5%.
- **Campaign efficiency**: Setup time reduction >= 20% vs single-batch runs.

## Implementation Status (Discrete vs Process Manufacturing)

- **Discrete manufacturing (current blueprint)**: The bounded context architecture above (BOM, routing, MRP, production orders, costing, capacity, subcontracting, analytics) is the planned baseline and is represented in `COMPLETE_STRUCTURE.txt`.
- **Process manufacturing (included in blueprint)**: The capabilities in the Process Manufacturing Extension are now modeled as the `manufacturing-process` subdomain (Port 9359) in `COMPLETE_STRUCTURE.txt`, including recipe management, process orders, batch genealogy, co-products/by-products, campaign management, and continuous production monitoring.

### Implementation Phasing
The **Process Manufacturing Extension** is **NOW included in the COMPLETE_STRUCTURE.txt blueprint** as of 2026-02-06. The `manufacturing-process` subdomain covers chemical/pharma/food/beverage industries with full hexagonal architecture.

**Current Scope (COMPLETE_STRUCTURE.txt - Full Manufacturing)**:
- ✅ BOM Management (manufacturing-bom)
- ✅ MRP Planning (manufacturing-mrp)
- ✅ Production Orders (manufacturing-production)
- ✅ Shop Floor Execution (manufacturing-shopfloor)
- ✅ Product Costing (manufacturing-costing)
- ✅ Capacity Planning (manufacturing-capacity)
- ✅ Subcontracting (manufacturing-subcontracting)
- ✅ Manufacturing Analytics (manufacturing-analytics)
- ✅ Quality Management (manufacturing-quality with 7 sub-subdomains)
- ✅ **Process Manufacturing (manufacturing-process)** - Port 9359
  - Recipe/Formula Management
  - Process Order Execution
  - Batch Genealogy & Traceability
  - Co-products & By-products Management
  - Campaign Management
  - Continuous Production Monitoring
  - SCADA/DCS Integration (OPC UA)

**Port Assignments**:
- Discrete Manufacturing: 9351-9358
- Process Manufacturing: 9359
- Quality Management: 9501-9507

---

## Compliance
- **SOX**: controlled postings for WIP and variances.
- **Regulatory traceability**: lot/serial tracking for regulated industries.
- **Quality compliance**: inspection and nonconformance workflows.

## Implementation Plan
- Phase 1: BOM, routing, and basic production orders.
- Phase 2: MRP and capacity planning.
- Phase 3: WIP tracking, variances, and CO integration.
- Phase 4: Subcontracting, quality integration, and analytics.

## References
### Related ADRs
- ADR-024: Inventory Management (MM-IM)
- ADR-023: Procurement (MM-PUR)
- ADR-028: Controlling / Management Accounting (CO)
- ADR-025: Sales & Distribution (SD)
- ADR-016: Analytics & Reporting Architecture

### Internal Documentation
- `docs/operations/production_requirements.md`

### External References
- SAP PP (Production Planning) module overview
