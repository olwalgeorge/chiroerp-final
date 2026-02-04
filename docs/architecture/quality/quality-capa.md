# Quality CAPA Management - ADR-039

> **Bounded Context:** `quality-capa`
> **Port:** `9504`
> **Database:** `chiroerp_quality_capa`
> **Kafka Consumer Group:** `quality-capa-cg`

## Overview

CAPA (Corrective and Preventive Actions) manages **root-cause analysis, corrective actions, and effectiveness verification** for quality issues. It ensures systemic fixes and feeds improvements back into inspection planning and manufacturing processes.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Root cause, corrective actions, effectiveness checks |
| **Aggregates** | CAPA, RootCauseAnalysis, ActionPlan |
| **Key Events** | CAPAInitiatedEvent, RootCauseIdentifiedEvent, CAPAClosedEvent |
| **Integration** | Nonconformance, Inspection Planning, Manufacturing |
| **Compliance** | 8D/5-Why audit trail, regulated sign-off |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [capa-domain.md](./capa/capa-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [capa-application.md](./capa/capa-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [capa-infrastructure.md](./capa/capa-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [capa-api.md](./capa/capa-api.md) | Endpoints and DTOs |
| **Events & Integration** | [capa-events.md](./capa/capa-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
quality-capa/
|-- capa-domain/
|-- capa-application/
`-- capa-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                         CAPA MANAGEMENT                                |
|-----------------------------------------------------------------------|
|  NC Trigger -> Root Cause -> Action Plan -> Effectiveness -> Close     |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Structured root cause analysis (5-Why, Fishbone, 8D).
2. Corrective and preventive action planning with owners and due dates.
3. Effectiveness checks and audit-ready closure.
4. Feedback loop to inspection plans and process changes.

## Integration Points

- **Nonconformance**: critical NCs trigger CAPA.
- **Inspection Planning**: update sampling and characteristics.
- **Manufacturing**: process change requests.
- **Analytics**: CAPA effectiveness reporting.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| CAPA initiation | < 2s p95 | > 5s |
| Action plan update | < 1s p95 | > 3s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- CAPA closure requires effectiveness verification.
- Role-based approval for critical actions.
- Immutable audit log for regulated compliance.

## Related ADRs

- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md)
- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
