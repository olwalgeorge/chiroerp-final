# Plant Maintenance Preventive Infrastructure Layer

> Part of [Plant Maintenance Preventive Maintenance](../maintenance-preventive.md)

## Directory Structure

```
preventive-infrastructure/
`-- src/main/kotlin/com.erp.maintenance.preventive.infrastructure/
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
|-- MaintenancePlanResource.kt
|   |-- POST /api/v1/maintenance/plans        -> createPlan()
|   |-- GET  /api/v1/maintenance/plans/{id}   -> getPlan()
|   `-- GET  /api/v1/maintenance/plans        -> listPlans()
|
|-- ScheduleResource.kt
|   `-- POST /api/v1/maintenance/plans/{id}/schedule -> generateSchedule()
```

---

## Event Consumers

```
adapter/input/event/
`-- EquipmentAssignedEventConsumer.kt
    `-- Consumes: EquipmentAssignedEvent -> Create plan scaffold
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/jpa/
|-- MaintenancePlanJpaAdapter.kt    -> implements MaintenancePlanRepository
`-- ScheduleJpaAdapter.kt           -> implements ScheduleRepository
```

```
adapter/output/persistence/jpa/entity/
|-- MaintenancePlanEntity.kt
`-- ScheduleEntity.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/integration/
`-- WorkOrderAdapter.kt
```

```
adapter/output/messaging/
|-- kafka/
|   |-- PreventiveEventPublisher.kt
|   `-- schema/
|       |-- MaintenancePlanCreatedSchema.avro
|       `-- MaintenanceScheduleGeneratedSchema.avro
`-- outbox/
    `-- PreventiveOutboxPublisher.kt
```

---

## Configuration & Resources

```
configuration/
|-- PreventiveConfiguration.kt
|-- PersistenceConfiguration.kt
`-- MessagingConfiguration.kt
```

```
resources/
|-- application.yml
`-- db/migration/
```
