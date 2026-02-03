# ADR-021: Fixed Asset Accounting (FI-AA)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Finance Team  
**Priority**: P1 (High)  
**Tier**: Core  
**Tags**: finance, fixed-assets, depreciation, compliance

## Context
Fixed asset accounting is required for GAAP/IFRS compliance and ERP parity with SAP FI-AA. We must support asset capitalization, depreciation, transfers, disposals, impairments, and auditability across multi-entity tenants, including book and tax views.

## Decision
Implement a dedicated **Fixed Asset Accounting** capability within the financial domain, aligned to GAAP/IFRS requirements and integrated with GL, procurement, and inventory.

### Scope
- Tangible fixed assets, construction-in-progress, and asset retirements.
- Book and tax depreciation areas.
- Multi-currency asset valuation.

### Feature Tiering (Core vs Advanced)
**Core**
- Asset master data, acquisition, disposal, and straight-line depreciation.
- Basic GL posting and asset register reporting.

**Advanced**
- Multiple depreciation areas (book/tax), componentization, impairment/revaluation.
- CIP workflows, transfers, and multi-currency valuation.

### Core Capabilities
- **Asset master data**: class, cost center, location, useful life, depreciation key.
- **Asset lifecycle**: acquisition, capitalization, in-service, revaluation, impairment, transfer, disposal.
- **Depreciation engines**: straight-line, declining balance, units of production.
- **Componentization**: asset components with separate depreciation.
- **CIP**: construction-in-progress with capitalization on completion.
- **Posting rules**: automatic GL postings for acquisition, depreciation, disposal.
- **Period controls**: asset sub-ledger close aligned with GL.
- **Depreciation areas**: book vs tax views with separate schedules.
- **Asset retirement obligations**: retirement/impairment controls where required.

### Data Model (Conceptual)
- `Asset`, `AssetClass`, `DepreciationKey`, `DepreciationArea`, `DepreciationRun`, `AssetTransaction`, `CIPProject`.

### Key Workflows
- **Acquisition**: PO/Invoice -> capitalization -> asset record -> depreciation.
- **Transfer**: cost center/location transfer with audit trail.
- **Disposal**: sale/scrap with gain/loss posting.
- **Impairment/Revaluation**: compliance posting with approval.
- **Depreciation run**: scheduled batch with posting to GL.

### Integration Points
- **Procurement/AP**: capitalization from vendor invoices and GR/IR.
- **Inventory**: asset reclassification for items moved to fixed assets.
- **GL**: automatic postings to asset, depreciation, and disposal accounts.
- **Authorization/SoD**: segregation of asset creation vs approval.

### Non-Functional Constraints
- **Auditability**: immutable asset transaction history.
- **Accuracy**: depreciation run reconciliation with GL.
- **Performance**: monthly depreciation close within SLA.

### KPIs and SLOs
- **Depreciation run completion**: p95 < 30 minutes for 10,000 assets.
- **Book vs tax depreciation sync**: same-day posting for dual-area consistency.
- **Asset transaction posting**: p95 < 500ms for capitalization and disposal.
- **GL reconciliation variance**: < 0.01% at period close.

## Alternatives Considered
- **External asset module**: rejected due to integration complexity and audit risk.
- **GL-only tracking**: rejected; lacks depreciation rules and compliance reporting.
- **Manual spreadsheets**: rejected; not audit-safe or scalable.

## Consequences
### Positive
- SAP-grade fixed asset accounting and depreciation automation.
- Strong auditability and compliance with IFRS/GAAP.
- Reduced month-end close effort.

### Negative
- Increased complexity in finance domain and reporting.
- Requires detailed configuration and validation for depreciation rules.

### Neutral
- Some asset classes may require phased rollout by industry.

## Compliance
- **IFRS/GAAP**: depreciation policies, impairment rules, audit trail.
- **SOX**: immutable posting records and approval controls.
- **Tax compliance**: separate depreciation areas for statutory reporting.

## Implementation Plan
- Phase 1: Asset master data and manual capitalization/disposal.
- Phase 2: Depreciation engine with scheduled runs.
- Phase 3: CIP workflows and procurement integration.
- Phase 4: Revaluation/impairment and asset reporting.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-015: Data Lifecycle Management
- ADR-023: Procurement

### Internal Documentation
- `docs/finance/asset_accounting_requirements.md`

### External References
- IAS 16 Property, Plant and Equipment
- ASC 360 Property, Plant, and Equipment
