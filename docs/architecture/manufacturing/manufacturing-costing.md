# Manufacturing Costing - ADR-037

> **Bounded Context:** `manufacturing-costing`
> **Port:** `9305`
> **Database:** `chiroerp_manufacturing_costing`
> **Kafka Consumer Group:** `manufacturing-costing-cg`

## Overview

Costing owns **product cost rollups, WIP settlement, and variance analysis**. It integrates with Controlling for cost postings and provides standard cost updates to BOM and Production Orders.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Standard cost, WIP, variances |
| **Aggregates** | CostEstimate, WIPBalance, VarianceRecord |
| **Key Events** | CostRollupCompletedEvent, WIPPostedEvent, ProductionVariancePostedEvent |
| **Integration** | Production Orders, Controlling, Inventory |
| **Compliance** | SOX controls for postings |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [costing-domain.md](./costing/costing-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [costing-application.md](./costing/costing-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [costing-infrastructure.md](./costing/costing-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [costing-api.md](./costing/costing-api.md) | Endpoints and DTOs |
| **Events & Integration** | [costing-events.md](./costing/costing-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
manufacturing-costing/
|-- costing-domain/
|-- costing-application/
`-- costing-infrastructure/
```

## Architecture Diagram

```
Cost Rollup -> WIP Posting -> Variance Settlement -> CO Posting
```

## Key Business Capabilities

1. Standard cost rollups from BOM and routing.
2. WIP accumulation and settlement.
3. Variance analysis by order, material, and work center.
4. Posting integration with Controlling.

## Integration Points

- **Production Orders**: confirmations and receipts
- **Controlling**: CO postings and variances
- **Inventory**: valuation updates

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Cost rollup duration | < 30m p95 | > 60m |
| WIP posting latency | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- SOX controls for WIP and variance postings
- Audit trail for standard cost updates

## Related ADRs

- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md)
