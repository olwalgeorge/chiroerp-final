# Analytics KPI Application Layer

> Part of [Analytics KPI Engine](../analytics-kpi.md)

## Directory Structure

```
kpi-application/
`-- src/main/kotlin/com.erp.analytics.kpi.application/
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
|-- DefineKpiCommand.kt
|   `-- name, formula, thresholds
|
|-- CalculateKpiCommand.kt
|   `-- kpiId, period
|
`-- UpdateKpiThresholdCommand.kt
    `-- kpiId, thresholds
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetKpiQuery.kt
|   `-- kpiId -> KpiDto
|
|-- ListKpisQuery.kt
|   `-- domain -> List<KpiDto>
|
`-- GetKpiResultQuery.kt
    `-- kpiId, period -> KpiResultDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- KpiRepository.kt
|-- WarehouseMetricPort.kt
|-- OlapAggregatePort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- KpiDefinitionCommandHandler.kt
|   `-- KpiCalculationCommandHandler.kt
`-- query/
    |-- KpiQueryHandler.kt
    `-- KpiResultQueryHandler.kt
```
