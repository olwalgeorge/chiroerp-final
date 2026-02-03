# CRM Service Orders Domain Layer

> Part of [CRM Service Orders](../crm-service-orders.md)

## Directory Structure

```
service-orders-domain/
`-- src/main/kotlin/com.erp.crm.serviceorders.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Service Order (`model/order/`)

```
|-- order/
|   |-- ServiceOrder.kt             # Aggregate Root
|   |-- ServiceOrderId.kt
|   |-- ServiceOrderStatus.kt       # Requested, Scheduled, InProgress, Completed, Cancelled
|   |-- ServiceLine.kt              # Entity
|   `-- Priority.kt
```

### Work Order (`model/work/`)

```
|-- work/
|   |-- WorkOrder.kt                # Aggregate Root
|   |-- WorkOrderId.kt
|   |-- WorkTask.kt                 # Entity
|   `-- WorkStatus.kt               # Assigned, InProgress, Done
```

### SLA (`model/sla/`)

```
|-- sla/
|   |-- SlaPolicy.kt                # Aggregate Root
|   |-- SlaId.kt
|   |-- ResponseTarget.kt
|   `-- ResolutionTarget.kt
```

---

## Domain Events

```
events/
|-- ServiceOrderCreatedEvent.kt
|-- ServiceOrderScheduledEvent.kt
|-- ServiceOrderDispatchedEvent.kt
|-- ServiceOrderCompletedEvent.kt
|-- ServiceOrderBilledEvent.kt
`-- SlaBreachedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- InvalidServiceOrderStatusException.kt
|-- SlaViolationException.kt
|-- PartsNotAvailableException.kt
`-- TechnicianUnavailableException.kt
```

---

## Domain Services

```
services/
|-- SlaCalculatorService.kt         # Response/resolution timers
|-- BillingEligibilityService.kt    # Billing readiness checks
|-- DispatchPolicyService.kt        # Scheduling and assignment rules
`-- PartsReservationService.kt      # Inventory checks
```

---

## Key Invariants

1. **Schedule Before Dispatch**: Orders must be scheduled prior to dispatch.
2. **Completion Requires Work Logs**: All tasks and parts must be logged.
3. **Billing Gate**: Billing requires completion and approvals.
4. **SLA Clock**: SLA timers start on order creation.
