# Finance Period Close - ADR-031

> **Bounded Context:** `finance-close`
> **Port:** `8091`
> **Database:** `chiroerp_finance_close`
> **Kafka Consumer Group:** `finance-close-cg`

## Overview

Period Close orchestrates financial close activities across subledgers. It manages close checklists, reconciliations, approvals, and coordinates the final period close with GL.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Close checklists, reconciliations, task orchestration |
| **Aggregates** | CloseRun, CloseChecklist, CloseTask, ReconciliationItem |
| **Key Events** | CloseRunStartedEvent, CloseTaskCompletedEvent, CloseRunCompletedEvent |
| **GL Integration** | Triggers FinancialPeriodClosed in GL |
| **Compliance** | SOX 404, audit trail, close governance |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [close-domain.md](./close/close-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [close-application.md](./close/close-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [close-infrastructure.md](./close/close-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [close-api.md](./close/close-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [close-events.md](./close/close-events.md) | Domain events, consumed events, GL integration, Avro schemas |

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FINANCE-CLOSE SERVICE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      APPLICATION LAYER                               │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐ │   │
│  │  │ StartCloseRun    │  │ CompleteTask     │  │ FinalizeClose     │ │   │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │ │   │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘ │   │
│  │           │                    │                       │            │   │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐ │   │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │ │   │
│  │  │ CloseRepo │ ChecklistRepo │ GLPort │ EventPublisherPort       │ │   │
│  │  └────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                        DOMAIN LAYER                                  │   │
│  │  ┌────────────┐  ┌──────────────┐  ┌──────────────┐                 │   │
│  │  │ CloseRun   │  │ CloseChecklist│ │ CloseTask    │                 │   │
│  │  │ Aggregate  │  │ Aggregate    │  │ Aggregate    │                 │   │
│  │  └────────────┘  └──────────────┘  └──────────────┘                 │   │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐   │   │
│  │  │ ReconciliationItem │  │ Domain Services                      │   │   │
│  │  │ Aggregate           │  │ Checklist, Period, Validation       │   │   │
│  │  └────────────────────┘  └─────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                    INFRASTRUCTURE LAYER                              │   │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │   │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ Scheduler  │   │   │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Close Orchestration
- Close run initiation and tracking
- Task dependencies and approvals

### 2. Reconciliation
- Subledger reconciliation tracking
- Variance capture and resolution

### 3. Close Governance
- Checklist enforcement
- Audit trail and approvals

## Integration Points

```
┌──────────────┐   CloseRunCompleted   ┌──────────────┐
│  finance-    │ ───────────────────►  │  finance-gl  │
│   close      │                       │              │
└──────────────┘                       └──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Close Run Completion | < 15 min | > 30 min |
| Checklist Query | < 200ms p95 | > 500ms |
| API Availability | 99.9% | < 99.8% |

## Compliance & Audit

- SOX 404 period close controls
- Immutable close run history

## Related ADRs

- [ADR-031: Period Close](../../adr/ADR-031-period-close-orchestration.md) - Close orchestration
- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
