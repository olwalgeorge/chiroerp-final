# ADR-040: Plant Maintenance (PM)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-02  
**Deciders**: Architecture Team, Operations Team, Facilities Team  
**Priority**: P3 (Optional Add-on)  
**Tier**: Add-on  
**Tags**: maintenance, asset-maintenance, preventive, predictive, cmms, eam

## Context
Asset-intensive industries (manufacturing, utilities, facilities, fleet) require plant maintenance capabilities to manage equipment upkeep, prevent downtime, and optimize asset lifecycle. A general-purpose ERP must support maintenance workflows as an optional add-on for tenants with significant physical assets. This includes not only reactive and preventive maintenance, but also **complete asset lifecycle management** from commissioning through decommissioning, health monitoring, and proactive replacement planning.

## Decision
Implement a **Plant Maintenance (PM)** add-on module that provides equipment management, preventive/predictive maintenance, work order management, spare parts planning, maintenance cost tracking, **asset commissioning/decommissioning, health scoring, and end-of-life planning** integrated with fixed assets, inventory, procurement, controlling, and budgeting.

### Scope
- Equipment/asset master data and hierarchy.
- Maintenance planning (preventive, predictive, condition-based).
- Work order management and execution.
- Maintenance task lists and checklists.
- Spare parts planning and inventory.
- Maintenance cost tracking and budgeting.
- Downtime and availability tracking.
- Calibration management.
- **Asset commissioning and decommissioning workflows**.
- **Asset health scoring and condition assessment**.
- **End-of-life planning and replacement forecasting**.

### Out of Scope (Future/External)
- **IoT sensor platform** (integrate with Azure IoT Hub, AWS IoT Core, or customer's existing platform).
- **AI/ML predictive maintenance on sensor data** (customers bring IoT data, we integrate for visualization/alerting).
- Building Management Systems (BMS).
- Fleet management (separate module - see ADR-053).

> **Predictive Maintenance Strategy**: ChiroERP provides **deterministic health scoring** (5-dimension model) and **condition-based maintenance** workflows. True AI/ML predictive maintenance requires IoT sensor data (vibration, temperature, oil analysis) that most customers don't have. For customers with existing IoT platforms, ChiroERP will integrate sensor data into health scores and alerts (future enhancement), but we will **not build IoT infrastructure or train ML models from scratch**. This avoids overengineering for the 95% of customers who use preventive maintenance schedules and periodic inspections.

### Core Capabilities

#### Equipment Master
- **Equipment hierarchy**: Plant → Location → System → Equipment → Component.
- **Technical attributes**: Model, serial, specifications, drawings.
- **Classification**: Equipment class, criticality (A/B/C).
- **Location tracking**: Functional location, installation history.
- **Manufacturer data**: OEM, warranty, service contracts.
- **Document links**: Manuals, schematics, photos.

#### Maintenance Planning
| Maintenance Type | Trigger | Description |
|------------------|---------|-------------|
| **Preventive (PM)** | Time/usage | Scheduled based on calendar or meter |
| **Predictive (PdM)** | Condition | Triggered by sensor data or inspection |
| **Corrective (CM)** | Failure | Reactive repair after breakdown |
| **Condition-Based (CBM)** | Threshold | Alert when limits exceeded |
| **Shutdown** | Planned outage | Major overhauls during downtime |

#### Maintenance Schedules
- **Time-based**: Daily, weekly, monthly, annual cycles.
- **Counter-based**: Hours, cycles, mileage, production units.
- **Calendar scheduling**: Planned maintenance calendar.
- **Maintenance cycles**: Single or multiple counter strategies.
- **Call horizon**: Advance notice for upcoming maintenance.

#### Work Orders
- **Work order types**: Preventive, corrective, improvement, inspection.
- **Planning**: Task list, labor, parts, tools, duration.
- **Scheduling**: Assignment, priority, due date.
- **Execution**: Clock in/out, progress, confirmations.
- **Completion**: Technical feedback, measurements, notes.
- **Settlement**: Cost allocation to cost center, asset, project.

#### Task Lists (Maintenance BOMs)
- **Operations**: Step-by-step maintenance instructions.
- **Labor**: Skill requirements, estimated hours.
- **Materials**: Spare parts, consumables.
- **Tools**: Required equipment and instruments.
- **Safety**: Permits, lockout/tagout (LOTO), PPE.
- **Checklists**: Inspection points, readings, pass/fail.

#### Spare Parts Management
- **Spare parts catalog**: Parts linked to equipment.
- **Bill of materials**: Equipment BOM for assemblies.
- **Inventory integration**: Stock availability, reservations.
- **Reorder planning**: Min/max, reorder points, lead times.
- **Critical spares**: Insurance spares for key equipment.
- **Vendor sourcing**: Preferred suppliers for parts.

#### Maintenance Costing
- **Cost elements**: Labor, materials, external services, overhead.
- **Cost collection**: Work order actuals vs. planned.
- **Budget management**: Annual maintenance budget by area.
- **Cost allocation**: Settlement to asset, cost center, project.
- **Total cost of ownership**: Lifecycle maintenance costs.
- **Maintenance KPIs**: Cost per unit, cost per hour, MTBF.

#### Downtime & Availability
- **Downtime tracking**: Start, end, duration, reason codes.
- **Availability calculation**: Uptime / (Uptime + Downtime).
- **MTBF**: Mean Time Between Failures.
- **MTTR**: Mean Time To Repair.
- **OEE integration**: Overall Equipment Effectiveness.
- **Production impact**: Lost production, cost of downtime.

#### Calibration Management
- **Calibration schedule**: Instruments requiring periodic calibration.
- **Calibration records**: Results, certificates, adjustments.
- **Out-of-tolerance**: Impact assessment, recall procedures.
- **Traceability**: NIST-traceable standards.
- **Compliance**: ISO 17025, FDA, GMP requirements.

#### Asset Commissioning & Decommissioning

**Scope**: Formal workflows for asset go-live and retirement to ensure proper lifecycle governance.

##### Commissioning Workflow
- **Pre-Commissioning Checklist**:
  - Installation verification (physical location, mounting, connections).
  - Safety inspection (lockout/tagout, guarding, emergency stops).
  - Utilities verification (power, air, water, network).
  - Documentation receipt (manuals, drawings, certificates, warranty).
- **Commissioning Work Order**: Create commissioning task with signoff gates.
- **Testing & Acceptance**:
  - Run-off testing (functional verification under load).
  - Performance benchmarking (output, efficiency, quality).
  - Safety certification (OSHA, ISO, local regulations).
  - Operator training and handover.
- **Go-Live Authorization**: Management approval to activate asset.
- **Fixed Asset Integration**: Trigger capitalization in ADR-021 (acquisition → in-service).
- **Maintenance Planning**: Auto-generate preventive maintenance schedules.
- **Equipment Master Activation**: Set status to "Active" with go-live date.

##### Decommissioning Workflow
- **Decommissioning Request**: Business unit requests asset retirement.
- **Impact Assessment**:
  - Production dependencies and replacement readiness.
  - Spare parts obsolescence risk.
  - Environmental/safety obligations (hazmat, EPA, ISO 14001).
- **Decommissioning Work Order**: Final maintenance, cleanup, and removal.
- **Final Inspection**:
  - Safe shutdown (lockout/tagout, isolation).
  - Hazardous material removal (fluids, refrigerants, batteries).
  - Salvage value assessment.
- **Disposal Options**:
  - **Sell/Auction**: List asset with condition report and valuation.
  - **Scrap**: Salvage for parts, recycle materials.
  - **Donate**: Transfer to charity with tax documentation.
  - **Transfer**: Move to another facility or entity.
- **Documentation**:
  - Decommissioning certificate with final condition report.
  - Environmental compliance records (disposal permits, recycling certificates).
  - Warranty and service contract cancellation.
- **Fixed Asset Integration**: Trigger disposal in ADR-021 (retirement → gain/loss posting).
- **Equipment Master Closure**: Set status to "Decommissioned" with retirement date.

#### Asset Health Scoring & Condition Assessment

**Scope**: Systematic evaluation of asset condition to support maintenance strategy and replacement decisions.

##### Health Score Model
- **Scoring Dimensions**:
  - **Physical Condition** (0-100): Wear, corrosion, vibration, alignment.
  - **Performance** (0-100): Output, efficiency, quality consistency.
  - **Reliability** (0-100): MTBF, failure frequency, unplanned downtime.
  - **Maintenance Cost** (0-100): Actual vs planned cost, trend analysis.
  - **Criticality** (0-100): Production impact if failed (A/B/C classification).
- **Composite Health Score**: Weighted average of dimensions (customizable by asset class).
- **Scoring Frequency**: Monthly or after major maintenance events.
- **Trend Analysis**: Health score degradation rate over time.

##### Condition States
| Score Range | Condition | Action Required |
|-------------|-----------|-----------------|
| **90-100** | Excellent | Continue PM schedule |
| **75-89** | Good | Monitor closely |
| **60-74** | Fair | Increase inspection frequency |
| **40-59** | Poor | Plan major overhaul |
| **0-39** | Critical | Immediate action or replacement |

##### Inspection & Condition Reporting
- **Condition Inspections**: Scheduled and ad-hoc inspections with checklists.
- **Visual Inspection**: Photos, videos, thermal imaging.
- **Performance Measurements**: Vibration analysis, oil analysis, thermography, ultrasonic testing.
- **Condition Reports**: Inspector findings with severity ratings.
- **Health Score Updates**: Inspection results feed into health score calculation.
- **Alert Thresholds**: Auto-escalate when health score drops below threshold.

#### End-of-Life Planning & Replacement Forecasting

**Scope**: Proactive planning for asset replacement based on lifecycle, condition, and business strategy.

##### End-of-Life (EOL) Indicators
- **Age-Based**: Asset age vs expected useful life (from ADR-021 depreciation schedule).
- **Condition-Based**: Health score below replacement threshold (e.g., <50).
- **Obsolescence**: OEM discontinuation, parts unavailable, technology outdated.
- **Economic**: Maintenance cost exceeds operating cost savings (run-to-failure analysis).
- **Regulatory**: Compliance changes (emissions, safety, efficiency standards).
- **Strategic**: Business process changes, capacity expansion, technology upgrades.

##### Replacement Forecasting
- **Replacement Planning Horizon**: 1-5 year rolling forecast by asset class.
- **Capital Budget Integration**: Feed replacement forecast into ADR-032 (Budgeting & Planning).
- **Prioritization Matrix**:
  - **Criticality** (A/B/C classification).
  - **Condition** (health score).
  - **Business Impact** (production loss, safety risk, compliance).
  - **Financial** (ROI, payback period, TCO improvement).
- **Replacement Scenarios**:
  - **Like-for-Like**: Replace with same model (proven technology).
  - **Upgrade**: Replace with newer/better model (efficiency, capacity).
  - **Eliminate**: Process change eliminates need for asset.
  - **Outsource**: Convert to service contract instead of owning asset.
- **Replacement Work Orders**: Create major project work orders for large replacements.
- **Decommissioning Trigger**: Link replacement decision to decommissioning workflow.

##### Total Cost of Ownership (TCO) Analysis
- **Acquisition Cost**: Purchase price, installation, commissioning.
- **Operating Cost**: Energy, consumables, labor for normal operation.
- **Maintenance Cost**: PM, CM, parts, external services (from work order history).
- **Downtime Cost**: Lost production, opportunity cost.
- **Disposal Cost**: Decommissioning, removal, environmental compliance.
- **TCO Comparison**: Compare current asset TCO vs replacement option TCO.
- **Break-Even Analysis**: Calculate years to recover replacement investment.

##### Asset Registry & Lifecycle Visibility
- **Asset Registry Dashboard**: Single view of all assets with status, health, age, cost.
- **Lifecycle Stage Tracking**: Commissioned → Active → Aging → Critical → Scheduled for Replacement → Decommissioned.
- **Cross-Module Integration**:
  - **ADR-021 (Fixed Assets)**: Acquisition, depreciation, disposal, NBV.
  - **ADR-040 (Maintenance)**: Work orders, downtime, costs, health scores.
  - **ADR-024 (Inventory)**: Spare parts availability, obsolescence risk.
  - **ADR-032 (Budgeting)**: Capital replacement budget, variance tracking.
  - **ADR-037 (Manufacturing)**: Production dependencies, OEE impact.
- **Reporting & Analytics**:
  - Asset aging report (assets by age bracket).
  - Health score distribution (condition by asset class).
  - Replacement forecast (planned CAPEX by year).
  - TCO analysis (maintenance cost trends).
  - Compliance status (calibration, inspections, certifications).

#### Repair Center / Depot Operations

**Scope**: Centralized repair and refurbishment workflows for repairable equipment and service parts.

##### Repair-or-Scrap Decision
- **Economic Repair Limit**: Calculate repair cost vs replacement cost threshold
  - Typical thresholds: 60-70% for electronics, 50% for mechanical
  - Formula: `Repair Cost < (Replacement Cost × Threshold %)`
- **Repairability Assessment**: Technical feasibility evaluation during diagnosis
- **Decision Matrix**:
  - Repairable + Economic → Route to repair work order
  - Repairable + Uneconomic → Scrap and order replacement
  - Not Repairable → Scrap with root cause analysis

##### Multi-Stage Repair Workflow
1. **Intake**: Create corrective work order, receive into repair queue
2. **Diagnosis**: Troubleshoot, identify failure mode and root cause
3. **Economic Evaluation**: Compare estimated repair cost to replacement cost
4. **Repair Planning**: Reserve spare parts, assign technician, estimate duration
5. **Repair Execution**: Replace components, clean, adjust, document work
6. **Testing**: Functional test with calibrated equipment, record results
7. **Quality Certification**: QA inspection against acceptance criteria
8. **Completion**: Update work order with actuals, settle costs
9. **Return to Stock**: Inventory receives refurbished unit (see ADR-024)

##### Repair Cost Tracking
- **Labor Costs**: Diagnosis hours, repair hours, testing hours by technician skill level
- **Component Costs**: Spare parts consumed from inventory (ADR-024)
- **Overhead Allocation**: Test equipment depreciation, facility costs, utilities
- **External Services**: Certified depot charges, OEM support costs
- **Cost Variance Analysis**: Actual vs estimated repair costs
- **Economic Validation**: Post-repair verification that costs stayed within limit

##### Test & Calibration Integration
- **Test Station Assignment**: Route units to appropriate test equipment based on product type
- **Test Scripts**: Automated test sequences with pass/fail criteria per equipment class
- **Test Equipment Master**: Calibrated test equipment with expiry tracking
- **Calibration Certificates**: Link test results to equipment calibration records
- **Acceptance Criteria**: Configurable thresholds per product specification
- **Failed Test Handling**: Rework routing for units failing quality tests
- **Compliance Documentation**: FDA 21 CFR Part 11, ISO 17025, UL certification

##### Loaner/Exchange Programs
**Workflow Integration**:
- **Loaner Request**: Customer needs immediate replacement
- **Loaner Issue Work Order**: Create service work order linking loaner to repair
- **Defective Unit Receipt**: Receive customer's defective unit (ADR-024 inventory movement)
- **Repair Work Order**: Create corrective work order for customer unit
- **Loaner Tracking**: Monitor loaner status, expected return date
- **Exchange Notification**: Alert customer when repair complete
- **Loaner Return Work Order**: Close loaner transaction, receive unit back

**Loaner Pool Management** (see ADR-024 for inventory aspects):
- Track loaner work order history
- Monitor loaner condition after each use
- Schedule preventive maintenance on loaners
- Retirement planning based on loan count and condition

##### Certified Repair Depot (3rd Party)
- **Depot Master Data**: Authorized repair centers, certifications, contact info
- **Depot Work Orders**: Ship defective units with work order documentation
- **Depot Receipt**: Receive repaired units with test results, certifications, invoice
- **Cost Allocation**: Post depot repair invoice as external service cost
- **Quality Tracking**: Vendor scorecard (repair quality, TAT, cost accuracy)
- **Performance Metrics**: Average TAT, first-pass-yield, warranty claims

##### Warranty Refurbishment Programs
- **Warranty Repair Policy**: Define when to repair vs replace under warranty
- **Refurb Work Orders**: Track warranty repair work separately for cost recovery
- **OEM Cost Recovery**: Submit warranty claims with work order documentation
- **Refurb as Replacement**: Use refurbished units for warranty fulfillment
- **Refurb Warranty Terms**: Define shorter warranty period for refurbished (e.g., 90 days)
- **Refurb Quality Standards**: Ensure refurbished units meet original specifications

##### Repair Data Model Extensions
```
RepairWorkOrder (extends WorkOrder):
  - repairOrderNumber: string
  - equipmentId: string
  - serialNumber: string
  - failureMode: enum (MECHANICAL, ELECTRICAL, FIRMWARE, WEAR, ACCIDENT, ENVIRONMENTAL)
  - rootCause: string
  - failureAnalysis: text
  - repairability: enum (REPAIRABLE, UNECONOMIC, NOT_REPAIRABLE)
  - economicRepairLimit: decimal (threshold %)
  - estimatedRepairCost: decimal
  - actualRepairCost: decimal
  - replacementCost: decimal
  - diagnosisLaborHours: decimal
  - repairLaborHours: decimal
  - testingLaborHours: decimal
  - componentsCost: decimal (from inventory)
  - externalServiceCost: decimal (depot charges)
  - overheadCost: decimal
  - repairStartDate: timestamp
  - repairCompletionDate: timestamp
  - turnaroundTime: integer (hours)
  - technician: string
  - testResults: text
  - qualityInspector: string
  - certificationDate: timestamp
  - linkedLoanerWorkOrder: string (if loaner issued)

LoanerWorkOrder (extends WorkOrder):
  - loanerSerialNumber: string
  - customerId: string
  - issueDate: timestamp
  - expectedReturnDate: timestamp
  - actualReturnDate: timestamp
  - returnCondition: enum (GOOD, FAIR, DAMAGED, NEEDS_REPAIR)
  - linkedRepairWorkOrder: string
  - loanerUsageNotes: text

CertifiedDepot:
  - depotId: string
  - depotName: string
  - certifications: list<string> (OEM authorized, ISO certified)
  - serviceTypes: list<string> (equipment classes handled)
  - averageTAT: integer (days)
  - qualityRating: decimal (1-5 scale)
  - costAccuracy: decimal (% actual vs quoted)
  - contactInfo: object
```

##### Integration Points
- **Inventory (ADR-024)**: Repair locations, loaner inventory, refurbished stock movements
- **Quality (ADR-039)**: Test results, calibration certificates, failure analysis
- **Controlling (ADR-028)**: Repair cost settlement, work order costing
- **Procurement (ADR-023)**: Spare parts for repairs, depot service invoices
- **Fixed Assets (ADR-021)**: Equipment asset linkage, loaner asset depreciation
- **Field Service (ADR-042)**: Service technician triggers repair, loaner exchanges

##### Repair Center KPIs
| Metric | Target | Description |
|--------|--------|-------------|
| **Repair TAT** | p95 < 5 days (in-house), p95 < 10 days (depot) | Time from intake to return to stock |
| **First Pass Yield** | ≥ 90% | Units passing testing on first attempt |
| **Economic Repair Rate** | ≥ 85% | Repairs within economic limit |
| **Repair Cost Accuracy** | Within 10% of estimate | Actual vs estimated cost variance |
| **Loaner Availability** | ≥ 95% | Loaners available when requested |
| **Loaner Return Rate** | ≥ 98% | On-time loaner returns |
| **Refurbished Quality** | ≥ 99% | Refurb units meeting QA standards |
| **Depot Performance** | Avg rating ≥ 4.0/5.0 | Certified depot quality score |
| **Test Equipment Calibration** | 100% | All test equipment within cal date |

### Data Model (Conceptual)
- `Equipment`, `FunctionalLocation`, `EquipmentClass`, `TechnicalAttribute`.
- `MaintenancePlan`, `MaintenanceSchedule`, `MaintenanceCycle`, `Counter`.
- `WorkOrder`, `WorkOrderOperation`, `TaskList`, `Checklist`.
- `SparePart`, `EquipmentBOM`, `PartReservation`.
- `MaintenanceCost`, `CostAllocation`, `MaintenanceBudget`.
- `DowntimeRecord`, `AvailabilityMetric`, `CalibrationRecord`.
- **Asset Lifecycle**: `CommissioningWorkOrder`, `DecommissioningWorkOrder`, `AssetConditionReport`, `DisposalRecord`.
- **Health & Condition**: `HealthScore`, `ConditionInspection`, `ConditionMeasurement`, `HealthScoreDimension`.
- **End-of-Life**: `ReplacementForecast`, `EOLIndicator`, `TCOAnalysis`, `ReplacementScenario`, `AssetRegistry`.

### Key Workflows
- **Preventive maintenance**: Schedule triggers → work order created → plan/execute → complete → settle costs.
- **Breakdown repair**: Notification → work order → diagnose → repair → confirm → analyze root cause.
- **Spare parts**: Work order → check stock → reserve/procure → issue → install → update BOM.
- **Calibration**: Schedule due → work order → calibrate → record results → next due.
- **Shutdown maintenance**: Plan outage → bundle work orders → execute → restart → document.
- **Commissioning**: Installation → testing → safety certification → training → go-live approval → ADR-021 capitalization.
- **Decommissioning**: Request → impact assessment → final maintenance → disposal decision → ADR-021 retirement posting → closure.
- **Health scoring**: Inspection → condition measurement → health score calculation → alert if critical → replacement planning.
- **Replacement planning**: Monitor health scores → identify EOL assets → TCO analysis → prioritize → budget (ADR-032) → procure → commission.

### Integration Points
- **Fixed Assets (ADR-021)**: Equipment linked to asset master, depreciation, loaner asset tracking; **commissioning triggers capitalization, decommissioning triggers disposal, NBV for replacement decisions**.
- **Inventory (ADR-024)**: Spare parts stock, reservations, issues; **repair inventory locations, loaner pool inventory, refurbished stock movements**.
- **Procurement (ADR-023)**: Spare parts purchasing, service contracts; **replacement asset procurement, disposal/salvage sales**.
- **Manufacturing (ADR-037)**: Production equipment, OEE, downtime impact; **commissioning readiness, decommissioning impact on production**.
- **Controlling (ADR-028)**: Maintenance cost allocation, budgets, **repair cost settlement, TCO tracking, replacement cost variance**.
- **Budgeting & Planning (ADR-032)**: **Replacement forecast feeds CAPEX budget, multi-year replacement planning, budget variance analysis**.
- **HR Integration (ADR-034)**: Maintenance technician skills, labor.
- **Quality (ADR-039)**: Calibration, equipment qualification, **repair test results, refurbished stock certification, commissioning acceptance tests**.
- **Field Service (ADR-042)**: **Field technician triggers repairs, customer site loaner exchanges**.
- **Analytics (ADR-016)**: Maintenance KPIs, reliability dashboards, **repair turnaround time, loaner utilization, health score trends, replacement forecast accuracy**.
- **IoT (future)**: Sensor data for predictive maintenance, **real-time condition monitoring for health scores**.

### Non-Functional Constraints
- **Availability**: Maintenance system uptime ≥ 99.5%.
- **Mobile**: Work order execution on mobile devices.
- **Offline**: Offline capability for field maintenance.
- **Performance**: Work order creation < 3 seconds.
- **Scalability**: Support 100,000+ equipment records per tenant.

### KPIs and SLOs
| Metric | Target |
|--------|--------|
| PM compliance (scheduled vs. completed) | ≥ 95% |
| Work order completion time | p95 < planned duration + 20% |
| Equipment availability | ≥ 95% (critical: ≥ 99%) |
| MTBF improvement | Year-over-year increase |
| MTTR reduction | Year-over-year decrease |
| Spare parts availability | ≥ 98% for critical spares |
| Maintenance cost variance | ≤ 10% vs. budget |
| Calibration compliance | 100% on schedule |
| **Commissioning cycle time** | **≤ 30 days from install to go-live** |
| **Decommissioning completion** | **≤ 15 days from request to disposal** |
| **Health score coverage** | **≥ 90% of critical assets scored monthly** |
| **Health score accuracy** | **≥ 85% predictive accuracy for failures** |
| **Replacement forecast accuracy** | **≥ 80% of forecasted replacements executed on time** |
| **TCO data completeness** | **≥ 95% of assets have complete TCO data** |
| **Asset registry accuracy** | **≥ 99% match between physical and system records** |

## Alternatives Considered
- **Spreadsheet/calendar tracking**: Rejected (no history, no cost tracking).
- **Standalone CMMS**: Rejected (data silos, no financial integration).
- **Reactive only**: Rejected (higher costs, more downtime).

## Consequences
### Positive
- Reduced unplanned downtime.
- Extended equipment life.
- Optimized spare parts inventory.
- Better maintenance cost control.
- Regulatory compliance (calibration, safety).
- **Complete asset lifecycle visibility from commissioning to decommissioning.**
- **Proactive replacement planning with TCO-based decision making.**
- **Data-driven health scoring reduces surprise failures.**
- **Integrated CAPEX budgeting for asset replacements (ADR-032).**
- **Stronger audit trail for asset lifecycle governance.**

### Negative
- Initial data setup (equipment hierarchy, task lists).
- Change management for maintenance teams.
- Mobile device requirements for field work.
- **Requires discipline in condition inspections for accurate health scores.**
- **Multi-year replacement forecasting needs executive buy-in.**

### Neutral
- Module is optional; enabled per tenant.
- Can integrate with external IoT/predictive platforms.

## Compliance
- **OSHA**: Safety procedures, lockout/tagout.
- **EPA**: Environmental compliance for equipment.
- **FDA/GMP**: Equipment qualification, calibration.
- **ISO 55000**: Asset management standard.
- **ISO 17025**: Calibration laboratory competence.

## Add-on Activation
- **Tenant feature flag**: `plant_maintenance_enabled`.
- **Licensing**: Optional module, separate pricing tier.
- **Prerequisites**: Core fixed assets, inventory, procurement.
- **Industry presets**: Manufacturing, utilities, facilities, healthcare.

## Implementation Plan
- Phase 1: Equipment master, functional locations, hierarchy.
- Phase 2: Work order management, task lists.
- Phase 3: Preventive maintenance scheduling.
- Phase 4: Spare parts integration, reservations.
- Phase 5: Cost tracking, budgeting, settlement.
- Phase 6: Calibration, downtime tracking, KPI dashboards.
- **Phase 7: Commissioning & decommissioning workflows, fixed asset integration (ADR-021).**
- **Phase 8: Asset health scoring, condition inspections, alert thresholds.**
- **Phase 9: End-of-life planning, replacement forecasting, TCO analysis, budget integration (ADR-032).**
- Phase 10: Mobile app, offline capability.
- Phase 11: Predictive maintenance (IoT integration).

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-016: Analytics & Reporting Architecture
- ADR-021: Fixed Asset Accounting (FI-AA) — commissioning capitalization, decommissioning disposal, NBV for replacement decisions
- ADR-023: Procurement (MM-PUR) — replacement asset procurement, disposal/salvage sales
- ADR-024: Inventory Management (MM-IM) — repair/loaner inventory
- ADR-028: Controlling / Management Accounting (CO) — TCO tracking, replacement cost variance
- ADR-032: Budgeting & Planning / FP&A — replacement forecast feeds CAPEX budget, multi-year planning
- ADR-034: HR Integration & Payroll Events
- ADR-037: Manufacturing & Production (PP) — commissioning readiness, decommissioning impact
- ADR-039: Quality Management (QM) — test results, certification, commissioning acceptance tests
- ADR-042: Field Service Operations — field technician triggers repairs, loaner exchanges
- ADR-053: Fleet Management — specialized asset lifecycle for vehicles (separate module)

### External References
- SAP PM (Plant Maintenance) module overview
- ISO 55000 Asset Management
- SMRP Best Practices (Society for Maintenance & Reliability Professionals)
