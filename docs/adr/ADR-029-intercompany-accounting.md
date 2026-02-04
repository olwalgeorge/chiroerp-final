# ADR-029: Intercompany Accounting (IC)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-01
**Deciders**: Architecture Team, Finance Team
**Priority**: P0 (Critical)
**Tier**: Advanced
**Tags**: intercompany, consolidation, eliminations, transfer-pricing

## Context
Multi-entity deployments require intercompany accounting for trading, loans, services, and inventory transfers. This ADR defines intercompany transactions, matching, eliminations, and reconciliation rules aligned to IFRS/GAAP to support consolidated reporting and mismatch resolution workflows.

## Decision
Implement an **Intercompany Accounting** capability with intercompany document flows, matching, eliminations, and consolidation integration, including transfer pricing and settlement workflows.

### Scope
- Intercompany sales, services, loans, and inventory transfers.
- Automated intercompany document creation and mirror postings.
- Matching, reconciliation, and elimination entries.
- Intercompany settlement and netting.

### Core Capabilities
- **Intercompany master data**: trading partner relationships and rules.
- **Intercompany documents**: paired postings (seller/buyer) with linked references.
- **Transfer pricing**: market, cost-plus, or negotiated pricing rules.
- **Matching engine**: automatic matching on amount, currency, period, partner, and document.
- **Elimination postings**: elimination at consolidation layer with audit trail.
- **Dispute management**: mismatch resolution workflow and adjustments.
- **Netting and settlement**: periodic clearing of intercompany balances.
- **Transfer pricing documentation**: policy versioning, benchmarking inputs, and audit-ready documentation packs.
- **APA support**: store approved Advance Pricing Agreements and enforce applicable rules.

### Data Model (Conceptual)
- `TradingPartner`, `IntercompanyAgreement`, `IntercompanyDocument`, `IntercompanyMatch`, `EliminationEntry`, `NettingBatch`, `DisputeCase`, `TransferPricingPolicy`, `Benchmark`, `APAAgreement`, `TransferPricingDocumentation`, `PriceAdjustment`.

### Key Workflows
- **Intercompany sale**: order -> delivery -> invoice -> mirror posting -> reconciliation.
- **Service charge**: allocation -> posting -> match -> settlement.
- **Inventory transfer**: transfer order -> goods issue/receipt -> valuation -> match.
- **Period close**: intercompany reconciliation -> eliminations -> consolidation.
- **Transfer pricing true-up**: policy review -> adjustment posting -> documentation export.

### Integration Points
- **FI/GL**: intercompany postings and balances.
- **CO**: cost allocations and transfer pricing rules.
- **Inventory (MM-IM)**: intercompany stock transfers and valuation.
- **Sales (SD)**: intercompany sales orders and billing.
- **Treasury (TR-CM)**: settlement and netting.
- **Analytics**: consolidated reporting and elimination dashboards.

### Non-Functional Constraints
- **Reconciliation accuracy**: 99.9% match rate by close.
- **Performance**: intercompany matching p95 < 30 minutes for 100k pairs.
- **Auditability**: full trace from source docs to elimination entries.
- **Policy traceability**: every intercompany transaction linked to a pricing policy version.

### KPIs and SLOs
- **Match completion**: p95 < 30 minutes for 100k intercompany pairs.
- **Elimination readiness**: 100% of matched items available for consolidation by close.
- **Mismatch resolution**: < 48 hours for P1 disputes.
- **Intercompany balance variance**: < 0.01% at consolidation.
- **Transfer pricing coverage**: 100% of intercompany transactions linked to a pricing policy.
- **Documentation readiness**: 100% of required OECD transfer pricing docs available by close.

## Alternatives Considered
- **Manual eliminations**: rejected (error-prone, not scalable).
- **External consolidation tool only**: rejected (loss of traceability).
- **Spreadsheet-based netting**: rejected (audit risk).

## Consequences
### Positive
- SAP-grade intercompany controls and consolidation readiness.
- Reduced close effort and improved auditability.
- Clear visibility of intercompany exposures.

### Negative
- Requires disciplined master data and trading partner setup.
- Additional workflow complexity for dispute resolution.

### Neutral
- Advanced transfer pricing scenarios can be phased.

## Compliance
- **IFRS/GAAP**: elimination of intercompany profits and balances.
- **SOX**: controlled reconciliations and audit trails.
- **OECD transfer pricing**: documentation of pricing rules.
- **BEPS Action 13**: Master File / Local File alignment where applicable.

## Implementation Plan
- Phase 1: Trading partner master data and intercompany documents.
- Phase 2: Matching engine and reconciliation workflows.
- Phase 3: Elimination postings and consolidation integration.
- Phase 4: Netting and settlement automation.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-028: Controlling / Management Accounting (CO)
- ADR-024: Inventory Management (MM-IM)
- ADR-025: Sales & Distribution (SD)
- ADR-026: Treasury & Cash Management (TR-CM)
- ADR-016: Analytics & Reporting Architecture

### Internal Documentation
- `docs/finance/intercompany_requirements.md`

### External References
- SAP Intercompany Accounting and Consolidation
- IFRS consolidation guidance
- OECD Transfer Pricing Guidelines
- OECD BEPS Action 13 guidance
