# Quality Inspection Planning Infrastructure Layer

> Part of [Quality Inspection Planning](../quality-inspection-planning.md)

## Directory Structure

```
inspection-planning-infrastructure/
`-- src/main/kotlin/com.erp.quality.inspectionplanning.infrastructure/
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
|-- InspectionPlanResource.kt
|   |-- POST /api/v1/quality/inspection-plans          -> createPlan()
|   |-- GET  /api/v1/quality/inspection-plans/{id}     -> getPlan()
|   `-- GET  /api/v1/quality/inspection-plans          -> listPlans()
|
|-- CharacteristicResource.kt
|   `-- POST /api/v1/quality/inspection-plans/{id}/characteristics -> addCharacteristic()
|
|-- SamplingPlanResource.kt
|   `-- PUT  /api/v1/quality/inspection-plans/{id}/sampling -> updateSampling()
|
`-- TriggerRuleResource.kt
    `-- POST /api/v1/quality/inspection-plans/{id}/triggers -> activateTrigger()
```

---

## Event Consumers

```
adapter/input/event/
|-- GoodsReceivedEventConsumer.kt
|   `-- Consumes: GoodsReceivedEvent -> Evaluate incoming triggers
|
|-- ProductionOrderReleasedEventConsumer.kt
|   `-- Consumes: ProductionOrderReleasedEvent -> Evaluate in-process triggers
|
`-- ReturnReceivedEventConsumer.kt
    `-- Consumes: ReturnReceivedEvent -> Evaluate return triggers
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- InspectionPlanJpaAdapter.kt       -> implements InspectionPlanRepository
|-- SamplingPlanJpaAdapter.kt         -> implements SamplingPlanRepository
`-- TriggerRuleJpaAdapter.kt          -> implements TriggerRuleRepository
```

```
adapter/output/persistence/jpa/entity/
|-- InspectionPlanEntity.kt
|-- SamplingPlanEntity.kt
`-- TriggerRuleEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- ItemCatalogAdapter.kt
`-- SupplierAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- InspectionPlanEventPublisher.kt
|   `-- schema/
|       |-- InspectionPlanCreatedSchema.avro
|       `-- TriggerRuleActivatedSchema.avro
`-- outbox/
    `-- InspectionPlanningOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- InspectionPlanningConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
