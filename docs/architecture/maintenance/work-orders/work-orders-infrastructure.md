# Plant Maintenance Work Orders Infrastructure Layer

> Part of [Plant Maintenance Work Orders](../maintenance-work-orders.md)

## Directory Structure

```
work-orders-infrastructure/
`-- src/main/kotlin/com.erp.maintenance.workorders.infrastructure/
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
|-- WorkOrderResource.kt
|   |-- POST /api/v1/maintenance/work-orders        -> createWorkOrder()
|   |-- GET  /api/v1/maintenance/work-orders/{id}   -> getWorkOrder()
|   `-- GET  /api/v1/maintenance/work-orders        -> listWorkOrders()
|
|-- WorkOrderReleaseResource.kt
|   `-- POST /api/v1/maintenance/work-orders/{id}/release -> releaseWorkOrder()
|
`-- WorkOrderConfirmResource.kt
    `-- POST /api/v1/maintenance/work-orders/{id}/confirm -> confirmWorkOrder()
```

---

## Event Consumers

```
adapter/input/event/
|-- MaintenanceSchedulePublishedEventConsumer.kt
|   `-- Consumes: MaintenanceSchedulePublishedEvent -> Create work order
|
`-- BreakdownReportedEventConsumer.kt
    `-- Consumes: BreakdownReportedEvent -> Create corrective order
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- WorkOrderJpaAdapter.kt         -> implements WorkOrderRepository
`-- WorkOrderOperationJpaAdapter.kt -> implements WorkOrderOperationRepository
```

```
adapter/output/persistence/jpa/entity/
|-- WorkOrderEntity.kt
`-- WorkOrderOperationEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- InventoryReservationAdapter.kt
|-- ProcurementServiceAdapter.kt
`-- FinancePostingAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- WorkOrderEventPublisher.kt
|   `-- schema/
|       |-- WorkOrderCreatedSchema.avro
|       `-- MaintenanceCostPostedSchema.avro
`-- outbox/
    `-- WorkOrderOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- WorkOrderConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
