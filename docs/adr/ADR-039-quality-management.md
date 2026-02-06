# ADR-039: Quality Management System (QMS)

**Status**: Accepted (Production-Ready; Implementation Starting Q2 2027)
**Date**: 2026-02-05
**Updated**: 2026-02-06 - Promoted to Production-Ready with world-class enhancements
**Deciders**: Architecture Team, Operations Team, Quality Team
**Priority**: P2 (Enhancement - Manufacturing Quality)
**Tier**: Add-on
**Parent Module**: Manufacturing (ADR-037)
**Investment**: $723K-$981K first year
**Timeline**: Q1-Q2 2027 (28 weeks, 3 phases)
**Tags**: qms, quality, inspection, spc, msa, nonconformance, capa, iso-9001, as9100, iso-13485, hexagonal-architecture

## Context

**Problem**: ChiroERP manufacturing customers require comprehensive Quality Management System (QMS) capabilities for ISO 9001, AS9100 (aerospace), and ISO 13485 (medical devices) certification. Current gaps prevent serving regulated industries and quality-critical manufacturers.

### Market Requirements

**Target Industries**: 
- ISO 9001 certified manufacturers (40% of ChiroERP manufacturing prospects)
- Aerospace (AS9100 requirement)
- Medical devices (ISO 13485 + FDA 21 CFR Part 11)
- Automotive (IATF 16949)

**Current Gaps**:
- ❌ No document control (ISO 9001 Clause 7.5)
- ❌ No NCR/CAPA management (ISO 9001 Clause 10)
- ❌ No Statistical Process Control (SPC)
- ❌ No Measurement System Analysis (MSA)
- ❌ No supplier quality management
- ❌ No calibration management

**Competitive Reality**:

| System | Document Control | NCR/CAPA | SPC/MSA | Supplier Quality | ISO Compliance |
|--------|-----------------|----------|---------|------------------|----------------|
| **SAP QM** | ✅ Deep | ✅ Full | ✅ Advanced | ✅ Full | ✅ ISO 9001/AS9100 |
| **Oracle QM Cloud** | ✅ Deep | ✅ Full | ✅ Good | ✅ Full | ✅ ISO 9001 |
| **Infor CloudSuite QM** | ✅ Good | ✅ Full | ✅ Good | ✅ Good | ✅ ISO 9001 |
| **MasterControl** | ✅ Deep | ✅ Full | ✅ Basic | ✅ Good | ✅ FDA/ISO |
| **ChiroERP** | ❌ **None** | ❌ **None** | ❌ **None** | ❌ **None** | ❌ **None** |

**Customer Quote** (VP Quality, Aerospace Manufacturer):
> "We need ISO 9001 and AS9100 certification. That requires document control with electronic signatures (21 CFR Part 11), NCR/CAPA workflow, SPC with X-bar/R charts, supplier quality scorecards, and calibration management. Your system can't support any of this."

### Organizational Decision

Quality Management has been incorporated as a subdomain within the Manufacturing bounded context (`manufacturing/manufacturing-quality/`) to ensure tight coupling with production processes, inspection points, and manufacturing analytics. This placement enables seamless integration with production orders, work centers, and bill of materials while maintaining quality gates at every manufacturing stage.

## Decision
Implement a **Quality Management (QM)** add-on module that provides inspection planning, quality checks, nonconformance management, CAPA workflows, supplier quality, and audit management integrated with procurement, inventory, manufacturing, and sales.

### Scope
- Inspection planning and execution.
- Quality characteristics and specifications.
- Sampling plans and statistical quality control (SQC).
- Nonconformance (NC) and defect tracking.
- Corrective and Preventive Action (CAPA).
- Supplier quality management.
- Quality certificates (CoA, CoC).
- Audit management (internal and external).
- Regulatory compliance (ISO, FDA, GMP).

### Out of Scope (Future/External)
- Laboratory Information Management (LIMS).
- Advanced SPC/SQC tools (integrate with Minitab, JMP).
- Product lifecycle management (PLM).

### Subdomain Architecture
Quality Management is implemented as 7 subdomains within the Manufacturing bounded context, each following **hexagonal architecture** with clean separation of concerns:

```
manufacturing/
└── manufacturing-quality/                    # Quality Management (ADR-039)
    ├── quality-inspection-planning/          # Inspection Planning (Port 9501)
    ├── quality-execution/                    # Inspection Execution (Port 9502)
    ├── quality-nonconformance/               # NC Management (Port 9503)
    ├── quality-capa/                         # CAPA Workflow (Port 9504)
    ├── quality-supplier/                     # Supplier Quality (Port 9505)
    ├── quality-certificates/                 # Quality Certificates (Port 9506)
    └── quality-analytics/                    # Quality Analytics (Port 9507)
```

#### 1. Quality Inspection Planning (Port 9501)
**Package**: `com.chiroerp.manufacturing.quality.inspectionplanning`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `InspectionPlan`, `InspectionCharacteristic`, `CharacteristicType`, `SamplingProcedure`, `SampleSize`, `InspectionTrigger`, `InspectionPoint`, `ControlLimit`, `MasterInspectionCharacteristic`, `InspectionMethod`, `InspectionEquipment` | Core inspection planning entities |
| **Domain Events** | `InspectionPlanCreatedEvent`, `InspectionPlanActivatedEvent`, `CharacteristicAddedEvent` | Plan lifecycle events |
| **Input Ports** | `CreateInspectionPlanUseCase`, `ActivateInspectionPlanUseCase`, `InspectionPlanQueryPort` | Planning use cases |
| **Output Ports** | `InspectionPlanRepository`, `CharacteristicRepository`, `MaterialMasterPort` | Persistence and integration |
| **Domain Services** | `InspectionPlanService`, `CharacteristicService`, `SamplingService` | Business logic |
| **REST Controllers** | `InspectionPlanController`, `CharacteristicController`, `SamplingController` | API endpoints |

#### 2. Quality Execution (Port 9502)
**Package**: `com.chiroerp.manufacturing.quality.execution`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `InspectionLot`, `InspectionResult`, `ResultRecord`, `UsageDecision`, `DecisionCode`, `Defect`, `DefectCode`, `DefectLocation`, `ControlChart`, `MeasurementPoint` | Inspection execution entities |
| **Domain Events** | `InspectionLotCreatedEvent`, `ResultRecordedEvent`, `UsageDecisionMadeEvent`, `DefectRecordedEvent`, `LotReleasedEvent`, `LotRejectedEvent` | Execution lifecycle events |
| **Input Ports** | `CreateInspectionLotUseCase`, `RecordResultUseCase`, `MakeUsageDecisionUseCase`, `InspectionLotQueryPort` | Execution use cases |
| **Output Ports** | `InspectionLotRepository`, `ResultRepository`, `InspectionPlanPort`, `InventoryPort` | Persistence and integration |
| **Domain Services** | `InspectionExecutionService`, `ResultRecordingService`, `UsageDecisionService`, `DefectRecordingService` | Business logic |
| **REST Controllers** | `InspectionLotController`, `ResultController`, `UsageDecisionController`, `DefectController` | API endpoints |

#### 3. Quality Nonconformance (Port 9503)
**Package**: `com.chiroerp.manufacturing.quality.nonconformance`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `Nonconformance`, `NCType`, `SeverityLevel`, `DefectRecord`, `Disposition`, `DispositionType`, `QualityCost`, `CostCategory`, `ContainmentAction` | NC tracking entities |
| **Domain Events** | `NonconformanceCreatedEvent`, `DispositionDecidedEvent`, `NCClosedEvent`, `QualityCostRecordedEvent`, `ContainmentActionTakenEvent` | NC lifecycle events |
| **Input Ports** | `CreateNonconformanceUseCase`, `RecordDispositionUseCase`, `TrackQualityCostUseCase`, `NonconformanceQueryPort` | NC management use cases |
| **Output Ports** | `NonconformanceRepository`, `DispositionRepository`, `QualityCostRepository`, `InspectionLotPort` | Persistence and integration |
| **Domain Services** | `NonconformanceService`, `DispositionService`, `QualityCostService`, `ContainmentService` | Business logic |
| **REST Controllers** | `NonconformanceController`, `DispositionController`, `QualityCostController` | API endpoints |

#### 4. Quality CAPA (Port 9504)
**Package**: `com.chiroerp.manufacturing.quality.capa`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `CAPA`, `CAPAType`, `CAPAStatus`, `RootCauseAnalysis`, `FiveWhyAnalysis`, `FishboneAnalysis`, `EightDReport`, `CorrectiveAction`, `PreventiveAction`, `ActionAssignment`, `EffectivenessCheck` | CAPA workflow entities |
| **Domain Events** | `CAPACreatedEvent`, `RootCauseIdentifiedEvent`, `ActionAssignedEvent`, `ActionCompletedEvent`, `EffectivenessVerifiedEvent`, `CAPAClosedEvent` | CAPA lifecycle events |
| **Input Ports** | `CreateCAPAUseCase`, `RecordRootCauseUseCase`, `AssignActionUseCase`, `CAPAQueryPort` | CAPA use cases |
| **Output Ports** | `CAPARepository`, `RCARepository`, `ActionRepository`, `NonconformancePort` | Persistence and integration |
| **Domain Services** | `CAPAService`, `RootCauseAnalysisService`, `ActionTrackingService`, `EffectivenessService` | Business logic |
| **REST Controllers** | `CAPAController`, `RCAController`, `ActionController` | API endpoints |

#### 5. Quality Supplier (Port 9505)
**Package**: `com.chiroerp.manufacturing.quality.supplier`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `SupplierQualityProfile`, `VendorScorecard`, `QualityScore`, `PPMTracking`, `ApprovedSupplierList`, `ASLStatus`, `SupplierAudit`, `AuditFinding`, `SupplierDevelopment`, `QualityAgreement`, `SupplierCertification` | Supplier quality entities |
| **Domain Events** | `VendorQualityScoreUpdatedEvent`, `ASLStatusChangedEvent`, `SupplierAuditCompletedEvent`, `PPMThresholdExceededEvent` | Supplier quality events |
| **Input Ports** | `UpdateVendorScoreUseCase`, `ManageASLUseCase`, `SupplierQualityQueryPort` | Supplier quality use cases |
| **Output Ports** | `SupplierQualityRepository`, `ScorecardRepository`, `ASLRepository`, `ProcurementPort` | Persistence and integration |
| **Domain Services** | `VendorScorecardService`, `ASLManagementService`, `PPMCalculationService`, `SupplierAuditService` | Business logic |
| **REST Controllers** | `VendorScorecardController`, `ASLController`, `SupplierAuditController` | API endpoints |

#### 6. Quality Certificates (Port 9506)
**Package**: `com.chiroerp.manufacturing.quality.certificates`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `CertificateOfAnalysis`, `CertificateOfConformance`, `CertificateTemplate`, `CertificateContent`, `TestResult`, `Specification`, `RegulatorySubmission`, `BatchRelease`, `DigitalSignature`, `AuditTrail` | Certificate entities |
| **Domain Events** | `CertificateGeneratedEvent`, `CertificateApprovedEvent`, `BatchReleasedEvent`, `RegulatorySubmissionSentEvent` | Certificate lifecycle events |
| **Input Ports** | `GenerateCertificateUseCase`, `ApproveCertificateUseCase`, `CertificateQueryPort` | Certificate use cases |
| **Output Ports** | `CertificateRepository`, `TemplateRepository`, `InspectionLotPort`, `DocumentOutputPort` | Persistence and integration |
| **Domain Services** | `CertificateGenerationService`, `TemplateService`, `BatchReleaseService`, `RegulatorySubmissionService` | Business logic |
| **REST Controllers** | `CoAController`, `CoCController`, `BatchReleaseController` | API endpoints |
| **Output Adapters** | `PDFGeneratorAdapter` | Document generation |

#### 7. Quality Analytics (Port 9507)
**Package**: `com.chiroerp.manufacturing.quality.analytics`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `QualityKPI`, `SPCChart`, `ControlChart`, `ProcessCapability`, `FirstPassYield`, `RolledThroughputYield`, `DefectRate`, `CostOfQuality`, `COQCategory`, `TrendAnalysis`, `ParetoAnalysis` | Analytics entities |
| **Domain Events** | `QualityKPICalculatedEvent`, `SPCViolationDetectedEvent`, `ProcessOutOfControlEvent`, `COQReportGeneratedEvent` | Analytics events |
| **Input Ports** | `CalculateSPCUseCase`, `CalculateYieldUseCase`, `CalculateCOQUseCase`, `QualityAnalyticsQueryPort` | Analytics use cases |
| **Output Ports** | `QualityKPIRepository`, `SPCRepository`, `InspectionDataPort`, `AnalyticsWarehousePort` | Persistence and integration |
| **Domain Services** | `SPCCalculationService`, `YieldCalculationService`, `ProcessCapabilityService`, `COQCalculationService` | Business logic |
| **REST Controllers** | `SPCController`, `YieldController`, `COQController` | API endpoints |

### Inter-Subdomain Communication

| Source Subdomain | Target Subdomain | Communication | Purpose |
|------------------|------------------|---------------|---------|
| Inspection Planning | Execution | Event/Query | Provide inspection plan to lot creation |
| Execution | Nonconformance | Event | Create NC on rejection/defects |
| Nonconformance | CAPA | Event | Trigger CAPA on NC patterns |
| CAPA | Nonconformance | Event | Close related NCs on CAPA effectiveness |
| Execution | Certificates | Event | Provide results for certificate generation |
| Execution | Analytics | Event | Provide data for SPC/yield calculation |
| Supplier | Execution | Query | Vendor incoming inspection results |
| Analytics | All Subdomains | Query | Aggregate data for KPIs and reports |

### Core Capabilities

#### Inspection Planning
- **Inspection types**: Incoming (vendor), in-process, final, periodic.
- **Inspection points**: GR, production, shipment, customer return.
- **Trigger rules**: Automatic lot creation on goods movement.
- **Skip lot**: Reduced inspection for trusted suppliers.
- **Dynamic modification**: Adjust inspection based on history.

#### Quality Characteristics
- **Quantitative**: Numeric measurements (length, weight, temperature).
- **Qualitative**: Pass/fail, visual inspection, attribute checks.
- **Specifications**: Target, upper/lower limits, tolerance bands.
- **Master data**: Characteristic catalogs, standard specs.
- **Formulas**: Calculated characteristics, derived values.

#### Sampling Plans
- **Fixed sampling**: Sample size per lot size.
- **AQL sampling**: Acceptable Quality Level (ISO 2859).
- **Skip-lot sampling**: Reduced frequency for quality history.
- **100% inspection**: Full lot inspection for critical items.
- **Destructive testing**: Sample destruction tracking.

#### Inspection Execution
- **Inspection lots**: Created at trigger points.
- **Results recording**: Manual entry, instrument integration.
- **Usage decision**: Accept, reject, conditional accept, rework.
- **Stock posting**: Release to stock or block.
- **Defect recording**: Defect codes, quantities, locations.

#### Nonconformance Management
- **NC types**: Material, process, supplier, customer complaint.
- **Severity**: Critical, major, minor classification.
- **Disposition**: Use as-is, rework, scrap, return to vendor.
- **Cost tracking**: Quality costs (internal failure, external failure).
- **Escalation**: Automatic escalation for critical NCs.

#### CAPA (Corrective & Preventive Action)
- **Root cause analysis**: 5 Why, Fishbone, 8D methodology.
- **Corrective actions**: Address immediate issue.
- **Preventive actions**: Prevent recurrence.
- **Action tracking**: Assignments, due dates, status.
- **Effectiveness verification**: Follow-up inspection.
- **Closure workflow**: Approval and sign-off.

#### Supplier Quality
- **Supplier scorecards**: Quality rating, PPM, delivery.
- **Approved supplier list (ASL)**: Qualified vendors per item.
- **Incoming inspection results**: Vendor quality history.
- **Supplier audits**: Audit scheduling and findings.
- **Supplier CAPA**: Vendor corrective action requests.

#### Quality Certificates
- **Certificate of Analysis (CoA)**: Test results per lot.
- **Certificate of Conformance (CoC)**: Specification compliance.
- **Auto-generation**: Populate from inspection results.
- **Customer requirements**: Certificate templates by customer.
- **Digital signatures**: Approval and authentication.

#### Audit Management
- **Audit types**: Internal, supplier, customer, regulatory.
- **Audit planning**: Schedule, scope, auditors.
- **Checklist management**: Audit questions and criteria.
- **Findings**: Observations, minor/major nonconformances.
- **Audit CAPA**: Link findings to corrective actions.
- **Audit reports**: Standardized reporting templates.

### Data Model (Conceptual)
- `InspectionPlan`, `InspectionLot`, `InspectionResult`, `QualityCharacteristic`.
- `SamplingPlan`, `SamplingProcedure`, `DefectCode`, `DefectRecord`.
- `Nonconformance`, `NCDisposition`, `QualityCost`.
- `CAPA`, `CAPAAction`, `RootCauseAnalysis`, `EffectivenessCheck`.
- `SupplierQualityRecord`, `QualityCertificate`, `CertificateTemplate`.
- `Audit`, `AuditChecklist`, `AuditFinding`, `AuditCAPA`.

### Key Workflows
- **Incoming inspection**: GR → inspection lot → record results → usage decision → stock.
- **In-process inspection**: Production operation → inspection → pass/fail → continue/rework.
- **Final inspection**: Finished goods → inspection → release → ship.
- **NC workflow**: Defect found → NC created → disposition → cost → close.
- **CAPA workflow**: NC pattern → root cause → actions → verify → close.
- **Supplier audit**: Plan → execute → findings → CAPA → follow-up.

### Integration Points
- **Manufacturing (ADR-037)**: Parent bounded context; in-process inspection at work centers; production order quality gates; OEE quality rate integration.
- **Procurement (ADR-023)**: Vendor quality, incoming inspection at goods receipt.
- **Inventory (ADR-024)**: Stock status (QC hold, released, blocked); batch/lot traceability.
- **Warehouse (ADR-038)**: Quarantine zones, inspection locations, quality sampling.
- **Sales (ADR-025)**: Customer complaints, returns quality, CoA/CoC for shipments.
- **Finance (ADR-009)**: Quality costs (internal/external failure, prevention, appraisal); scrap/rework costs.
- **Master Data (ADR-027)**: Item specifications, vendor master, characteristic catalogs.
- **Analytics (ADR-016)**: Quality KPIs, trend analysis, PPM dashboards, SPC alerts.

### Port Assignments

| Subdomain | Port | Package |
|-----------|------|---------|
| Quality Inspection Planning | 9501 | `com.chiroerp.manufacturing.quality.inspectionplanning` |
| Quality Execution | 9502 | `com.chiroerp.manufacturing.quality.execution` |
| Quality Nonconformance | 9503 | `com.chiroerp.manufacturing.quality.nonconformance` |
| Quality CAPA | 9504 | `com.chiroerp.manufacturing.quality.capa` |
| Quality Supplier | 9505 | `com.chiroerp.manufacturing.quality.supplier` |
| Quality Certificates | 9506 | `com.chiroerp.manufacturing.quality.certificates` |
| Quality Analytics | 9507 | `com.chiroerp.manufacturing.quality.analytics` |

### Non-Functional Constraints
- **Traceability**: Full lot/serial trace from raw material to finished goods.
- **Auditability**: Immutable inspection records, electronic signatures.
- **Performance**: Inspection lot creation < 5 seconds at GR.
- **Compliance**: 21 CFR Part 11, ISO 9001, GMP ready.

### KPIs and SLOs
| Metric | Target |
|--------|--------|
| Inspection lot processing | p95 < 4 hours from creation |
| NC closure time | p95 < 30 days (critical: < 7 days) |
| CAPA effectiveness rate | ≥ 90% (no recurrence) |
| Supplier PPM (defects per million) | ≤ 500 PPM |
| First pass yield | ≥ 98% |
| Customer complaint response | p95 < 24 hours |
| Audit finding closure | p95 < 60 days |

## Alternatives Considered
- **Spreadsheet tracking**: Rejected (no traceability, audit risk).
- **Standalone QMS**: Rejected (data silos, reconciliation).
- **Basic pass/fail only**: Rejected (insufficient for regulated industries).

## Consequences
### Positive
- Regulatory compliance readiness.
- Reduced quality costs (scrap, rework, returns).
- Supplier quality visibility.
- Customer confidence (certificates, audits).

### Negative
- Configuration complexity (inspection plans, characteristics).
- Requires disciplined quality data capture.
- Training for inspectors and quality engineers.

### Neutral
- Module is optional; enabled per tenant.
- Can integrate with external LIMS/SPC tools.

## Compliance
- **ISO 9001**: Quality management system standard.
- **ISO 13485**: Medical device quality management.
- **FDA 21 CFR Part 11**: Electronic records and signatures.
- **GMP**: Good Manufacturing Practice (pharma, food).

---

## World-Class Implementation Plan (February 2026 Update)

### Implementation Roadmap

**Phase 1: Document Control & NCR (Q1 2027 - 10 weeks)**
- Week 1-3: Document control service (hierarchical structure Level 1-4, version control, approval workflows)
- Week 4-6: Electronic signatures (21 CFR Part 11 with PKI digital certificates)
- Week 7-10: NCR/CAPA service (NCR workflow, root cause analysis 5-Why/Fishbone/Pareto, CAPA tracking)

**Phase 2: Inspection & SPC (Q2 2027 - 10 weeks)**
- Week 11-14: Inspection management (incoming/in-process/final, inspection plans, sampling AQL/LTPD/AOQL)
- Week 15-18: Statistical Process Control (control charts X-bar/R/p/c/CUSUM/EWMA, process capability Cp/Cpk/Pp/Ppk)
- Week 19-20: Measurement System Analysis (Gage R&R Type 1/2/3, bias/linearity/stability)

**Phase 3: Supplier Quality & Calibration (Q2 2027 - 8 weeks)**
- Week 21-24: Supplier quality (scorecards OTD/quality/cost, performance ratings A/B/C/D, PPAP)
- Week 25-26: Calibration management (schedules by criticality, certificates vendor/internal, tolerance verification)
- Week 27-28: Training management (competency matrix, training records, certifications, effectiveness assessment)

### Cost Estimate

**Total Investment**: **$723K-$981K** (first year)

**Development Costs**: $680K-$905K
- Backend developers: 2 × 6 months @ $120K-160K = $240K-320K
- QA specialist (domain expert): 1 × 5 months @ $140K-180K = $140K-180K
- Frontend developer: 1 × 3 months @ $100K-130K = $100K-130K
- Testing/QA: 1 × 4 months @ $100K-125K = $100K-125K
- Tech lead (20% allocation): 6 months @ $50K-75K = $50K-75K
- DevOps (10% allocation): 6 months @ $25K-37.5K = $25K-37.5K
- Documentation: 1 × 1 month @ $25K-37.5K = $25K-37.5K

**Infrastructure Costs**: $43K-$76K
- Document management storage (S3/Azure Blob): $10K-20K/year
- Electronic signature service (DocuSign/Adobe Sign): $15K-30K/year
- SPC software licenses (Minitab/JMP optional): $10K-18K/year
- Calibration database: $5K/year
- Training LMS integration: $3K/year

### Success Metrics

**Quality KPIs**:
- ✅ NCR cycle time: <30 days from 90 days baseline (67% reduction)
- ✅ CAPA effectiveness: >85% (no recurrence within 12 months)
- ✅ Defect rate: 60% reduction (from baseline 2% to 0.8%)
- ✅ Cost of quality: 50% reduction (from 5% of COGS to 2.5%)

**Document Control KPIs**:
- ✅ Electronic signatures: >90% adoption (from 0% paper-based)
- ✅ Approval cycle time: 70% reduction (from 7 days to <2 days)
- ✅ Document retrieval: <10 seconds (from manual filing)
- ✅ Audit findings: 80% reduction (from poor document control)

**Compliance KPIs**:
- ✅ ISO 9001 certification: Q2 2027 (first customer certified)
- ✅ AS9100 aerospace: Q3 2027 (aerospace customers)
- ✅ ISO 13485 medical: Q4 2027 (medical device customers)
- ✅ Audit findings: <5 per year (minor/major combined)

**Inspection KPIs**:
- ✅ SPC charts: Real-time (from manual Excel)
- ✅ Process capability (Cpk >1.33): >90% of processes
- ✅ MSA Gage R&R: <10% for all critical measurements
- ✅ Inspection time: 50% reduction (from paper-based)

**Business KPIs**:
- ✅ Quality-certified manufacturing customers: +30% revenue growth
- ✅ Customer complaints: 60% reduction
- ✅ Warranty costs: 50% reduction
- ✅ Supplier defects: 70% reduction (from improved supplier quality)

### Integration with Other ADRs

- **ADR-037**: Manufacturing Execution (production orders, work centers, BOM)
- **ADR-025**: Manufacturing domain (shop floor integration)
- **ADR-024**: Inventory Management (quality inspection stock)
- **ADR-058**: SOC 2 (audit trails, access controls)
- **ADR-059**: ISO 27001 (information security overlap)
- **ADR-066**: Healthcare Industry (21 CFR Part 11 electronic signatures overlap)

### Competitive Parity Achieved

| Capability | SAP QM | Oracle QM | ChiroERP QMS (ADR-039) |
|------------|--------|-----------|------------------------|
| Document Control | ✅ Full | ✅ Full | ✅ **Full** (ISO 9001 compliant) |
| Electronic Signatures | ✅ 21 CFR Part 11 | ✅ Yes | ✅ **21 CFR Part 11** |
| NCR/CAPA | ✅ Full | ✅ Full | ✅ **Full** (5-Why, Fishbone, 8D) |
| SPC Charts | ✅ X-bar/R/p/c | ✅ Yes | ✅ **X-bar/R/p/c/CUSUM/EWMA** |
| Process Capability | ✅ Cp/Cpk | ✅ Yes | ✅ **Cp/Cpk/Pp/Ppk + Six Sigma** |
| MSA | ✅ Gage R&R | ✅ Type 1-3 | ✅ **Type 1-3 full suite** |
| Supplier Quality | ✅ Scorecards | ✅ Yes | ✅ **Scorecards + PPAP** |
| ISO Certification | ✅ 9001/13485 | ✅ 9001 | ✅ **9001/AS9100/13485** |

**Target Rating**: 9/10 (world-class manufacturing quality management)

---

**Related Documents**:
- WORLD-CLASS-ERP-GAP-ANALYSIS.md (Manufacturing Quality gap addressed)
- WORLD-CLASS-ROADMAP.md (P2 Enhancement phase)
- COMPLETE_STRUCTURE.txt (manufacturing/manufacturing-quality/ structure)
- **IATF 16949**: Automotive quality management.
- **AS9100**: Aerospace quality management.

## Add-on Activation
- **Tenant feature flag**: `quality_management_enabled`.
- **Licensing**: Optional module, separate pricing tier.
- **Prerequisites**: Core inventory, procurement modules.
- **Industry presets**: Pharma, food, automotive, aerospace configurations.

## Implementation Plan
Implementation follows the subdomain architecture within `manufacturing/manufacturing-quality/`:

- **Phase 1**: `quality-inspection-planning` - Inspection plans, characteristics, sampling procedures.
- **Phase 2**: `quality-execution` - Inspection lots, results recording, usage decisions, defect tracking.
- **Phase 3**: `quality-nonconformance` - NC creation, disposition, quality cost tracking.
- **Phase 4**: `quality-capa` - CAPA workflow, root cause analysis (5 Why, Fishbone, 8D), action tracking.
- **Phase 5**: `quality-supplier` - Vendor scorecards, ASL management, PPM tracking, supplier audits.
- **Phase 6**: `quality-certificates` - CoA/CoC generation, batch release, regulatory submissions.
- **Phase 7**: `quality-analytics` - SPC charts, process capability (Cp/Cpk), yield calculations, COQ analysis.

## References
### Related ADRs
- ADR-037: Manufacturing & Production (PP) - **Parent bounded context**
- ADR-016: Analytics & Reporting Architecture
- ADR-023: Procurement (MM-PUR)
- ADR-024: Inventory Management (MM-IM)
- ADR-027: Master Data Governance (MDG)
- ADR-038: Warehouse Execution System (WES/WMS)

### External References
- SAP QM (Quality Management) module overview
- ISO 9001:2015 Quality Management Systems
- ISO 2859 Sampling Procedures
- FDA 21 CFR Part 11 Electronic Records
