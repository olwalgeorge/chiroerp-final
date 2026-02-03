# ADR-028: Controlling / Management Accounting (CO)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Finance Team  
**Priority**: P0 (Critical)  
**Tier**: Advanced  
**Tags**: controlling, management-accounting, cost-center, profit-center, copa

## Context
A SAP-grade ERP requires full Controlling (CO) to support management accounting, profitability analysis, and internal cost transparency. This ADR defines the CO sub-ledger, allocation logic, and integrations with FI, MM, SD, and inventory to support cost center accounting (CO-CCA), profit center accounting (EC-PCA), internal orders (CO-OM), product costing (CO-PC), and profitability analysis (CO-PA).

## Decision
Implement a **Controlling / Management Accounting** capability with cost center and profit center accounting, internal orders, product costing, and profitability analysis.

### Scope
- Cost center accounting and cost allocations.
- Profit center accounting with segment reporting.
- Internal orders for temporary cost tracking and settlement.
- Product costing and variance analysis.
- Profitability analysis (CO-PA) with contribution margin reporting.
- Activity-based costing (ABC) and cost driver definitions.
- Transfer pricing for intercompany services and goods.

### Core Capabilities
- **Cost center accounting (CO-CCA)**: cost centers, cost elements, planning, and allocations.
- **Profit center accounting (EC-PCA)**: profit centers, segment reporting, and internal reporting.
- **Internal orders (CO-OM)**: order budgeting, postings, and settlement.
- **Cost elements**: primary (from FI) and secondary (allocations).
- **Activity types and rates**: internal activity allocations (labor, machine hours).
- **Allocation cycles**: assessment and distribution with traceable rules.
- **Product costing (CO-PC)**: standard cost, actual cost, and variance analysis.
- **Profitability analysis (CO-PA)**: account-based and optional costing-based model.
- **Planning and forecasting**: plan vs actual with variance reporting.
- **Activity-based costing (ABC)**: cost pools, drivers, and allocation rules.
- **Transfer pricing**: intercompany pricing methods (market, cost-plus, negotiated).

### Data Model (Conceptual)
- `CostCenter`, `ProfitCenter`, `InternalOrder`, `CostElement`, `ActivityType`, `AllocationCycle`, `AssessmentRule`, `DistributionRule`, `SettlementRule`, `CostingSheet`, `ProductCostEstimate`, `ProfitabilitySegment`.

### Key Workflows
- **Plan to actual**: budget planning -> postings -> variance analysis.
- **Allocations**: cycle definitions -> execution -> posting to CO and FI.
- **Internal orders**: create -> post costs -> settlement to cost center/asset.
- **Product costing**: estimate -> standard cost -> variance analysis at period close.
- **Profitability reporting**: revenue and cost allocation -> margin analysis.
- **ABC allocation**: define cost pools -> assign drivers -> allocate to products or services.
- **Transfer pricing**: define pricing rules -> post intercompany charges -> reconcile.

### Integration Points
- **FI/GL**: primary cost elements and reconciliation to general ledger.
- **Procurement (MM-PUR)**: purchase costs allocated to cost centers.
- **Inventory (MM-IM)**: material costs and valuations feed product costing.
- **Sales (SD)**: revenue postings into CO-PA.
- **Fixed Assets (FI-AA)**: asset-related costs allocated to cost centers.
- **Treasury**: financing costs allocated to profit centers.
- **MDG**: governance of cost centers and profit centers.
- **Analytics**: management reporting and KPI dashboards.

### Non-Functional Constraints
- **Reconciliation**: CO postings reconcile to FI within period close SLAs.
- **Performance**: allocation cycles complete within close window (p95 < 2 hours).
- **Auditability**: allocation rules and overrides fully traceable.

### KPIs and SLOs
- **Allocation run completion**: p95 < 2 hours, p99 < 4 hours during close.
- **CO-FI reconciliation accuracy**: 99.9% match at period close.
- **Plan vs actual variance reporting**: available within 4 hours of close.
- **Cost center posting latency**: p95 < 60 seconds from source posting.
- **Profitability reporting freshness**: daily by 08:00 local time; intraday on demand.
- **ABC allocation accuracy**: variance < 1% vs baseline cost model.
- **Transfer pricing compliance**: 100% of intercompany postings linked to pricing rule.

## Alternatives Considered
- **FI-only cost reporting**: rejected (no allocations or profitability analysis).
- **External CPM/EPM tool**: rejected (integration complexity and latency).
- **Spreadsheet-based costing**: rejected (no auditability or scalability).

## Consequences
### Positive
- Full management accounting capabilities with SAP-grade parity.
- Clear visibility into costs, margins, and profitability by segment.
- Strong basis for budgeting, forecasting, and executive reporting.

### Negative
- Complex configuration and maintenance of allocation rules.
- Requires disciplined master data governance and planning processes.

### Neutral
- Some advanced CO features (ABC, detailed transfer pricing) can be phased.

## Compliance
- **SOX**: controlled allocation rules and audit trails.
- **IFRS/GAAP**: segment reporting alignment and cost allocations.
- **Tax compliance**: transfer pricing support for intercompany allocations.
- **OECD transfer pricing**: documentation-ready allocation and pricing basis.

## Implementation Plan
- Phase 1: Cost center accounting, cost elements, plan vs actual.
- Phase 2: Internal orders and basic allocations (distribution/assessment).
- Phase 3: Profit center accounting and segment reporting.
- Phase 4: Product costing and variance analysis.
- Phase 5: Profitability analysis (CO-PA) and executive dashboards.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-016: Analytics & Reporting Architecture
- ADR-021: Fixed Asset Accounting (FI-AA)
- ADR-023: Procurement (MM-PUR)
- ADR-024: Inventory Management (MM-IM)
- ADR-025: Sales & Distribution (SD)
- ADR-027: Master Data Governance (MDG)

### Internal Documentation
- `docs/finance/controlling_requirements.md`

### External References
- SAP CO (Controlling) module documentation
- IFRS segment reporting guidance
