# ADR-050: Public Sector & Government Accounting (Add-on)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-03  
**Deciders**: Architecture Team, Finance Team  
**Priority**: P3 (Niche Market)  
**Tier**: Add-on  
**Tags**: public-sector, fund-accounting, grants, budgetary-control, government, non-profit

## Context
Public sector entities (federal, state, local governments, municipalities, non-profits, universities) operate under different accounting principles than commercial enterprises. They require fund accounting, budgetary control, encumbrances, appropriations accounting, and grants management to ensure compliance with governmental accounting standards (GASB, OMB Circular A-87, 2 CFR Part 200) and transparent use of taxpayer funds.

Unlike commercial ERP's profit-oriented focus, public sector requires:
- **Fund-based accounting**: Segregating resources by purpose/restriction rather than profit centers.
- **Budgetary control**: Enforcing spending limits at line-item level with real-time budget vs actual.
- **Encumbrances**: Recording commitments (POs, contracts) to prevent overspending before actual expenditure.
- **Grant lifecycle management**: Tracking grant awards, drawdowns, expenditures, compliance, and reporting.
- **Appropriations**: Legislative authorization to spend funds for specific purposes.

## Decision
Adopt a **Public Sector & Government Accounting** domain as an add-on to enable fund accounting, budgetary control, grants management, and appropriations tracking, integrating with core FI/GL and procurement modules while introducing public-sector-specific workflows and reporting.

## Scope
### Fund Accounting
- **Fund Types**: General fund, special revenue funds, capital projects funds, debt service funds, enterprise funds, internal service funds, trust funds, agency funds.
- **Fund-Based GL**: Chart of accounts structured by fund, department, program, project, grant.
- **Interfund Transactions**: Transfers, loans, reimbursements between funds.
- **Fund Balance Classification**: Nonspendable, restricted, committed, assigned, unassigned (GASB 54).

### Budgetary Control
- **Budget Preparation**: Multi-year budgeting, line-item budgets, budget amendments.
- **Budget vs Actual**: Real-time tracking at fund, department, program, line-item levels.
- **Budget Enforcement**: Hard stops (prevent transactions exceeding budget) or soft warnings.
- **Budget Transfers**: Reallocations between line items with approval workflows.
- **Budget Revisions**: Supplemental appropriations, budget cuts, continuing resolutions.

### Encumbrance Accounting
- **Pre-Encumbrances**: Requisitions (intent to purchase).
- **Encumbrances**: Purchase orders, contracts (commitment to spend).
- **Expenditures**: Actual invoices and payments.
- **Encumbrance Liquidation**: Reducing encumbrances when goods/services received.
- **Year-End Rollover**: Carrying forward outstanding encumbrances to next fiscal year.

### Grants Management
- **Grant Lifecycle**: Application, award, acceptance, setup, drawdown, expenditure, reporting, closeout.
- **Grant Master Data**: Grantor, grant number, award amount, period, allowable costs, matching requirements.
- **Cost Allocation**: Direct costs, indirect costs (F&A rates), cost pools.
- **Drawdown Management**: Requesting funds from grantor based on expenditures or advances.
- **Compliance Monitoring**: Unallowable costs, cost categories, time-and-effort reporting.
- **Grant Reporting**: SF-425 (FFR), progress reports, final financial reports.

### Appropriations Accounting
- **Appropriation Authorization**: Legislative approval to spend funds.
- **Apportionment**: OMB allocation of appropriations to agencies (federal level).
- **Allotments**: Agency allocation to departments/programs.
- **Commitments**: Obligations incurred (POs, contracts).
- **Obligations**: Legal commitments to pay.
- **Expenditures**: Actual payments.
- **USSGL Compliance**: U.S. Standard General Ledger for federal agencies.

## Core Capabilities

### Fund Accounting
- **Multi-Fund GL Structure**:
  - Fund segments in chart of accounts (Fund-Department-Program-Project-Account).
  - Fund-level financial statements (balance sheet by fund, statement of revenues/expenditures).
  - Interfund transaction recording (due to/from, transfers in/out).
  
- **Fund Balance Management**:
  - Classification per GASB 54 (nonspendable, restricted, committed, assigned, unassigned).
  - Fund balance policies (minimum reserve requirements).
  - Fund balance reporting in ACFR (Annual Comprehensive Financial Report).

### Budgetary Control
- **Budget Entry & Approval**:
  - Budget templates by fund, department, program.
  - Line-item budgets with justifications.
  - Budget approval workflows (department → finance → executive → legislative).
  
- **Budget Monitoring**:
  - Real-time budget vs actual dashboards.
  - Variance analysis (favorable/unfavorable).
  - Budget consumption alerts (e.g., 80% spent, 90% spent).
  
- **Budget Enforcement**:
  - Hard stops: Block PR/PO/invoice if exceeds available budget.
  - Soft warnings: Alert user but allow override with justification.
  - Available budget calculation: Original budget + amendments - encumbrances - expenditures.

### Encumbrance Processing
- **Encumbrance Lifecycle**:
  - Create pre-encumbrance from requisition (reserve budget).
  - Convert to encumbrance when PO issued.
  - Liquidate encumbrance when invoice received (replace with expenditure).
  - Release encumbrance if PO cancelled.
  
- **Encumbrance Reporting**:
  - Budget reports showing: Budget | Encumbrances | Expenditures | Available.
  - Open encumbrances report (outstanding POs/contracts).
  - Year-end encumbrance rollover.

### Grants Management
- **Grant Setup**:
  - Grant master record (grantor, award number, CFDA, amount, period, terms).
  - Cost categories and budget by category.
  - Match requirements (cash, in-kind).
  
- **Grant Expenditure Tracking**:
  - Charge expenditures to grants (personnel, supplies, travel, equipment).
  - Direct vs indirect cost allocation.
  - F&A rate application (facilities & administrative overhead).
  
- **Grant Drawdowns**:
  - Reimbursement-based: Request funds after expenditure.
  - Advance-based: Request funds upfront, reconcile later.
  - Drawdown schedules and grantor reporting.
  
- **Grant Compliance**:
  - Unallowable cost detection (alcohol, lobbying, entertainment per 2 CFR 200).
  - Time-and-effort reporting for personnel charges.
  - Single Audit (OMB A-133 / 2 CFR 200 Subpart F) compliance.

### Appropriations Accounting (Federal)
- **Appropriation Tracking**:
  - Record appropriations by fiscal year, Treasury Appropriation Fund Symbol (TAFS).
  - Track apportionments (OMB allocations).
  - Monitor obligations against appropriation authority.
  
- **USSGL Posting**:
  - Federal accounting standard accounts (1000-9999 range).
  - Budgetary and proprietary account integration.
  - SF-133 (Report on Budget Execution) and trial balance reporting.

## Data Model

### Core Entities
- `Fund`: fund code, fund type (general, special revenue, capital, debt service, enterprise, trust), description, fund balance classifications.
- `Budget`: fiscal year, fund, department, program, account, original amount, amendments, current budget.
- `BudgetAmendment`: amendment number, fund, account, amount, justification, approval status, effective date.
- `Encumbrance`: encumbrance number, type (pre-encumbrance, encumbrance), fund, account, amount, document reference (PR/PO number), status (open, liquidated, cancelled).
- `Grant`: grant number, grantor, CFDA number, award amount, start date, end date, match requirement, F&A rate, allowable cost categories.
- `GrantBudget`: grant number, cost category, budgeted amount, actual expenditures, available balance.
- `GrantDrawdown`: drawdown number, grant number, request date, request amount, payment date, payment amount, reconciliation status.
- `Appropriation`: appropriation number, TAFS (Treasury Appropriation Fund Symbol), fiscal year, appropriated amount, apportionments, allotments, obligations, expenditures.

### Relationships
- **Budget ↔ Encumbrance ↔ Expenditure**: Budget reduced by encumbrances (POs) then by expenditures (invoices).
- **Grant ↔ Expenditure**: Grant expenditures tracked by cost category, compared to grant budget.
- **Fund ↔ GL Account**: Chart of accounts includes fund segment for fund-based reporting.
- **Appropriation ↔ Obligation**: Appropriations consumed by obligations (contracts, POs).

## Integration Points

### Financial Accounting (ADR-009)
- **GL Posting**: Fund-based postings with fund segment in account code.
- **Financial Statements**: Fund-based statements (balance sheet by fund, revenues/expenditures by fund).
- **Interfund Transactions**: Transfers between funds posted as revenues/expenditures or operating transfers.

### Procurement (ADR-023)
- **Purchase Requisitions**: Create pre-encumbrances, check budget availability.
- **Purchase Orders**: Create encumbrances, reduce available budget.
- **Invoice Processing**: Liquidate encumbrances, post expenditures.

### Accounts Payable (ADR-009 FI-AP)
- **Invoice Approval**: Verify budget availability before approval.
- **Payment Processing**: Post expenditures to fund/account, reduce grant budgets if grant-funded.

### Payroll / HR (ADR-034)
- **Personnel Expenditures**: Charge salaries to funds, grants, programs.
- **Time-and-Effort Reporting**: Allocate employee time to grants for compliance.

### Reporting (ADR-016)
- **Budgetary Reports**: Budget vs actual, encumbrance reports, available budget.
- **Grant Reports**: Grant expenditure summary, drawdown status, compliance reports.
- **ACFR**: Annual Comprehensive Financial Report (GASB compliance).
- **Single Audit**: Schedule of Expenditures of Federal Awards (SEFA), A-133 audit package.

## Non-Functional Requirements

### Performance
- **Budget availability check**: < 500 ms per transaction (requisition, PO, invoice).
- **Budget vs actual report**: p95 < 10 seconds for fiscal year across all funds.
- **Grant expenditure query**: < 2 seconds for grant-to-date actuals.
- **Encumbrance rollover**: < 1 hour for year-end processing (10,000 open encumbrances).

### Compliance
- **GASB Standards**: GASB 34 (financial reporting), GASB 54 (fund balance), GASB 87 (leases).
- **Federal Regulations**: 2 CFR Part 200 (Uniform Guidance), OMB circulars, USSGL.
- **Audit Requirements**: Single Audit (2 CFR 200 Subpart F), A-133 compliance.

### Auditability
- **Budget audit trail**: All budget entries, amendments, transfers logged with user, timestamp, justification.
- **Encumbrance audit trail**: PR → PO → liquidation chain fully traceable.
- **Grant audit trail**: All grant expenditures with supporting documentation links.

## KPIs / SLOs

### Budget Management
- **Budget variance accuracy**: Calculated budget vs actual variance within 0.01% of manual calculation.
- **Budget enforcement effectiveness**: 100% of transactions blocked if hard stop enabled and budget exceeded.
- **Budget amendment cycle time**: Approval within 5 business days of submission.

### Encumbrance Management
- **Encumbrance accuracy**: 100% of POs create encumbrances automatically.
- **Encumbrance liquidation timeliness**: >= 95% liquidated within 2 business days of invoice receipt.
- **Year-end rollover accuracy**: 100% of open encumbrances rolled forward to next fiscal year.

### Grants Management
- **Grant expenditure accuracy**: 100% of grant charges to allowable cost categories.
- **Drawdown timeliness**: Drawdown requests submitted within 5 business days of expenditure threshold.
- **Grant compliance rate**: >= 99% of expenditures pass unallowable cost checks.
- **Grant reporting timeliness**: 100% of required reports submitted by deadline.

### Appropriations (Federal)
- **Appropriation utilization**: Track % of appropriation obligated by fiscal year.
- **Obligation accuracy**: 100% of obligations recorded within appropriation authority.
- **USSGL compliance**: 100% of postings use correct USSGL accounts.

## Alternatives Considered

### Use Commercial FI/GL with Projects
- **Rejected**: Projects don't enforce budgetary control at line-item level, no encumbrance accounting, no fund balance classifications.

### Third-Party Government Accounting Software
- **Rejected**: Weak integration with procurement, HR, payroll; requires double-entry for encumbrances.

### Manual Budget Tracking in Spreadsheets
- **Rejected**: Error-prone, no real-time budget availability, audit trail gaps.

## Consequences

### Positive
- Enables ChiroERP to serve government entities (federal, state, local), universities, non-profits.
- Provides budgetary control and accountability for public funds.
- Supports grant compliance and audit readiness (Single Audit, GASB, OMB).
- Differentiates from commercial ERPs lacking public sector capabilities.

### Negative / Risks
- **Complexity**: Fund accounting and encumbrances add significant workflow complexity vs commercial GL.
- **Niche Market**: Public sector is smaller market segment than commercial enterprises.
- **Regulatory Changes**: Frequent GASB/OMB regulation updates require ongoing maintenance.

### Neutral
- **Optional Add-on**: Not required for commercial customers; P3 priority reflects niche focus.
- **Federal vs State/Local**: USSGL appropriations accounting is federal-specific; state/local may not need.

## Compliance

### GASB Standards
- **GASB 34**: Financial reporting model (MD&A, fund statements, government-wide statements).
- **GASB 54**: Fund balance reporting and classification.
- **GASB 87**: Lease accounting for government entities.

### Federal Regulations
- **2 CFR Part 200**: Uniform Administrative Requirements, Cost Principles, and Audit Requirements (Uniform Guidance).
- **OMB Circular A-87**: Cost principles for state/local governments (superseded by 2 CFR 200).
- **OMB Circular A-133**: Single Audit requirements (superseded by 2 CFR 200 Subpart F).
- **USSGL**: U.S. Standard General Ledger for federal agencies.

### Audit Standards
- **Single Audit**: Required for entities expending >= $750K in federal awards annually.
- **Yellow Book**: Government Auditing Standards (GAO).

## Implementation Plan

### Phase 1: Fund Accounting & Budgetary Control (6 months)
- Fund-based chart of accounts and GL structure.
- Budget entry, amendments, and approval workflows.
- Budget vs actual reporting.
- Budget availability checks (hard/soft enforcement).

### Phase 2: Encumbrance Accounting (4 months)
- Pre-encumbrance creation from requisitions.
- Encumbrance creation from purchase orders.
- Encumbrance liquidation from invoices.
- Encumbrance reporting and year-end rollover.

### Phase 3: Grants Management (6 months)
- Grant master data and budget setup.
- Grant expenditure tracking and cost allocation.
- Drawdown management (reimbursement and advance).
- Grant compliance rules (unallowable costs, time-and-effort).

### Phase 4: Appropriations Accounting (Federal) (4 months)
- USSGL account mapping.
- Appropriation, apportionment, allotment tracking.
- Obligation vs appropriation monitoring.
- SF-133 and federal reporting.

### Phase 5: Reporting & Compliance (3 months)
- ACFR (Annual Comprehensive Financial Report) templates.
- Single Audit package (SEFA, data collection form).
- Grant reporting (SF-425, progress reports).
- Budget and encumbrance dashboards.

**Total Duration**: ~23 months (staggered phases, can prioritize based on customer segment).

## References

### Related ADRs
- ADR-009: Financial Accounting Domain (FI-GL, FI-AP, FI-AR)
- ADR-023: Procurement (MM-PUR)
- ADR-027: Master Data Governance
- ADR-034: HR Integration & Payroll Events
- ADR-016: Analytics & Reporting Architecture

### External References
- **GASB**: Governmental Accounting Standards Board ([www.gasb.org](https://www.gasb.org))
- **2 CFR Part 200**: Uniform Guidance ([ecfr.gov](https://www.ecfr.gov))
- **USSGL**: U.S. Standard General Ledger ([fiscal.treasury.gov](https://fiscal.treasury.gov))
- **OMB**: Office of Management and Budget circulars
- **GFOA**: Government Finance Officers Association best practices

### Industry Standards
- GASB 34 (Financial Reporting Model)
- GASB 54 (Fund Balance Reporting)
- 2 CFR Part 200 (Uniform Guidance)
- Single Audit Act
- Federal Acquisition Regulation (FAR)
