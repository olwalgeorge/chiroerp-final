# Finance Controlling (CO) - ADR-028

> **Bounded Context:** `finance-controlling`  
> **Port:** `8088`  
> **Database:** `chiroerp_finance_controlling`  
> **Kafka Consumer Group:** `finance-controlling-cg`

## Overview

The Controlling subdomain provides internal management accounting: cost centers, profit centers, internal orders, activity rates, and allocation cycles. It consumes GL postings and produces allocation entries back to GL to support management reporting and profitability analysis.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Cost/profit centers, internal orders, allocations, activity rates |
| **Aggregates** | CostCenter, ProfitCenter, InternalOrder, AllocationCycle, AllocationRun |
| **Key Events** | AllocationRunPostedEvent, CostCenterCreatedEvent, InternalOrderClosedEvent |
| **GL Integration** | Allocation postings and management adjustments |
| **Compliance** | SOX 404, audit trail, segregation of duties |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [controlling-domain.md](./controlling/controlling-domain.md) | Aggregates, entities, value objects, domain events, exceptions, domain services |
| **Application Layer** | [controlling-application.md](./controlling/controlling-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [controlling-infrastructure.md](./controlling/controlling-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [controlling-api.md](./controlling/controlling-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [controlling-events.md](./controlling/controlling-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       FINANCE-CONTROLLING SERVICE                           │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      APPLICATION LAYER                               │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐ │   │
│  │  │ RunAllocation    │  │ MaintainCostCtr │  │ MaintainOrder     │ │   │
│  │  │ CommandHandler   │  │ CommandHandler  │  │ CommandHandler    │ │   │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘ │   │
│  │           │                    │                       │            │   │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐ │   │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │ │   │
│  │  │ CostCenterRepo │ AllocationRepo │ GLPort │ EventPublisherPort  │ │   │
│  │  └────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────┴───────────────────────────────────┐   │
│  │                        DOMAIN LAYER                                  │   │
│  │  ┌────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │   │
│  │  │ CostCenter │  │ ProfitCenter │  │ InternalOrder│  │ Allocation  │ │   │
│  │  │ Aggregate  │  │ Aggregate    │  │ Aggregate    │  │ Cycle Agg   │ │   │
│  │  └────────────┘  └──────────────┘  └──────────────┘  └─────────────┘ │   │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐   │   │
│  │  │ AllocationRun      │  │ Domain Services                      │   │   │
│  │  │ Aggregate          │  │ Rules, Validation, Activity Rates    │   │   │
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

### 1. Cost & Profit Centers
- Hierarchy and responsibility assignments
- Validity dates and status control
- Manager and org-unit linkage

### 2. Internal Orders
- Project and operational order tracking
- Budget control and settlement
- Order lifecycle management

### 3. Allocations & Activity Rates
- Allocation cycles and rules
- Activity rate definitions
- Periodic allocation runs and postings

### 4. Plan vs Actual
- Budget capture and approvals
- Plan/actual variance reporting
- Management adjustment entries

### 5. Reporting
- Cost center reports
- Profitability analysis
- Internal order status dashboards

## Integration Points

```
┌──────────────┐   Actuals Posted      ┌──────────────┐
│  finance-gl  │ ───────────────────►  │ controlling  │
│              │                       │              │
└──────────────┘  Allocation Entries   │              │
        ▲       ◄───────────────────   └──────────────┘
        │
        ▼
┌──────────────┐
│   finance-   │
│  ap/ar/assets│
└──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Allocation Run (10k lines) | < 2 min | > 4 min |
| Cost Center Query | < 100ms p95 | > 200ms |
| Allocation Posting Latency | < 200ms p99 | > 400ms |
| API Availability | 99.95% | < 99.90% |

## Compliance & Audit

- SOX 404 internal control requirements
- Audit trail for allocation runs and approvals
- Segregation of duties for budget approvals

## Related ADRs

- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md) - Management accounting
- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
