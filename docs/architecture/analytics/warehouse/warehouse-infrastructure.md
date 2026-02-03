# Analytics Warehouse Infrastructure Layer

> Part of [Analytics Data Warehouse](../analytics-warehouse.md)

## Directory Structure

```
warehouse-infrastructure/
`-- src/main/kotlin/com.erp.analytics.warehouse.infrastructure/
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
|-- WarehouseLoadResource.kt
|   |-- POST /api/v1/analytics/warehouse/loads   -> startLoad()
|   `-- GET  /api/v1/analytics/warehouse/loads/{id} -> getLoadStatus()
```

---

## Event Consumers

```
adapter/input/event/
|-- DomainEventIngestConsumer.kt
|   `-- Consumes: Domain events -> Stage warehouse load
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- WarehouseLoadJpaAdapter.kt     -> implements WarehouseLoadRepository
|-- DimensionJpaAdapter.kt         -> implements DimensionRepository
`-- FactJpaAdapter.kt              -> implements FactRepository
```

```
adapter/output/persistence/jpa/entity/
|-- WarehouseLoadEntity.kt
|-- DimensionEntity.kt
`-- FactPartitionEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- CdcIngestAdapter.kt
`-- DbtBuildAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- WarehouseEventPublisher.kt
|   `-- schema/
|       |-- WarehouseLoadCompletedSchema.avro
|       `-- DimensionUpdatedSchema.avro
`-- outbox/
    `-- WarehouseOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- WarehouseConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
