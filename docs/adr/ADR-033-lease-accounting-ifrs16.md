# ADR-033: Lease Accounting (IFRS 16 / ASC 842)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Finance Team  
**Priority**: P2 (Medium)  
**Tier**: Add-on  
**Tags**: lease-accounting, ifrs16, asc842, compliance

## Context
Lease accounting standards (IFRS 16 and ASC 842) require recognition of right-of-use (ROU) assets and lease liabilities, along with interest and depreciation calculations. SAP-grade ERP must handle lease lifecycle events, remeasurements, and disclosures.

## Decision
Implement a **Lease Accounting** capability that manages lease contracts, ROU assets, liabilities, and statutory disclosures.

### Scope
- Operating and finance leases.
- ROU assets and lease liability schedules.
- Lease modifications, renewals, and terminations.
- Disclosure and compliance reporting.

### Core Capabilities
- **Lease contract management**: terms, payments, renewal options.
- **ROU asset calculation** and amortization schedules.
- **Lease liability schedules** with interest accretion.
- **Remeasurement** for modifications and index changes.
- **Impairment and termination** handling.
- **Disclosures** for statutory reporting.

### Data Model (Conceptual)
- `LeaseContract`, `LeasePayment`, `ROUAsset`, `LeaseLiability`, `LeaseSchedule`, `LeaseModification`.

### Key Workflows
- **Lease onboarding**: contract -> recognition -> ROU asset + liability.
- **Periodic posting**: interest + depreciation postings.
- **Modification**: remeasure liability and adjust ROU asset.
- **Termination**: derecognition and gain/loss posting.

### Integration Points
- **FI/GL**: lease postings and reconciliation.
- **Fixed Assets (FI-AA)**: ROU assets and depreciation.
- **Treasury (TR-CM)**: lease payment scheduling.
- **Analytics**: lease liability and disclosure reporting.

### Non-Functional Constraints
- **Accuracy**: schedules reconcile to GL with <0.01% variance.
- **Auditability**: all lease modifications tracked with approvals.
- **Performance**: monthly lease posting runs within close window.

### KPIs and SLOs
- **Lease recognition run**: p95 < 30 minutes for 5,000 leases.
- **Posting accuracy**: < 0.01% variance between schedules and GL.
- **Modification processing**: < 1 business day turnaround.
- **Disclosure readiness**: 100% of required data available at close.

## Alternatives Considered
- **Manual lease tracking**: rejected (non-compliance risk).
- **External lease tool only**: rejected (integration complexity).
- **Spreadsheet schedules**: rejected (audit risk and error-prone).

## Consequences
### Positive
- Compliance with IFRS 16 and ASC 842.
- Improved transparency of lease obligations.
- Automated postings and schedules reduce close effort.

### Negative
- Requires ongoing contract maintenance and remeasurement rules.
- Additional complexity for finance teams.

### Neutral
- Specialized leases (real estate, fleet) can be phased later.

---

## Real Estate Property Management Extension

### Context
Real estate companies, property management firms, and commercial landlords require lessor accounting (property owner perspective), tenant management, rent roll tracking, property operations (maintenance, CAM reconciliation), and multi-property portfolio management—beyond basic lessee lease accounting.

### Scope Extension
- **Lessor Accounting**: Rental income recognition (operating/finance leases from landlord side), lease incentives, straight-line rent.
- **Property Master Data**: Property units, square footage, zoning, valuations.
- **Tenant Management**: Tenant master, lease origination, renewals, evictions, security deposits.
- **Rent Roll**: Current occupancy, vacancy analysis, lease expirations, rent escalations.
- **Property Operations**: Maintenance work orders, vendor management, capital improvements.
- **Common Area Maintenance (CAM)**: CAM charges, reconciliation, tenant recovery.
- **Property Valuation**: Appraisals, fair market value, cap rate analysis.
- **Real Estate Taxes**: Property tax tracking, assessments, appeals.
- **Multi-Property Portfolio**: Consolidated reporting, property performance benchmarking.

### Additional Capabilities
- **Lessor Accounting**:
  - Operating lease: Straight-line rent income recognition, lease incentive amortization.
  - Finance lease (sales-type): Lease receivable, interest income, residual value.
  - Lease modifications and renewals from lessor perspective.
  - Impairment testing for lease receivables.
  
- **Tenant Lifecycle**:
  - Tenant application and credit checks.
  - Lease origination (terms, rent schedules, escalations, options).
  - Lease execution and move-in.
  - Lease renewals, amendments, expansions.
  - Lease termination and move-out (security deposit disposition).
  - Eviction workflows and legal holds.
  
- **Rent Roll Management**:
  - Current rent schedule by tenant and unit.
  - Occupancy rate and vacancy tracking.
  - Lease expiration calendar and renewal pipeline.
  - Rent escalation tracking (CPI, fixed percentage, market adjustments).
  - Delinquency tracking and collections.
  
- **Property Operations**:
  - Maintenance work orders (routine, emergency, tenant-requested).
  - Vendor contracts and service agreements.
  - Capital improvement projects (renovation, expansion).
  - Property inspections and compliance audits.
  
- **CAM Reconciliation**:
  - CAM budget allocation to tenants (pro-rata by square footage).
  - Actual CAM expenses tracking.
  - Annual reconciliation and tenant true-ups.
  - CAM charge disputes and adjustments.
  
- **Property Financials**:
  - Property-level P&L (rental income, operating expenses, NOI).
  - Cap rate and IRR calculations.
  - Property valuations (appraisal, comparable sales, income approach).
  - Property acquisition and disposition accounting.

### Data Model Extensions
- `Property`: property ID, address, type (office, retail, industrial, residential), total square footage, acquisition cost, current valuation.
- `PropertyUnit`: unit number, square footage, unit type, lease status (vacant, occupied, under renovation).
- `Tenant`: tenant master, credit rating, contact info, lease history.
- `LessorLease`: property unit, tenant, lease term, base rent, escalations, CAM charges, security deposit.
- `RentRoll`: property, unit, tenant, lease start/end, monthly rent, occupancy status.
- `PropertyWorkOrder`: property, unit, work type (maintenance, repair, capital improvement), vendor, cost.
- `CAMReconciliation`: period, property, budgeted CAM, actual CAM, tenant allocations, true-ups.
- `PropertyValuation`: valuation date, method (appraisal, income, comparable sales), fair market value.

### Integration Points
- **Finance/FI-GL**: Rental income postings, CAM revenue, property expenses, lease receivables.
- **Fixed Assets/FI-AA**: Property acquisition cost, capital improvements, depreciation (building, not land).
- **Accounts Receivable/FI-AR**: Tenant billing, rent collections, delinquency management.
- **Treasury/TR-CM**: Security deposit tracking, tenant refunds.
- **Controlling/CO**: Property-level P&L, NOI analysis, portfolio performance.

### KPIs / SLOs
- **Rent roll accuracy**: >= 99.9% (current occupancy vs physical).
- **Lease renewal rate**: Track pipeline 6 months before expiration.
- **Occupancy rate**: Portfolio-wide and property-level tracking.
- **CAM reconciliation**: Completion within 90 days of year-end.
- **Work order response**: Emergency < 4 hours, routine < 5 business days.
- **Collection rate**: Rent collection >= 98% within 30 days of due date.
- **Property valuation**: Annual appraisal completion for portfolio.

### Implementation Phasing
- **Phase 5A**: Lessor accounting and tenant master data (4 months).
- **Phase 5B**: Rent roll and lease lifecycle management (4 months).
- **Phase 5C**: Property operations and work orders (3 months).
- **Phase 5D**: CAM reconciliation and property financials (3 months).

## Compliance
- **IFRS 16 / ASC 842**: lease recognition and disclosure requirements.
- **SOX**: audit trails for lease changes.
- **GDPR**: minimize PII in lease records.

## Implementation Plan
- Phase 1: Lease contract model and basic schedules.
- Phase 2: ROU asset and liability postings.
- Phase 3: Modifications, remeasurements, and terminations.
- Phase 4: Disclosure reporting and analytics.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-021: Fixed Asset Accounting (FI-AA)
- ADR-026: Treasury & Cash Management (TR-CM)
- ADR-016: Analytics & Reporting Architecture

### Internal Documentation
- `docs/finance/lease_accounting_requirements.md`

### External References
- IFRS 16 Leases
- ASC 842 Leases
