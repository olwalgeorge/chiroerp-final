# Field Service (FSM) Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-042

## First-Level Modules
- `fsm-shared/` — ADR-006 COMPLIANT: Identifiers and value objects only
- `fsm-service-orders/` — Service Order Management (Port 9601)
- `fsm-dispatch/` — Dispatch & Scheduling (Port 9602)
- `fsm-parts-consumption/` — Parts & Material Consumption (Port 9603)
- `fsm-repair-depot/` — Customer Repair/RMA Services (Port 9604)
