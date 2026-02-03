# Plant Maintenance Preventive Domain Layer

> Part of [Plant Maintenance Preventive Maintenance](../maintenance-preventive.md)

## Directory Structure

```
preventive-domain/
`-- src/main/kotlin/com.erp.maintenance.preventive.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Maintenance Plan (`model/plan/`)

```
|-- plan/
|   |-- MaintenancePlan.kt         # Aggregate Root
|   |-- MaintenancePlanId.kt
|   |-- PlanType.kt                # Time, Counter
|   `-- PlanStatus.kt              # Draft, Active, Retired
```

### Schedule (`model/schedule/`)

```
|-- schedule/
|   |-- MaintenanceSchedule.kt     # Entity
|   |-- ScheduleWindow.kt
|   `-- CallHorizon.kt
```

### Counter (`model/counter/`)

```
|-- counter/
|   |-- MaintenanceCounter.kt      # Entity
|   |-- CounterType.kt             # Hours, Cycles
|   `-- CounterReading.kt
```

---

## Domain Events

```
events/
|-- MaintenancePlanCreatedEvent.kt
|-- MaintenanceScheduleGeneratedEvent.kt
`-- PreventiveWorkOrderTriggeredEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- MaintenancePlanNotFoundException.kt
|-- ScheduleConflictException.kt
|-- CounterInvalidException.kt
`-- PlanInactiveException.kt
```

---

## Domain Services

```
services/
|-- SchedulingService.kt           # Call horizon logic
|-- CounterEvaluationService.kt    # Counter-based triggers
`-- PlanActivationService.kt
```

---

## Key Invariants

1. **Active Plan**: Only active plans generate schedules.
2. **Window Integrity**: Schedules must not overlap shutdown windows.
3. **Counter Validity**: Counter readings must be non-decreasing.
4. **Trigger Control**: Only one trigger per plan per window.
