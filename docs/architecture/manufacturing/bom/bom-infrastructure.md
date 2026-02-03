# Manufacturing BOM Infrastructure Layer

> Part of [Manufacturing BOM Management](../manufacturing-bom.md)

## Directory Structure

```
bom-infrastructure/
`-- src/main/kotlin/com.erp.manufacturing.bom.infrastructure/
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
|-- BomResource.kt
|   |-- POST /api/v1/manufacturing/boms       -> createBom()
|   |-- POST /api/v1/manufacturing/boms/{id}/publish -> publishBom()
|   `-- GET  /api/v1/manufacturing/boms/{id}  -> getBom()
|
`-- RoutingResource.kt
    |-- POST /api/v1/manufacturing/routings   -> createRouting()
    `-- GET  /api/v1/manufacturing/routings/{id} -> getRouting()
```

---

## Event Consumers

```
adapter/input/event/
`-- ItemDiscontinuedEventConsumer.kt
    `-- Consumes: ItemDiscontinuedEvent -> Retire BOM
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- BomJpaAdapter.kt
`-- RoutingJpaAdapter.kt
```

```
adapter/output/persistence/jpa/entity/
|-- BomEntity.kt
|-- BomItemEntity.kt
`-- RoutingEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
`-- ItemMasterAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- BomEventPublisher.kt
|   `-- schema/
|       |-- BomPublishedSchema.avro
|       `-- RoutingUpdatedSchema.avro
`-- outbox/
    `-- BomOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- BomDependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
