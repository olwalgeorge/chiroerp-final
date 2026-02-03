# Plant Maintenance Work Orders Application Layer

> Part of [Plant Maintenance Work Orders](../maintenance-work-orders.md)

## Directory Structure

```
work-orders-application/
`-- src/main/kotlin/com.erp.maintenance.workorders.application/
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
|-- CreateWorkOrderCommand.kt
|   `-- equipmentId, type, priority, description
|
|-- ReleaseWorkOrderCommand.kt
|   `-- workOrderId
|
|-- ConfirmWorkOrderCommand.kt
|   `-- workOrderId, laborHours, partsUsed
|
`-- CompleteWorkOrderCommand.kt
    `-- workOrderId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetWorkOrderQuery.kt
|   `-- workOrderId -> WorkOrderDto
|
|-- ListWorkOrdersQuery.kt
|   `-- status -> List<WorkOrderDto>
|
`-- GetWorkOrderCostsQuery.kt
    `-- workOrderId -> CostSummaryDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- WorkOrderRepository.kt
|-- InventoryReservationPort.kt
|-- ProcurementServicePort.kt
|-- FinancePostingPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- WorkOrderCommandHandler.kt
|   `-- WorkOrderConfirmationHandler.kt
`-- query/
    |-- WorkOrderQueryHandler.kt
    `-- WorkOrderCostQueryHandler.kt
```
