# ADR-051: Insurance Policy Administration & Claims Management (Add-on)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-03  
**Deciders**: Architecture Team, Operations Team  
**Priority**: P3 (Niche Market)  
**Tier**: Add-on  
**Tags**: insurance, policy-admin, claims, underwriting, actuarial, reinsurance

## Context
Insurance companies (property & casualty, life, health, reinsurance) require specialized policy administration, claims processing, underwriting, actuarial calculations, and reinsurance management—capabilities not present in standard commercial ERP systems.

Unlike general business operations, insurance requires:
- **Policy Lifecycle Management**: Quoting, rating, binding, endorsements, renewals, cancellations.
- **Claims Processing**: First Notice of Loss (FNOL), claim assignment, adjudication, reserves, settlements.
- **Underwriting Workflow**: Risk assessment, pricing, approval, policy issuance.
- **Actuarial Calculations**: Loss reserves, IBNR (Incurred But Not Reported), premium calculations, loss ratios.
- **Reinsurance Management**: Treaty and facultative reinsurance, cessions, recoveries.
- **Regulatory Reporting**: Statutory filings (NAIC, SAP), solvency monitoring, rate filings.

## Decision
Adopt an **Insurance Policy Administration & Claims Management** domain as an add-on to enable policy lifecycle, claims processing, underwriting, actuarial functions, and reinsurance tracking, integrating with core FI/AR for premium billing and claims payables.

## Scope
### Policy Administration
- **Policy Types**: Property (homeowners, commercial property), casualty (auto, GL, WC), life, health, specialty lines.
- **Policy Lifecycle**: Quote, bind, issue, endorse, renew, cancel, non-renew.
- **Rating Engine**: Premium calculation based on risk factors, coverages, deductibles, limits.
- **Endorsements**: Mid-term changes (coverage additions, address changes, driver changes).
- **Renewals**: Automated renewal quotes, manual underwriting review.

### Claims Management
- **Claims Lifecycle**: FNOL, triage, assignment, investigation, adjudication, settlement, closure.
- **Reserves**: Case reserves (adjuster estimate), IBNR (actuarial estimate), bulk reserves.
- **Payments**: Claimant payments, vendor payments (repair shops, medical providers), defense costs.
- **Subrogation**: Recovery from third parties.
- **Litigation Management**: Legal case tracking, defense attorney management.

### Underwriting
- **Submission Intake**: Quote requests, risk information gathering.
- **Risk Assessment**: Underwriting rules, risk scoring, third-party data (MVR, CLUE, credit).
- **Pricing**: Rate manual application, territory/class factors, credits/surcharges.
- **Approval Workflow**: Referrals to senior underwriters, declinations, counter-offers.
- **Policy Issuance**: Binding authority, policy documents, declaration pages.

### Actuarial Functions
- **Loss Reserving**: Case reserves, IBNR (chain ladder, Bornhuetter-Ferguson), bulk reserves.
- **Premium Calculations**: Rate manual, loss cost models, expense loading, profit margin.
- **Loss Ratio Analysis**: Incurred vs earned premium, pure premium, frequency/severity trends.
- **Experience Rating**: Retrospective rating, dividend calculations, mod factors.

### Reinsurance
- **Treaty Reinsurance**: Quota share, surplus share, excess of loss, aggregate stop-loss.
- **Facultative Reinsurance**: Certificate management, case-by-case cessions.
- **Cessions**: Premium ceded to reinsurers, commission income.
- **Recoveries**: Reinsurance recoveries on paid claims, reserve credits.
- **Reinsurer Accounting**: Bordereaux reporting, cash calls, settlements.

## Core Capabilities

### Policy Administration
- **Quote Management**:
  - Quote creation with risk details (property address, vehicle VIN, driver info, etc.).
  - Coverage selection (A, B, C, D for auto; dwelling, liability for homeowners).
  - Deductible and limit options.
  - Premium calculation via rating engine.
  - Quote comparison (quote vs bind, renewal vs prior term).
  
- **Policy Binding & Issuance**:
  - Bind quote into in-force policy.
  - Generate policy number, effective date, expiration date.
  - Issue policy documents (declarations page, jacket, endorsements).
  - Premium invoice generation (full-pay, installment plans).
  
- **Endorsements**:
  - Mid-term changes (add driver, replace vehicle, increase limits).
  - Pro-rata or short-rate premium adjustments.
  - Audit trail of policy changes.
  
- **Renewals & Cancellations**:
  - Automated renewal quotes (re-rate with updated risk data).
  - Manual underwriting review for complex risks.
  - Cancellation processing (insured request, non-payment, underwriting).
  - Earned vs unearned premium calculations.

### Claims Management
- **FNOL (First Notice of Loss)**:
  - Claim intake (phone, web, mobile app).
  - Loss details (date, location, cause, parties involved).
  - Coverage verification (policy in force, covered peril).
  - Claim number assignment.
  
- **Claim Assignment**:
  - Auto-assignment rules (claim type, complexity, geography, adjuster workload).
  - Adjuster skills matching (auto physical damage, liability, property, injury).
  
- **Investigation & Adjudication**:
  - Adjuster notes, photos, estimates.
  - Third-party reports (police reports, medical records, repair estimates).
  - Coverage determination (covered vs denied).
  - Reserve establishment (case reserve by coverage).
  
- **Payments**:
  - Claimant payments (settlement, reimbursement).
  - Vendor payments (repair shops, medical providers, rental cars).
  - Defense costs (attorney fees, expert witnesses).
  - Payment approval workflows (authority limits, supervisor approval).
  
- **Subrogation**:
  - Subrogation opportunity identification (third-party fault).
  - Recovery tracking (demand letters, settlements, arbitration).
  - Subrogo recovery posting (reduce claim costs).

### Underwriting Workflow
- **Submission Management**:
  - Quote request intake (agent portal, direct).
  - Risk information gathering (property inspection, loss history, MVR).
  - Third-party data integration (LexisNexis, ISO, credit bureaus).
  
- **Underwriting Rules Engine**:
  - Eligibility rules (age, prior claims, credit score, construction type).
  - Referral rules (high value, unusual risks, prior declinations).
  - Auto-approval vs manual review.
  
- **Pricing & Rating**:
  - Base rate lookup (territory, class, coverage).
  - Risk factor adjustments (age, claims history, credit, safety features).
  - Discounts and surcharges (multi-policy, claims-free, DUI).
  - Final premium calculation.
  
- **Approval & Declination**:
  - Underwriter review and approval.
  - Declination letters with reasons.
  - Counter-offers (higher deductibles, exclusions).

### Actuarial Calculations
- **Loss Reserving**:
  - Case reserves: Adjuster estimates by claim and coverage.
  - IBNR reserves: Actuarial models (chain ladder, Bornhuetter-Ferguson, expected loss ratio).
  - Bulk reserves: Aggregate reserves for small claims.
  - Reserve adequacy testing and adjustments.
  
- **Premium Rating**:
  - Pure premium (expected losses per exposure).
  - Expense loading (commissions, overhead, premium tax).
  - Profit and contingency margin.
  - Rate manual development and filing.
  
- **Loss Ratio Analysis**:
  - Incurred losses (paid + reserves) / earned premium.
  - Ultimate loss projections (development triangles).
  - Frequency and severity trending.

### Reinsurance Management
- **Treaty Administration**:
  - Treaty terms (reinsurer, coverage type, retention, limit, commission).
  - Automatic cessions per treaty terms.
  - Premium ceded calculations (quota share %, surplus share layers).
  - Ceding commission income.
  
- **Facultative Certificates**:
  - Case-by-case reinsurance placement.
  - Certificate tracking (reinsurer, terms, premium).
  - Claim notification to facultative reinsurers.
  
- **Reinsurance Recoveries**:
  - Recovery calculations (excess of loss, quota share).
  - Recovery posting (reduce net claim costs).
  - Reinsurer collectibility monitoring (A.M. Best rating).
  
- **Bordereaux Reporting**:
  - Monthly/quarterly reports to reinsurers (premiums, claims, reserves).
  - Cash settlements with reinsurers.

## Data Model

### Core Entities
- `Policy`: policy number, product line, effective date, expiration date, insured, premium, status (quote, bound, in-force, cancelled, expired).
- `Coverage`: policy number, coverage code (A/B/C/D for auto, dwelling/liability for property), limit, deductible, premium.
- `Insured`: name, DOB, address, credit score, loss history.
- `RiskObject`: property address/construction, vehicle VIN/make/model, driver license/MVR.
- `Premium`: policy number, term premium, installments, earned premium (daily accrual), unearned premium.
- `Claim`: claim number, policy number, loss date, reported date, claim type (property, liability, injury), status (open, closed, reopened).
- `ClaimReserve`: claim number, coverage, case reserve, IBNR reserve, total incurred.
- `ClaimPayment`: claim number, payee, payment type (indemnity, expense, subrogation recovery), amount, check number, payment date.
- `Underwriting`: submission number, quote number, risk score, referral reasons, underwriter, decision (approve, decline, counter-offer).
- `ReinsuranceTreaty`: treaty number, reinsurer, treaty type (quota share, excess of loss), terms (retention, limit, commission).
- `Cession`: policy/claim number, treaty number, ceded premium, ceded loss, recovery amount.

### Relationships
- **Policy → Coverage**: One policy, multiple coverages.
- **Policy → Premium**: Premium calculation and billing schedule.
- **Claim → Policy**: Claim references policy for coverage verification.
- **Claim → Reserves → Payments**: Reserves established, paid claims reduce reserves.
- **Policy → Cession**: Premium ceded to reinsurers per treaty terms.
- **Claim → Recovery**: Reinsurance recoveries reduce net claim costs.

## Integration Points

### Financial Accounting (ADR-009)
- **Premium Billing (FI-AR)**: Policy premium invoices, installment payments, cancellations/refunds.
- **Claims Payable (FI-AP)**: Claim payments to claimants and vendors.
- **Reinsurance Accounting**: Ceded premium, ceding commission, reinsurance recoveries.
- **Statutory Accounting**: Unearned premium reserve, loss and LAE reserves, reinsurance recoverables.

### Sales / CRM (ADR-025, ADR-043)
- **Agent Commission**: Commission calculations on premium (new, renewal, endorsement).
- **Customer Management**: Insured master data, policy history, claims history.

### Reporting / Analytics (ADR-016)
- **Loss Ratios**: Incurred losses / earned premium by product, territory, underwriter.
- **Claims Analytics**: Frequency, severity, cycle time, reserve adequacy.
- **Premium Analytics**: Written, earned, unearned premium by period.
- **Reinsurance Analytics**: Ceded vs net retained premium and losses.

### Compliance / Regulatory
- **Rate Filings**: Submit rates to state DOI (Department of Insurance) for approval.
- **Statutory Reporting**: Annual Statement (NAIC), Quarterly Statement, RBC (Risk-Based Capital).
- **SAP (Statutory Accounting Principles)**: Unearned premium reserve, loss reserve discount, admitted assets.

## Non-Functional Requirements

### Performance
- **Quote generation**: p95 < 3 seconds (rating engine execution).
- **Policy issuance**: Document generation < 10 seconds.
- **Claim assignment**: Auto-assignment within 5 minutes of FNOL.
- **Reserve calculation**: IBNR reserve run < 2 hours for 100K open claims.
- **Reinsurance recovery**: Calculation within 1 business day of claim payment.

### Compliance
- **Regulatory**: State insurance department compliance (rate filings, form approvals, market conduct).
- **NAIC Standards**: Annual Statement compliance, statutory accounting principles.
- **Privacy**: GLBA (Gramm-Leach-Bliley Act), state insurance data privacy laws.

### Auditability
- **Policy audit trail**: All policy changes (endorsements, cancellations) logged with user, timestamp, reason.
- **Claims audit trail**: FNOL → investigation → payment chain fully traceable.
- **Reserve audit trail**: Reserve changes with adjuster/actuary notes and justification.

## KPIs / SLOs

### Policy Administration
- **Quote conversion rate**: % of quotes that bind (target: 25-40% depending on line).
- **Policy retention rate**: % of policies renewing (target: >= 85%).
- **Quote turnaround time**: Time from submission to quote (target: < 24 hours for standard risks).
- **Endorsement processing time**: < 2 business days for mid-term changes.

### Claims Management
- **FNOL to assignment**: < 1 hour for high-severity claims, < 4 hours for routine.
- **First contact with insured**: Within 24 hours of FNOL.
- **Claim cycle time**: Median days from FNOL to closure (target: property 30 days, auto 45 days, liability 180 days).
- **Reserve adequacy**: Favorable vs adverse development ratio (target: 0.95-1.05).
- **Customer satisfaction**: CSAT score >= 4.0 / 5.0 for claims experience.

### Underwriting
- **Underwriting hit ratio**: % of quotes that meet target loss ratio (target: >= 80%).
- **Referral rate**: % of submissions requiring manual review (lower is better for efficiency).
- **New business growth**: Written premium growth year-over-year.

### Actuarial
- **Loss ratio**: Incurred losses / earned premium (target: < 65% for profitability).
- **Combined ratio**: (Losses + expenses) / earned premium (target: < 100%).
- **Reserve accuracy**: Actual ultimate losses within 5% of reserved amounts.

### Reinsurance
- **Cession accuracy**: 100% of cessions calculated per treaty terms.
- **Recovery timeliness**: Reinsurance recoveries collected within 60 days of claim payment.
- **Reinsurer credit quality**: >= 95% of recoverable from A- or better rated reinsurers.

## Alternatives Considered

### Use Third-Party Policy Administration System (PAS)
- **Rejected**: Weak integration with ERP finance, duplicate data entry for premium billing and claims payables.

### Build Policy Admin in CRM (ADR-043)
- **Rejected**: CRM not designed for complex rating, reserves, reinsurance; actuarial calculations not feasible.

### Excel-Based Policy & Claims Tracking
- **Rejected**: Not scalable, no workflow automation, no audit trail, high error risk.

## Consequences

### Positive
- Enables ChiroERP to serve property & casualty, life, and health insurers.
- Provides end-to-end policy lifecycle and claims processing with ERP financial integration.
- Supports actuarial functions (loss reserves, IBNR) and reinsurance accounting.
- Differentiates from general ERPs lacking insurance-specific capabilities.

### Negative / Risks
- **Complexity**: Insurance domain requires deep expertise (rating, reserves, reinsurance).
- **Niche Market**: Insurance is smaller market segment than general commercial enterprises.
- **Regulatory Complexity**: State-by-state rate filings, form approvals, and compliance requirements.
- **Competition**: Established insurance platforms (Duck Creek, Guidewire, Majesco) have strong market presence.

### Neutral
- **Optional Add-on**: Not required for non-insurance customers; P3 priority reflects niche focus.
- **Product Line Focus**: Initial implementation may focus on P&C (property & casualty); life and health can be phased.

## Compliance

### Regulatory Requirements
- **State Insurance Departments**: Rate and form filings, market conduct compliance.
- **NAIC (National Association of Insurance Commissioners)**: Annual Statement, RBC, solvency monitoring.
- **SAP (Statutory Accounting Principles)**: SSAP standards for asset valuation, reserve requirements.
- **GLBA (Gramm-Leach-Bliley Act)**: Privacy and data security for consumer information.

### Rating Agency Requirements
- **A.M. Best**: Financial strength rating based on reserves, reinsurance, capital adequacy.
- **S&P, Moody's, Fitch**: Credit ratings for insurance companies issuing debt or seeking reinsurance.

## Implementation Plan

### Phase 1: Policy Administration (8 months)
- Policy master data and lifecycle (quote, bind, endorse, renew, cancel).
- Rating engine integration (rate manual, factor tables).
- Premium billing and installment plans.
- Agent commission calculations.

### Phase 2: Claims Management (8 months)
- FNOL intake and claim creation.
- Claim assignment and adjuster workflows.
- Reserve establishment (case reserves).
- Claim payments and approval workflows.

### Phase 3: Underwriting (5 months)
- Submission intake and risk data gathering.
- Underwriting rules engine (eligibility, referrals, auto-approval).
- Third-party data integration (MVR, CLUE, credit).
- Approval and declination workflows.

### Phase 4: Actuarial Functions (6 months)
- Loss reserving models (IBNR, chain ladder, Bornhuetter-Ferguson).
- Premium rate manual and loss cost models.
- Loss ratio and combined ratio reporting.
- Experience rating and dividend calculations.

### Phase 5: Reinsurance Management (5 months)
- Treaty administration and automatic cessions.
- Facultative certificate tracking.
- Reinsurance recoveries on paid claims.
- Bordereaux reporting to reinsurers.

### Phase 6: Regulatory Reporting (4 months)
- NAIC Annual Statement (blue book) templates.
- Quarterly statement and RBC calculations.
- State-specific rate and form filing workflows.
- SAP (Statutory Accounting Principles) compliance.

**Total Duration**: ~36 months (staggered phases, can prioritize based on product line: P&C first, then life/health).

## References

### Related ADRs
- ADR-009: Financial Accounting Domain (FI-GL, FI-AP, FI-AR)
- ADR-025: Sales & Distribution
- ADR-043: CRM & Customer Management
- ADR-027: Master Data Governance
- ADR-016: Analytics & Reporting Architecture

### External References
- **NAIC**: National Association of Insurance Commissioners ([www.naic.org](https://www.naic.org))
- **SAP (Statutory Accounting Principles)**: SSAP standards
- **A.M. Best**: Insurance rating agency ([www.ambest.com](https://www.ambest.com))
- **ISO**: Insurance Services Office (loss costs, policy forms)
- **ACORD**: Insurance data standards (ACORD forms, XML messages)

### Industry Standards
- NAIC Annual Statement (statutory financial reporting)
- ACORD data standards (policy and claims data exchange)
- IFRS 17 (international insurance accounting standard)
- US GAAP ASC 944 (insurance accounting)
- Risk-Based Capital (RBC) formula
