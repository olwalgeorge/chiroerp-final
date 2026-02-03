# Quality CAPA Infrastructure Layer

> Part of [Quality CAPA Management](../quality-capa.md)

## Directory Structure

```
capa-infrastructure/
`-- src/main/kotlin/com.erp.quality.capa.infrastructure/
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
|-- CAPAResource.kt
|   |-- POST /api/v1/quality/capa               -> initiateCAPA()
|   |-- GET  /api/v1/quality/capa/{id}          -> getCAPA()
|   `-- GET  /api/v1/quality/capa               -> listCAPA()
|
|-- RootCauseResource.kt
|   `-- POST /api/v1/quality/capa/{id}/root-cause -> recordRootCause()
|
|-- ActionPlanResource.kt
|   `-- POST /api/v1/quality/capa/{id}/actions    -> createActionPlan()
|
`-- ClosureResource.kt
    `-- POST /api/v1/quality/capa/{id}/close      -> closeCAPA()
```

---

## Event Consumers

```
adapter/input/event/
`-- NonconformanceCreatedEventConsumer.kt
    `-- Consumes: NonconformanceCreatedEvent -> Initiate CAPA
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- CAPAJpaAdapter.kt          -> implements CAPARepository
`-- ActionPlanJpaAdapter.kt    -> implements ActionPlanRepository
```

```
adapter/output/persistence/jpa/entity/
|-- CAPAEntity.kt
`-- ActionPlanEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- ActionTaskAdapter.kt
|-- InspectionPlanAdapter.kt
`-- ManufacturingProcessAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- CAPAEventPublisher.kt
|   `-- schema/
|       |-- CAPAInitiatedSchema.avro
|       `-- CAPAClosedSchema.avro
`-- outbox/
    `-- CAPAOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- CAPAConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
