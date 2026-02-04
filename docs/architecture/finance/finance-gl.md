# Finance General Ledger (GL) - ADR-009

> **Bounded Context:** `finance-gl`
> **Port:** `8081`
> **Database:** `chiroerp_finance_gl`
> **Kafka Consumer Group:** `finance-gl-cg`

## Overview

The General Ledger subdomain is the **system of record** for financial postings. It manages the chart of accounts, accounting periods, journal entries, and trial balance, and provides the canonical source for financial statements. GL integrates with AP, AR, Assets, Tax, Treasury, and other domains via event-driven postings.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Chart of accounts, journal entries, accounting periods, trial balance |
| **Aggregates** | Ledger, ChartOfAccounts, Account, JournalEntry, PostingBatch, AccountingPeriod |
| **Key Events** | JournalEntryPostedEvent, JournalEntryReversedEvent, FinancialPeriodClosedEvent |
| **GL Integration** | Central posting target for AP/AR/Assets/Tax/Treasury |
| **Compliance** | GAAP/IFRS, SOX 404, audit trail, period controls |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [gl-domain.md](./gl/gl-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [gl-application.md](./gl/gl-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [gl-infrastructure.md](./gl/gl-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [gl-api.md](./gl/gl-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [gl-events.md](./gl/gl-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FINANCE-GL SERVICE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      APPLICATION LAYER                               │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐ │   │
│  │  │ PostJournalEntry │  │ ClosePeriod      │  │ GenerateTrialBal  │ │   │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ QueryHandler      │ │   │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘ │   │
│  │           │                    │                       │            │   │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐ │   │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │ │   │
│  │  │ LedgerRepo │ JournalRepo │ PeriodRepo │ EventPublisherPort     │ │   │
│  │  └────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                        DOMAIN LAYER                                  │   │
│  │  ┌──────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │   │
│  │  │ Ledger   │  │ ChartOfAcct  │  │ JournalEntry │  │ Accounting   │  │   │
│  │  │ Aggregate│  │ Aggregate    │  │ Aggregate    │  │ Period Agg   │  │   │
│  │  └──────────┘  └──────────────┘  └──────────────┘  └──────────────┘  │   │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐   │   │
│  │  │ PostingBatch       │  │ Domain Services                      │   │   │
│  │  │ Aggregate          │  │ Validation, Balancing, Period Ctrl  │   │   │
│  │  └────────────────────┘  └─────────────────────────────────────┘   │   │
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

### 1. Chart of Accounts Management
- Account hierarchy and numbering
- Account types and posting rules
- Segment validation and control

### 2. Journal Entry Processing
- Balanced journal entry creation and posting
- Reversals and adjustments
- Source document traceability

### 3. Accounting Period Control
- Open, soft-close, and hard-close
- Period lock enforcement across subledgers
- Period close checklist integration

### 4. Trial Balance & Reporting
- Trial balance snapshots by period
- GL account balance reporting
- Financial statement outputs

### 5. Audit & Compliance
- Immutable posting history
- Segregation of duties and approvals
- SOX-compliant controls

## Integration Points

```
┌──────────────┐  JournalEntryPosted   ┌──────────────┐
│   finance-   │ ◄───────────────────  │  finance-gl  │
│   ap/ar/     │   Posting results     │              │
│  assets/tax  │ ───────────────────►  │ (Journal     │
└──────────────┘  Posting requests     │  Entries)    │
                                      └──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Journal Entry Posting Latency | < 200ms p95 | > 400ms |
| Period Close (1M lines) | < 5 min | > 10 min |
| Trial Balance Query | < 100ms p95 | > 200ms |
| Event Publishing Latency | < 100ms p99 | > 200ms |
| API Availability | 99.99% | < 99.95% |

## Compliance & Audit

- GAAP/IFRS compliant posting rules
- SOX 404 internal control enforcement
- Full audit trail with immutable journal history
- Period close governance and approvals

## Related ADRs

- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
- [ADR-031: Period Close](../../adr/ADR-031-period-close-orchestration.md) - Close orchestration
