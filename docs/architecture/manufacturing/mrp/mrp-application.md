# Manufacturing MRP Application Layer

> Part of [Manufacturing MRP](../manufacturing-mrp.md)

## Directory Structure

```
mrp-application/
`-- src/main/kotlin/com.erp.manufacturing.mrp.application/
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
|-- RunMRPCommand.kt
|   `-- plantId, horizonDays, includeSafetyStock
|
|-- CreatePlannedOrderCommand.kt
|   `-- itemId, quantity, dueDate, supplyType
|
|-- ConvertPlannedOrderCommand.kt
|   `-- plannedOrderId, targetType (PO or ProdOrder)
|
`-- FreezePlanCommand.kt
    `-- planId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetMRPPlanQuery.kt
|   `-- planId -> MRPPlanDto
|
|-- GetPlannedOrdersQuery.kt
|   `-- plantId, dateRange -> List<PlannedOrderDto>
|
`-- GetShortageReportQuery.kt
    `-- plantId -> ShortageReportDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- MRPPlanRepository.kt
|-- PlannedOrderRepository.kt
|-- DemandSignalPort.kt            # Sales and forecast demand
|-- InventorySnapshotPort.kt       # On-hand and safety stock
|-- BomPort.kt                     # BOM explosion
|-- RoutingPort.kt                 # Lead times
|-- ProcurementPort.kt             # Convert to PR/PO
|-- ProductionPort.kt              # Convert to production order
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- MRPRunCommandHandler.kt
|   |-- PlannedOrderCommandHandler.kt
|   `-- PlanFreezeCommandHandler.kt
`-- query/
    |-- MRPPlanQueryHandler.kt
    `-- ShortageQueryHandler.kt
```
