# Quality Nonconformance - ADR-039

> **Bounded Context:** `quality-nonconformance`
> **Port:** `9503`
> **Database:** `chiroerp_quality_nonconformance`
> **Kafka Consumer Group:** `quality-nonconformance-cg`

## Overview

Nonconformance Management tracks **quality issues, dispositions, and cost impacts**. It coordinates rework, scrap, or return decisions and posts quality costs to Finance/Controlling.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Defects, dispositions, quality costs |
| **Aggregates** | Nonconformance, Disposition, QualityCost |
| **Key Events** | NonconformanceCreatedEvent, DispositionDeterminedEvent, QualityCostPostedEvent |
| **Integration** | Quality Execution, Inventory, Finance/CO, Procurement |
| **Compliance** | NC traceability, root cause linkage |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [nonconformance-domain.md](./nonconformance/nonconformance-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [nonconformance-application.md](./nonconformance/nonconformance-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [nonconformance-infrastructure.md](./nonconformance/nonconformance-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [nonconformance-api.md](./nonconformance/nonconformance-api.md) | Endpoints and DTOs |
| **Events & Integration** | [nonconformance-events.md](./nonconformance/nonconformance-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
quality-nonconformance/
|-- nonconformance-domain/
|-- nonconformance-application/
`-- nonconformance-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                     NONCONFORMANCE MANAGEMENT                          |
|-----------------------------------------------------------------------|
|  Defect -> NC Record -> Disposition -> Cost Posting -> CAPA (if needed)
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Record defects with severity and classification.
2. Disposition workflows (rework, scrap, return to vendor).
3. Quality cost capture and posting.
4. Trigger CAPA for critical issues.

## Integration Points

- **Quality Execution**: defect detection and NC creation.
- **Inventory**: scrap and rework transactions.
- **Finance/CO**: quality cost postings.
- **Procurement**: vendor notifications and claims.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| NC creation | < 1s p95 | > 3s |
| Disposition decision | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- NC records are immutable after closure.
- Disposition approvals require role-based sign-off.
- Traceability from defect to corrective action.

## Related ADRs

- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md)
- [ADR-028: Controlling](../../adr/ADR-028-controlling-management-accounting.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
