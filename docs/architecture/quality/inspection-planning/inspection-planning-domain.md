# Quality Inspection Planning Domain Layer

> Part of [Quality Inspection Planning](../quality-inspection-planning.md)

## Directory Structure

```
inspection-planning-domain/
`-- src/main/kotlin/com.erp.quality.inspectionplanning.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Inspection Plan (`model/plan/`)

```
|-- plan/
|   |-- InspectionPlan.kt           # Aggregate Root
|   |-- InspectionPlanId.kt
|   |-- PlanStatus.kt               # Draft, Active, Retired
|   |-- PlanVersion.kt
|   `-- Effectivity.kt
```

### Characteristics (`model/characteristic/`)

```
|-- characteristic/
|   |-- InspectionCharacteristic.kt # Entity
|   |-- CharacteristicId.kt
|   |-- SpecLimit.kt                # Value Object (min/max)
|   `-- UnitOfMeasure.kt
```

### Sampling (`model/sampling/`)

```
|-- sampling/
|   |-- SamplingPlan.kt             # Aggregate Root
|   |-- SamplingPlanId.kt
|   |-- AQLLevel.kt
|   `-- SampleSizeRule.kt
```

### Trigger Rules (`model/trigger/`)

```
|-- trigger/
|   |-- TriggerRule.kt              # Entity
|   |-- TriggerType.kt              # Incoming, InProcess, Return
|   `-- TriggerScope.kt             # Item, Supplier, Plant
```

---

## Domain Events

```
events/
|-- InspectionPlanCreatedEvent.kt
|-- InspectionPlanUpdatedEvent.kt
|-- SamplingPlanUpdatedEvent.kt
`-- TriggerRuleActivatedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- CharacteristicInvalidException.kt
|-- SamplingPlanInvalidException.kt
|-- TriggerRuleConflictException.kt
`-- PlanNotFoundException.kt
```

---

## Domain Services

```
services/
|-- SamplingService.kt              # Sample size evaluation
|-- TriggerEvaluationService.kt     # Rule matching
`-- PlanVersioningService.kt        # Effectivity checks
```

---

## Key Invariants

1. **Plan Completeness**: An active plan must have at least one characteristic.
2. **Sampling Validity**: Sampling plans must define a sample size rule.
3. **Trigger Exclusivity**: Trigger rules cannot overlap for the same scope.
4. **Effectivity Control**: Only one active plan per item per date.
