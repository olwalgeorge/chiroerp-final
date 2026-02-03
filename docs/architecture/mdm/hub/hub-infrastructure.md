# Master Data Hub Infrastructure Layer

> Part of [Master Data Hub](../mdm-hub.md)

## Directory Structure

```
hub-infrastructure/
`-- src/main/kotlin/com.erp.mdm.hub.infrastructure/
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
|-- MasterRecordResource.kt
|   |-- POST /api/v1/mdm/masters        -> createMaster()
|   |-- GET  /api/v1/mdm/masters/{id}   -> getMaster()
|   `-- GET  /api/v1/mdm/masters        -> searchMasters()
|
|-- ChangeRequestResource.kt
|   |-- POST /api/v1/mdm/changes        -> submitChange()
|   `-- POST /api/v1/mdm/changes/{id}/approve -> approveChange()
```

---

## Event Consumers

```
adapter/input/event/
|-- MasterRecordMergedEventConsumer.kt
|   `-- Consumes: MasterRecordMergedEvent -> Update index
|
`-- DataQualityScoreUpdatedEventConsumer.kt
    `-- Consumes: DataQualityScoreUpdatedEvent -> Flag low quality
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- MasterRecordJpaAdapter.kt       -> implements MasterRecordRepository
`-- ChangeRequestJpaAdapter.kt      -> implements ChangeRequestRepository
```

```
adapter/output/persistence/jpa/entity/
|-- MasterRecordEntity.kt
`-- ChangeRequestEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- StewardshipAdapter.kt
`-- DataQualityAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- MasterRecordEventPublisher.kt
|   `-- schema/
|       |-- MasterRecordPublishedSchema.avro
|       `-- ChangeRequestApprovedSchema.avro
`-- outbox/
    `-- MdmHubOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- MdmHubConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
