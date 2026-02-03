# Warehouse Execution Domain Layer

> Part of [Inventory Warehouse Execution](../inventory-warehouse.md)

## Directory Structure

```
warehouse-domain/
└── src/main/kotlin/com.erp.inventory.warehouse.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Warehouse Structure (`model/warehouse/`)

```
├── warehouse/
│   ├── WarehouseZone.kt            # Aggregate Root
│   ├── ZoneId.kt
│   ├── Aisle.kt                    # Entity
│   ├── Rack.kt                     # Entity
│   ├── Bin.kt                      # Entity
│   ├── BinId.kt
│   ├── Capacity.kt
│   └── TemperatureZone.kt
```

---

### Task and Wave (`model/task/`)

```
├── task/
│   ├── Task.kt                     # Aggregate Root
│   ├── TaskId.kt
│   ├── TaskType.kt                 # Putaway, Pick, Replenish, Count
│   ├── TaskStatus.kt               # Pending, Assigned, Completed, Cancelled
│   ├── TaskBatch.kt                # Entity
│   ├── PickWave.kt                 # Aggregate Root
│   ├── PickWaveId.kt
│   └── WaveStatus.kt               # Planned, Released, Completed
```

**Task Aggregate**:
```kotlin
class Task private constructor(
    val id: TaskId,
    val type: TaskType,
    var status: TaskStatus,
    val sourceBin: BinId?,
    val targetBin: BinId?,
    val itemId: String,
    val quantity: MoneyQuantity
) {
    fun assign(workerId: String): TaskAssignedEvent
    fun complete(actualQty: MoneyQuantity): TaskCompletedEvent
}
```

---

### Putaway and Replenishment (`model/rules/`)

```
├── rules/
│   ├── PutawayRule.kt              # Aggregate Root
│   ├── ReplenishmentRule.kt        # Aggregate Root
│   ├── SlottingRule.kt             # Entity
│   └── MinMaxPolicy.kt
```

---

### Labor and Returns (`model/ops/`)

```
├── ops/
│   ├── LaborStandard.kt            # Aggregate Root
│   ├── ProductivityRecord.kt       # Entity
│   ├── ReturnDisposition.kt        # Aggregate Root
│   ├── DispositionType.kt          # Restock, Refurbish, Scrap
│   └── ReturnInspection.kt         # Entity
```

---

## Domain Events

```
events/
├── WaveReleasedEvent.kt
├── TaskAssignedEvent.kt
├── TaskCompletedEvent.kt
├── PutawayConfirmedEvent.kt
├── PickConfirmedEvent.kt
├── ReplenishmentTriggeredEvent.kt
├── ReplenishmentCompletedEvent.kt
└── ReturnDispositionedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── BinCapacityExceededException.kt
├── TaskAlreadyCompletedException.kt
├── InvalidPickLocationException.kt
├── WaveNotReleasedException.kt
└── ReturnDispositionNotAllowedException.kt
```

---

## Domain Services

```
services/
├── WavePlanningService.kt          # Batch selection and priority
├── PickPathOptimizationService.kt  # Route optimization
├── PutawaySuggestionService.kt     # Directed putaway rules
├── ReplenishmentService.kt         # Min/max triggers
└── LaborProductivityService.kt     # KPI calculations
```

---

## Key Invariants

1. **Bin Capacity**: Putaway cannot exceed bin capacity.
2. **Wave Status**: Picks cannot start until wave is released.
3. **Task Completeness**: Completed tasks are immutable.
4. **Pick Accuracy**: Task quantities must match confirmations or trigger exception.
5. **Returns Control**: Disposition rules enforced per item class.
