# Finance Accounts Receivable (AR) - ADR-009

> **Bounded Context:** `finance-ar`  
> **Port:** `8082`  
> **Database:** `chiroerp_finance_ar`  
> **Kafka Consumer Group:** `finance-ar-cg`

## Overview

Accounts Receivable manages the **customer-facing financial transactions** including invoicing, payment collection, credit management, aging analysis, and dunning (collection) processes. AR posts to GL and integrates with Sales Orders and Treasury.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Customer invoicing, collections, credit, dunning, aging |
| **Aggregates** | CustomerAccount, Invoice, Payment, DunningRun, AgingSnapshot, CreditMemo |
| **Key Events** | InvoicePostedEvent, PaymentReceivedEvent, DunningNoticeGeneratedEvent, CreditMemoIssuedEvent |
| **GL Integration** | Invoice posted (DR AR, CR Revenue/Tax); payment received (DR Cash/Bank, CR AR) |
| **Compliance** | SOX 404 controls, audit trail, customer data privacy |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [ar-domain.md](./ar/ar-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [ar-application.md](./ar/ar-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [ar-infrastructure.md](./ar/ar-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [ar-api.md](./ar/ar-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [ar-events.md](./ar/ar-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |

## Bounded Context

```
finance-ar/
├── ar-domain/
├── ar-application/
└── ar-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FINANCE-AR SERVICE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      APPLICATION LAYER                               │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │   │
│  │  │ PostInvoice      │  │ RecordPayment    │  │ RunDunning        │  │   │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │  │   │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │   │
│  │           │                    │                       │             │   │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │   │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │   │
│  │  │ InvoiceRepo │ PaymentRepo │ GLPort │ NotificationPort          │  │   │
│  │  └────────────────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                        DOMAIN LAYER                                  │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌──────────────┐    │   │
│  │  │CustomerAcct│  │ Invoice    │  │ Payment    │  │ DunningRun   │    │   │
│  │  │ Aggregate  │  │ Aggregate  │  │ Aggregate  │  │ Aggregate    │    │   │
│  │  └────────────┘  └────────────┘  └────────────┘  └──────────────┘    │   │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │   │
│  │  │ Aging / Credit      │  │ Domain Services                      │    │   │
│  │  │ Aggregates          │  │ Allocation, Dunning, Credit Check   │    │   │
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

### 1. Customer & Credit Management
- Customer master data and segments
- Credit limits, holds, and approvals
- Dunning profiles and escalation rules

### 2. Invoice & Billing
- Invoice creation and posting
- Tax calculation and adjustments
- Credit/debit memo handling

### 3. Collections & Dunning
- Automated dunning runs
- Collection cases and workflows
- Customer statements and reminders

### 4. Cash Application
- Payment recording and allocation
- Lockbox/auto-match support
- Cash discounts and write-offs

### 5. Aging & Analytics
- Aging snapshots and buckets
- DSO metrics and trend reporting
- Cash forecast visibility

## Integration Points

```
┌──────────────┐   InvoicePosted         ┌──────────────┐
│   finance-   │ ─────────────────────►  │  finance-gl  │
│      ar      │   PaymentReceived       │              │
│              │ ─────────────────────►  │ (Journal     │
└──────────────┘                         │  Entries)    │
       ▲                                └──────────────┘
       │ SalesOrderFulfilled, ContractBilled
┌──────┴───────┐
│   sales /   │
│  commerce   │
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
| Payment Application Latency | < 200ms p99 | > 400ms |
| Dunning Run (1000 accounts) | < 60s | > 120s |
| Aging Report Query | < 100ms p95 | > 200ms |
| API Availability | 99.95% | < 99.90% |

## Compliance & Audit

- SOX 404 internal control requirements
- Audit trail for invoices, payments, and adjustments
- Data privacy controls for customer PII
- Segregation of duties for credit and write-off approvals

## Related ADRs

- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md) - Upstream order events
- [ADR-026: Treasury](../../adr/ADR-026-treasury-cash-management.md) - Cash management integration
