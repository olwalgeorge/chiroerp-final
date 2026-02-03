# Analytics Warehouse Application Layer

> Part of [Analytics Data Warehouse](../analytics-warehouse.md)

## Directory Structure

```
warehouse-application/
`-- src/main/kotlin/com.erp.analytics.warehouse.application/
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
|-- StartWarehouseLoadCommand.kt
|   `-- sourceSystem, batchId
|
|-- PublishFactPartitionCommand.kt
|   `-- factName, partitionDate
|
`-- UpdateDimensionCommand.kt
    `-- dimensionName, version
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetLoadStatusQuery.kt
|   `-- batchId -> LoadStatusDto
|
|-- ListDimensionsQuery.kt
|   `-- domain -> List<DimensionDto>
|
`-- GetFactPartitionQuery.kt
    `-- factName, date -> FactPartitionDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- WarehouseLoadRepository.kt
|-- DimensionRepository.kt
|-- FactRepository.kt
|-- CdcIngestPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- WarehouseLoadCommandHandler.kt
|   `-- DimensionCommandHandler.kt
`-- query/
    |-- WarehouseLoadQueryHandler.kt
    `-- WarehouseMetadataQueryHandler.kt
```
