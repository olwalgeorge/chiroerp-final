# Manufacturing Shop Floor Execution - ADR-037

> **Bounded Context:** `manufacturing-shopfloor`
> **Port:** `9303`
> **Database:** `chiroerp_manufacturing_shopfloor`
> **Kafka Consumer Group:** `manufacturing-shopfloor-cg`

## Overview

Shop Floor Execution manages **dispatching, work center execution, and real-time confirmations** from operators or MES integrations. It synchronizes progress with Production Orders and captures labor, machine, and scrap data.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Dispatching, confirmations, labor tracking |
| **Aggregates** | WorkOrderDispatch, OperationConfirmation, LaborTicket |
| **Key Events** | OperationCompletedEvent, ScrapRecordedEvent |
| **Integration** | Production Orders, Inventory, Quality |
| **Compliance** | Traceability and audit trail |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [shopfloor-domain.md](./shopfloor/shopfloor-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [shopfloor-application.md](./shopfloor/shopfloor-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [shopfloor-infrastructure.md](./shopfloor/shopfloor-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [shopfloor-api.md](./shopfloor/shopfloor-api.md) | Endpoints and DTOs |
| **Events & Integration** | [shopfloor-events.md](./shopfloor/shopfloor-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
manufacturing-shopfloor/
|-- shopfloor-domain/
|-- shopfloor-application/
`-- shopfloor-infrastructure/
```

## Architecture Diagram

```
Dispatch -> Start Operation -> Confirm -> Report Scrap -> Close
```

## Key Business Capabilities

1. Dispatch and sequencing for work centers.
2. Real-time confirmations for operations.
3. Labor and machine time capture.
4. Scrap and yield reporting.

## Integration Points

- **Production Orders**: dispatch and confirmation updates
- **Inventory**: component consumption and byproducts
- **Quality**: inspection points and nonconformance

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Operation confirm latency | < 1s p95 | > 3s |
| Dispatch update latency | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Operator traceability
- Audit trail for scrap and rework

## Related ADRs

- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md)
