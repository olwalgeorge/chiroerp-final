# Quality Execution Infrastructure Layer

> Part of [Quality Execution](../quality-execution.md)

## Directory Structure

```
execution-infrastructure/
`-- src/main/kotlin/com.erp.quality.execution.infrastructure/
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
|-- InspectionLotResource.kt
|   |-- POST /api/v1/quality/inspection-lots          -> createLot()
|   |-- GET  /api/v1/quality/inspection-lots/{id}     -> getLot()
|   `-- GET  /api/v1/quality/inspection-lots          -> listLots()
|
|-- InspectionResultResource.kt
|   `-- POST /api/v1/quality/inspection-lots/{id}/results -> recordResult()
|
|-- UsageDecisionResource.kt
|   `-- POST /api/v1/quality/inspection-lots/{id}/decision -> decide()
|
`-- StockReleaseResource.kt
    `-- POST /api/v1/quality/inspection-lots/{id}/release -> releaseStock()
```

---

## Event Consumers

```
adapter/input/event/
|-- GoodsReceivedEventConsumer.kt
|   `-- Consumes: GoodsReceivedEvent -> Create inspection lot
|
|-- ProductionOrderReleasedEventConsumer.kt
|   `-- Consumes: ProductionOrderReleasedEvent -> Create in-process lot
|
`-- ReturnReceivedEventConsumer.kt
    `-- Consumes: ReturnReceivedEvent -> Create return lot
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- InspectionLotJpaAdapter.kt       -> implements InspectionLotRepository
`-- InspectionResultJpaAdapter.kt    -> implements InspectionResultRepository
```

```
adapter/output/persistence/jpa/entity/
|-- InspectionLotEntity.kt
`-- InspectionResultEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- InspectionPlanAdapter.kt
`-- InventoryBlockAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- QualityExecutionEventPublisher.kt
|   `-- schema/
|       |-- InspectionCompletedSchema.avro
|       `-- StockBlockedSchema.avro
`-- outbox/
    `-- QualityExecutionOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- QualityExecutionConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
