# CRM Pipeline Infrastructure Layer

> Part of [CRM Pipeline](../crm-pipeline.md)

## Directory Structure

```
pipeline-infrastructure/
`-- src/main/kotlin/com.erp.crm.pipeline.infrastructure/
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
|-- OpportunityResource.kt
|   |-- POST /api/v1/crm/opportunities         -> createOpportunity()
|   |-- GET  /api/v1/crm/opportunities/{id}    -> getOpportunity()
|   |-- POST /api/v1/crm/opportunities/{id}/stage -> changeStage()
|   `-- POST /api/v1/crm/opportunities/{id}/close -> closeOpportunity()
|-- ForecastResource.kt
|   |-- POST /api/v1/crm/forecasts/snapshot    -> createSnapshot()
|   `-- GET  /api/v1/crm/forecasts/{id}        -> getSnapshot()
```

---

## Event Consumers

```
adapter/input/event/
|-- CustomerProfileUpdatedEventConsumer.kt
|   `-- Consumes: CustomerProfileUpdatedEvent -> refresh account data
`-- QuoteCreatedEventConsumer.kt
    `-- Consumes: QuoteCreatedEvent -> attach pricing context
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- OpportunityJpaAdapter.kt
`-- ForecastJpaAdapter.kt
```

```
adapter/output/persistence/jpa/entity/
|-- OpportunityEntity.kt
|-- OpportunityLineEntity.kt
`-- ForecastEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/messaging/
|-- kafka/
|   |-- PipelineEventPublisher.kt
|   `-- schema/
|       |-- OpportunityCreatedSchema.avro
|       `-- OpportunityClosedWonSchema.avro
`-- outbox/
    `-- PipelineOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- PipelineDependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
