# CRM Service Orders Infrastructure Layer

> Part of [CRM Service Orders](../crm-service-orders.md)

## Directory Structure

```
service-orders-infrastructure/
`-- src/main/kotlin/com.erp.crm.serviceorders.infrastructure/
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
|-- ServiceOrderResource.kt
|   |-- POST /api/v1/crm/service-orders              -> createServiceOrder()
|   |-- GET  /api/v1/crm/service-orders/{id}         -> getServiceOrder()
|   |-- POST /api/v1/crm/service-orders/{id}/schedule -> scheduleServiceOrder()
|   |-- POST /api/v1/crm/service-orders/{id}/complete -> completeServiceOrder()
|   `-- POST /api/v1/crm/service-orders/{id}/billing/approve -> approveBilling()
`-- WorkLogResource.kt
    `-- POST /api/v1/crm/service-orders/{id}/work-logs -> recordWorkLog()
```

---

## Event Consumers

```
adapter/input/event/
|-- TechnicianAssignedEventConsumer.kt
|   `-- Consumes: TechnicianAssignedEvent -> mark dispatched
`-- PartsConsumedEventConsumer.kt
    `-- Consumes: PartsConsumedEvent -> attach parts usage
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- ServiceOrderJpaAdapter.kt
`-- WorkOrderJpaAdapter.kt
```

```
adapter/output/persistence/jpa/entity/
|-- ServiceOrderEntity.kt
|-- WorkOrderEntity.kt
`-- WorkTaskEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- InventoryReservationAdapter.kt
`-- DispatchAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- ServiceOrdersEventPublisher.kt
|   `-- schema/
|       |-- ServiceOrderCreatedSchema.avro
|       `-- ServiceOrderBilledSchema.avro
`-- outbox/
    `-- ServiceOrdersOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- ServiceOrdersDependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
