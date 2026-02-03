# Quality Execution Application Layer

> Part of [Quality Execution](../quality-execution.md)

## Directory Structure

```
execution-application/
`-- src/main/kotlin/com.erp.quality.execution.application/
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
|-- CreateInspectionLotCommand.kt
|   `-- sourceType, referenceId, itemId, quantity, lotNumber
|
|-- RecordInspectionResultCommand.kt
|   `-- inspectionLotId, characteristicId, value
|
|-- MakeUsageDecisionCommand.kt
|   `-- inspectionLotId, decisionType, reason
|
`-- ReleaseStockCommand.kt
    `-- inspectionLotId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetInspectionLotQuery.kt
|   `-- inspectionLotId -> InspectionLotDto
|
|-- ListInspectionLotsQuery.kt
|   `-- status -> List<InspectionLotDto>
|
`-- GetUsageDecisionQuery.kt
    `-- inspectionLotId -> UsageDecisionDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- InspectionLotRepository.kt
|-- InspectionResultRepository.kt
|-- InspectionPlanPort.kt
|-- InventoryBlockPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- InspectionLotCommandHandler.kt
|   |-- InspectionResultCommandHandler.kt
|   `-- UsageDecisionCommandHandler.kt
`-- query/
    |-- InspectionLotQueryHandler.kt
    `-- UsageDecisionQueryHandler.kt
```
