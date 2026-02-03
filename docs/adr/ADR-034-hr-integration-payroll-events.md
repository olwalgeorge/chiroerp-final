# ADR-034: HR Integration & Payroll Events

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Finance Team, HR Team  
**Priority**: P2 (Medium)  
**Tier**: Core  
**Tags**: hr, payroll, integration, expense, cost-allocation

## Context
HR and payroll are country-specific and complex. For SAP-grade ERP coverage, we need a robust integration layer that treats employees as master data, ingests payroll results from external HCM/payroll systems, and posts payroll to the GL with cost center and project allocations. Travel & Expense (T&E) must also integrate with finance for reimbursements and expense reporting.

## Decision
Implement a **HR Integration & Payroll Events** capability that:
- Defines employee master data in MDG (ADR-027).
- Consumes payroll events from external HCM/payroll systems.
- Posts payroll results to the GL with cost allocations.
- Supports T&E workflow integration for expense approvals and reimbursements.
  - **Note**: Full T&E capabilities (booking, receipt OCR, corporate cards, policy engine) are defined in ADR-054.

### Scope
**In Scope**
- Employee master data governance (via ADR-027).
- Payroll integration events (gross, taxes, deductions, net pay).
- Payroll-to-GL posting rules.
- Cost center and project allocations for labor.
- Travel & Expense (T&E) approval and GL posting integration.

**Out of Scope**
- Payroll calculation and statutory rules engine.
- Benefits administration.
- Recruitment, onboarding, performance management.
- Time capture UI (integration only).

### Feature Tiering (Core vs Advanced)
**Core**
- Employee master data, payroll event ingestion, and GL postings.

**Advanced**
- Cost allocations to projects and full T&E workflow integration.

### Core Capabilities
- **Employee master data**: legal entity, cost center, position, and attributes.
- **Payroll event ingestion**: standardized payroll event schema.
- **GL posting rules**: mapping of payroll components to accounts.
- **Cost allocations**: distribute labor costs by cost center/project.
- **T&E integration**: expense approval, reimbursement, and GL posting.
- **Audit trail**: immutable event trace and posting evidence.

### Payroll Event Types
- **Gross pay**: salary, wages, bonuses.
- **Taxes**: employer/employee tax components.
- **Deductions**: benefits, garnishments, retirement.
- **Net pay**: cash disbursement and liability postings.

### Data Model (Conceptual)
- `Employee`, `EmployeeAssignment`, `PayrollEvent`, `PayrollComponent`, `PayrollRun`, `PayrollPosting`, `CostAllocation`, `ExpenseReport`, `ExpenseItem`.

### Key Workflows
- **Payroll integration**: ingest payroll run -> validate -> post to GL.
- **Cost allocation**: allocate payroll components to cost centers/projects.
- **T&E**: submit -> approve -> reimburse -> post expense.

### Integration Points
- **MDG (ADR-027)**: employee master data stewardship.
- **FI/GL**: payroll postings and liabilities.
- **CO**: labor cost allocations to cost/profit centers.
- **Treasury (TR-CM)**: payroll payments and cash planning.
- **Analytics**: payroll expense reporting and workforce cost dashboards.
- **T&E (ADR-054)**: full travel & expense workflow integration.

### Non-Functional Constraints
- **Accuracy**: payroll postings reconcile to source payroll run.
- **Latency**: payroll events processed within 2 hours of receipt.
- **Auditability**: full trace from payroll run to GL posting.

### KPIs and SLOs
- **Payroll ingestion latency**: p95 < 2 hours after payroll file receipt.
- **Posting accuracy**: 99.9% match between payroll run and GL postings.
- **Cost allocation completeness**: 100% of payroll cost allocated by close.
- **T&E processing time**: p95 < 3 business days from submission to reimbursement.

## Alternatives Considered
- **Full in-house payroll engine**: rejected (complexity and compliance risk).
- **Manual payroll journal entries**: rejected (audit risk).
- **External HCM only with no integration**: rejected (loss of financial visibility).

## Consequences
### Positive
- Clear financial visibility into labor costs.
- Reduced audit risk with standardized payroll postings.
- Consistent workforce cost allocation across projects and cost centers.

### Negative
- Dependence on external payroll providers and file formats.
- Requires strong integration monitoring and reconciliation.

### Neutral
- Some HR modules remain external by design.

## Compliance
- **SOX**: payroll approval and posting controls.
- **GDPR**: minimization and protection of employee PII.
- **Tax compliance**: correct payroll tax posting to liability accounts.

## Implementation Plan
- Phase 1: Employee master data model and payroll event schema.
- Phase 2: Payroll ingestion pipeline and GL posting rules.
- Phase 3: Cost allocation engine and CO integration.
- Phase 4: T&E workflow integration and reporting.

## References
### Related ADRs
- ADR-027: Master Data Governance (MDG)
- ADR-009: Financial Accounting Domain Strategy
- ADR-028: Controlling / Management Accounting (CO)
- ADR-026: Treasury & Cash Management (TR-CM)
- ADR-016: Analytics & Reporting Architecture
- ADR-054: Travel & Expense Management (T&E)

### Internal Documentation
- `docs/hr/payroll_integration_requirements.md`

### External References
- SAP HCM / SuccessFactors integration patterns
- IRS payroll reporting guidance
