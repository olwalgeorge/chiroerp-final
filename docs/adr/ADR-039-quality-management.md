# ADR-039: Quality Management (QM)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-02
**Deciders**: Architecture Team, Operations Team, Quality Team
**Priority**: P3 (Optional Add-on)
**Tier**: Add-on
**Tags**: quality, inspection, nonconformance, audit, iso, fda, gmp

## Context
Manufacturing, distribution, and regulated industries require quality management capabilities to ensure product conformance, manage inspections, track nonconformances, and maintain regulatory compliance. A general-purpose ERP must support quality workflows as an optional add-on for tenants with quality-critical operations.

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
- **Procurement (ADR-023)**: Vendor quality, incoming inspection.
- **Inventory (ADR-024)**: Stock status (QC hold, released, blocked).
- **Warehouse (ADR-038)**: Quarantine zones, inspection locations.
- **Manufacturing (ADR-037)**: In-process inspection, production quality.
- **Sales (ADR-025)**: Customer complaints, returns quality.
- **Finance (ADR-009)**: Quality costs, scrap costs, rework costs.
- **Master Data (ADR-027)**: Item specifications, vendor master.
- **Analytics (ADR-016)**: Quality KPIs, trend analysis, PPM dashboards.

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
- **IATF 16949**: Automotive quality management.
- **AS9100**: Aerospace quality management.

## Add-on Activation
- **Tenant feature flag**: `quality_management_enabled`.
- **Licensing**: Optional module, separate pricing tier.
- **Prerequisites**: Core inventory, procurement modules.
- **Industry presets**: Pharma, food, automotive, aerospace configurations.

## Implementation Plan
- Phase 1: Inspection planning, characteristics, basic inspection.
- Phase 2: Sampling plans, usage decisions, stock integration.
- Phase 3: Nonconformance management, defect tracking.
- Phase 4: CAPA workflow, root cause analysis.
- Phase 5: Supplier quality, scorecards, incoming inspection.
- Phase 6: Certificates, audit management, regulatory templates.

## References
### Related ADRs
- ADR-016: Analytics & Reporting Architecture
- ADR-023: Procurement (MM-PUR)
- ADR-024: Inventory Management (MM-IM)
- ADR-027: Master Data Governance (MDG)
- ADR-037: Manufacturing & Production (PP)
- ADR-038: Warehouse Execution System (WES/WMS)

### External References
- SAP QM (Quality Management) module overview
- ISO 9001:2015 Quality Management Systems
- ISO 2859 Sampling Procedures
- FDA 21 CFR Part 11 Electronic Records
