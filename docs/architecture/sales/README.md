# Sales & Distribution Domain Architecture

This directory contains hexagonal architecture specifications for Sales & Distribution (SD) subdomains.

## Subdomain Index

### Phase 1 Modules (Implemented)

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Sales Core** | ADR-025 | 9201 | Core | [sales-core.md](./sales-core.md) | Orders, quotes, approvals, order lifecycle |
| **Pricing & Promotions** | ADR-025 | 9202 | Core | [sales-pricing.md](./sales-pricing.md) | Price lists, discounts, promotions, overrides |
| **Fulfillment** | ADR-025 | 9203 | Core | [sales-fulfillment.md](./sales-fulfillment.md) | Pick/pack/ship, delivery, shipment confirmation |
| **Returns & RMA** | ADR-025 | 9204 | Core | [sales-returns.md](./sales-returns.md) | RMA, restock, refunds, credit notes |
| **Credit Management** | ADR-025 | 9205 | Core | [sales-credit.md](./sales-credit.md) | Credit limits, holds, approvals |

### Phase 2 Modules (Implemented)

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **Commissions** | ADR-025 | 9206 | Core | [sales-commissions.md](./sales-commissions.md) | Commission plans, accruals, payouts |
| **Rebates & Incentives** | ADR-025 | 9207 | Core | [sales-rebates.md](./sales-rebates.md) | Rebate programs, accruals, claims |

## Documentation Structure

### Complex Subdomains (with subfolders)
- **Sales Core** (`/core/`) - 5 detailed files (domain, application, infrastructure, api, events)
- **Pricing & Promotions** (`/pricing/`) - 5 detailed files

### Simple Subdomains (inline documentation)
- **Fulfillment** - Single file with logistics and shipment workflows
- **Returns & RMA** - Single file with restock/refund workflows
- **Credit Management** - Single file with approval and hold rules
- **Commissions** - Single file with accruals and payouts
- **Rebates & Incentives** - Single file with rebate programs and claims

## Port Allocation

| Port | Service | Database | Status |
|------|---------|----------|--------|
| 9201 | sales-core | chiroerp_sales_core | ✅ Implemented |
| 9202 | sales-pricing | chiroerp_sales_pricing | ✅ Implemented |
| 9203 | sales-fulfillment | chiroerp_sales_core (shared) | ✅ Implemented |
| 9204 | sales-returns | chiroerp_sales_core (shared) | ✅ Implemented |
| 9205 | sales-credit | chiroerp_sales_core (shared) | ✅ Implemented |
| 9206 | sales-commissions | chiroerp_sales_core (shared) | ✅ Implemented |
| 9207 | sales-rebates | chiroerp_sales_core (shared) | ✅ Implemented |

**Note:** Inline modules (fulfillment, returns, credit) share the `chiroerp_sales_core` database and run as part of the Core service. Port numbers are logical identifiers for documentation purposes.

## Integration Map

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SALES & DISTRIBUTION DOMAIN                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌───────────────────┐     Pricing/Rules      ┌──────────────────────┐    │
│   │ sales-core        │ <────────────────────> │ sales-pricing        │    │
│   │ (order lifecycle) │                        └──────────────────────┘    │
│   └───────────────────┘             │                                        │
│            │ Fulfillment events     │                                        │
│            ▼                        ▼                                        │
│   ┌───────────────────┐       ┌───────────────────┐                         │
│   │ fulfillment       │       │ credit mgmt       │                         │
│   └───────────────────┘       └───────────────────┘                         │
│            │                         │                                      │
│            └──────────────┬──────────┘                                      │
│                           ▼                                                 │
│                      ┌──────────┐     ┌──────────┐                          │
│                      │ inventory│     │ finance  │                          │
│                      └──────────┘     └──────────┘                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Integration Points

### Upstream Dependencies (Consume Events)
- **Inventory** → `ReservationCreatedEvent`, `StockAdjustedEvent` → ATP checks
- **Finance/AR** → `CreditLimitExceededEvent`, `CustomerBlockedEvent` → Credit limits
- **Tax Engine** → `TaxRateUpdatedEvent` → Pricing and invoices

### Downstream Consumers (Publish Events)
- **Inventory** ← `SalesOrderAllocatedEvent`, `ShipmentConfirmedEvent`, `ReturnReceivedEvent`
- **Finance/AR** ← `SalesOrderFulfilledEvent`, `CreditMemoRequestedEvent`
- **Revenue** ← `SalesOrderFulfilledEvent`
- **Finance/AP** ← `CommissionPayableCreatedEvent`
- **Finance/AR** ← `RebateIssuedEvent`

## References

### Related ADRs
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md) - Core SD decisions
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md) - AR integration
- [ADR-022: Revenue Recognition](../../adr/ADR-022-revenue-recognition.md) - Contract billing
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md) - ATP and reservations
- [ADR-014: Authorization & SoD](../../adr/ADR-014-authorization-objects-sod.md) - Approval controls

### Related Domains
- [Inventory Architecture](../inventory/README.md) - Reservations and fulfillment
- [Finance Architecture](../finance/README.md) - AR and billing integration

---

## Phase 2 Status: ✅ Complete

**7 modules implemented:**
- Sales Core
- Pricing & Promotions
- Fulfillment
- Returns & RMA
- Credit Management
- Commissions
- Rebates & Incentives

**Total documentation:** 18 files (2 overview + 10 detailed subfolder files + 5 inline + 1 README)

**Next steps:**
- Optional: HR/Payroll integration for commission payouts
- Optional: Advanced rebate settlement and accrual reporting
