# Analytics OLAP Infrastructure Layer

> Part of [Analytics OLAP & Cube Engine](../analytics-olap.md)

## Directory Structure

```
olap-infrastructure/
`-- src/main/kotlin/com.erp.analytics.olap.infrastructure/
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
|-- CubeResource.kt
|   |-- POST /api/v1/analytics/olap/cubes      -> defineCube()
|   |-- GET  /api/v1/analytics/olap/cubes/{id} -> getCube()
|   `-- POST /api/v1/analytics/olap/cubes/{id}/refresh -> refreshCube()
```

---

## Event Consumers

```
adapter/input/event/
`-- WarehouseLoadCompletedEventConsumer.kt
    `-- Consumes: WarehouseLoadCompletedEvent -> Refresh cubes
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- CubeJpaAdapter.kt         -> implements CubeRepository
`-- CubeSnapshotJpaAdapter.kt -> implements SnapshotRepository
```

```
adapter/output/persistence/jpa/entity/
|-- CubeEntity.kt
`-- CubeSnapshotEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
`-- WarehouseFactAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- OlapEventPublisher.kt
|   `-- schema/
|       |-- CubeRefreshedSchema.avro
|       `-- AggregateSnapshotSchema.avro
`-- outbox/
    `-- OlapOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- OlapConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
