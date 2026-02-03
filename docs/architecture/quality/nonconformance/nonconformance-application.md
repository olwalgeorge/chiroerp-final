# Quality Nonconformance Application Layer

> Part of [Quality Nonconformance](../quality-nonconformance.md)

## Directory Structure

```
nonconformance-application/
`-- src/main/kotlin/com.erp.quality.nonconformance.application/
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
|-- CreateNonconformanceCommand.kt
|   `-- sourceLotId, ncType, severity, description
|
|-- DetermineDispositionCommand.kt
|   `-- ncId, dispositionType, notes
|
|-- PostQualityCostCommand.kt
|   `-- ncId, costCategory, amount
|
`-- CloseNonconformanceCommand.kt
    `-- ncId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetNonconformanceQuery.kt
|   `-- ncId -> NonconformanceDto
|
|-- ListNonconformanceQuery.kt
|   `-- status, severity -> List<NonconformanceDto>
|
`-- GetQualityCostSummaryQuery.kt
    `-- period -> QualityCostSummaryDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- NonconformanceRepository.kt
|-- InventoryDispositionPort.kt
|-- FinancePostingPort.kt
|-- CapaPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- NonconformanceCommandHandler.kt
|   `-- DispositionCommandHandler.kt
`-- query/
    |-- NonconformanceQueryHandler.kt
    `-- QualityCostQueryHandler.kt
```
