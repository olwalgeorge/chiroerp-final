# Plant Maintenance Equipment Application Layer

> Part of [Plant Maintenance Equipment Master](../maintenance-equipment.md)

## Directory Structure

```
equipment-application/
`-- src/main/kotlin/com.erp.maintenance.equipment.application/
    |-- port/
    |   |-- input/
    |   |   |-- command/
    |   |   `-- query/
    |   `-- output/
    `-- service/
        |-- command/
        `-- query/
```

---

## Commands (Write Operations)

```
port/input/command/
|-- CreateEquipmentCommand.kt
|   `-- equipmentId, classId, description, criticality
|
|-- AssignEquipmentCommand.kt
|   `-- equipmentId, functionalLocationId
|
|-- UpdateTechnicalAttributeCommand.kt
|   `-- equipmentId, key, value
|
`-- DecommissionEquipmentCommand.kt
    `-- equipmentId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetEquipmentQuery.kt
|   `-- equipmentId -> EquipmentDto
|
|-- ListEquipmentQuery.kt
|   `-- classId, status -> List<EquipmentDto>
|
`-- GetFunctionalLocationQuery.kt
    `-- locationId -> FunctionalLocationDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- EquipmentRepository.kt
|-- FunctionalLocationRepository.kt
|-- FixedAssetPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- EquipmentCommandHandler.kt
|   `-- EquipmentAssignmentHandler.kt
`-- query/
    |-- EquipmentQueryHandler.kt
    `-- FunctionalLocationQueryHandler.kt
```
