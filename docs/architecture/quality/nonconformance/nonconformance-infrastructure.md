# Quality Nonconformance Infrastructure Layer

> Part of [Quality Nonconformance](../quality-nonconformance.md)

## Directory Structure

```
nonconformance-infrastructure/
`-- src/main/kotlin/com.erp.quality.nonconformance.infrastructure/
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
|-- NonconformanceResource.kt
|   |-- POST /api/v1/quality/nonconformances        -> createNC()
|   |-- GET  /api/v1/quality/nonconformances/{id}   -> getNC()
|   `-- GET  /api/v1/quality/nonconformances        -> listNCs()
|
|-- DispositionResource.kt
|   `-- POST /api/v1/quality/nonconformances/{id}/disposition -> determineDisposition()
|
`-- ClosureResource.kt
    `-- POST /api/v1/quality/nonconformances/{id}/close -> closeNC()
```

---

## Event Consumers

```
adapter/input/event/
|-- DefectDetectedEventConsumer.kt
|   `-- Consumes: DefectDetectedEvent -> Create NC
|
`-- ReturnReceivedEventConsumer.kt
    `-- Consumes: ReturnReceivedEvent -> Customer complaint NC
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- NonconformanceJpaAdapter.kt     -> implements NonconformanceRepository
`-- QualityCostJpaAdapter.kt        -> implements QualityCostRepository
```

```
adapter/output/persistence/jpa/entity/
|-- NonconformanceEntity.kt
`-- QualityCostEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- InventoryDispositionAdapter.kt
|-- FinancePostingAdapter.kt
`-- CapaAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- NonconformanceEventPublisher.kt
|   `-- schema/
|       |-- NonconformanceCreatedSchema.avro
|       `-- QualityCostPostedSchema.avro
`-- outbox/
    `-- NonconformanceOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- NonconformanceConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
