# ADR-041: Marketplace Domain (Add-on)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-01
**Deciders**: Architecture Team, Product Team
**Priority**: Medium
**Tier**: Add-on
**Tags**: marketplace, sellers, listings, commissions, fulfillment

## Context
The current draft architecture includes a marketplace subdomain, but the ERP ADR suite does not explicitly define marketplace capabilities. A marketplace introduces multi-party commerce (seller onboarding, listing governance, commission settlement, split payments, and cross-border tax exposure) that is materially different from standard SD order-to-cash flows. This ADR defines a **Marketplace Domain** as an optional add-on with explicit boundaries and integration points to core ERP domains.

## Decision
Adopt a **Marketplace Domain** as an add-on capability that supports multi-seller listings, commission settlement, and marketplace fulfillment orchestration while integrating with SD, Inventory, Tax, and Finance for accounting correctness.

### Scope
- Seller onboarding and compliance (KYC, tax profiles, banking details).
- Product listings with marketplace-specific pricing and policies.
- Order routing across sellers, warehouses, and fulfillment options.
- Commission calculation, settlement, and fee invoicing.
- Dispute/returns handling with seller responsibility tracking.

### Key Capabilities
- **Seller Management**: seller profiles, contracts, payout schedules, compliance status.
- **Listing Governance**: listing approvals, pricing rules, content moderation.
- **Commission Engine**: % fees, fixed fees, promotional fee waivers.
- **Split Payments**: marketplace fee + seller payout separation.
- **Marketplace Fulfillment**: seller-fulfilled vs marketplace-fulfilled flows.

### Integration Points
- **Sales (ADR-025)**: marketplace orders flow into SD order lifecycle with seller attribution.
- **Inventory (ADR-024)**: multi-party stock visibility, seller-owned inventory, consignment rules.
- **Tax (ADR-030)**: marketplace facilitator tax rules and jurisdiction handling.
- **Finance (ADR-009)**: commission revenue, seller payouts, liabilities, and clearing accounts.
- **Treasury (ADR-026)**: payout schedules and bank file generation.

### Non-Functional Constraints / KPIs
- **Order routing latency**: p95 < 500ms for seller allocation.
- **Commission calculation**: p95 < 250ms per order.
- **Payout accuracy**: >= 99.9% (settlement reconciliation).
- **Listing approval SLA**: p95 < 4 hours for standard categories.

## Alternatives Considered
- **Embed in SD only**: rejected due to seller lifecycle and payout complexity.
- **External marketplace SaaS**: rejected for tight ERP integration and accounting control requirements.
- **Custom plugin model**: partially accepted via integration extension points.

## Consequences
### Positive
- Clear separation of marketplace-specific responsibilities.
- Enables multi-seller business models without contaminating core SD.
- Better accounting controls for commission revenue and liabilities.

### Negative / Risks
- Increased complexity in order routing and settlement logic.
- Higher compliance burden (KYC, tax facilitator obligations).

### Neutral
- Marketplace scope is an add-on; can be deferred without blocking core ERP.

## Compliance
- Tax facilitator rules (VAT/GST/Sales Tax).
- AML/KYC requirements for seller onboarding.
- PCI-DSS alignment for marketplace payment processing.

## Implementation Plan
1. Seller onboarding and compliance module.
2. Listing governance and pricing policy engine.
3. Commission and payout engine with financial postings.
4. Marketplace fulfillment orchestration.
5. Disputes, returns, and reconciliation workflows.

## References
- ADR-025 Sales & Distribution
- ADR-024 Inventory Management
- ADR-030 Tax Engine & Compliance
- ADR-009 Financial Accounting
- ADR-026 Treasury & Cash Management
