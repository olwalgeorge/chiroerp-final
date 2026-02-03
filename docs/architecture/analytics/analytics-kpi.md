# Analytics KPI Engine - ADR-016

> **Bounded Context:** `analytics-kpi`  
> **Port:** `9803`  
> **Database:** `chiroerp_analytics_kpi`  
> **Kafka Consumer Group:** `analytics-kpi-cg`

## Overview

The KPI Engine computes **cross-domain metrics and thresholds** for executive reporting and operational KPIs. It aggregates facts from the warehouse and cubes, then publishes KPI updates.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | KPI definitions, thresholds, alerts |
| **Aggregates** | KPIDefinition, KPIResult, KpiAlert |
| **Key Events** | KpiCalculatedEvent, KpiThresholdBreachedEvent |
| **Integration** | Data Warehouse, OLAP, Dashboards |
| **Compliance** | KPI calculation auditability |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [kpi-domain.md](./kpi/kpi-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [kpi-application.md](./kpi/kpi-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [kpi-infrastructure.md](./kpi/kpi-infrastructure.md) | Scheduler, persistence, messaging adapters |
| **REST API** | [kpi-api.md](./kpi/kpi-api.md) | KPI admin and query endpoints |
| **Events & Integration** | [kpi-events.md](./kpi/kpi-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
analytics-kpi/
|-- kpi-domain/
|-- kpi-application/
`-- kpi-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                             KPI ENGINE                                |
|-----------------------------------------------------------------------|
|  Facts/Cubes -> KPI Rules -> KPI Results -> Alerts/Dashboards          |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. KPI definitions with calculation logic and thresholds.
2. Scheduled KPI computation and alerting.
3. KPI result history and trend analysis.
4. Threshold breach notifications.

## Integration Points

- **Data Warehouse**: source metrics.
- **OLAP Engine**: aggregated inputs.
- **Dashboards**: KPI display and alerts.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| KPI computation | < 2m p95 | > 5m |
| KPI query latency | < 300ms p95 | > 800ms |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- KPI formulas are versioned.
- Calculation runs logged.

## Related ADRs

- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
