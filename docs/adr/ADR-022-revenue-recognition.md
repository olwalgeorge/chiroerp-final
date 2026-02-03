# ADR-022: Revenue Recognition (ASC 606 / IFRS 15)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Finance Team  
**Priority**: P1 (High)  
**Tier**: Advanced  
**Tags**: finance, revenue-recognition, compliance

## Context
Revenue recognition is a mandatory compliance requirement for SaaS and subscription businesses. We must support the 5-step model (ASC 606/IFRS 15), deferred revenue, contract modifications, and audit-ready schedules with integration to sales, billing, and GL.

## Decision
Implement a **Revenue Recognition** capability integrated with sales, billing, and GL, supporting contract-based recognition schedules and compliance reporting.

### Scope
- Contracts with multiple performance obligations.
- Subscription, usage-based, and milestone billing.
- Contract modifications and remeasurement.

### Core Capabilities
- **Contract modeling**: customers, obligations, stand-alone selling prices (SSP).
- **Transaction price allocation** across obligations.
- **Recognition methods**: point-in-time, over-time, usage-based.
- **Deferred revenue** and reclassification to recognized revenue.
- **Contract modifications** (prospective/retrospective).
- **Disclosure reporting**: remaining performance obligations, deferred balances.
- **Variable consideration**: discounts, rebates, and refunds with constraint handling.

### Data Model (Conceptual)
- `RevenueContract`, `PerformanceObligation`, `SSP`, `RevenueSchedule`, `DeferredRevenueBalance`, `ContractModification`.

### Key Workflows
- **Contract creation**: identify obligations -> allocate price -> schedule.
- **Billing events**: invoice -> deferred revenue posting.
- **Recognition run**: scheduled revenue posting to GL.
- **Modification**: adjust allocations and schedules with audit trail.

---

## Subscription & Usage-Based Billing Extension (Lifecycle + Metering)

### Context
While this ADR defines revenue recognition compliance (ASC 606 / IFRS 15), SaaS and subscription businesses require **full subscription lifecycle and usage metering** to generate billable events that feed Revenue Recognition.

### Scope
- **Subscription lifecycle**: signup → trial → activate → renew → upgrade/downgrade → cancel.
- **Plan catalog**: tiers, add‑ons, usage caps, minimum commitments.
- **Usage metering**: ingest usage events (API calls, seats, storage, transactions).
- **Rating & pricing**: tiered pricing, overages, proration, true‑ups.
- **Billing orchestration**: invoice generation, dunning, credit notes, refunds.
- **Revenue handoff**: create contract modifications & schedules for RevRec.

### Core Capabilities
- **Metering ingestion**: event capture with idempotency and time‑bucket aggregation.
- **Rating engine**: apply plan rules, tier thresholds, and minimums.
- **Proration**: mid‑cycle changes with prorated charges.
- **Invoice creation**: generate billing events for AR.
- **Dunning**: failed payment workflows (retry, suspend, cancel).
- **Revenue linkage**: map billed amounts to performance obligations for RevRec.

### Data Model (Conceptual)
- `Subscription`, `Plan`, `PlanTier`, `AddOn`, `UsageEvent`, `UsageBucket`, `RatingRule`,
  `InvoiceSchedule`, `ProrationAdjustment`, `CancellationReason`.

### Integration Points
- **Sales/SD**: subscription signup, quotes, amendments.
- **Billing/AR**: invoices, credit memos, collections.
- **Payments/Treasury**: payment retries, refunds.
- **Tax Engine**: digital services tax, VAT/GST rules.
- **Revenue Recognition**: contract modifications, schedule updates.
- **Analytics**: MRR/ARR, churn, NDR, usage KPIs.

### Implementation Phasing
- **Phase 1**: Plan catalog + subscription lifecycle + basic metering.
- **Phase 2**: Tiered pricing + proration + dunning.
- **Phase 3**: Usage caps + commitment true‑ups + analytics.

### Integration Points
- **Sales/SD**: contracts, pricing, amendments.
- **Billing/AR**: invoices and payment events.
- **GL**: automated postings for deferred and recognized revenue.
- **Analytics**: revenue waterfall and forecast reporting.

### Non-Functional Constraints
- **Auditability**: immutable contract and schedule history.
- **Performance**: monthly recognition run within close SLA.
- **Accuracy**: reconciliation to GL and billing.

## Alternatives Considered
- **Manual revenue schedules**: rejected (audit risk, error-prone).
- **External RevRec system**: rejected (integration complexity and latency).
- **GL-only adjustments**: rejected (no contract-level audit trail).

## Consequences
### Positive
- Compliance with ASC 606/IFRS 15.
- Clear audit trail for revenue and deferred balances.
- Better forecasting and revenue reporting.

### Negative
- Complex rules and exceptions increase implementation effort.
- Requires high-quality contract data and configuration.

### Neutral
- Some industries may require specialized add-ons (e.g., milestone billing).

## Compliance
- **ASC 606 / IFRS 15**: recognition schedules and contract modifications.
- **SOX**: approval workflows for contract changes.
- **GDPR**: contract data handling with minimization and audit logs.

## Implementation Plan
- Phase 1: Contract + obligation modeling; manual recognition schedules.
- Phase 2: Automated schedules and deferred revenue posting.
- Phase 3: Modifications and usage-based recognition.
- Phase 4: Revenue reports and audit exports.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-025: Sales & Distribution (SD)

### Internal Documentation
- `docs/finance/revenue_recognition_requirements.md`

### External References
- ASC 606 Revenue from Contracts with Customers
- IFRS 15 Revenue from Contracts with Customers
