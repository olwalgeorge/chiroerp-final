# ADR-038: Warehouse Execution System (WES/WMS)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-01
**Deciders**: Architecture Team, Operations Team
**Priority**: P2 (Medium)
**Tier**: Add-on
**Tags**: warehouse, wms, wes, execution, logistics

## Context
ERP-grade inventory (MM-IM) provides accounting control and stock visibility, but best-in-class warehouse operations require execution capabilities: wave planning, task interleaving, directed putaway, slotting, labor management, and automation integration. This ADR defines the WES/WMS execution layer and its boundary with ADR-024.

## Decision
Implement a **Warehouse Execution System (WES/WMS)** capability for operational execution while keeping inventory valuation and stock ledger ownership in ADR-024.

### Scope
- Wave and task management.
- Directed putaway and pick path optimization.
- Bin/zone hierarchy and capacity rules.
- Labor management and productivity tracking.
- Automation and IoT integration readiness.
- Returns disposition workflows.

### Core Capabilities
- **Bin/zone hierarchy**: zone -> aisle -> rack -> level -> bin with capacity constraints.
- **Directed putaway**: rules based on item class, velocity, temperature, hazard.
- **Wave planning**: batch picking with priority and cut-off times.
- **Task interleaving**: combine putaway/pick/replenishment for efficiency.
- **Pick optimization**: path optimization, cluster picking, multi-order picking.
- **Replenishment**: min/max, demand-driven triggers, forward pick replenishment.
- **Labor management**: engineered standards, productivity KPIs.
- **Returns disposition**: restock, refurbish, quarantine, scrap, liquidation routing.
- **Automation hooks**: RFID, AMR/AGV, conveyor/sorter integration.

### Data Model (Conceptual)
- `WarehouseZone`, `Bin`, `PutawayRule`, `PickWave`, `Task`, `TaskBatch`, `ReplenishmentRule`, `LaborStandard`, `ReturnDisposition`, `AutomationEvent`.

### Key Workflows
- **Inbound**: receive -> inspection -> directed putaway.
- **Outbound**: wave -> pick -> pack -> ship confirmation.
- **Replenishment**: trigger -> task -> confirm.
- **Returns**: RMA receipt -> grade -> disposition.

### Integration Points
- **Inventory (ADR-024)**: stock ledger ownership and valuation.
- **Procurement (ADR-023)**: inbound receipts and ASN handling.
- **Sales (ADR-025)**: outbound fulfillment, ATP reservations.
- **Manufacturing (ADR-037)**: component staging and finished goods receipt.
- **Analytics (ADR-016)**: operational KPIs and labor dashboards.

### Non-Functional Constraints
- **Execution latency**: task confirmations reflected in MM-IM within 60 seconds.
- **Availability**: WMS uptime 99.9% during warehouse operating hours.
- **Traceability**: full audit trail for all task executions.

### KPIs and SLOs
- **Pick accuracy**: >= 99.9%.
- **Order cycle time**: p95 < 4 hours for standard orders.
- **Putaway cycle time**: p95 < 30 minutes from receipt.
- **Dock-to-stock**: < 24 hours.
- **Perfect order rate**: >= 98%.
- **Labor productivity variance**: < 5% from engineered standard.

## Alternatives Considered
- **Use ERP-only inventory**: rejected (no execution optimization).
- **External WMS only**: rejected (integration and reconciliation risk).
- **Build full EWM replacement**: rejected (scope too large for initial phases).

## Consequences
### Positive
- Best-in-class warehouse execution without overloading ERP inventory.
- Higher throughput and accuracy in warehouse operations.
- Clear separation of accounting vs execution responsibilities.

### Negative
- Additional integration and monitoring complexity.
- Requires operational change management for warehouse teams.

### Neutral
- Advanced automation features can be phased based on warehouse maturity.

## Compliance
- **SOX**: controlled movements and reconciliation with MM-IM.
- **Regulated industries**: traceability and audit logs for lot/serial.
- **Safety**: hazardous material handling and zone segregation rules.

## Implementation Plan
- Phase 1: Bin/zone model, task engine, directed putaway.
- Phase 2: Wave planning, pick optimization, replenishment rules.
- Phase 3: Labor management and productivity KPIs.
- Phase 4: Automation/IoT integrations and advanced returns disposition.

## References
### Related ADRs
- ADR-024: Inventory Management (MM-IM)
- ADR-023: Procurement (MM-PUR)
- ADR-025: Sales & Distribution (SD)
- ADR-037: Manufacturing & Production (PP)
- ADR-016: Analytics & Reporting Architecture

### Internal Documentation
- `docs/inventory/wms_requirements.md`

### External References
- SAP EWM / Warehouse Management
- Manhattan Associates WMS
- Blue Yonder WMS
