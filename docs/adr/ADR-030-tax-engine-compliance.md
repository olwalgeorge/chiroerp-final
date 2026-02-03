# ADR-030: Tax Engine & Compliance

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Finance Team, Tax Team  
**Priority**: P1 (High)  
**Tier**: Core  
**Tags**: tax, vat, gst, withholding, compliance

## Context
A SAP-grade ERP requires comprehensive tax handling for VAT/GST/Sales Tax and withholding tax (WHT), including reverse charge, exemptions, and country-specific reporting. This ADR defines tax calculation, determination, posting, and reporting requirements to support compliant financial postings and statutory returns.

## Decision
Implement a **Tax Engine & Compliance** capability to calculate, validate, post, and report indirect and withholding taxes, with integration across sales, procurement, and finance.

### Scope
- Indirect taxes: VAT, GST, Sales Tax.
- Withholding tax (WHT) and tax deductions.
- Reverse charge, exemptions, and tax jurisdiction rules.
- Tax returns preparation and audit trails.

### Feature Tiering (Core vs Advanced)
**Core**
- Single-jurisdiction rates, basic tax calculation, and GL postings.

**Advanced**
- Multi-jurisdiction VAT/GST/WHT, reverse charge, returns, and external tax providers.

### Core Capabilities
- **Tax determination**: jurisdiction, product/service category, customer/vendor tax status.
- **Tax calculation**: rates, thresholds, and rounding rules.
- **Exemption handling**: certificates and expiration tracking.
- **Reverse charge**: self-assessed VAT/GST postings.
- **Withholding tax**: rates, certificates, and remittance schedules.
- **Tax postings**: separate tax accounts and GL integration.
- **Tax reporting**: jurisdictional returns and audit-ready extracts.

### Data Model (Conceptual)
- `TaxJurisdiction`, `TaxRate`, `TaxRule`, `TaxCategory`, `TaxExemption`, `WithholdingRule`, `TaxDocument`, `TaxReturn`.

### Key Workflows
- **Sales tax**: order -> tax determination -> invoice -> tax posting.
- **Purchase tax**: PO -> GR -> invoice -> input tax credit.
- **Reverse charge**: determine -> self-assess -> post.
- **Tax return**: aggregate -> validate -> submit -> archive.

### Integration Points
- **Sales (SD)**: tax calculation on invoices and credit notes.
- **Procurement (MM-PUR)**: input tax and WHT on invoices.
- **Finance/GL**: tax posting and reconciliation.
- **Analytics**: tax dashboards and compliance reporting.
- **External tax providers** (optional): Vertex, Avalara, Sovos.

### Non-Functional Constraints
- **Accuracy**: 100% deterministic calculations for given inputs.
- **Performance**: tax calculation p95 < 150ms per transaction.
- **Auditability**: full trace from transaction to tax return line.

### KPIs and SLOs
- **Tax calculation latency**: p95 < 150ms.
- **Return readiness**: 100% of required tax data available by close.
- **Reconciliation variance**: < 0.01% between tax sub-ledger and GL.
- **Exemption validation**: 100% certificate validation at posting.

## Alternatives Considered
- **Manual tax handling**: rejected (non-compliance risk).
- **External tax engine only**: rejected (integration dependency).
- **Static tax tables**: rejected (insufficient for jurisdictional complexity).

## Consequences
### Positive
- Regulatory compliance for VAT/GST/WHT.
- Reduced audit risk with traceable tax calculations.
- Faster close and accurate statutory returns.

### Negative
- Continuous tax rate maintenance required.
- Increased complexity in sales and procurement flows.

### Neutral
- Advanced tax optimization (transfer pricing) remains in CO/IC modules.

## Compliance
- **VAT/GST**: jurisdictional rates and reporting.
- **WHT**: remittance schedules and certificate tracking.
- **SOX**: audit trail and approval controls.
- **GDPR**: minimize PII in tax reporting.

## Implementation Plan
- Phase 1: Tax jurisdiction model, rate tables, and basic calculation.
- Phase 2: Integration with SD and MM-PUR; tax postings to GL.
- Phase 3: Reverse charge and WHT workflows.
- Phase 4: Tax returns, dashboards, and audit exports.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-023: Procurement (MM-PUR)
- ADR-025: Sales & Distribution (SD)
- ADR-029: Intercompany Accounting (IC)
- ADR-016: Analytics & Reporting Architecture

### Internal Documentation
- `docs/finance/tax_engine_requirements.md`

### External References
- VAT/GST compliance guides
- OECD Tax Administration guidance
