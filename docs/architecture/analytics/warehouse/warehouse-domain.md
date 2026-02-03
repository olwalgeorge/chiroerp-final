# Analytics Warehouse Domain Layer

> Part of [Analytics Data Warehouse](../analytics-warehouse.md)

## Directory Structure

```
warehouse-domain/
`-- src/main/kotlin/com.erp.analytics.warehouse.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Warehouse Load (`model/load/`)

```
|-- load/
|   |-- WarehouseLoad.kt           # Aggregate Root
|   |-- WarehouseLoadId.kt
|   |-- LoadStatus.kt              # Started, Completed, Failed
|   `-- LoadBatch.kt
```

### Dimension Model (`model/dimension/`)

```
|-- dimension/
|   |-- DimensionModel.kt          # Aggregate Root
|   |-- DimensionVersion.kt
|   `-- SCDType.kt                 # Type1, Type2
```

### Fact Table (`model/fact/`)

```
|-- fact/
|   |-- FactTable.kt               # Entity
|   |-- FactDefinition.kt
|   `-- FactPartition.kt
```

---

## Domain Events

```
events/
|-- WarehouseLoadStartedEvent.kt
|-- WarehouseLoadCompletedEvent.kt
|-- DimensionUpdatedEvent.kt
`-- FactPartitionPublishedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- LoadFailedException.kt
|-- DimensionNotFoundException.kt
|-- FactDefinitionInvalidException.kt
`-- PartitionWriteException.kt
```

---

## Domain Services

```
services/
|-- LoadOrchestrationService.kt
|-- DimensionService.kt
`-- FactPublishingService.kt
```

---

## Key Invariants

1. **Load Atomicity**: Facts published only after successful load.
2. **SCD Integrity**: SCD2 dimensions require versioned rows.
3. **Partition Completeness**: Fact partitions must align to dates.
4. **Auditability**: Each load has a unique batch id.
