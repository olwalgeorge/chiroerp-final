# Manufacturing (PP) Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-037, ADR-039

## First-Level Modules
- `manufacturing-shared/` — ADR-006 COMPLIANT: Identifiers only
- `manufacturing-mrp/` — MRP Planning (Port 9351)
- `manufacturing-production/` — Production Orders (Port 9352)
- `manufacturing-shopfloor/` — Shop Floor Execution (Port 9353)
- `manufacturing-bom/` — BOM Management (Port 9354)
- `manufacturing-costing/` — Product Costing (Port 9355)
- `manufacturing-capacity/` — Capacity Planning (Port 9356)
- `manufacturing-subcontracting/` — Subcontracting (Port 9357)
- `manufacturing-analytics/` — Manufacturing Analytics (Port 9358)
- `manufacturing-process/` — Process Manufacturing Extension (Port 9359)
- `manufacturing-quality/` — Quality Management - Incorporated (ADR-039)

## Embedded Quality Suite
Quality Management is embedded under Manufacturing as `manufacturing-quality` (ports 9501-9507). See `COMPLETE_STRUCTURE.txt` for details.
