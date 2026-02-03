# Analytics KPI Domain Layer

> Part of [Analytics KPI Engine](../analytics-kpi.md)

## Directory Structure

```
kpi-domain/
`-- src/main/kotlin/com.erp.analytics.kpi.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### KPI Definition (`model/definition/`)

```
|-- definition/
|   |-- KPIDefinition.kt           # Aggregate Root
|   |-- KpiId.kt
|   |-- Formula.kt
|   `-- Threshold.kt
```

### KPI Result (`model/result/`)

```
|-- result/
|   |-- KPIResult.kt               # Entity
|   |-- ResultPeriod.kt
|   `-- ResultStatus.kt
```

---

## Domain Events

```
events/
|-- KpiCalculatedEvent.kt
|-- KpiThresholdBreachedEvent.kt
`-- KpiDefinitionUpdatedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- KpiNotFoundException.kt
|-- InvalidFormulaException.kt
`-- ThresholdInvalidException.kt
```

---

## Domain Services

```
services/
|-- KpiCalculationService.kt
|-- ThresholdEvaluationService.kt
`-- KpiSchedulingService.kt
```

---

## Key Invariants

1. **Formula Validity**: KPI formulas must validate before activation.
2. **Threshold Range**: Thresholds must be defined per KPI.
3. **Auditability**: KPI definition changes are versioned.
4. **Single Source**: KPI results derived from warehouse/cubes only.
