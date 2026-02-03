# ADR-023: Procurement (MM-PUR)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Procurement Team  
**Priority**: P1 (High)  
**Tier**: Core  
**Tags**: procurement, purchasing, approvals, compliance, supplier

## Context
Procurement drives vendor spend, compliance, and inventory availability. A SAP-grade ERP requires a full Procure-to-Pay (P2P) lifecycle: requisitioning, sourcing, purchasing, receiving, invoice matching, and supplier performance management, with strict approval and audit controls.

## Decision
Implement a **Procurement** capability with sourcing, purchasing, receiving, invoice verification, and supplier governance integrated with inventory and finance.

### Scope
- Direct and indirect procurement.
- Goods and services procurement.
- Multi-currency purchasing with tax and duty support.
- Supplier onboarding, compliance, and scorecards.

### Feature Tiering (Core vs Advanced)
**Core**
- Requisitions, PO lifecycle, GR, basic 2/3-way match, and vendor master.

**Advanced**
- RFQ/RFP sourcing, catalogs/contracts/blanket POs, landed cost, supplier scorecards.

### Core Capabilities
- **Requisitioning** with role-based approvals and budget checks.
- **Sourcing**: RFQ/RFP, quote comparison, and award decision.
- **Supplier management**: onboarding, KYC, compliance docs, vendor classification.
- **Purchasing**: PO lifecycle, change control, blanket PO, contract-based purchasing.
- **Catalogs and contracts**: price catalogs, framework agreements, and compliance checks.
- **Receiving**: goods receipt, service entry, and quality inspection.
- **Invoice verification**: 2-way and 3-way match (PO, GR, invoice).
- **Landed cost** allocation and freight tracking.
- **Spend analytics** and supplier performance KPIs.
- **Multi-currency and tax**: foreign currency POs, VAT/GST, and import duty handling.

### Data Model (Conceptual)
- `Supplier`, `SupplierContract`, `SupplierScorecard`, `Requisition`, `RFQ`, `Quote`, `PurchaseOrder`, `GoodsReceipt`, `ServiceEntry`, `InvoiceMatch`, `LandedCost`.

### Key Workflows
- **Requisition to PO**: request -> approval -> PO -> receipt -> invoice match.
- **Sourcing**: RFQ -> quote evaluation -> award -> contract.
- **Services procurement**: service entry sheets -> approval -> invoice match.
- **Returns**: vendor returns with credit memo linkage.

### Integration Points
- **Inventory**: goods receipts, returns, and stock updates.
- **Finance/AP**: invoice posting, GR/IR clearing, payment runs.
- **Treasury**: payment approvals and cash forecasting.
- **Authorization/SoD**: buyer vs approver separation.
- **MDG**: vendor master governance.

### Non-Functional Constraints
- **Auditability**: all approvals and changes are logged.
- **Latency**: PO and GR updates within 60 seconds to inventory.
- **Compliance**: automated controls for high-risk spend.

## Alternatives Considered
- **Email/manual approvals**: rejected (lack of auditability).
- **Procurement-only external tool**: rejected (integration overhead).
- **Minimal PO system**: rejected (no GR/IR or compliance controls).
- **No sourcing workflow**: rejected (missed savings and compliance risks).

## Consequences
### Positive
- End-to-end P2P traceability with audit-ready controls.
- Reduced maverick spend and improved supplier governance.
- Better GR/IR accuracy and period close.

### Negative
- Higher workflow and configuration complexity.
- Requires strong master data governance for suppliers and items.

### Neutral
- Advanced strategic sourcing features can be phased later.

## Compliance
- **SOX**: approval workflows and audit trails for POs/invoices.
- **Anti-bribery**: vendor onboarding and approval segregation.
- **GDPR**: supplier PII minimized and protected.
- **Tax compliance**: VAT/GST and duty handling in receipts.

## Implementation Plan
- Phase 1: Requisitions, approvals, and basic PO lifecycle.
- Phase 2: Supplier onboarding, RFQ/quote comparison.
- Phase 3: GR/IR matching, service procurement, and AP integration.
- Phase 4: Landed cost, spend analytics, and supplier scorecards.

## References
### Related ADRs
- ADR-009: Financial Accounting Domain Strategy
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-024: Inventory Management
- ADR-027: Master Data Governance

### Internal Documentation
- `docs/procurement/procurement_requirements.md`

### External References
- SAP MM-PUR purchasing processes
- ISO 20400 Sustainable Procurement Guidance
