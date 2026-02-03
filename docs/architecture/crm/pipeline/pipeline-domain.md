# CRM Pipeline Domain Layer

> Part of [CRM Pipeline](../crm-pipeline.md)

## Directory Structure

```
pipeline-domain/
`-- src/main/kotlin/com.erp.crm.pipeline.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Opportunity (`model/opportunity/`)

```
|-- opportunity/
|   |-- Opportunity.kt              # Aggregate Root
|   |-- OpportunityId.kt
|   |-- OpportunityStage.kt         # Prospecting, Qualification, Proposal, Negotiation, Closed
|   |-- OpportunityLine.kt          # Entity
|   `-- CloseReason.kt
```

### Pipeline Stage (`model/stage/`)

```
|-- stage/
|   |-- StageDefinition.kt          # Aggregate Root
|   |-- StageId.kt
|   `-- StageRule.kt                # Entry/exit criteria
```

### Forecast (`model/forecast/`)

```
|-- forecast/
|   |-- ForecastSnapshot.kt         # Aggregate Root
|   |-- ForecastId.kt
|   `-- ForecastLine.kt             # Entity
```

---

## Domain Events

```
events/
|-- OpportunityCreatedEvent.kt
|-- OpportunityStageChangedEvent.kt
|-- OpportunityClosedWonEvent.kt
|-- OpportunityClosedLostEvent.kt
`-- ForecastSnapshotCreatedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- OpportunityNotFoundException.kt
|-- InvalidStageTransitionException.kt
`-- ForecastLockedException.kt
```

---

## Domain Services

```
services/
|-- StagePolicyService.kt           # Stage entry/exit rules
|-- ForecastingService.kt           # Weighted pipeline
`-- ProbabilityCalculatorService.kt # Stage-based probability
```

---

## Key Invariants

1. **Stage Progression**: Opportunities follow configured stage order.
2. **Closed Final**: Closed Won/Lost is terminal.
3. **Forecast Snapshot**: Snapshots are immutable once published.
4. **Close Reasons**: Closed Lost requires a reason code.
