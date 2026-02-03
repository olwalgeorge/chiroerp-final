# Analytics Data Warehouse - ADR-016

> **Bounded Context:** `analytics-warehouse`  
> **Port:** `9801`  
> **Database:** `chiroerp_analytics_warehouse`  
> **Kafka Consumer Group:** `analytics-warehouse-cg`

## Overview

The Data Warehouse provides **historical, cross-domain analytics** using star schemas and slowly changing dimensions (SCD2). It ingests events and CDC streams, builds facts/dimensions, and serves enterprise reporting and financial consolidation.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Fact/dimension modeling, historical BI |
| **Aggregates** | WarehouseLoad, DimensionModel, FactTable |
| **Key Events** | WarehouseLoadCompletedEvent, DimensionUpdatedEvent |
| **Integration** | All domains (events/CDC), BI tools |
| **Compliance** | Audit trails, data lineage |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [warehouse-domain.md](./warehouse/warehouse-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [warehouse-application.md](./warehouse/warehouse-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [warehouse-infrastructure.md](./warehouse/warehouse-infrastructure.md) | ETL/ELT, CDC, persistence adapters |
| **REST API** | [warehouse-api.md](./warehouse/warehouse-api.md) | Admin endpoints and DTOs |
| **Events & Integration** | [warehouse-events.md](./warehouse/warehouse-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
analytics-warehouse/
|-- warehouse-domain/
|-- warehouse-application/
`-- warehouse-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                         DATA WAREHOUSE                                 |
|-----------------------------------------------------------------------|
|  CDC/Events -> Staging -> Dimensions/Facts -> BI/Reporting              |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. SCD2 dimensions (customer, product, supplier, GL).
2. Fact tables for finance, sales, procurement, manufacturing.
3. Data lineage and load audit trails.
4. Historical reporting and trend analysis.

## Integration Points

- **All domains**: ingest events and CDC streams.
- **BI tools**: reporting and dashboard consumption.
- **Analytics**: feeds OLAP and KPI engine.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Incremental load latency | < 15m p95 | > 30m |
| Full load duration | < 6h p95 | > 8h |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Lineage captured per load batch.
- Access controls on sensitive dimensions.

## Related ADRs

- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
- [ADR-015: Data Lifecycle Management](../../adr/ADR-015-data-lifecycle-management.md)
