# Analytics OLAP Application Layer

> Part of [Analytics OLAP & Cube Engine](../analytics-olap.md)

## Directory Structure

```
olap-application/
`-- src/main/kotlin/com.erp.analytics.olap.application/
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
|-- DefineCubeCommand.kt
|   `-- cubeName, measures, dimensions
|
|-- RefreshCubeCommand.kt
|   `-- cubeId
|
`-- PublishAggregateSnapshotCommand.kt
    `-- cubeId, window
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetCubeQuery.kt
|   `-- cubeId -> CubeDto
|
|-- ListCubesQuery.kt
|   `-- domain -> List<CubeDto>
|
`-- RunCubeQuery.kt
    `-- cubeId, filters -> CubeResultDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- CubeRepository.kt
|-- WarehouseFactPort.kt
|-- CachePort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- CubeDefinitionCommandHandler.kt
|   `-- CubeRefreshCommandHandler.kt
`-- query/
    |-- CubeQueryHandler.kt
    `-- CubeRunQueryHandler.kt
```
