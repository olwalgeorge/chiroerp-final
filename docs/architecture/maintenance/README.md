# Plant Maintenance (PM) Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-040

## First-Level Modules
- `maintenance-shared/` — Shared maintenance types
- `maintenance-equipment/` — Port 9401 - Equipment hierarchy, attributes, classifications
- `maintenance-work-orders/` — Port 9402 - Work order planning, execution, settlement
- `maintenance-preventive/` — Port 9403 - Maintenance plans, schedules, triggers
- `maintenance-breakdown/` — Port 9404 - Failure notifications, corrective actions
- `maintenance-scheduling/` — Port 9405 - Calendars, resource allocation
- `maintenance-spare-parts/` — Port 9406 - Parts catalog, reservations
- `maintenance-analytics/` — Port 9407 - MTBF, MTTR, availability KPIs
- `maintenance-commissioning/` — Port 9408 - Asset commissioning (Advanced ALM)
- `maintenance-decommissioning/` — Port 9409 - Asset decommissioning (Advanced ALM)
- `maintenance-health-scoring/` — Port 9410 - Asset health scoring (Advanced ALM)
- `maintenance-eol-planning/` — Port 9411 - End-of-life planning (Advanced ALM)
