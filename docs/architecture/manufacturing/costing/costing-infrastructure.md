# Manufacturing Costing Infrastructure Layer

> Part of [Manufacturing Costing](../manufacturing-costing.md)

## Directory Structure

```
costing-infrastructure/
`-- src/main/kotlin/com.erp.manufacturing.costing.infrastructure/
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
|-- CostRollupResource.kt
|   |-- POST /api/v1/manufacturing/costing/rollup -> runRollup()
|
|-- WipResource.kt
|   |-- POST /api/v1/manufacturing/costing/wip    -> postWip()
|
`-- VarianceResource.kt
    `-- POST /api/v1/manufacturing/costing/variance -> postVariance()
```

---

## Event Consumers

```
adapter/input/event/
|-- ProductionConfirmedEventConsumer.kt
|   `-- Consumes: ProductionConfirmedEvent -> Update WIP
|
|-- ProductionReceiptPostedEventConsumer.kt
|   `-- Consumes: ProductionReceiptPostedEvent -> Settle WIP
|
`-- BomPublishedEventConsumer.kt
    `-- Consumes: BOMPublishedEvent -> Trigger rollup
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- CostEstimateJpaAdapter.kt
|-- WipJpaAdapter.kt
`-- VarianceJpaAdapter.kt
```

```
adapter/output/persistence/jpa/entity/
|-- CostEstimateEntity.kt
|-- WipEntity.kt
`-- VarianceEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
|-- BomAdapter.kt
|-- ProductionAdapter.kt
|-- ControllingAdapter.kt
`-- InventoryValuationAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- CostingEventPublisher.kt
|   `-- schema/
|       |-- WipPostedSchema.avro
|       `-- ProductionVarianceSchema.avro
`-- outbox/
    `-- CostingOutboxEventPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- CostingDependencyInjection.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
