# Manufacturing Production Orders Application Layer

> Part of [Manufacturing Production Orders](../manufacturing-production.md)

## Directory Structure

```
production-application/
`-- src/main/kotlin/com.erp.manufacturing.production.application/
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
|-- CreateProductionOrderCommand.kt
|   `-- itemId, quantity, dueDate, routingId
|
|-- ReleaseProductionOrderCommand.kt
|   `-- orderId
|
|-- IssueMaterialCommand.kt
|   `-- orderId, componentId, quantity
|
|-- ConfirmOperationCommand.kt
|   `-- orderId, operationId, laborTime, machineTime
|
|-- PostReceiptCommand.kt
|   `-- orderId, quantity
|
`-- CloseProductionOrderCommand.kt
    `-- orderId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetProductionOrderByIdQuery.kt
|   `-- orderId -> ProductionOrderDto
|
|-- GetOpenOrdersQuery.kt
|   `-- plantId -> List<ProductionOrderDto>
|
`-- GetWipByOrderQuery.kt
    `-- orderId -> WipDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- ProductionOrderRepository.kt
|-- BomPort.kt
|-- InventoryPort.kt
|-- ShopFloorPort.kt
|-- CostingPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- ProductionOrderCommandHandler.kt
|   |-- MaterialIssueCommandHandler.kt
|   `-- ConfirmationCommandHandler.kt
`-- query/
    |-- ProductionOrderQueryHandler.kt
    `-- WipQueryHandler.kt
```
