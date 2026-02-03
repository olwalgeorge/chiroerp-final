# Manufacturing Shop Floor Application Layer

> Part of [Manufacturing Shop Floor Execution](../manufacturing-shop-floor.md)

## Directory Structure

```
shopfloor-application/
`-- src/main/kotlin/com.erp.manufacturing.shopfloor.application/
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
|-- DispatchWorkOrderCommand.kt
|   `-- orderId, workCenterId
|
|-- StartOperationCommand.kt
|   `-- orderId, operationId
|
|-- CompleteOperationCommand.kt
|   `-- orderId, operationId, laborTime, machineTime
|
|-- RecordScrapCommand.kt
|   `-- orderId, operationId, quantity, reason
|
`-- LogDowntimeCommand.kt
    `-- workCenterId, duration, reason
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetDispatchQueueQuery.kt
|   `-- workCenterId -> List<DispatchDto>
|
|-- GetOperationStatusQuery.kt
|   `-- orderId -> OperationStatusDto
|
`-- GetOeeSnapshotQuery.kt
    `-- workCenterId -> OeeSnapshotDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- DispatchRepository.kt
|-- ProductionOrderPort.kt
|-- InventoryPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- DispatchCommandHandler.kt
|   |-- OperationCommandHandler.kt
|   `-- ScrapCommandHandler.kt
`-- query/
    |-- DispatchQueryHandler.kt
    `-- OeeQueryHandler.kt
```
