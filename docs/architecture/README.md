# ChiroERP Architecture Overview

This document provides a master index, integration map, and standards for the ChiroERP architecture documentation set.

**ADR Coverage**: 57+ ADRs (ADR-001 through ADR-057)

## Domain Index

| Domain | Modules | Port Range | README | Tier | Status |
|--------|---------|------------|--------|------|--------|
| Finance (FI/CO) | 12 | 8081-8093 | [finance/README.md](./finance/README.md) | Core | Complete |
| Inventory (MM-IM) | 8 | 9001-9008 | [inventory/README.md](./inventory/README.md) | Core | Complete + Advanced Ops |
| Procurement (MM-PUR) | 5 | 9101-9105 | [procurement/README.md](./procurement/README.md) | Core | Complete |
| Sales & Distribution (SD) | 7 | 9201-9207 | [sales/README.md](./sales/README.md) | Core | Complete |
| Manufacturing (PP) | 8 | 9301-9308 | [manufacturing/README.md](./manufacturing/README.md) | Add-on | Complete |
| CRM & Field Service | 8 | 9401-9408 | [crm/README.md](./crm/README.md) | Add-on | Complete |
| Quality Management (QM) | 7 | 9501-9507 | [quality/README.md](./quality/README.md) | Add-on | Complete |
| Plant Maintenance (PM) | 11 | 9601-9611 | [maintenance/README.md](./maintenance/README.md) | Add-on | **Enhanced: Physical ALM** |
| Master Data Governance (MDG) | 5 | 9701-9705 | [mdm/README.md](./mdm/README.md) | Advanced | Complete |
| Analytics & Reporting | 6 | 9801-9806 | [analytics/README.md](./analytics/README.md) | Advanced | Complete |
| **Human Capital Management (HCM)** | **6** | **9901-9906** | **[hr/README.md](./hr/README.md)** | **Advanced** | **New: T&E + Contingent + WFM** |
| **Fleet Management** | **5** | **10001-10005** | **[fleet/README.md](./fleet/README.md)** | **Add-on** | **New** |

**Total: 92 modules across 12 domains**

### New Domains (2026-02-03)
- **Human Capital Management (HCM)**: Travel & Expense (ADR-054), Contingent Workforce (ADR-052), Workforce Scheduling (ADR-055)
- **Fleet Management**: Vehicle lifecycle, telematics, driver management, fuel, compliance (ADR-053)

### Enhanced Domains (2026-02-03)
- **Plant Maintenance (PM)**: Added Physical Asset Lifecycle Management (commissioning, decommissioning, health scoring, EOL planning) - ADR-040 enhanced

## Integration Map (High-Level)

```
+--------------------------------------------------------------------------------+
|                               CHIROERP INTEGRATION                              |
+--------------------------------------------------------------------------------+
|                                                                                |
|   Procurement  --->  Inventory  --->  Finance (AP/GL)                          |
|        |                 ^              ^                                      |
|        |                 |              |                                      |
|        v                 |              |                                      |
|   Manufacturing  --------+--------------+                                      |
|        ^                 |              |                                      |
|        |                 |              |                                      |
|   Quality Management ----+--------------+                                      |
|        ^                                |                                      |
|        |                                |                                      |
|   Plant Maintenance ---->| Inventory ---> Finance (Assets/CO)                 |
|        ^                                                             ^         |
|        |                                                             |         |
|   Sales & Distribution  --->  Inventory  --->  Finance (AR/GL)                 |
|        ^                                                             ^         |
|        |                                                             |         |
|   CRM & Field Service  -----------------------------------------------+         |
|   Master Data Governance  --->  All Domains (Golden Records)          |
|   Analytics & Reporting  <---  All Domains (Events/CDC)               |
|                                                                                |
+--------------------------------------------------------------------------------+
```

## Cross-Domain Event Catalog (Non-Exhaustive)

These are representative events used across domains. See each domain README for full lists.

### Procure-to-Pay
- Procurement -> Inventory: `GoodsReceivedEvent`, `PurchaseOrderApprovedEvent`
- Procurement -> Finance/AP: `InvoiceMatchCompletedEvent`, `PurchaseOrderApprovedEvent`

### Order-to-Cash
- Sales -> Inventory: `SalesOrderAllocatedEvent`, `ShipmentConfirmedEvent`, `ReturnReceivedEvent`
- Inventory -> Sales: `ReservationCreatedEvent`
- Sales -> Finance/AR: `SalesOrderFulfilledEvent`, `CreditMemoRequestedEvent`
- Sales -> Finance/AP: `CommissionPayableCreatedEvent`
- Sales -> Finance/AR: `RebateIssuedEvent`

### Plan-to-Produce
- Manufacturing -> Inventory: `MaterialIssuedEvent`, `ProductionReceiptPostedEvent`
- Inventory -> Manufacturing: `StockAdjustedEvent`
- Manufacturing -> Finance/GL: `WIPPostedEvent`, `ProductionVariancePostedEvent`

### Quality-to-Inventory
- Quality -> Inventory: `StockBlockedEvent`, `StockReleasedEvent`
- Quality -> Finance/CO: `QualityCostPostedEvent`
- Quality -> Procurement: `VendorQualityScoreUpdatedEvent`, `ASLStatusChangedEvent`

### Maintenance-to-Cost
- Maintenance -> Inventory: `SparePartReservedEvent`, `SparePartIssuedEvent`
- Maintenance -> Finance/CO: `MaintenanceCostPostedEvent`
- Maintenance -> Manufacturing: `DowntimeRecordedEvent`

### Master Data Distribution
- MDM -> All Domains: `MasterRecordPublishedEvent`
- MDM -> Stewardship: `ChangeRequestApprovedEvent`
- MDM -> Analytics: `DataQualityScoreUpdatedEvent`

### Analytics Feeds
- Analytics <- All Domains: `DomainEventIngested`, `WarehouseLoadCompletedEvent`
- Analytics -> Dashboards: `KpiCalculatedEvent`, `CubeRefreshedEvent`

### Service-to-Cash
- CRM Service Orders -> Finance/AR: `ServiceOrderBilledEvent`
- CRM Parts Consumption -> Inventory: `PartsConsumedEvent`, `PartsReturnedEvent`
- CRM Pipeline -> Sales: `OpportunityClosedWonEvent`

## Standards and Conventions

### Ports
- Domain ranges are reserved by domain (Finance 8081-8093, Inventory 9001-9007, Procurement 9101-9105, Sales 9201-9207, Manufacturing 9301-9308, CRM 9401-9408).
- Inline modules use logical ports and share the parent service database.

### Database Naming
- Pattern: `chiroerp_{domain}_{subdomain}` for complex subdomains.
- Inline modules share the parent service database (documented per domain README).

### Event Naming
- Events end with `Event` (example: `SalesOrderFulfilledEvent`).
- Event payloads are documented with JSON examples in each subdomain events doc.

### Kafka Topic Naming
- Pattern: `{domain}.{subdomain}.{entity}.{action}` (example: `sales.core.order.fulfilled`).

### API Base URL and Error Format
- Base URL: `https://api.chiroerp.com`
- Error responses follow the standard format defined in ADR-010.

## ADR Coverage Matrix

### Platform & Standards
- **Core Architecture**: ADR-001, ADR-002, ADR-003, ADR-004, ADR-005, ADR-006, ADR-007, ADR-008
- **API/Validation/Testing**: ADR-010, ADR-017, ADR-018, ADR-019, ADR-020
- **Integration Patterns**: ADR-011, ADR-013
- **Customization & Extensibility**: ADR-012, ADR-044, ADR-049
- **Org/Workflow/Localization/UI**: ADR-045, ADR-046, ADR-047, ADR-048

### Core & Advanced Domains
| Domain | Primary ADRs |
|--------|--------------|
| Finance (FI/CO) | ADR-009, ADR-021, ADR-022, ADR-026, ADR-028, ADR-029, ADR-031, ADR-032, ADR-033 |
| Inventory (MM-IM) | ADR-024, ADR-038 |
| Procurement (MM-PUR) | ADR-023 |
| Sales & Distribution (SD) | ADR-025, ADR-022 |
| Manufacturing (PP) | ADR-037 |
| Quality Management (QM) | ADR-039 |
| Plant Maintenance (PM) | ADR-040 |
| CRM & Field Service | ADR-042, ADR-043 |
| Master Data Governance (MDG) | ADR-027 |
| Analytics & Reporting | ADR-016 |
| HR Integration & Payroll | ADR-034 |

### Industry / Add-on Domains
- **Marketplace**: ADR-041
- **ESG & Sustainability**: ADR-035
- **Project Accounting (PS)**: ADR-036
- **Public Sector**: ADR-050
- **Insurance**: ADR-051
- **Contingent Workforce / VMS**: ADR-052
- **Fleet Management**: ADR-053
- **Travel & Expense (T&E)**: ADR-054
- **Workforce Scheduling (WFM)**: ADR-055

**Full ADR Index**: See `docs/adr/README.md` for the complete ADR list and statuses.

## Documentation Structure

Each domain README provides:
- Subdomain index with ports
- Documentation structure (subfolders vs inline)
- Integration map and key events
- ADR references and related domains

Use this master README as the entry point, then navigate into domain-specific docs for details.
