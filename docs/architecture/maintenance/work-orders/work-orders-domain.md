# Plant Maintenance Work Orders Domain Layer

> Part of [Plant Maintenance Work Orders](../maintenance-work-orders.md)

## Directory Structure

```
work-orders-domain/
`-- src/main/kotlin/com.erp.maintenance.workorders.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Work Order (`model/order/`)

```
|-- order/
|   |-- WorkOrder.kt              # Aggregate Root
|   |-- WorkOrderId.kt
|   |-- WorkOrderType.kt          # Preventive, Corrective, Inspection
|   |-- WorkOrderStatus.kt        # Planned, Released, Completed
|   `-- Priority.kt
```

### Operations (`model/operation/`)

```
|-- operation/
|   |-- WorkOrderOperation.kt     # Entity
|   |-- OperationStatus.kt
|   `-- LaborRequirement.kt
```

### Materials (`model/material/`)

```
|-- material/
|   |-- PartsReservation.kt       # Entity
|   `-- PartsIssue.kt             # Entity
```

---

## Domain Events

```
events/
|-- WorkOrderCreatedEvent.kt
|-- WorkOrderReleasedEvent.kt
|-- WorkOrderCompletedEvent.kt
`-- MaintenanceCostPostedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- WorkOrderNotFoundException.kt
|-- InvalidWorkOrderStatusException.kt
|-- PartsUnavailableException.kt
`-- LaborRequirementMissingException.kt
```

---

## Domain Services

```
services/
|-- WorkOrderSchedulingService.kt
|-- CostPostingService.kt
`-- PartsReservationService.kt
```

---

## Key Invariants

1. **Status Flow**: Planned -> Released -> Completed only.
2. **Parts Reservation**: Required for critical parts before release.
3. **Cost Integrity**: Costs must be tied to a work order.
4. **Completion Rules**: All operations must be closed before completion.
