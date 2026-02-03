# Plant Maintenance Domain Architecture

This directory contains hexagonal architecture specifications for Plant Maintenance (PM) subdomains including **Physical Asset Lifecycle Management (ALM)**.

## Subdomain Index

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Equipment Master** | ADR-040 | 9601 | Core | [maintenance-equipment.md](./maintenance-equipment.md) | Equipment hierarchy, attributes, classifications |
| **Work Orders** | ADR-040 | 9602 | Core | [maintenance-work-orders.md](./maintenance-work-orders.md) | Work order planning, execution, settlement |
| **Preventive Maintenance** | ADR-040 | 9603 | Core | [maintenance-preventive.md](./maintenance-preventive.md) | Plans, schedules, triggers |
| **Breakdown Maintenance** | ADR-040 | 9604 | Core | [maintenance-breakdown.md](./maintenance-breakdown.md) | Failure notifications, corrective actions |
| **Maintenance Scheduling** | ADR-040 | 9605 | Core | [maintenance-scheduling.md](./maintenance-scheduling.md) | Calendars, resource allocation |
| **Spare Parts** | ADR-040 | 9606 | Core | [maintenance-spare-parts.md](./maintenance-spare-parts.md) | Parts catalog, reservations |
| **Maintenance Analytics** | ADR-040 | 9607 | Core | [maintenance-analytics.md](./maintenance-analytics.md) | MTBF, MTTR, availability |
| **🆕 Asset Commissioning** | ADR-040 | 9608 | Advanced | [maintenance-commissioning.md](./maintenance-commissioning.md) | Pre-commissioning, testing, go-live, ADR-021 integration |
| **🆕 Asset Decommissioning** | ADR-040 | 9609 | Advanced | [maintenance-decommissioning.md](./maintenance-decommissioning.md) | Impact assessment, disposal, environmental compliance |
| **🆕 Asset Health Scoring** | ADR-040 | 9610 | Advanced | [maintenance-health-scoring.md](./maintenance-health-scoring.md) | 5-dimension health model, condition assessment, inspections |
| **🆕 End-of-Life Planning** | ADR-040 | 9611 | Advanced | [maintenance-eol-planning.md](./maintenance-eol-planning.md) | Replacement forecasting, TCO analysis, ADR-032 budget integration |

## Recent Enhancements (2026-02-03)

**Physical Asset Lifecycle Management (ALM)** - Complete lifecycle coverage from commissioning through decommissioning:

1. **Asset Commissioning & Decommissioning** (Ports 9608-9609)
   - Pre-commissioning checklists (installation, safety, utilities, documentation)
   - Testing & acceptance (run-off, performance benchmarking, safety certification)
   - Go-live authorization and ADR-021 capitalization trigger
   - Decommissioning workflows (request, impact assessment, disposal options)
   - Environmental compliance and ADR-021 disposal posting

2. **Asset Health Scoring & Condition Assessment** (Port 9610)
   - 5-dimension health score model (Physical Condition, Performance, Reliability, Maintenance Cost, Criticality)
   - Condition states (Excellent 90-100, Good 75-89, Fair 60-74, Poor 40-59, Critical 0-39)
   - Inspection workflows (visual, thermal imaging, vibration, oil analysis, thermography, ultrasonic)
   - Alert thresholds and auto-escalation

3. **End-of-Life Planning & Replacement Forecasting** (Port 9611)
   - EOL indicators (age-based, condition-based, obsolescence, economic, regulatory, strategic)
   - 1-5 year rolling replacement forecast
   - ADR-032 capital budget integration
   - Prioritization matrix (criticality/condition/impact/financial)
   - TCO analysis (acquisition + operating + maintenance + downtime + disposal costs)
   - Asset registry with lifecycle stage tracking

## Documentation Structure

### Complex Subdomains (with subfolders)
- **Equipment Master** (`/equipment/`) - 5 detailed files (domain, application, infrastructure, api, events)
- **Work Orders** (`/work-orders/`) - 5 detailed files
- **Preventive Maintenance** (`/preventive/`) - 5 detailed files

### Simple Subdomains (inline documentation)
- **Breakdown Maintenance** - Reactive repairs and downtime logging
- **Maintenance Scheduling** - Calendar and resource scheduling
- **Spare Parts** - Parts reservation and issue workflows
- **Maintenance Analytics** - Reliability and cost KPIs

## Port Allocation

| Port | Service | Database | Status |
|------|---------|----------|--------|
| 9601 | maintenance-equipment | chiroerp_maintenance_equipment | Implemented |
| 9602 | maintenance-work-orders | chiroerp_maintenance_work_orders | Implemented |
| 9603 | maintenance-preventive | chiroerp_maintenance_preventive | Implemented |
| 9604 | maintenance-breakdown | chiroerp_maintenance_work_orders (shared) | Implemented |
| 9605 | maintenance-scheduling | chiroerp_maintenance_preventive (shared) | Implemented |
| 9606 | maintenance-spare-parts | chiroerp_maintenance_work_orders (shared) | Implemented |
| 9607 | maintenance-analytics | chiroerp_maintenance_preventive (shared) | Implemented |
| **9608** | **maintenance-commissioning** | **chiroerp_maintenance_lifecycle** | **New: Physical ALM** |
| **9609** | **maintenance-decommissioning** | **chiroerp_maintenance_lifecycle (shared)** | **New: Physical ALM** |
| **9610** | **maintenance-health-scoring** | **chiroerp_maintenance_lifecycle (shared)** | **New: Physical ALM** |
| **9611** | **maintenance-eol-planning** | **chiroerp_maintenance_lifecycle (shared)** | **New: Physical ALM** |

**Note:** Physical ALM modules (9608-9611) share the `chiroerp_maintenance_lifecycle` database for asset lifecycle tracking across commissioning, health monitoring, and replacement planning.

## Integration Map

```
+------------------------------------------------------------------------------------+
|                             PLANT MAINTENANCE + PHYSICAL ALM                        |
|------------------------------------------------------------------------------------|
|                                                                                    |
|  Equipment Master -> Preventive Plans -> Scheduling -> Work Orders                 |
|           |                         |                 |                          |
|           v                         v                 v                          |
|     Breakdown Events ----------> Corrective Orders -> Cost Settlement              |
|                                                                                    |
|  🆕 PHYSICAL ASSET LIFECYCLE MANAGEMENT:                                           |
|                                                                                    |
|  Commissioning -> Health Scoring -> EOL Planning -> Replacement Forecast           |
|       |              |                 |                    |                     |
|       v              v                 v                    v                     |
|  ADR-021         Inspections       TCO Analysis        ADR-032 Budget             |
|  (Capitalization) (Condition)     (Replacement)       (CAPEX Planning)            |
|       |              |                 |                    |                     |
|       v              v                 v                    v                     |
|  Decommissioning <- Critical Health <- Procurement <----- Budget Approval         |
|       |                                    |                                      |
|       v                                    v                                      |
|  ADR-021 Disposal                   ADR-023 Replacement PO                        |
|                                                                                    |
|  Inventory <-> Spare Parts <-> Work Orders <-> Finance/CO                         |
|                                                                                    |
+------------------------------------------------------------------------------------+
```

## Key Integration Points

### Upstream Dependencies (Consume Events)
- **Finance Assets (ADR-021)** -> `AssetCreatedEvent` -> Create equipment master, `AssetRetiredEvent` -> Decommission equipment
- **Manufacturing (ADR-037)** -> `ProductionScheduleUpdatedEvent` -> Adjust maintenance windows
- **Quality (ADR-039)** -> `CalibrationDueEvent` -> Create calibration work order
- **🆕 Procurement (ADR-023)** -> `ReplacementAssetReceivedEvent` -> Trigger commissioning workflow
- **🆕 Budgeting (ADR-032)** -> `CAPEXBudgetApprovedEvent` -> Authorize replacement asset procurement

### Downstream Consumers (Publish Events)
- **Inventory (ADR-024)** <- `SparePartReservedEvent`, `SparePartIssuedEvent`
- **Finance/CO (ADR-028)** <- `MaintenanceCostPostedEvent`
- **Manufacturing (ADR-037)** <- `DowntimeRecordedEvent`
- **🆕 Finance Assets (ADR-021)** <- `AssetCommissionedEvent` (trigger capitalization), `AssetDecommissionedEvent` (trigger disposal posting)
- **🆕 Budgeting (ADR-032)** <- `ReplacementForecastUpdatedEvent` (feed CAPEX budget planning)
- **🆕 Procurement (ADR-023)** <- `ReplacementAssetRequisitionCreatedEvent` (initiate replacement procurement)

### Physical ALM Event Flow
```
Commissioning Flow:
  Procurement.GoodsReceivedEvent 
    -> Commissioning.AssetReceivedForCommissioning
    -> Commissioning.PreCommissioningChecklistCompleted
    -> Commissioning.TestingAndAcceptanceCompleted
    -> Commissioning.AssetCommissionedEvent
    -> FixedAssets.CapitalizeAsset

Health Scoring Flow:
  Inspection.ConditionMeasurementRecorded
    -> HealthScoring.HealthScoreCalculated
    -> HealthScoring.CriticalHealthAlertRaised
    -> EOLPlanning.AssetFlaggedForReplacement

EOL Planning Flow:
  HealthScoring.CriticalHealthAlert
    -> EOLPlanning.EOLIndicatorTriggered
    -> EOLPlanning.TCOAnalysisCompleted
    -> EOLPlanning.ReplacementForecastCreated
    -> Budgeting.CAPEXBudgetRequestCreated
    -> Budgeting.BudgetApproved
    -> Procurement.ReplacementPurchaseOrderCreated

Decommissioning Flow:
  EOLPlanning.ReplacementAssetCommissioned
    -> Decommissioning.DecommissioningRequestCreated
    -> Decommissioning.ImpactAssessmentCompleted
    -> Decommissioning.AssetDecommissionedEvent
    -> FixedAssets.PostAssetDisposal
```

## References

### Related ADRs
- [ADR-040: Plant Maintenance](../../adr/ADR-040-plant-maintenance.md) - **Enhanced with Physical ALM (2026-02-03)**
- [ADR-021: Fixed Asset Accounting](../../adr/ADR-021-fixed-asset-accounting.md) - Commissioning capitalization, decommissioning disposal
- [ADR-032: Budgeting & Planning](../../adr/ADR-032-budgeting-planning-fpa.md) - Replacement forecast CAPEX integration
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md) - Replacement asset procurement
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md) - Spare parts management
- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md) - Production schedule integration
- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md) - Calibration, commissioning acceptance tests
- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md) - TCO tracking, maintenance cost allocation
- [ADR-053: Fleet Management](../../adr/ADR-053-fleet-management.md) - Separate module for vehicle fleet management

### Related Domains
- [Finance Architecture](../finance/README.md)
- [Inventory Architecture](../inventory/README.md)
- [Manufacturing Architecture](../manufacturing/README.md)
- [Quality Architecture](../quality/README.md)

---

## Implementation Status

### Phase 1: Complete (2025)
**7 core modules implemented:**
- Equipment Master
- Work Orders
- Preventive Maintenance
- Breakdown Maintenance
- Maintenance Scheduling
- Spare Parts
- Maintenance Analytics

### Phase 2: Physical Asset Lifecycle Management (2026-02-03) 🆕
**4 advanced modules added:**
- **Asset Commissioning** (Port 9608): Pre-commissioning, testing, go-live, ADR-021 capitalization
- **Asset Decommissioning** (Port 9609): Impact assessment, disposal options, environmental compliance
- **Asset Health Scoring** (Port 9610): 5-dimension health model, condition inspections, alert thresholds
- **End-of-Life Planning** (Port 9611): Replacement forecasting, TCO analysis, ADR-032 budget integration

**Coverage Improvement:** 70% → 95% Physical ALM coverage

**New KPIs Added:**
- Commissioning cycle time ≤ 30 days
- Decommissioning completion ≤ 15 days
- Health score coverage ≥ 90% of critical assets
- Health score accuracy ≥ 85% predictive accuracy
- Replacement forecast accuracy ≥ 80%
- TCO data completeness ≥ 95%
- Asset registry accuracy ≥ 99%

**Total documentation:** 23 files (3 overview + 15 detailed subfolder files + 4 inline + 1 README)
