# Manufacturing Production Orders Infrastructure Layer

> Part of [Manufacturing Production Orders](../manufacturing-production.md)

## Directory Structure

```
production-infrastructure/
`-- src/main/kotlin/com.erp.manufacturing.production.infrastructure/
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
|-- ProductionOrderResource.kt
|   |-- POST /api/v1/manufacturing/production/orders        -> createOrder()
|   |-- POST /api/v1/manufacturing/production/orders/{id}/release -> releaseOrder()
|   |-- POST /api/v1/manufacturing/production/orders/{id}/issue   -> issueMaterial()
|   |-- POST /api/v1/manufacturing/production/orders/{id}/confirm -> confirmOperation()
|   `-- POST /api/v1/manufacturing/production/orders/{id}/receipt -> postReceipt()
```

---

## Event Consumers

```
adapter/input/event/
|-- PlannedOrderCreatedEventConsumer.kt
|   `-- Consumes: PlannedOrderCreatedEvent -> Create production order
|
|-- CapacityConstraintDetectedConsumer.kt
|   `-- Consumes: CapacityConstraintDetectedEvent -> Reschedule
|
`-- RoutingUpdatedEventConsumer.kt
    `-- Consumes: RoutingUpdatedEvent -> Update operations
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
`-- ProductionOrderJpaAdapter.kt    -> implements ProductionOrderRepository
```

```
adapter/output/persistence/jpa/entity/
|-- ProductionOrderEntity.kt
|-- OperationEntity.kt
|-- MaterialIssueEntity.kt
`-- ReceiptEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- InventoryAdapter.kt
|-- ShopFloorAdapter.kt
|-- CostingAdapter.kt
`-- BomAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- ProductionEventPublisher.kt
|   `-- schema/
|       |-- ProductionOrderReleasedSchema.avro
|       `-- ProductionReceiptPostedSchema.avro
`-- outbox/
    `-- ProductionOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- ProductionDependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
