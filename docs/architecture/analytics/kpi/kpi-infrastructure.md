# Analytics KPI Infrastructure Layer

> Part of [Analytics KPI Engine](../analytics-kpi.md)

## Directory Structure

```
kpi-infrastructure/
`-- src/main/kotlin/com.erp.analytics.kpi.infrastructure/
    |-- adapter/
    |   |-- input/
    |   |   |-- rest/
    |   |   `-- event/
    |   `-- output/
    |       |-- persistence/
    |       |-- integration/
    |       `-- messaging/
    |-- configuration/
    `-- resources/
```

---

## REST Adapters (Primary/Driving)

```
adapter/input/rest/
|-- KpiResource.kt
|   |-- POST /api/v1/analytics/kpis          -> defineKpi()
|   |-- GET  /api/v1/analytics/kpis/{id}     -> getKpi()
|   `-- GET  /api/v1/analytics/kpis          -> listKpis()
```

---

## Event Consumers

```
adapter/input/event/
`-- CubeRefreshedEventConsumer.kt
    `-- Consumes: CubeRefreshedEvent -> Recalculate KPI
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- KpiJpaAdapter.kt           -> implements KpiRepository
`-- KpiResultJpaAdapter.kt     -> implements KpiResultRepository
```

```
adapter/output/persistence/jpa/entity/
|-- KpiEntity.kt
`-- KpiResultEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- WarehouseMetricAdapter.kt
`-- OlapAggregateAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- KpiEventPublisher.kt
|   `-- schema/
|       |-- KpiCalculatedSchema.avro
|       `-- KpiThresholdBreachedSchema.avro
`-- outbox/
    `-- KpiOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- KpiConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
