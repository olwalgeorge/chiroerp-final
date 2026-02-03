# Finance Domain Architecture

This directory contains detailed hexagonal architecture specifications for each Finance subdomain.

## Subdomain Index

| Subdomain | ADR | Tier | File | Description |
|-----------|-----|------|------|-------------|
| General Ledger | ADR-009 | Core | [finance-gl.md](./finance-gl.md) | Chart of Accounts, Journal Entries, Trial Balance |
| Accounts Receivable | ADR-009 | Core | [finance-ar.md](./finance-ar.md) | Invoicing, Payments, Aging, Dunning |
| Accounts Payable | ADR-009 | Core | [finance-ap.md](./finance-ap.md) | Vendor Bills, Payments, 3-Way Match |
| Fixed Assets | ADR-021 | Core | [finance-assets.md](./finance-assets.md) | Depreciation, Capitalization, Disposal |
| Revenue Recognition | ADR-022 | Advanced | [finance-revenue.md](./finance-revenue.md) | ASC 606, POC, Deferred Revenue |
| Tax Engine | ADR-030 | Core | [finance-tax.md](./finance-tax.md) | Sales Tax, VAT, Withholding |
| Controlling | ADR-028 | Advanced | [finance-controlling.md](./finance-controlling.md) | Cost Centers, Profit Centers, Internal Orders |
| Treasury | ADR-026 | Advanced | [finance-treasury.md](./finance-treasury.md) | Cash Management, Bank Reconciliation |
| Intercompany | ADR-029 | Advanced | [finance-intercompany.md](./finance-intercompany.md) | IC Transactions, Elimination, Netting |
| Period Close | ADR-031 | Core | [finance-close.md](./finance-close.md) | Soft/Hard Close, Checklists, Reconciliation |
| FP&A | ADR-032 | Add-on | [finance-planning.md](./finance-planning.md) | Budgeting, Forecasting, Variance Analysis |
| Lease Accounting | ADR-033 | Add-on | [finance-lease.md](./finance-lease.md) | ASC 842, IFRS 16, ROU Assets |

## Documentation Structure

- **Detailed subfolders (complex)**: AP, AR, Assets, GL, Controlling, Revenue, Intercompany, Period Close, Lease
- **Inline docs (simple)**: Tax, Treasury, FP&A

## Integration Map

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FINANCE DOMAIN                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│    ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐         │
│    │    AR    │────▶│    GL    │◀────│    AP    │     │   Tax    │         │
│    └──────────┘     └──────────┘     └──────────┘     └──────────┘         │
│         │                │                │                │                │
│         │                │                │                │                │
│         ▼                ▼                ▼                ▼                │
│    ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐         │
│    │ Treasury │     │Controlling│    │  Assets  │     │ Revenue  │         │
│    └──────────┘     └──────────┘     └──────────┘     └──────────┘         │
│         │                │                │                │                │
│         └────────────────┼────────────────┼────────────────┘                │
│                          ▼                ▼                                 │
│                    ┌──────────┐     ┌──────────┐                           │
│                    │Intercompany│   │  Close   │                           │
│                    └──────────┘     └──────────┘                           │
│                          │                │                                 │
│                          ▼                ▼                                 │
│                    ┌──────────┐     ┌──────────┐                           │
│                    │   FP&A   │     │  Lease   │                           │
│                    └──────────┘     └──────────┘                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Event Flows

### AR → GL Flow
```
InvoicePostedEvent → GL JournalEntry (Debit: AR, Credit: Revenue)
PaymentReceivedEvent → GL JournalEntry (Debit: Cash, Credit: AR)
```

### AP → GL Flow
```
BillPostedEvent → GL JournalEntry (Debit: Expense/Inventory, Credit: AP)
PaymentSentEvent → GL JournalEntry (Debit: AP, Credit: Cash)
```

### Assets → GL Flow
```
AssetCapitalizedEvent → GL JournalEntry (Debit: Asset, Credit: AP/Cash)
DepreciationRunEvent → GL JournalEntry (Debit: Depreciation Expense, Credit: Accumulated Depreciation)
```

## Technology Stack

- **Language**: Kotlin 1.9+
- **Framework**: Quarkus 3.x
- **Messaging**: Apache Kafka with Avro schemas
- **Database**: PostgreSQL (write model), MongoDB (read model)
- **Cache**: Redis
- **Search**: Elasticsearch
