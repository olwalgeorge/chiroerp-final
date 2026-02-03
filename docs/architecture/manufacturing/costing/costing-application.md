# Manufacturing Costing Application Layer

> Part of [Manufacturing Costing](../manufacturing-costing.md)

## Directory Structure

```
costing-application/
`-- src/main/kotlin/com.erp.manufacturing.costing.application/
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
|-- RunCostRollupCommand.kt
|   `-- plantId, costVersion
|
|-- PostWipCommand.kt
|   `-- orderId, amount
|
|-- PostVarianceCommand.kt
|   `-- orderId, varianceType, amount
|
`-- SetStandardCostCommand.kt
    `-- itemId, costVersion
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetStandardCostQuery.kt
|   `-- itemId -> StandardCostDto
|
|-- GetWipByOrderQuery.kt
|   `-- orderId -> WipDto
|
`-- GetVarianceReportQuery.kt
    `-- plantId, period -> VarianceReportDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- CostEstimateRepository.kt
|-- WipRepository.kt
|-- VarianceRepository.kt
|-- BomPort.kt
|-- ProductionPort.kt
|-- ControllingPort.kt
|-- InventoryValuationPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- CostRollupCommandHandler.kt
|   |-- WipCommandHandler.kt
|   `-- VarianceCommandHandler.kt
`-- query/
    |-- CostQueryHandler.kt
    `-- VarianceQueryHandler.kt
```
