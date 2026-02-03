# Finance Lease Accounting - ADR-033

> **Bounded Context:** `finance-lease`  
> **Port:** `8093`  
> **Database:** `chiroerp_finance_lease`  
> **Kafka Consumer Group:** `finance-lease-cg`

## Overview

Lease Accounting manages lease contracts, right-of-use (ROU) assets, and lease liability schedules under ASC 842 and IFRS 16. It posts amortization and interest entries to GL and integrates with Assets.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Lease contracts, ROU assets, liability schedules |
| **Aggregates** | LeaseContract, LeaseSchedule, ROUAsset, LeaseLiability |
| **Key Events** | LeaseActivatedEvent, LeaseAmortizationPostedEvent, LeaseRemeasuredEvent |
| **GL Integration** | Lease amortization and interest postings |
| **Compliance** | ASC 842, IFRS 16 |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [lease-domain.md](./lease/lease-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [lease-application.md](./lease/lease-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [lease-infrastructure.md](./lease/lease-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [lease-api.md](./lease/lease-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [lease-events.md](./lease/lease-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          FINANCE-LEASE SERVICE                              │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      APPLICATION LAYER                               │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐ │   │
│  │  │ CreateLease      │  │ PostAmortization │  │ RemeasureLease    │ │   │
│  │  │ CommandHandler   │  │ CommandHandler   │  │ CommandHandler    │ │   │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘ │   │
│  │           │                    │                       │            │   │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐ │   │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │ │   │
│  │  │ LeaseRepo │ ScheduleRepo │ GLPort │ EventPublisherPort        │ │   │
│  │  └────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                        DOMAIN LAYER                                  │   │
│  │  ┌────────────┐  ┌──────────────┐  ┌──────────────┐                 │   │
│  │  │ Lease      │  │ ROU Asset     │  │ LeaseSchedule│                │   │
│  │  │ Contract   │  │ Aggregate     │  │ Aggregate    │                │   │
│  │  └────────────┘  └──────────────┘  └──────────────┘                 │   │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐   │   │
│  │  │ LeaseLiability     │  │ Domain Services                      │   │   │
│  │  │ Aggregate          │  │ Amortization, Remeasurement          │   │   │
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

### 1. Lease Lifecycle
- Lease creation and activation
- Classification and term updates

### 2. Amortization & Interest
- Periodic amortization schedules
- Interest expense calculations

### 3. ROU Asset & Liability
- ROU asset tracking
- Liability roll-forward

## Integration Points

```
┌──────────────┐   LeaseAmortization   ┌──────────────┐
│  finance-    │ ───────────────────►  │  finance-gl  │
│    lease     │                       │              │
└──────────────┘                       └──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Amortization Run (5k leases) | < 3 min | > 6 min |
| Lease Update | < 200ms p95 | > 500ms |
| API Availability | 99.9% | < 99.8% |

## Compliance & Audit

- ASC 842 / IFRS 16 compliance
- Audit trail for lease modifications

## Related ADRs

- [ADR-033: Lease Accounting](../../adr/ADR-033-lease-accounting-ifrs16.md) - Lease domain decisions
- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
