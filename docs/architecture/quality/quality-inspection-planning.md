# Quality Inspection Planning - ADR-039

> **Bounded Context:** `quality-inspection-planning`
> **Port:** `9501`
> **Database:** `chiroerp_quality_inspection_planning`
> **Kafka Consumer Group:** `quality-inspection-planning-cg`

## Overview

Inspection Planning defines **what to inspect, when to inspect, and how to sample**. It owns inspection plans, characteristics, sampling rules, and trigger conditions used by Quality Execution to create inspection lots for incoming, in-process, and customer-return inspections.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Inspection plans, characteristics, sampling, trigger rules |
| **Aggregates** | InspectionPlan, InspectionCharacteristic, SamplingPlan, TriggerRule |
| **Key Events** | InspectionPlanCreatedEvent, TriggerRuleActivatedEvent |
| **Integration** | Inventory GR, Manufacturing in-process, Procurement supplier quality |
| **Compliance** | ISO/GMP auditability and plan versioning |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [inspection-planning-domain.md](./inspection-planning/inspection-planning-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [inspection-planning-application.md](./inspection-planning/inspection-planning-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [inspection-planning-infrastructure.md](./inspection-planning/inspection-planning-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [inspection-planning-api.md](./inspection-planning/inspection-planning-api.md) | Endpoints and DTOs |
| **Events & Integration** | [inspection-planning-events.md](./inspection-planning/inspection-planning-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
quality-inspection-planning/
|-- inspection-planning-domain/
|-- inspection-planning-application/
`-- inspection-planning-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                   QUALITY INSPECTION PLANNING                         |
|-----------------------------------------------------------------------|
|  Plans + Characteristics + Sampling Rules -> Trigger Rules -> Lots    |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Versioned inspection plans with effectivity dates.
2. Quantitative and qualitative inspection characteristics.
3. Sampling plans (fixed, AQL, skip-lot) with dynamic adjustment.
4. Trigger rules for incoming, in-process, and returns inspection.

## Integration Points

- **Inventory**: Goods receipt triggers against active plans.
- **Manufacturing**: In-process inspection triggers by operation.
- **Procurement**: Supplier quality history affects sampling rules.
- **Sales/Returns**: Customer returns inspection triggers.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Inspection plan lookup | < 200ms p95 | > 500ms |
| Trigger evaluation | < 1s p95 | > 3s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Plan version history and approval trail.
- Sampling rule changes require justification.
- Effectivity dates prevent retroactive changes.

## Related ADRs

- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
