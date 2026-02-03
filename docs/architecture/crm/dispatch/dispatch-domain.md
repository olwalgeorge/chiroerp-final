# CRM Dispatch Domain Layer

> Part of [CRM Dispatch](../crm-dispatch.md)

## Directory Structure

```
dispatch-domain/
`-- src/main/kotlin/com.erp.crm.dispatch.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Dispatch Schedule (`model/schedule/`)

```
|-- schedule/
|   |-- DispatchSchedule.kt         # Aggregate Root
|   |-- DispatchId.kt
|   |-- DispatchStatus.kt           # Pending, Assigned, Confirmed, Cancelled
|   `-- Assignment.kt               # Entity
```

### Technician (`model/technician/`)

```
|-- technician/
|   |-- Technician.kt               # Aggregate Root
|   |-- TechnicianId.kt
|   |-- SkillSet.kt                 # Value Object
|   `-- AvailabilityWindow.kt
```

### Route Plan (`model/route/`)

```
|-- route/
|   |-- RoutePlan.kt                # Aggregate Root
|   |-- RouteId.kt
|   `-- Stop.kt                     # Entity
```

---

## Domain Events

```
events/
|-- TechnicianAssignedEvent.kt
|-- DispatchConfirmedEvent.kt
|-- DispatchRejectedEvent.kt
|-- RouteOptimizedEvent.kt
`-- TechnicianStatusChangedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- TechnicianNotAvailableException.kt
|-- AssignmentConflictException.kt
`-- RouteOptimizationFailedException.kt
```

---

## Domain Services

```
services/
|-- RoutingService.kt               # Route optimization
|-- CapacityBalancingService.kt     # Load balancing
`-- SkillsMatchingService.kt        # Skill-based assignment
```

---

## Key Invariants

1. **Skill Match Required**: Assignments must satisfy required skills.
2. **No Overlap**: Technician assignments cannot overlap.
3. **Time Window Compliance**: Dispatch must respect SLA windows.
4. **Route Feasibility**: Optimized routes must meet travel constraints.
