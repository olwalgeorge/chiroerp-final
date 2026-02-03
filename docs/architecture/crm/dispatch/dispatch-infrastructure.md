# CRM Dispatch Infrastructure Layer

> Part of [CRM Dispatch](../crm-dispatch.md)

## Directory Structure

```
dispatch-infrastructure/
`-- src/main/kotlin/com.erp.crm.dispatch.infrastructure/
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
|   |-- POST /api/v1/crm/dispatch/requests          -> createDispatchRequest()
|   |-- POST /api/v1/crm/dispatch/{id}/assign       -> assignTechnician()
|   |-- POST /api/v1/crm/dispatch/{id}/confirm      -> confirmDispatch()
|   `-- POST /api/v1/crm/dispatch/{id}/reject       -> rejectDispatch()
|-- TechnicianResource.kt
|   |-- GET  /api/v1/crm/dispatch/technicians/available -> listAvailable()
|   `-- POST /api/v1/crm/dispatch/technicians/{id}/status -> updateStatus()
`-- RouteResource.kt
    `-- POST /api/v1/crm/dispatch/{id}/optimize     -> optimizeRoute()
```

---

## Event Consumers

```
adapter/input/event/
|-- ServiceOrderScheduledEventConsumer.kt
|   `-- Consumes: ServiceOrderScheduledEvent -> create dispatch request
`-- WeatherAlertEventConsumer.kt
    `-- Consumes: WeatherAlertEvent -> re-route assignments
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- DispatchJpaAdapter.kt
`-- TechnicianJpaAdapter.kt
```

```
adapter/output/persistence/jpa/entity/
|-- DispatchEntity.kt
|-- AssignmentEntity.kt
`-- TechnicianEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
`-- RoutingAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- DispatchEventPublisher.kt
|   `-- schema/
|       |-- TechnicianAssignedSchema.avro
|       `-- DispatchConfirmedSchema.avro
`-- outbox/
    `-- DispatchOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- DispatchDependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
