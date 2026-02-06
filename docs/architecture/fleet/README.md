# Fleet Management Architecture

**Source of Truth**: `COMPLETE_STRUCTURE.txt`
**Primary ADRs**: ADR-053

## First-Level Modules
- `fleet-shared/` — ADR-006 COMPLIANT: Identifiers only
- `fleet-vehicle-master/` — Vehicle Master Data (Port 9761)
- `fleet-driver-management/` — Driver Management (Port 9762)
- `fleet-telematics/` — Telematics Integration (Port 9763)
- `fleet-fuel-management/` — Fuel Card Management (Port 9764)
- `fleet-maintenance/` — Fleet Maintenance (Port 9765)
- `fleet-compliance/` — Compliance Management (Port 9766)
- `fleet-utilization/` — Utilization & TCO (Port 9767)
- `fleet-lifecycle/` — Vehicle Lifecycle (Port 9768)
