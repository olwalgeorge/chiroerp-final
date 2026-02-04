# Analytics OLAP & Cube Engine - ADR-016

> **Bounded Context:** `analytics-olap`
> **Port:** `9802`
> **Database:** `chiroerp_analytics_olap`
> **Kafka Consumer Group:** `analytics-olap-cg`

## Overview

The OLAP & Cube Engine provides **aggregations, dimensional cubes, and query acceleration** for executive dashboards and ad-hoc analytics. It consumes warehouse facts and builds pre-aggregated cubes.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Cubes, aggregations, dimensional analysis |
| **Aggregates** | CubeDefinition, CubeRefresh, AggregateSnapshot |
| **Key Events** | CubeRefreshedEvent, AggregateSnapshotPublishedEvent |
| **Integration** | Data Warehouse, KPI Engine |
| **Compliance** | Aggregation audit logs |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [olap-domain.md](./olap/olap-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [olap-application.md](./olap/olap-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [olap-infrastructure.md](./olap/olap-infrastructure.md) | Cube storage, caching adapters |
| **REST API** | [olap-api.md](./olap/olap-api.md) | Cube admin/query endpoints |
| **Events & Integration** | [olap-events.md](./olap/olap-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
analytics-olap/
|-- olap-domain/
|-- olap-application/
`-- olap-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                          OLAP/CUBE ENGINE                              |
|-----------------------------------------------------------------------|
|  Warehouse Facts -> Cubes -> Aggregations -> Dashboards                |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Cube definitions by domain and metric.
2. Incremental cube refresh and cache invalidation.
3. High-performance dimensional queries.
4. Pre-aggregated snapshots for dashboards.

## Integration Points

- **Data Warehouse**: source facts/dimensions.
- **KPI Engine**: aggregated metrics.
- **Dashboards**: pre-aggregated query results.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Cube refresh duration | < 30m p95 | > 60m |
| Query latency | < 500ms p95 | > 1s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Cube refresh logs retained.
- Access controls by tenant and domain.

## Related ADRs

- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
