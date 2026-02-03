# Plant Maintenance Equipment Domain Layer

> Part of [Plant Maintenance Equipment Master](../maintenance-equipment.md)

## Directory Structure

```
equipment-domain/
`-- src/main/kotlin/com.erp.maintenance.equipment.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Equipment (`model/equipment/`)

```
|-- equipment/
|   |-- Equipment.kt               # Aggregate Root
|   |-- EquipmentId.kt
|   |-- EquipmentStatus.kt         # Active, Inactive, Decommissioned
|   `-- EquipmentCriticality.kt    # A, B, C
```

### Functional Location (`model/location/`)

```
|-- location/
|   |-- FunctionalLocation.kt      # Aggregate Root
|   |-- FunctionalLocationId.kt
|   `-- LocationHierarchy.kt
```

### Attributes (`model/attribute/`)

```
|-- attribute/
|   |-- TechnicalAttribute.kt      # Entity
|   |-- AttributeKey.kt
|   `-- AttributeValue.kt
```

---

## Domain Events

```
events/
|-- EquipmentCreatedEvent.kt
|-- EquipmentUpdatedEvent.kt
|-- EquipmentAssignedEvent.kt
`-- EquipmentDecommissionedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- EquipmentNotFoundException.kt
|-- InvalidEquipmentStatusException.kt
|-- AttributeValidationException.kt
`-- FunctionalLocationNotFoundException.kt
```

---

## Domain Services

```
services/
|-- EquipmentAssignmentService.kt
|-- EquipmentClassificationService.kt
`-- EquipmentLifecycleService.kt
```

---

## Key Invariants

1. **Unique Identifier**: Equipment ID must be unique per tenant.
2. **Valid Assignment**: Equipment assigned to a valid functional location.
3. **Lifecycle Control**: Decommissioned equipment cannot be scheduled.
4. **Attribute Validity**: Attributes must match equipment class schema.
