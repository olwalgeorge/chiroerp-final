# Manufacturing BOM Domain Layer

> Part of [Manufacturing BOM Management](../manufacturing-bom.md)

## Directory Structure

```
bom-domain/
`-- src/main/kotlin/com.erp.manufacturing.bom.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### BOM (`model/bom/`)

```
|-- bom/
|   |-- BOM.kt                      # Aggregate Root
|   |-- BOMId.kt
|   |-- BOMItem.kt                  # Entity
|   |-- BOMStatus.kt                # Draft, Active, Retired
|   |-- Revision.kt
|   `-- EffectivityDateRange.kt
```

### Routing (`model/routing/`)

```
|-- routing/
|   |-- Routing.kt                  # Aggregate Root
|   |-- RoutingId.kt
|   |-- Operation.kt                # Entity
|   |-- WorkCenterId.kt
|   `-- SetupRunTime.kt
```

---

## Domain Events

```
events/
|-- BOMPublishedEvent.kt
|-- BOMRetiredEvent.kt
`-- RoutingUpdatedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- BomNotFoundException.kt
|-- BomCycleDetectedException.kt
`-- RoutingNotFoundException.kt
```

---

## Domain Services

```
services/
|-- BomExplosionService.kt          # Multi-level explosion
`-- EffectivityService.kt           # Revision and effectivity checks
```

---

## Key Invariants

1. **No Cycles**: BOM must be acyclic.
2. **Effectivity**: Only one active revision per effectivity window.
3. **Routing Completeness**: Each operation must reference a valid work center.
