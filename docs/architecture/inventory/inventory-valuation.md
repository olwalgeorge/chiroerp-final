# Inventory Valuation & Costing - ADR-024

> **Bounded Context:** `inventory-valuation`
> **Port:** `9005`
> **Database:** `chiroerp_inventory_valuation`
> **Kafka Consumer Group:** `inventory-valuation-cg`

## Overview

Inventory Valuation owns **costing, cost layers, and financial valuation** for inventory. It calculates FIFO/WAC/Standard costs, performs landed cost allocation, and posts valuation adjustments and revaluations to GL. It consumes stock movements from Core Inventory and publishes valuation results for Finance and analytics.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Cost layers, valuation runs, landed cost, FX revaluation |
| **Aggregates** | CostLayer, ValuationRun, LandedCost, FxRevaluation |
| **Key Events** | ValuationRunCompletedEvent, LandedCostAllocatedEvent, FxRevaluationPostedEvent |
| **GL Integration** | Inventory valuation, COGS, FX gains/losses |
| **Compliance** | GAAP/IFRS valuation rules, audit trail |

## Module Documentation

This subdomain is documented in modular files for maintainability:

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [valuation-domain.md](./valuation/valuation-domain.md) | Aggregates, entities, value objects, domain events, exceptions, services |
| **Application Layer** | [valuation-application.md](./valuation/valuation-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [valuation-infrastructure.md](./valuation/valuation-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [valuation-api.md](./valuation/valuation-api.md) | REST endpoints and DTOs |
| **Events & Integration** | [valuation-events.md](./valuation/valuation-events.md) | Domain events, consumed events, GL posting rules, Avro schemas |

## Bounded Context

```
inventory-valuation/
├── valuation-domain/
├── valuation-application/
└── valuation-infrastructure/
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        INVENTORY-VALUATION SERVICE                           │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      APPLICATION LAYER                               │    │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │    │
│  │  │ RunValuation     │  │ AllocateLanded   │  │ PostFxReval       │  │    │
│  │  │ CommandHandler   │  │ CostHandler      │  │ CommandHandler    │  │    │
│  │  └────────┬─────────┘  └────────┬─────────┘  └─────────┬─────────┘  │    │
│  │           │                    │                       │             │    │
│  │  ┌────────┴────────────────────┴───────────────┬────────┴─────────┐  │    │
│  │  │                OUTPUT PORTS (Interfaces)    │                  │  │    │
│  │  │ CostLayerRepo │ FxRatePort │ GLPort │ EventPublisherPort        │  │    │
│  │  └────────────────────────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                        DOMAIN LAYER                                  │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │    │
│  │  │ CostLayer    │  │ ValuationRun │  │ LandedCost   │  │ FxReval   │ │    │
│  │  │ Aggregate    │  │ Aggregate    │  │ Aggregate    │  │ Aggregate │ │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  └───────────┘ │    │
│  │  ┌────────────────────┐  ┌─────────────────────────────────────┐    │    │
│  │  │ Costing Methods     │  │ Domain Services                      │    │    │
│  │  │ FIFO/WAC/Standard   │  │ Allocation, Revaluation, Posting     │    │    │
│  │  └────────────────────┘  └─────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                    │                                         │
│  ┌─────────────────────────────────┴───────────────────────────────────┐    │
│  │                    INFRASTRUCTURE LAYER                              │    │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │    │
│  │  │ REST API    │  │ JPA Adapters │  │ Kafka      │  │ Scheduler  │   │    │
│  │  │ (Quarkus)   │  │ (PostgreSQL) │  │ Publisher  │  │ (Batch)    │   │    │
│  │  └─────────────┘  └──────────────┘  └────────────┘  └────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Business Capabilities

### 1. Cost Layering and Valuation
- FIFO, Weighted Average, Standard Cost engines
- Cost layer creation and audit trails

### 2. Landed Cost Allocation
- Freight, duty, insurance allocation
- Rule-based distribution (quantity, value, weight)

### 3. FX Revaluation
- Period-end revaluation of inventory
- Gain/loss posting to GL

### 4. Financial Reporting
- Inventory valuation snapshots
- COGS and margin reporting support

## Integration Points

```
┌──────────────┐   Stock Movements   ┌────────────────────┐
│ inventory-   │ ──────────────────> │ inventory-valuation│
│   core       │                     │                    │
└──────────────┘                     └────────────────────┘
       ▲                                      │
       │ Valuation results                    │ GL postings
       │                                      ▼
┌──────────────┐                          ┌──────────────┐
│ analytics    │                          │ finance / GL │
└──────────────┘                          └──────────────┘
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Valuation run (1M movements) | < 10 min | > 20 min |
| Landed cost allocation | < 60s | > 120s |
| FX revaluation posting | < 5 min | > 10 min |
| API availability | 99.95% | < 99.90% |

## Compliance & Audit

- GAAP/IFRS-compliant valuation methods
- Audit trail for cost adjustments and revaluations
- Segregation of duties for cost overrides

## Related ADRs

- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md)
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md)
- [ADR-017: Performance Standards](../../adr/ADR-017-performance-standards-monitoring.md)
