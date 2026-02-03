# Quality Execution Domain Layer

> Part of [Quality Execution](../quality-execution.md)

## Directory Structure

```
execution-domain/
`-- src/main/kotlin/com.erp.quality.execution.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Inspection Lot (`model/lot/`)

```
|-- lot/
|   |-- InspectionLot.kt          # Aggregate Root
|   |-- InspectionLotId.kt
|   |-- LotStatus.kt              # Created, InProgress, Completed
|   `-- LotSource.kt              # Incoming, InProcess, Return
```

### Results (`model/result/`)

```
|-- result/
|   |-- InspectionResult.kt       # Entity
|   |-- ResultId.kt
|   |-- ResultValue.kt
|   `-- ResultStatus.kt           # Pass, Fail, Hold
```

### Usage Decision (`model/decision/`)

```
|-- decision/
|   |-- UsageDecision.kt          # Aggregate Root
|   |-- DecisionType.kt           # Accept, Reject, Rework
|   `-- DecisionReason.kt
```

---

## Domain Events

```
events/
|-- InspectionLotCreatedEvent.kt
|-- InspectionCompletedEvent.kt
|-- StockBlockedEvent.kt
|-- StockReleasedEvent.kt
`-- DefectDetectedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- InspectionLotNotFoundException.kt
|-- UsageDecisionRequiredException.kt
|-- ResultOutOfRangeException.kt
`-- DecisionNotAllowedException.kt
```

---

## Domain Services

```
services/
|-- UsageDecisionService.kt       # Accept/reject logic
|-- StockBlockService.kt          # Inventory blocking
`-- ResultEvaluationService.kt    # Pass/fail determination
```

---

## Key Invariants

1. **Results Required**: Usage decisions require recorded results.
2. **Single Decision**: Only one usage decision per inspection lot.
3. **Stock Integrity**: Blocked stock cannot be issued.
4. **Audit Trace**: Results are immutable after decision.
