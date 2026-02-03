# Procurement Domain Architecture

This directory contains hexagonal architecture specifications for Procurement subdomains.

## Subdomain Index

### Phase 1 Modules (Implemented)

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Procurement Core** | ADR-023 | 9101 | Core | [procurement-core.md](./procurement-core.md) | Requisitions, approvals, purchase orders, change control |
| **Sourcing & RFQ** | ADR-023 | 9102 | Core | [procurement-sourcing.md](./procurement-sourcing.md) | RFQ/RFP, quotes, award decisions |
| **Supplier Management** | ADR-023 | 9103 | Core | [procurement-suppliers.md](./procurement-suppliers.md) | Onboarding, compliance, vendor lifecycle |
| **Receiving & Inspection** | ADR-023 | 9104 | Core | [procurement-receiving.md](./procurement-receiving.md) | Goods receipt, service entry, inspections |
| **Invoice Verification** | ADR-023 | 9105 | Core | [procurement-invoice-match.md](./procurement-invoice-match.md) | 2/3-way match, GR/IR reconciliation |

## Documentation Structure

### Complex Subdomains (with subfolders)
- **Procurement Core** (`/core/`) - 5 detailed files (domain, application, infrastructure, api, events)
- **Sourcing & RFQ** (`/sourcing/`) - 5 detailed files

### Simple Subdomains (inline documentation)
- **Supplier Management** - Single file with governance and compliance
- **Receiving & Inspection** - Single file with GR and service entry workflows
- **Invoice Verification** - Single file with matching and exceptions

## Port Allocation

| Port | Service | Database | Status |
|------|---------|----------|--------|
| 9101 | procurement-core | chiroerp_procurement_core | ✅ Implemented |
| 9102 | procurement-sourcing | chiroerp_procurement_sourcing | ✅ Implemented |
| 9103 | procurement-suppliers | chiroerp_procurement_core (shared) | ✅ Implemented |
| 9104 | procurement-receiving | chiroerp_procurement_core (shared) | ✅ Implemented |
| 9105 | procurement-invoice-match | chiroerp_procurement_core (shared) | ✅ Implemented |

**Note:** Inline modules (suppliers, receiving, invoice match) share the `chiroerp_procurement_core` database and run as part of the Core service. Port numbers are logical identifiers for documentation purposes.

## Integration Map

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         PROCUREMENT DOMAIN                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌────────────────────┐        RFQ / Quotes       ┌───────────────────┐    │
│   │ procurement-core   │ <───────────────────────> │ procurement-       │    │
│   │ (req/PO/approval)  │                           │ sourcing           │    │
│   └────────────────────┘                           └───────────────────┘    │
│            │  PO / GR events                                │                │
│            ▼                                                │                │
│   ┌────────────────────┐                                   │                │
│   │ receiving & inspect│                                   │                │
│   └────────────────────┘                                   │                │
│            │                                                │                │
│            ▼                                                ▼                │
│   ┌────────────────────┐                           ┌──────────────────┐     │
│   │ invoice verification│                           │ supplier mgmt   │     │
│   └────────────────────┘                           └──────────────────┘     │
│            │                                                │                │
│            └──────────────┬─────────────────────────────────┘                │
│                           ▼                                                  │
│                      ┌──────────┐        ┌──────────┐                        │
│                      │ inventory│        │ finance  │                        │
│                      └──────────┘        └──────────┘                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Integration Points

### Upstream Dependencies (Consume Events)
- **MDG** → `VendorCreatedEvent`, `VendorUpdatedEvent` → Supplier sync
- **Inventory** → `InventorySnapshotEvent` → Receiving validation
- **Finance Close** → `FinancialPeriodClosedEvent` → Lock postings

### Downstream Consumers (Publish Events)
- **Inventory** ← `GoodsReceivedEvent`, `PurchaseOrderApprovedEvent`
- **Finance/AP** ← `PurchaseOrderApprovedEvent`, `InvoiceMatchCompletedEvent`
- **Treasury** ← `PaymentApprovalRequestedEvent`

## References

### Related ADRs
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md) - Core procurement decisions
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md) - AP integration
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md) - GR events and stock updates
- [ADR-014: Authorization & SoD](../../adr/ADR-014-authorization-objects-sod.md) - Approval controls
- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md) - Supplier master data

### Related Domains
- [Inventory Architecture](../inventory/README.md) - Goods receipt and stock updates
- [Finance Architecture](../finance/README.md) - AP and GR/IR integration

---

## Phase 1 Status: ✅ Complete

**5 modules implemented:**
- Procurement Core
- Sourcing & RFQ
- Supplier Management
- Receiving & Inspection
- Invoice Verification

**Total documentation:** 16 files (2 overview + 10 detailed subfolder files + 3 inline + 1 README)

**Next steps:**
- Integration: Build Sales/Distribution domain
- Integration: Build Manufacturing domain
