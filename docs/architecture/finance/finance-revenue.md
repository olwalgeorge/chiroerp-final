# Finance Revenue Recognition - ADR-022

> **Bounded Context:** `finance-revenue`
> **Port:** `8086`
> **Database:** `chiroerp_finance_revenue`
> **Kafka Consumer Group:** `finance-revenue-cg`

## Overview

Revenue Recognition manages ASC 606/IFRS 15 compliance: contract identification, performance obligations, transaction price allocation, and revenue schedules. It posts recognized and deferred revenue to GL and supports audit-ready disclosures.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Contracts, performance obligations, revenue schedules |
| **Aggregates** | Contract, PerformanceObligation, RevenueSchedule, DeferredRevenue |
| **Key Events** | RevenueRecognizedEvent, RevenueDeferredEvent, ContractModifiedEvent |
| **GL Integration** | Recognized revenue and deferred revenue postings |
| **Compliance** | ASC 606, IFRS 15 |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [revenue-domain.md](./revenue/revenue-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [revenue-application.md](./revenue/revenue-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [revenue-infrastructure.md](./revenue/revenue-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [revenue-api.md](./revenue/revenue-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [revenue-events.md](./revenue/revenue-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       FINANCE-REVENUE SERVICE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      APPLICATION LAYER                               │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐ │   │
│  │  │ IdentifyContract │  │ AllocatePrice    │  │ RecognizeRevenue  │ │   │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │ │   │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘ │   │
│  │           │                    │                       │            │   │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐ │   │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │ │   │
│  │  │ ContractRepo │ ScheduleRepo │ GLPort │ EventPublisherPort     │ │   │
│  │  └────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                        DOMAIN LAYER                                  │   │
│  │  ┌────────────┐  ┌────────────────┐  ┌────────────────┐              │   │
│  │  │ Contract   │  │ Performance    │  │ RevenueSchedule│              │   │
│  │  │ Aggregate  │  │ Obligation Agg │  │ Aggregate      │              │   │
│  │  └────────────┘  └────────────────┘  └────────────────┘              │   │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐   │   │
│  │  │ DeferredRevenue     │  │ Domain Services                      │   │   │
│  │  │ Aggregate           │  │ Allocation, Recognition, Reallocation│   │   │
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

### 1. Contract Identification
- Contract creation and modification
- Term and price change tracking

### 2. Performance Obligations
- Obligation definition and satisfaction
- Standalone selling price allocation

### 3. Revenue Scheduling
- Straight-line and usage-based schedules
- Catch-up and true-up adjustments

### 4. Deferred Revenue Management
- Contract asset/liability tracking
- Disclosure-ready balances

### 5. Audit & Compliance
- ASC 606/IFRS 15 audit trail
- Versioned contract history

## Integration Points

```
┌──────────────┐   RevenueRecognized   ┌──────────────┐
│  revenue     │ ───────────────────►  │  finance-gl  │
│              │   RevenueDeferred     │              │
└──────────────┘ ───────────────────►  │ (Journal     │
       ▲                               │  Entries)    │
       │ Sales/Billing Events          └──────────────┘
┌──────┴───────┐
│ commerce     │
└──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Revenue Schedule Calc | < 2s p95 | > 5s |
| Contract Update | < 200ms p95 | > 500ms |
| API Availability | 99.9% | < 99.8% |

## Compliance & Audit

- ASC 606 / IFRS 15 compliance
- Immutable schedule history
- Period close alignment with GL

## Related ADRs

- [ADR-022: Revenue Recognition](../../adr/ADR-022-revenue-recognition.md) - Revenue domain decisions
- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
