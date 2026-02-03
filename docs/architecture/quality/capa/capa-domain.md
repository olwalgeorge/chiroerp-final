# Quality CAPA Domain Layer

> Part of [Quality CAPA Management](../quality-capa.md)

## Directory Structure

```
capa-domain/
`-- src/main/kotlin/com.erp.quality.capa.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### CAPA (`model/capa/`)

```
|-- capa/
|   |-- CAPA.kt                    # Aggregate Root
|   |-- CAPAId.kt
|   |-- CAPAType.kt                # Corrective, Preventive
|   `-- CAPAStatus.kt              # Open, InProgress, Closed
```

### Root Cause (`model/rootcause/`)

```
|-- rootcause/
|   |-- RootCauseAnalysis.kt       # Entity
|   |-- Method.kt                  # FiveWhy, Fishbone, EightD
|   `-- RootCauseStatement.kt
```

### Action Plan (`model/action/`)

```
|-- action/
|   |-- ActionPlan.kt              # Entity
|   |-- ActionItem.kt              # Entity
|   `-- EffectivenessCheck.kt
```

---

## Domain Events

```
events/
|-- CAPAInitiatedEvent.kt
|-- RootCauseIdentifiedEvent.kt
|-- CorrectiveActionCompletedEvent.kt
`-- CAPAClosedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- CAPANotFoundException.kt
|-- RootCauseRequiredException.kt
|-- ActionPlanIncompleteException.kt
`-- EffectivenessCheckRequiredException.kt
```

---

## Domain Services

```
services/
|-- RootCauseService.kt            # 5-Why/Fishbone helpers
|-- ActionPlanService.kt           # Action tracking
`-- EffectivenessService.kt        # Closure validation
```

---

## Key Invariants

1. **Root Cause Required**: CAPA cannot close without root cause.
2. **Actions Completed**: All actions must be complete before closure.
3. **Effectiveness Verified**: Effectiveness check required for closure.
4. **Audit Trace**: CAPA changes must be fully auditable.
