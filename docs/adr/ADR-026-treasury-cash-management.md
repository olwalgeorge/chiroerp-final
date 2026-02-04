# ADR-026: Treasury & Cash Management (TR-CM)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-01
**Deciders**: Architecture Team, Finance Team
**Priority**: P2 (Medium)
**Tier**: Advanced
**Tags**: treasury, cash-management, banking, liquidity, payments

## Context
Treasury functions manage liquidity, bank integrations, and cash risk. A SAP-grade ERP requires bank account management, automated bank statement processing, payment controls, cash forecasting, and FX exposure tracking with strong auditability.

## Decision
Implement a **Treasury & Cash Management** capability covering cash positioning, bank statement import, payment controls, and liquidity forecasting, integrated with AP/AR and GL.

### Scope
- Bank account master data and signatory governance.
- Bank statement ingestion and reconciliation.
- Payment initiation, approval, and file generation.
- Cash positioning, liquidity forecasting, and FX exposure.

### Core Capabilities
- **Bank account management**: accounts, signatories, limits, and approval workflows.
- **Bank statement ingestion**: MT940/MT942, BAI2, ISO 20022 CAMT, CSV.
- **Bank connectivity**: SWIFT/EBICS/SFTP integration patterns with key rotation.
- **Reconciliation**: automated matching rules with manual exceptions.
- **Payment runs**: ACH, SEPA, wire, check with dual approval.
- **Cash positioning**: daily cash position by entity and currency.
- **Liquidity forecasting**: short-term (daily) and medium-term (weekly/monthly).
- **FX exposure**: realized/unrealized gains, revaluation.

### Data Model (Conceptual)
- `BankAccount`, `BankStatement`, `CashPosition`, `PaymentBatch`, `PaymentItem`, `ReconciliationRule`, `FXRate`.

### Key Workflows
- **Bank statement to GL**: import -> match -> post -> exceptions.
- **Payment run**: select invoices -> approval -> file generation -> bank confirmation.
- **Cash forecast**: AR/AP pipeline -> forecast -> variance analysis.

### Integration Points
- **Finance/GL**: cash account postings, FX revaluations.
- **AP/AR**: payments and collections.
- **Integration layer**: bank file formats and APIs.
- **Authorization/SoD**: dual control for payments and bank changes.

### Non-Functional Constraints
- **Security**: encryption of bank files and credentials.
- **Auditability**: full trace of approvals and file transmissions.
- **Latency**: daily cash position available by 8 AM local time.

### KPIs and SLOs
- **FX revaluation completion**: by 06:00 local time (before cash position window).
- **Payment file generation**: p95 < 5 minutes for 1,000 payment items.
- **Bank statement import**: < 15 minutes after file receipt.
- **Reconciliation auto-match rate**: >= 95% for standard bank statement feeds.

## Alternatives Considered
- **Manual bank reconciliation**: rejected (high risk and error-prone).
- **External treasury system only**: rejected (integration complexity).
- **Simple cash balance reports**: rejected (no forecasting or reconciliation).

## Consequences
### Positive
- Improved liquidity visibility and control.
- Automated reconciliation reduces close effort.
- Strong payment control with dual approval.

### Negative
- Bank integrations vary by region and require ongoing support.
- Payment file testing with banks can be time-consuming.

### Neutral
- Advanced treasury (hedging, cash pooling) can be phased later.

---

## Advanced Treasury Extension (Banking & Finance Industry)

### Context
Banks, financial institutions, and large multinational corporations require advanced treasury capabilities beyond basic cash management: derivatives trading, hedge accounting, cash pooling structures, investment portfolio management, and debt/securities management.

### Scope Extension
- **Derivatives Management**: Forwards, futures, options, swaps (FX, interest rate, commodity).
- **Hedge Accounting**: Effectiveness testing, cash flow hedges, fair value hedges (IFRS 9 / ASC 815).
- **Cash Pooling**: Physical pooling, notional pooling, zero-balancing, target-balancing.
- **Investment Management**: Money market funds, bonds, equities, portfolio tracking, mark-to-market.
- **Securities Trading**: Trade execution, settlement (T+2), counterparty management.
- **Debt Management**: Bond issuance, loan tracking, covenant monitoring, repayment schedules.

### Additional Capabilities
- **Derivative Instruments**:
  - Contract management (counterparties, terms, notional amounts, strike prices).
  - Valuation models (Black-Scholes for options, discounted cash flow for swaps).
  - Settlement and margin calls.

- **Hedge Accounting**:
  - Hedge designation and documentation.
  - Effectiveness testing (dollar-offset, regression analysis).
  - Hedge reserve tracking (OCI for cash flow hedges).
  - Hedge ineffectiveness recognition in P&L.

- **Cash Pooling**:
  - Multi-entity structures (header accounts, participant accounts).
  - Interest allocation and netting arrangements.
  - Automated sweeping and concentration.

- **Investment Portfolio**:
  - Security master data (ISIN, CUSIP, ratings).
  - Acquisition, accrual, valuation, disposal.
  - Dividend/coupon income recognition.
  - Portfolio performance analytics (YTM, duration, convexity).

- **Debt & Securities**:
  - Issuance tracking (bonds, commercial paper, syndicated loans).
  - Amortized cost vs fair value accounting.
  - Covenant compliance monitoring.

### Data Model Extensions
- `DerivativeContract`: instrument type, counterparty, notional, maturity, terms.
- `HedgeRelationship`: hedged item, hedging instrument, hedge type, effectiveness test results.
- `CashPoolStructure`: header entity, participants, sweeping rules, interest allocation.
- `InvestmentSecurity`: ISIN, security type, acquisition cost, current market value, accrued interest.
- `DebtInstrument`: instrument type, principal, coupon rate, maturity, covenants.

### Integration Points
- **FI/GL**: Hedge accounting postings, fair value adjustments, interest accruals.
- **Controlling/CO**: Investment income allocation, derivative P&L attribution.
- **Compliance**: IFRS 9 / ASC 815 hedge accounting, EMIR/Dodd-Frank reporting.
- **Market Data**: Real-time FX rates, interest rate curves, security prices.

### KPIs / SLOs
- **Derivative valuation**: Daily mark-to-market by 08:00 local time.
- **Hedge effectiveness**: Testing completion within 3 business days of month-end.
- **Cash pooling**: Zero-balancing execution within 1 hour of cutoff.
- **Investment portfolio**: Valuation accuracy >= 99.99%, price variance < 0.01%.
- **Covenant monitoring**: Automated alerts 30 days before breach threshold.

### Implementation Phasing
- **Phase 5A**: Derivatives management and hedge accounting (6 months).
- **Phase 5B**: Cash pooling structures (3 months).
- **Phase 5C**: Investment portfolio management (4 months).
- **Phase 5D**: Debt and securities tracking (3 months).

## Compliance
- **SOX**: reconciliation and audit trail for cash movements.
- **PCI-DSS**: controls for sensitive payment data.
- **GDPR**: minimization of PII in bank files.
- **Anti-fraud**: dual approval and limit checks for payments.

## Implementation Plan
- Phase 1: Bank master data and statement import.
- Phase 2: Automated reconciliation rules and exception handling.
- Phase 3: Payment runs with dual approvals and file generation.
- Phase 4: Cash forecasting and FX exposure dashboards.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-013: External Integration Patterns (B2B / EDI)
- ADR-014: Authorization Objects & Segregation of Duties

### Internal Documentation
- `docs/treasury/cash_management_requirements.md`

### External References
- SWIFT MT940/MT942 standards
- ISO 20022 CAMT and PAIN
- BAI2 cash management standard
