# Master Data Quality Infrastructure Layer

> Part of [Master Data Quality Rules](../mdm-data-quality.md)

## Directory Structure

```
data-quality-infrastructure/
`-- src/main/kotlin/com.erp.mdm.quality.infrastructure/
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
|-- ValidationRuleResource.kt
|   |-- POST /api/v1/mdm/quality/rules        -> createRule()
|   |-- PUT  /api/v1/mdm/quality/rules/{id}   -> activateRule()
|   `-- GET  /api/v1/mdm/quality/rules        -> listRules()
|
`-- QualityScoreResource.kt
    `-- GET /api/v1/mdm/quality/scores/{id}   -> getScore()
```

---

## Event Consumers

```
adapter/input/event/
`-- MasterRecordPublishedEventConsumer.kt
    `-- Consumes: MasterRecordPublishedEvent -> Recalculate score
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- ValidationRuleJpaAdapter.kt      -> implements ValidationRuleRepository
`-- DataQualityScoreJpaAdapter.kt    -> implements DataQualityScoreRepository
```

```
adapter/output/persistence/jpa/entity/
|-- ValidationRuleEntity.kt
`-- DataQualityScoreEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
`-- MasterDataAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- DataQualityEventPublisher.kt
|   `-- schema/
|       |-- ValidationRuleActivatedSchema.avro
|       `-- DataQualityScoreUpdatedSchema.avro
`-- outbox/
    `-- DataQualityOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- DataQualityConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
