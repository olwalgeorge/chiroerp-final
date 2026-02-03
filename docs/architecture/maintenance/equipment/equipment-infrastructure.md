# Plant Maintenance Equipment Infrastructure Layer

> Part of [Plant Maintenance Equipment Master](../maintenance-equipment.md)

## Directory Structure

```
equipment-infrastructure/
`-- src/main/kotlin/com.erp.maintenance.equipment.infrastructure/
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
|-- EquipmentResource.kt
|   |-- POST /api/v1/maintenance/equipment        -> createEquipment()
|   |-- GET  /api/v1/maintenance/equipment/{id}   -> getEquipment()
|   `-- GET  /api/v1/maintenance/equipment        -> listEquipment()
|
|-- EquipmentAssignmentResource.kt
|   `-- POST /api/v1/maintenance/equipment/{id}/assign -> assignEquipment()
```

---

## Event Consumers

```
adapter/input/event/
`-- AssetRetiredEventConsumer.kt
    `-- Consumes: AssetRetiredEvent -> Decommission equipment
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- EquipmentJpaAdapter.kt          -> implements EquipmentRepository
`-- FunctionalLocationJpaAdapter.kt -> implements FunctionalLocationRepository
```

```
adapter/output/persistence/jpa/entity/
|-- EquipmentEntity.kt
`-- FunctionalLocationEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
`-- FixedAssetAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- EquipmentEventPublisher.kt
|   `-- schema/
|       |-- EquipmentCreatedSchema.avro
|       `-- EquipmentAssignedSchema.avro
`-- outbox/
    `-- EquipmentOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- EquipmentConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
