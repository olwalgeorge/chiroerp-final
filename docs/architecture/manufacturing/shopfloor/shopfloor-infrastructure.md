# Manufacturing Shop Floor Infrastructure Layer

> Part of [Manufacturing Shop Floor Execution](../manufacturing-shop-floor.md)

## Directory Structure

```
shopfloor-infrastructure/
`-- src/main/kotlin/com.erp.manufacturing.shopfloor.infrastructure/
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
|-- DispatchResource.kt
|   |-- POST /api/v1/manufacturing/shopfloor/dispatch -> dispatch()
|   `-- GET  /api/v1/manufacturing/shopfloor/dispatch -> listQueue()
|
|-- OperationResource.kt
|   |-- POST /api/v1/manufacturing/shopfloor/operations/start -> start()
|   `-- POST /api/v1/manufacturing/shopfloor/operations/complete -> complete()
|
`-- ScrapResource.kt
    `-- POST /api/v1/manufacturing/shopfloor/scrap -> recordScrap()
```

---

## Event Consumers

```
adapter/input/event/
`-- ProductionOrderReleasedEventConsumer.kt
    `-- Consumes: ProductionOrderReleasedEvent -> Create dispatch
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- DispatchJpaAdapter.kt
`-- ConfirmationJpaAdapter.kt
```

```
adapter/output/persistence/jpa/entity/
|-- DispatchEntity.kt
|-- ConfirmationEntity.kt
`-- ScrapRecordEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
`-- ProductionOrderAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- ShopFloorEventPublisher.kt
|   `-- schema/
|       |-- OperationCompletedSchema.avro
|       `-- ScrapRecordedSchema.avro
`-- outbox/
    `-- ShopFloorOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- ShopFloorDependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
