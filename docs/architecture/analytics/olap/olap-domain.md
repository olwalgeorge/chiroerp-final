# Analytics OLAP Domain Layer

> Part of [Analytics OLAP & Cube Engine](../analytics-olap.md)

## Directory Structure

```
olap-domain/
`-- src/main/kotlin/com.erp.analytics.olap.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Cube Definition (`model/cube/`)

```
|-- cube/
|   |-- CubeDefinition.kt          # Aggregate Root
|   |-- CubeId.kt
|   |-- Measure.kt
|   `-- DimensionRef.kt
```

### Cube Refresh (`model/refresh/`)

```
|-- refresh/
|   |-- CubeRefresh.kt             # Entity
|   |-- RefreshStatus.kt           # Started, Completed, Failed
|   `-- RefreshWindow.kt
```

---

## Domain Events

```
events/
|-- CubeRefreshedEvent.kt
|-- CubeRefreshFailedEvent.kt
`-- AggregateSnapshotPublishedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- CubeNotFoundException.kt
|-- RefreshFailedException.kt
`-- DimensionMissingException.kt
```

---

## Domain Services

```
services/
|-- CubeRefreshService.kt
|-- AggregationService.kt
`-- QueryOptimizationService.kt
```

---

## Key Invariants

1. **Cube Validity**: Each cube must reference existing dimensions.
2. **Refresh Ordering**: Dimensions must be refreshed before facts.
3. **Snapshot Consistency**: Aggregates align to refresh window.
4. **Auditability**: Refresh results logged per cube.
