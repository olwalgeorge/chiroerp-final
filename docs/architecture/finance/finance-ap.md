# Finance Accounts Payable (AP) - ADR-009

> **Bounded Context:** `finance-ap`  
> **Port:** `8083`  
> **Database:** `chiroerp_finance_ap`  
> **Kafka Consumer Group:** `finance-ap-cg`

## Overview

Accounts Payable manages **vendor-facing financial transactions** including vendor bills/invoices, payment processing, 3-way matching (PO-GRN-Invoice), aging analysis, and payment scheduling. AP posts to GL and integrates with Procurement and Treasury.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Vendor invoices, payments, 3-way matching, aging |
| **Aggregates** | VendorAccount, VendorInvoice, Payment, PaymentRun, AgingSnapshot |
| **Key Events** | VendorInvoicePostedEvent, PaymentSentEvent, PaymentRunExecutedEvent |
| **GL Integration** | Bill posted (DR Expense/Inventory, CR AP); payment sent (DR AP, CR Cash/Bank) |
| **Compliance** | 1099 reporting, withholding/VAT/GST, SOX 404 controls |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Model** | [ap-domain.md](./ap/ap-domain.md) | Aggregates, entities, value objects |
| **Application Layer** | [ap-application.md](./ap/ap-application.md) | Commands, queries, ports, handlers |
| **Infrastructure** | [ap-infrastructure.md](./ap/ap-infrastructure.md) | REST, persistence, messaging adapters |
| **API Reference** | [ap-api.md](./ap/ap-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [ap-events.md](./ap/ap-events.md) | Domain events and GL integration |

## Bounded Context

```
finance-ap/
├── ap-domain/
├── ap-application/
└── ap-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FINANCE-AP SERVICE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      APPLICATION LAYER                               │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │   │
│  │  │ PostVendorInvoice│  │ RecordPayment    │  │ ExecutePaymentRun │  │   │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │  │   │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │   │
│  │           │                    │                       │             │   │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │   │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │   │
│  │  │ VendorInvoiceRepo │ PaymentRepo │ GLPort │ BankPort           │  │   │
│  │  └────────────────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                        DOMAIN LAYER                                  │   │
│  │  ┌────────────┐  ┌─────────────┐  ┌────────────┐  ┌──────────────┐   │   │
│  │  │VendorAccount│ │VendorInvoice│  │ Payment    │  │ PaymentRun   │   │   │
│  │  │ Aggregate   │ │ Aggregate   │  │ Aggregate  │  │ Aggregate    │   │   │
│  │  └────────────┘  └─────────────┘  └────────────┘  └──────────────┘   │   │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │   │
│  │  │ Matching / Aging   │  │ Domain Services                      │    │   │
│  │  │ Aggregates         │  │ Match, Allocation, Discount         │    │   │
│  │  └────────────────────┘  └─────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                    INFRASTRUCTURE LAYER                              │   │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │   │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ Scheduler  │   │   │
│  │  │ (Quarkus)   │  │ (PostgreSQL) │  │ Publisher  │  │ (Batch)    │   │   │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Vendor & Terms Management
- Vendor master data, bank details, and payment terms
- Vendor status control (active, on-hold, blocked)
- Credit limits and compliance attributes

### 2. Invoice Processing & Matching
- Invoice capture and validation
- 2-way and 3-way matching with tolerance rules
- Variance exception handling and resolution

### 3. Payment Processing & Runs
- Payment proposal generation and approval
- Batch execution by bank and method
- Remittance advice and payment status tracking

### 4. Cash Planning & Aging
- AP aging snapshots and vendor statements
- Cash requirements forecasting
- DPO metrics and payment prioritization

### 5. Tax & Withholding
- Withholding tax calculation and reporting
- 1099 reporting (US)
- VAT/GST handling where configured

## Integration Points

```
┌──────────────┐   VendorInvoicePosted   ┌──────────────┐
│   finance-   │ ─────────────────────►  │  finance-gl  │
│      ap      │   PaymentSent           │              │
│              │ ─────────────────────►  │ (Journal     │
└──────────────┘                         │  Entries)    │
       ▲                                └──────────────┘
       │ PurchaseOrderApproved, GoodsReceived
┌──────┴───────┐
│ procurement │
└──────────────┘
       │
       ▼
┌──────────────┐
│  treasury /  │
│  banking     │
└──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Invoice Posting Latency | < 150ms p99 | > 300ms |
| 3-Way Match Latency | < 500ms p99 | > 1000ms |
| Payment Run (1000 invoices) | < 60s | > 120s |
| API Availability | 99.95% | < 99.90% |

## Compliance & Audit

- IRS 1099 reporting and withholding compliance
- VAT/GST handling where configured
- SOX 404 internal control requirements
- Full audit trail for invoices, matches, and payments
- Segregation of duties for payment approvals

## Related ADRs

- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md) - Upstream PO/GRN events
- [ADR-026: Treasury](../../adr/ADR-026-treasury-cash-management.md) - Cash management integration
