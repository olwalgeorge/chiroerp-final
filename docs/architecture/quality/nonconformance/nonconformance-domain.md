# Quality Nonconformance Domain Layer

> Part of [Quality Nonconformance](../quality-nonconformance.md)

## Directory Structure

```
nonconformance-domain/
`-- src/main/kotlin/com.erp.quality.nonconformance.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Nonconformance (`model/nc/`)

```
|-- nc/
|   |-- Nonconformance.kt          # Aggregate Root
|   |-- NonconformanceId.kt
|   |-- NCType.kt                  # Material, Process, Supplier, Customer
|   |-- NCSeverity.kt              # Minor, Major, Critical
|   `-- NCStatus.kt                # Open, Dispositioned, Closed
```

### Disposition (`model/disposition/`)

```
|-- disposition/
|   |-- Disposition.kt             # Entity
|   |-- DispositionType.kt         # Rework, Scrap, Return
|   `-- DispositionCost.kt
```

### Quality Cost (`model/cost/`)

```
|-- cost/
|   |-- QualityCost.kt             # Entity
|   |-- CostCategory.kt            # Internal, External
|   `-- CostAmount.kt
```

---

## Domain Events

```
events/
|-- NonconformanceCreatedEvent.kt
|-- DispositionDeterminedEvent.kt
|-- NonconformanceClosedEvent.kt
`-- QualityCostPostedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- NonconformanceNotFoundException.kt
|-- DispositionRequiredException.kt
|-- InvalidSeverityException.kt
`-- QualityCostInvalidException.kt
```

---

## Domain Services

```
services/
|-- DispositionService.kt          # Rework/scrap logic
|-- QualityCostService.kt          # Cost calculation
`-- EscalationService.kt           # Critical NC escalation
```

---

## Key Invariants

1. **Disposition Required**: NCs must be dispositioned before closure.
2. **Severity Validity**: Severity cannot be downgraded once critical.
3. **Cost Integrity**: Quality costs must be tied to an NC record.
4. **Traceability**: NC must reference source inspection lot.
