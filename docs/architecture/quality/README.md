# Quality Management Domain Architecture

This directory contains hexagonal architecture specifications for Quality Management (QM) subdomains.

## Subdomain Index

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Inspection Planning** | ADR-039 | 9501 | Core | [quality-inspection-planning.md](./quality-inspection-planning.md) | Plans, characteristics, sampling, triggers |
| **Quality Execution** | ADR-039 | 9502 | Core | [quality-execution.md](./quality-execution.md) | Inspection lots, results, usage decisions |
| **Nonconformance** | ADR-039 | 9503 | Core | [quality-nonconformance.md](./quality-nonconformance.md) | Defects, dispositions, quality costs |
| **CAPA Management** | ADR-039 | 9504 | Core | [quality-capa.md](./quality-capa.md) | Root cause analysis, corrective actions |
| **Supplier Quality** | ADR-039 | 9505 | Core | [quality-supplier-quality.md](./quality-supplier-quality.md) | Vendor scorecards, ASL, PPM |
| **Quality Certificates** | ADR-039 | 9506 | Core | [quality-certificates.md](./quality-certificates.md) | CoA/CoC generation, regulatory submissions |
| **Quality Analytics** | ADR-039 | 9507 | Core | [quality-analytics.md](./quality-analytics.md) | SPC, yield, cost of quality |

## Documentation Structure

### Complex Subdomains (with subfolders)
- **Inspection Planning** (`/inspection-planning/`) - 5 detailed files (domain, application, infrastructure, api, events)
- **Quality Execution** (`/execution/`) - 5 detailed files
- **Nonconformance** (`/nonconformance/`) - 5 detailed files
- **CAPA Management** (`/capa/`) - 5 detailed files

### Simple Subdomains (inline documentation)
- **Supplier Quality** - Single file with scorecards and ASL workflows
- **Quality Certificates** - Single file with CoA/CoC generation
- **Quality Analytics** - Single file with KPI and SPC reporting

## Port Allocation

| Port | Service | Database | Status |
|------|---------|----------|--------|
| 9501 | quality-inspection-planning | chiroerp_quality_inspection_planning | Implemented |
| 9502 | quality-execution | chiroerp_quality_execution | Implemented |
| 9503 | quality-nonconformance | chiroerp_quality_nonconformance | Implemented |
| 9504 | quality-capa | chiroerp_quality_capa | Implemented |
| 9505 | quality-supplier-quality | chiroerp_quality_execution (shared) | Implemented |
| 9506 | quality-certificates | chiroerp_quality_execution (shared) | Implemented |
| 9507 | quality-analytics | chiroerp_quality_execution (shared) | Implemented |

**Note:** Inline modules (supplier quality, certificates, analytics) share the `chiroerp_quality_execution` database and run as part of the Quality Execution service. Port numbers are logical identifiers for documentation purposes.

## Integration Map

```
+--------------------------------------------------------------------------------+
|                              QUALITY MANAGEMENT                                 |
|--------------------------------------------------------------------------------|
|                                                                                |
|  Procurement -> Incoming GR -> Inspection Lots -> Usage Decision -> Inventory  |
|                         ^                 |                  |                 |
|                         |                 v                  v                 |
|  Manufacturing ---------+           Nonconformance --------> CAPA              |
|                         |                 |                  |                 |
|                         v                 v                  v                 |
|  Sales Returns --------> Inspection Lots -> Quality Costs -> Finance/CO         |
|                                                                                |
+--------------------------------------------------------------------------------+
```

## Key Integration Points

### Upstream Dependencies (Consume Events)
- **Procurement** -> `GoodsReceivedEvent` -> Incoming inspection lots
- **Manufacturing** -> `ProductionOrderReleasedEvent` -> In-process inspection
- **Sales** -> `ReturnReceivedEvent` -> Customer return inspection

### Downstream Consumers (Publish Events)
- **Inventory** <- `StockBlockedEvent`, `StockReleasedEvent`
- **Procurement** <- `VendorQualityScoreUpdatedEvent`, `ASLStatusChangedEvent`
- **Finance/CO** <- `QualityCostPostedEvent`
- **Analytics** <- `QualityKPICalculatedEvent`

## References

### Related ADRs
- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)

### Related Domains
- [Manufacturing Architecture](../manufacturing/README.md)
- [Inventory Architecture](../inventory/README.md)
- [Procurement Architecture](../procurement/README.md)
- [Sales Architecture](../sales/README.md)
- [Finance Architecture](../finance/README.md)

---

## Phase 1 Status: Complete

**7 modules implemented:**
- Inspection Planning
- Quality Execution
- Nonconformance
- CAPA Management
- Supplier Quality
- Quality Certificates
- Quality Analytics

**Total documentation:** 28 files (4 overview + 20 detailed subfolder files + 3 inline + 1 README)
