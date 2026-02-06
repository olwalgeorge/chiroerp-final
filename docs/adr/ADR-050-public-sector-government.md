# ADR-050: Public Sector & Government Accounting (Add-on)

**Status**: Accepted (Implementation Starting Q2 2027)
**Date**: 2026-02-06 (Updated)
**Deciders**: Architecture Team, Finance Team
**Investment**: $1.15M-$1.58M (first year)
**Timeline**: Q2-Q4 2027 (32 weeks, 4 phases)
**Priority**: P2 (Enhancement - Strategic Vertical)
**Tier**: Add-on
**Tags**: public-sector, fund-accounting, grants, budgetary-control, government, non-profit, gasb, encumbrance, appropriations

## Context

### Problem Statement
Public sector organizations (federal/state/local governments, municipalities, universities, non-profits) represent a **$500B+ annual government spending market** in the US alone, yet lack modern ERP platforms that combine world-class government accounting (fund accounting, encumbrances, grants, GASB compliance) with commercial ERP capabilities (procurement, HR, inventory, asset management). Legacy government systems (Tyler Munis, CGI Advantage, Oracle PeopleSoft Financials for Government) are 15-20 years old with poor user experience, weak integration, and expensive customization.

### Market Requirements
Public sector entities operate under fundamentally different accounting principles than commercial enterprises:
- **Fund-based accounting**: Segregating resources by purpose/restriction rather than profit centers (GASB 54 fund balance classifications)
- **Budgetary control**: Enforcing spending limits at line-item level with real-time budget vs actual
- **Encumbrance accounting**: Recording commitments (POs, contracts) to prevent overspending **before** actual expenditure
- **Grant lifecycle management**: Tracking federal/state grant awards, drawdowns, expenditures, compliance (2 CFR Part 200, Single Audit)
- **Appropriations accounting**: Legislative authorization to spend funds for specific purposes (federal USSGL)

**Target Segments**:
- **State/Local Governments**: 50 US states, 3,000+ counties, 19,000+ municipalities ($2.3T spending 2024)
- **Federal Agencies**: 430+ agencies, $6.8T federal budget FY2024 (USSGL compliance)
- **Higher Education**: 4,000+ colleges/universities, $700B revenue (fund accounting, grants)
- **Non-Profits**: 1.5M+ tax-exempt organizations, $2T revenue (fund restrictions, donor tracking)
- **K-12 School Districts**: 13,000+ districts, $730B spending (GASB 34, fund accounting)

### Current Gaps
ChiroERP lacks public sector-specific capabilities:
- ❌ **No fund accounting**: Cannot segregate resources by fund type (general, special revenue, capital projects, debt service, enterprise, trust)
- ❌ **No budgetary control**: No real-time budget vs actual enforcement at line-item level
- ❌ **No encumbrance accounting**: Cannot record PO commitments before expenditure (causes budget overruns)
- ❌ **No grants management**: No grant awards tracking, drawdowns, cost allocation, Single Audit compliance
- ❌ **No appropriations tracking**: Cannot track federal appropriations, apportionments, allotments, obligations (USSGL)
- ❌ **No GASB reporting**: Missing government-wide financial statements, fund statements, MD&A, ACFR

### Competitive Reality

| Capability | Tyler Munis | Oracle Gov | CGI Advantage | SAP Public Sector | ChiroERP (Today) |
|------------|-------------|------------|---------------|-------------------|------------------|
| Fund Accounting | ✅ Full | ✅ Full | ✅ Full | ✅ Full | ❌ None |
| Budgetary Control | ✅ Full | ✅ Full | ✅ Full | ✅ Full | ❌ None |
| Encumbrance Accounting | ✅ Full | ✅ Full | ✅ Full | ✅ Full | ❌ None |
| Grants Management | ✅ Good | ✅ Full | ✅ Good | ✅ Full | ❌ None |
| Appropriations (USSGL) | ❌ Limited | ✅ Full | ✅ Full | ✅ Full | ❌ None |
| GASB 34/54 Reporting | ✅ Full | ✅ Full | ✅ Full | ✅ Full | ❌ None |
| Single Audit (2 CFR 200) | ✅ Good | ✅ Full | ✅ Full | ✅ Full | ❌ None |
| Modern UX | ❌ Legacy | ❌ Legacy | ❌ Legacy | ⚠️ Dated | ✅ Modern |
| Cloud-Native | ⚠️ Hybrid | ⚠️ Hybrid | ❌ On-prem | ⚠️ Hybrid | ✅ Full |
| Procurement Integration | ✅ Good | ✅ Full | ✅ Good | ✅ Full | ✅ Full (ADR-023) |

**Market Opportunity**: Public sector customers spend 18-25 months evaluating ERP replacements, value modern UX and cloud-native architecture, but **cannot** select ChiroERP without fund accounting and encumbrance capabilities.

### Customer Quote
> *"We're a mid-size county government (500K population, 2,500 employees, $850M annual budget). Our 1998-vintage financial system requires double-entry for encumbrances, has no real-time budget checks (we overspend then scramble for budget amendments), and Single Audit prep takes 6 weeks of manual Excel work. We need fund-based accounting with GASB 34/54 compliance, real-time encumbrances preventing budget overruns, grant tracking for 40+ federal awards ($120M), and modern procurement integration. Tyler Munis works but feels like Windows 95. We'd love a modern cloud ERP **if** it has fund accounting and encumbrances."*
>
> — CFO, County Government (550K population, $850M budget, 40+ federal grants)

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

---

## World-Class Implementation Plan (February 2026 Update)

### Implementation Roadmap

**Phase 1: Fund Accounting & Budgetary Control (Q2 2027 - 10 weeks)**
- Week 1-3: Fund-based chart of accounts (Fund-Department-Program-Project-Account structure)
- Week 4-6: Budget entry, amendments, approval workflows (department → finance → executive → legislative)
- Week 7-8: Budget vs actual reporting (real-time dashboards, variance analysis)
- Week 9-10: Budget availability checks (hard stop: block transactions if budget exceeded; soft warning: alert with override)

**Phase 2: Encumbrance Accounting (Q3 2027 - 8 weeks)**
- Week 11-13: Pre-encumbrance creation from requisitions (reserve budget at PR stage)
- Week 14-16: Encumbrance creation from purchase orders (commitment to spend)
- Week 17-18: Encumbrance liquidation from invoices (replace encumbrance with expenditure)
- Week 19-20: Encumbrance reporting and year-end rollover (carry forward outstanding POs)

**Phase 3: Grants Management (Q3 2027 - 10 weeks)**
- Week 21-23: Grant master data and budget setup (grantor, CFDA, award amount, cost categories)
- Week 24-26: Grant expenditure tracking and cost allocation (direct, indirect, F&A rates)
- Week 27-28: Drawdown management (reimbursement-based, advance-based)
- Week 29-30: Grant compliance rules (unallowable costs per 2 CFR 200.420-475, time-and-effort reporting)

**Phase 4: Appropriations & Reporting (Q4 2027 - 6 weeks)**
- Week 31-32: USSGL account mapping (1000-9999 federal accounts)
- Week 33-34: Appropriation, apportionment, allotment tracking (TAFS, OMB apportionments)
- Week 35-36: ACFR templates (GASB 34 government-wide statements, fund statements, MD&A), Single Audit package (SEFA, data collection form)

### Cost Estimate

**Total Investment**: **$1.15M-$1.58M** (first year)

**Development Costs**: $1.05M-$1.45M
- Backend developers: 3 × 8 months @ $120K-160K = $360K-480K
- Government accounting specialist: 1 × 8 months @ $140K-180K = $140K-180K (GASB/USSGL expertise)
- Frontend developer: 1 × 6 months @ $100K-130K = $100K-130K
- Testing/QA: 1 × 6 months @ $100K-125K = $100K-125K
- Tech lead (25% allocation): 8 months @ $62.5K-100K = $62.5K-100K
- DevOps (15% allocation): 8 months @ $37.5K-56K = $37.5K-56K
- Documentation: 1 × 3 months @ $50K-75K = $50K-75K
- Compliance consultant (GASB/OMB): 4 months @ $200K-300K/year = $67K-100K

**Infrastructure Costs**: $50K-80K
- Budget monitoring service: $15K-25K/year
- Grant compliance database: $10K-15K/year
- ACFR reporting tools: $15K-25K/year
- Single Audit software: $10K-15K/year

**Training & Certification**: $50K
- Government accounting training (GASB, OMB, USSGL): $30K
- Government Finance Officers Association (GFOA) certification: $20K

### Success Metrics

**Business KPIs**:
- ✅ Public sector customers: 25+ by end 2028 (state/local governments, universities, non-profits)
- ✅ Public sector revenue: $8M+ ARR (average $320K per customer)
- ✅ Market share: 2% of US government ERP market ($16B) by 2028
- ✅ Customer retention: >90% (public sector has high switching costs, values stability)

**Budget Management KPIs**:
- ✅ Budget variance accuracy: <0.01% vs manual calculation (real-time budget vs actual)
- ✅ Budget overrun prevention: 100% of transactions blocked when hard stop enabled and budget exceeded
- ✅ Budget amendment cycle time: <5 business days (approval workflow automation)
- ✅ Budget monitoring time: -70% (from 40 hours/month Excel to 12 hours/month dashboards)

**Encumbrance KPIs**:
- ✅ Encumbrance accuracy: 100% of POs create encumbrances automatically
- ✅ Encumbrance liquidation timeliness: >95% liquidated within 2 business days of invoice receipt
- ✅ Year-end rollover accuracy: 100% of open encumbrances rolled forward to next fiscal year
- ✅ Budget overrun reduction: -85% (from 12% of budget lines overrun to <2% with encumbrances)

**Grants Management KPIs**:
- ✅ Grant expenditure accuracy: 100% charges to allowable cost categories (automated unallowable cost checks)
- ✅ Drawdown timeliness: <5 business days from expenditure threshold to drawdown request
- ✅ Grant compliance rate: >99% of expenditures pass 2 CFR Part 200 checks
- ✅ Single Audit prep time: -60% (from 6 weeks Excel to 2.5 weeks with SEFA automation)

**Compliance KPIs**:
- ✅ GASB 34/54 compliance: 100% (government-wide statements, fund statements, MD&A, fund balance classifications)
- ✅ Single Audit compliance: 100% for entities with >$750K federal awards (2 CFR 200 Subpart F)
- ✅ USSGL compliance: 100% for federal agencies (correct account codes, trial balance, SF-133)
- ✅ Audit findings: <3 per year (down from 8-12 with legacy systems)

### Integration with Other ADRs

- **ADR-009**: Financial Accounting (fund-based GL postings, interfund transactions, statutory financial statements)
- **ADR-023**: Procurement (requisitions create pre-encumbrances, POs create encumbrances, budget checks)
- **ADR-034**: HR & Payroll (personnel expenditures to funds/grants, time-and-effort reporting for grants)
- **ADR-027**: Master Data Governance (fund codes, grant codes, appropriation codes)
- **ADR-016**: Analytics & Reporting (budget vs actual dashboards, grant reports, ACFR, SEFA)
- **ADR-058**: SOC 2 (audit trails for budget changes, encumbrances, grant expenditures)

### Competitive Parity Achieved

| Capability | Tyler Munis | Oracle Gov | SAP Public Sector | ChiroERP Public (ADR-050) |
|------------|-------------|------------|-------------------|---------------------------|
| Fund Accounting | ✅ Full | ✅ Full | ✅ Full | ✅ **Full** (GASB 54 fund types, interfund transactions) |
| Budgetary Control | ✅ Full | ✅ Full | ✅ Full | ✅ **Real-time** (hard stop, soft warning, <500ms check) |
| Encumbrance Accounting | ✅ Full | ✅ Full | ✅ Full | ✅ **Full** (PR → PO → Invoice lifecycle, year-end rollover) |
| Grants Management | ✅ Good | ✅ Full | ✅ Full | ✅ **Full** (2 CFR 200, SEFA, time-and-effort) |
| Appropriations (USSGL) | ❌ Limited | ✅ Full | ✅ Full | ✅ **Full** (TAFS, apportionments, SF-133) |
| GASB 34/54 Reporting | ✅ Full | ✅ Full | ✅ Full | ✅ **Full** (government-wide, fund statements, MD&A) |
| Single Audit | ✅ Good | ✅ Full | ✅ Full | ✅ **Automated** (SEFA generation, unallowable cost checks) |
| Modern UX | ❌ Legacy | ❌ Legacy | ⚠️ Dated | ✅ **World-class** (React, mobile-responsive) |
| Cloud-Native | ⚠️ Hybrid | ⚠️ Hybrid | ⚠️ Hybrid | ✅ **100%** (Kubernetes, multi-tenant) |

**Target Rating**: 9/10 (world-class public sector ERP)

---
