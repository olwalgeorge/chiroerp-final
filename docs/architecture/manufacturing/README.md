# Manufacturing Domain Architecture

This directory contains hexagonal architecture specifications for Manufacturing subdomains.

## Subdomain Index

### Phase 1 Modules (Implemented)

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **MRP** | ADR-037 | 9301 | Core | [manufacturing-mrp.md](./manufacturing-mrp.md) | Demand planning, net requirements, planned orders |
| **Production Orders** | ADR-037 | 9302 | Core | [manufacturing-production.md](./manufacturing-production.md) | Release, issue, confirm, receipt, WIP tracking |
| **Shop Floor Execution** | ADR-037 | 9303 | Core | [manufacturing-shop-floor.md](./manufacturing-shop-floor.md) | Dispatching, work center execution, confirmations |
| **BOM Management** | ADR-037 | 9304 | Core | [manufacturing-bom.md](./manufacturing-bom.md) | Multi-level BOMs, alternates, effectivity |
| **Costing** | ADR-037 | 9305 | Core | [manufacturing-costing.md](./manufacturing-costing.md) | Product costing, variances, WIP settlement |
| **Capacity Planning** | ADR-037 | 9306 | Core | [manufacturing-capacity.md](./manufacturing-capacity.md) | Work center capacity, constraints, smoothing |
| **Subcontracting** | ADR-037 | 9307 | Core | [manufacturing-subcontracting.md](./manufacturing-subcontracting.md) | External operations, component provisioning |
| **Manufacturing Analytics** | ADR-037 | 9308 | Core | [manufacturing-analytics.md](./manufacturing-analytics.md) | OEE, yield, throughput, WIP variance |

## Documentation Structure

### Complex Subdomains (with subfolders)
- **MRP** (`/mrp/`) - 5 detailed files (domain, application, infrastructure, api, events)
- **Production Orders** (`/production/`) - 5 detailed files
- **Shop Floor Execution** (`/shopfloor/`) - 5 detailed files
- **BOM Management** (`/bom/`) - 5 detailed files
- **Costing** (`/costing/`) - 5 detailed files

### Simple Subdomains (inline documentation)
- **Capacity Planning** - Single file with capacity and constraint workflows
- **Subcontracting** - Single file with external operations and GR workflows
- **Manufacturing Analytics** - Single file with KPI definitions and data flows

## Port Allocation

| Port | Service | Database | Status |
|------|---------|----------|--------|
| 9301 | manufacturing-mrp | chiroerp_manufacturing_mrp | Implemented |
| 9302 | manufacturing-production | chiroerp_manufacturing_production | Implemented |
| 9303 | manufacturing-shopfloor | chiroerp_manufacturing_shopfloor | Implemented |
| 9304 | manufacturing-bom | chiroerp_manufacturing_bom | Implemented |
| 9305 | manufacturing-costing | chiroerp_manufacturing_costing | Implemented |
| 9306 | manufacturing-capacity | chiroerp_manufacturing_production (shared) | Implemented |
| 9307 | manufacturing-subcontracting | chiroerp_manufacturing_production (shared) | Implemented |
| 9308 | manufacturing-analytics | chiroerp_manufacturing_production (shared) | Implemented |

**Note:** Inline modules (capacity, subcontracting, analytics) share the `chiroerp_manufacturing_production` database and run as part of the Production service. Port numbers are logical identifiers for documentation purposes.

## Integration Map

```
+-----------------------------------------------------------------------------+
|                           MANUFACTURING DOMAIN                               |
|-----------------------------------------------------------------------------+
|                                                                             |
|   +--------------+    Planned Orders     +------------------+               |
|   | MRP          | --------------------> | Production Orders|               |
|   `--------------+                       `------------------+               |
|            |                                    |                            |
|            | BOM/Routing                        | Dispatch/Confirm           |
|            v                                    v                            |
|   +--------------+                       +------------------+               |
|   | BOM Mgmt     | <-------------------- | Shop Floor Exec  |               |
|   `--------------+       Master Data     `------------------+               |
|            |                                    |                            |
|            | Costing + WIP                      | Capacity                   |
|            v                                    v                            |
|   +--------------+                       +------------------+               |
|   | Costing      | <-------------------- | Capacity Planning|               |
|   `--------------+                         (inline)                           |
|                                                                             |
|   +--------------+   GR/Issue events   +----------+   PR/PO   +----------+  |
|   | Inventory    | <-----------------> | Mfg Core | <-------> | Procure  |  |
|   `--------------+                      `----------+           `----------+  |
|                                                                             |
`-----------------------------------------------------------------------------+
```

## Key Integration Points

### Upstream Dependencies (Consume Events)
- **Sales** -> `SalesOrderAllocatedEvent` -> MTO demand
- **Inventory** -> `StockAvailabilityChangedEvent` -> MRP netting
- **Procurement** -> `PurchaseOrderApprovedEvent` -> Subcontracting supply
- **Finance Close** -> `FinancialPeriodClosedEvent` -> Lock postings

### Downstream Consumers (Publish Events)
- **Inventory** <- `ProductionOrderReleasedEvent`, `MaterialIssuedEvent`, `ProductionReceiptPostedEvent`
- **Procurement** <- `PlannedOrderCreatedEvent` -> Convert to PR
- **Controlling** <- `WIPPostedEvent`, `ProductionVariancePostedEvent`
- **Analytics** <- `ProductionConfirmedEvent`, `ScrapRecordedEvent`

## References

### Related ADRs
- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md) - Core manufacturing decisions
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md) - Issue/receipt integration
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md) - PR/PO conversion
- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md) - Costing and variances
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md) - MTO demand
- [ADR-016: Analytics & Reporting Architecture](../../adr/ADR-016-analytics-reporting-architecture.md) - KPI reporting

### Related Domains
- [Inventory Architecture](../inventory/README.md) - Material issue and receipt
- [Procurement Architecture](../procurement/README.md) - PR/PO flow
- [Finance Architecture](../finance/README.md) - CO and WIP posting

---

## Phase 1 Status: Complete

**8 modules implemented:**
- MRP
- Production Orders
- Shop Floor Execution
- BOM Management
- Costing
- Capacity Planning
- Subcontracting
- Manufacturing Analytics

**Total documentation:** 34 files (5 overview + 25 detailed subfolder files + 3 inline + 1 README)

**Next steps:**
- Phase 2: Quality Management (ADR-039) - Complete
- Phase 3: Plant Maintenance (ADR-040) - Complete
