# Finance Intercompany - ADR-029

> **Bounded Context:** `finance-intercompany`  
> **Port:** `8090`  
> **Database:** `chiroerp_finance_intercompany`  
> **Kafka Consumer Group:** `finance-intercompany-cg`

## Overview

Intercompany manages cross-entity transactions, eliminations, and netting. It ensures balanced postings across entities, automates elimination entries, and supports reconciliation between counterparties.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Intercompany transactions, eliminations, netting |
| **Aggregates** | IntercompanyAgreement, IntercompanyTransaction, NettingBatch, EliminationEntry |
| **Key Events** | IntercompanyPostedEvent, NettingCompletedEvent, EliminationPostedEvent |
| **GL Integration** | Elimination and settlement journal entries |
| **Compliance** | Transfer pricing, audit trail |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [intercompany-domain.md](./intercompany/intercompany-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [intercompany-application.md](./intercompany/intercompany-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [intercompany-infrastructure.md](./intercompany/intercompany-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [intercompany-api.md](./intercompany/intercompany-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [intercompany-events.md](./intercompany/intercompany-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      FINANCE-INTERCOMPANY SERVICE                           │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      APPLICATION LAYER                               │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐ │   │
│  │  │ PostICTransaction│  │ RunNetting       │  │ PostElimination   │ │   │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │ │   │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘ │   │
│  │           │                    │                       │            │   │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐ │   │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │ │   │
│  │  │ TransactionRepo │ NettingRepo │ GLPort │ EventPublisherPort   │ │   │
│  │  └────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                        DOMAIN LAYER                                  │   │
│  │  ┌────────────┐  ┌────────────────┐  ┌──────────────┐               │   │
│  │  │ Agreement  │  │ IC Transaction │  │ NettingBatch │               │   │
│  │  │ Aggregate  │  │ Aggregate      │  │ Aggregate    │               │   │
│  │  └────────────┘  └────────────────┘  └──────────────┘               │   │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐   │   │
│  │  │ EliminationEntry    │  │ Domain Services                      │   │   │
│  │  │ Aggregate           │  │ Netting, Matching, Pricing          │   │   │
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

### 1. Intercompany Agreements
- Counterparty terms and pricing rules
- Allowed transaction types

### 2. Intercompany Transactions
- Cross-entity postings
- Auto-balancing entries

### 3. Netting & Settlement
- Netting cycles and batches
- Settlement instructions

### 4. Eliminations
- Intercompany eliminations at close
- Audit-ready elimination trails

## Integration Points

```
┌──────────────┐   IC Transactions     ┌──────────────┐
│ intercompany │ ───────────────────►  │  finance-gl  │
│              │   Eliminations        │              │
└──────────────┘ ───────────────────►  │ (Journal     │
                                      │  Entries)    │
                                      └──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Netting Run (5k tx) | < 2 min | > 4 min |
| Intercompany Post | < 200ms p95 | > 500ms |
| API Availability | 99.9% | < 99.8% |

## Compliance & Audit

- Transfer pricing documentation
- Intercompany reconciliation controls

## Related ADRs

- [ADR-029: Intercompany](../../adr/ADR-029-intercompany-accounting.md) - Intercompany decisions
- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
