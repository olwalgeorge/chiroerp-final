# ChiroERP Architecture Overview

This document provides a master index, integration map, and standards for the ChiroERP architecture documentation set.

**ADR Coverage**: 57+ ADRs (ADR-001 through ADR-057)

## Domain Index

| Domain | Subdomains | Port Range | Docs | Tier | Status |
|--------|------------|------------|------|------|--------|
| Platform Shared | 7 | N/A | [platform-shared/README.md](./platform-shared/README.md) | Core | Complete |
| Tenancy & Identity | 3 | TBD | [tenancy-identity/README.md](./tenancy-identity/README.md) | Core | Complete |
| API Gateway | 1 | TBD | [api-gateway/README.md](./api-gateway/README.md) | Core | Complete |
| Finance (FI) | 6 | TBD (not specified in `COMPLETE_STRUCTURE.txt`) | [finance/README.md](./finance/README.md) | Core | Complete |
| Master Data Governance (MDG) | 6 | 9701-9705 | [mdm/README.md](./mdm/README.md) | Advanced | Complete |
| Inventory (MM-IM) | 8 | 9001-9009 | [inventory/README.md](./inventory/README.md) | Core | Complete + Advanced Ops |
| Analytics & Reporting | 7 | 9801-9806 | [analytics/README.md](./analytics/README.md) | Advanced | Complete |
| Commerce (SD / Omnichannel) | 6 | 9301-9305 | [commerce/README.md](./commerce/README.md) | Core | Complete |
| Human Capital Management (HCM) | 7 | 9101, 9901-9907 | [hr/README.md](./hr/README.md) | Advanced | New: T&E + Contingent + WFM |
| Procurement (MM-PUR) | 6 | 9201-9205 | [procurement/README.md](./procurement/README.md) | Core | Complete |
| Plant Maintenance (PM) | 12 | 9401-9411 | [maintenance/README.md](./maintenance/README.md) | Add-on | **Enhanced: Physical ALM** |
| Field Service (FSM) | 5 | 9601-9604 | [field-service/README.md](./field-service/README.md) | Add-on | Complete |
| Fleet Management | 9 | 9761-9768 | [fleet/README.md](./fleet/README.md) | Add-on | New |
| Manufacturing (PP) | 11 | 9351-9359 | [manufacturing/README.md](./manufacturing/README.md) | Add-on | Complete (includes Quality 9501-9507) |
| CRM | 6 | 9451-9455 | [crm/README.md](./crm/README.md) | Add-on | Complete |

**Total: 15 top-level architecture groups (see `COMPLETE_STRUCTURE.txt` for full module list)**

### New Domains (2026-02-03)
- **Human Capital Management (HCM)**: Core HR (9101) + Travel & Expense, Contingent Workforce, Workforce Scheduling, Analytics (9901-9907)
- **Fleet Management**: Vehicle lifecycle, telematics, driver management, fuel, compliance (9761-9768)

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
|   Commerce (SD)  --->  Inventory  --->  Finance (AR/GL)                        |
|        ^                                                             ^         |
|        |                                                             |         |
|   CRM  ---------------------------------------------------------------+         |
|   Field Service  -----------------------------------------------------+         |
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
- Commerce -> Inventory: `SalesOrderAllocatedEvent`, `ShipmentConfirmedEvent`, `ReturnReceivedEvent`
- Inventory -> Commerce: `ReservationCreatedEvent`
- Commerce -> Finance/AR: `SalesOrderFulfilledEvent`, `CreditMemoRequestedEvent`
- Commerce -> Finance/AP: `CommissionPayableCreatedEvent`
- Commerce -> Finance/AR: `RebateIssuedEvent`

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
- Field Service Orders -> Finance/AR: `ServiceOrderBilledEvent`
- Field Service Parts Consumption -> Inventory: `PartsConsumedEvent`, `PartsReturnedEvent`
- CRM Pipeline -> Commerce: `OpportunityClosedWonEvent`

## Standards and Conventions

### Ports
- Domain ranges are reserved by domain (Inventory 9001-9009, HR Core 9101, Procurement 9201-9205, Commerce 9301-9305, Manufacturing 9351-9359, Maintenance 9401-9411, CRM 9451-9455, Quality 9501-9507, Field Service 9601-9604, MDM 9701-9705, Fleet 9761-9768, Analytics 9801-9806, HR Advanced 9901-9907). Finance ports are TBD in `COMPLETE_STRUCTURE.txt`.
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
| Commerce (SD) | ADR-025, ADR-022 |
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

Domain documentation is arranged to mirror `COMPLETE_STRUCTURE.txt`:
- `docs/architecture/<domain>/README.md` per top-level domain
- `COMPLETE_STRUCTURE.txt` for full structure and ports
- ADRs in `docs/adr/` for decisions and scope

Use this master README as the entry point, then jump into the domain folder or ADRs for details.
