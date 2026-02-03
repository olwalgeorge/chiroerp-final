# ADR-025: Sales & Distribution (SD)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Sales Ops  
**Priority**: P1 (High)  
**Tier**: Core  
**Tags**: sales, orders, pricing, fulfillment, omnichannel

## Context
Sales order management, pricing, taxation, and fulfillment are core ERP capabilities. SAP-grade parity requires an end-to-end Order-to-Cash (O2C) flow with credit management, multi-channel orders (POS, ecommerce, marketplace, wholesale), returns, and audit-ready billing.

## Decision
Implement a **Sales & Distribution** capability with order management, pricing, fulfillment, billing integration, and multi-channel orchestration.

### Scope
- Direct sales, wholesale, and POS orders.
- Multi-channel order capture and allocation.
- Pricing, promotions, and tax determination.
- Returns, refunds, and credit memo handling.

### Feature Tiering (Core vs Advanced)
**Core**
- Sales order capture, invoicing, basic pricing, and basic returns.

**Advanced**
- Credit management, rebates/commissions, omnichannel orchestration, advanced ATP.

### Core Capabilities
- **Order lifecycle**: quote -> order -> delivery -> invoice -> payment.
- **Pricing engine**: multi-currency price lists, discounts, promotions, rebates, and approvals.
- **Tax integration**: VAT/GST/Sales Tax, exemptions, and jurisdiction rules.
- **Credit management**: customer limits, holds, and release workflow.
- **Returns management**: RMA, restock, refunds, and credit notes.
- **Channel orchestration**: POS, ecommerce, marketplace, and wholesale.
- **ATP and backorder** management with substitution rules.
- **Commissions and rebates**: sales incentives and rebate settlement workflows.

### Data Model (Conceptual)
- `Customer`, `SalesOrder`, `OrderLine`, `PriceCondition`, `Promotion`, `Delivery`, `Shipment`, `Invoice`, `CreditMemo`, `ReturnOrder`, `RMA`.

### Key Workflows
- **O2C**: order -> allocate -> pick/pack/ship -> invoice -> cash.
- **POS flow**: scan -> payment -> receipt -> inventory deduction.
- **Returns**: RMA approval -> receipt -> restock/write-off -> refund/credit.
- **Credit management**: automatic checks with override approvals.

### Integration Points
- **Inventory**: ATP, reservations, and shipment confirmation.
- **Finance/AR**: invoice posting, revenue and receivables.
- **Revenue Recognition**: contract and billing linkages.
- **POS**: real-time order and stock updates.
- **Tax engines**: external VAT/GST/Sales Tax services.

### Non-Functional Constraints
- **Latency**: channel orders processed within 2 seconds p95.
- **Consistency**: price and promotion evaluation deterministic.
- **Idempotency**: order submissions safe for retries.

## Alternatives Considered
- **Standalone CRM**: rejected (no fulfillment integration).
- **Minimal order tracking**: rejected (no pricing/tax controls).
- **External billing system**: rejected (audit and reconciliation complexity).
- **Single-channel only**: rejected (insufficient for omnichannel).

## Consequences
### Positive
- Full O2C traceability with audit-ready controls.
- Unified pricing and promotions across channels.
- Improved cash flow forecasting and credit control.

### Negative
- Complex configuration for pricing, tax, and credit policies.
- Requires strong master data governance for customers and products.

### Neutral
- Advanced SD capabilities (rebates, ATP optimization) can be phased.

## Compliance
- **SOX**: approvals for pricing overrides and credit notes.
- **GDPR**: customer PII handling controls.
- **Tax compliance**: jurisdictional tax rules and exemptions.

## Implementation Plan
- Phase 1: Sales orders, pricing rules, and basic billing.
- Phase 2: Fulfillment workflows and credit management.
- Phase 3: POS integration and omnichannel orchestration.
- Phase 4: Returns, disputes, and analytics dashboards.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-022: Revenue Recognition
- ADR-024: Inventory Management

### Internal Documentation
- `docs/sales/sd_requirements.md`

### External References
- SAP SD order-to-cash processes
- IFRS 15 / ASC 606 sales contract considerations
