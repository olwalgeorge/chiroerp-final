# Manufacturing MRP Infrastructure Layer

> Part of [Manufacturing MRP](../manufacturing-mrp.md)

## Directory Structure

```
mrp-infrastructure/
`-- src/main/kotlin/com.erp.manufacturing.mrp.infrastructure/
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
|-- MRPRunResource.kt
|   |-- POST /api/v1/manufacturing/mrp/runs         -> runMRP()
|   `-- GET  /api/v1/manufacturing/mrp/runs/{id}    -> getRun()
|
|-- PlannedOrderResource.kt
|   |-- GET  /api/v1/manufacturing/mrp/planned-orders -> listPlannedOrders()
|   `-- POST /api/v1/manufacturing/mrp/planned-orders/convert -> convertPlannedOrder()
```

---

## Event Consumers

```
adapter/input/event/
|-- SalesOrderAllocatedEventConsumer.kt
|   `-- Consumes: SalesOrderAllocatedEvent -> Demand signal
|
|-- StockAdjustedEventConsumer.kt
|   `-- Consumes: StockAdjustedEvent -> Refresh availability
|
`-- BomPublishedEventConsumer.kt
    `-- Consumes: BOMPublishedEvent -> Rebuild plans
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- MRPPlanJpaAdapter.kt           -> implements MRPPlanRepository
`-- PlannedOrderJpaAdapter.kt      -> implements PlannedOrderRepository
```

```
adapter/output/persistence/jpa/entity/
|-- MRPPlanEntity.kt
`-- PlannedOrderEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- InventorySnapshotAdapter.kt
|-- BomAdapter.kt
|-- RoutingAdapter.kt
|-- ProcurementAdapter.kt
`-- ProductionAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- MRPSchedulingEventPublisher.kt
|   `-- schema/
|       |-- PlannedOrderCreatedSchema.avro
|       `-- MRPRunCompletedSchema.avro
`-- outbox/
    `-- MRPOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- MRPDependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
