# Quality Execution - ADR-039

> **Bounded Context:** `quality-execution`  
> **Port:** `9502`  
> **Database:** `chiroerp_quality_execution`  
> **Kafka Consumer Group:** `quality-execution-cg`

## Overview

Quality Execution owns **inspection lots, result recording, and usage decisions**. It blocks or releases inventory based on inspection outcomes and triggers nonconformance workflows when defects are detected.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Inspection lots, results, usage decisions, stock blocking |
| **Aggregates** | InspectionLot, InspectionResult, UsageDecision |
| **Key Events** | InspectionLotCreatedEvent, InspectionCompletedEvent, StockBlockedEvent |
| **Integration** | Inventory, Manufacturing, Procurement, Sales Returns |
| **Compliance** | Inspector traceability, electronic signatures |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [execution-domain.md](./execution/execution-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [execution-application.md](./execution/execution-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [execution-infrastructure.md](./execution/execution-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [execution-api.md](./execution/execution-api.md) | Endpoints and DTOs |
| **Events & Integration** | [execution-events.md](./execution/execution-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
quality-execution/
|-- execution-domain/
|-- execution-application/
`-- execution-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                         QUALITY EXECUTION                              |
|-----------------------------------------------------------------------|
|  Triggers -> Inspection Lots -> Results -> Usage Decision -> Stock     |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Automatic inspection lot creation from trigger events.
2. Results capture with quantitative/qualitative checks.
3. Usage decisions (accept, reject, rework) with audit trail.
4. Inventory blocking and release based on quality status.

## Integration Points

- **Inventory**: Stock block/release and lot status updates.
- **Manufacturing**: In-process inspection and hold points.
- **Procurement**: Incoming inspection for suppliers.
- **Sales**: Return inspection and defect reporting.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Inspection lot creation | < 1s p95 | > 3s |
| Result recording | < 500ms p95 | > 1s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Electronic signatures for usage decisions.
- Inspector and timestamp traceability.
- Immutable result history after decision.

## Related ADRs

- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
