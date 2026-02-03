# Manufacturing Production Orders Domain Layer

> Part of [Manufacturing Production Orders](../manufacturing-production.md)

## Directory Structure

```
production-domain/
`-- src/main/kotlin/com.erp.manufacturing.production.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Production Order (`model/order/`)

```
|-- order/
|   |-- ProductionOrder.kt          # Aggregate Root
|   |-- ProductionOrderId.kt
|   |-- ProductionOrderStatus.kt    # Planned, Released, InProcess, Completed, Closed
|   |-- Operation.kt                # Entity
|   |-- OperationStatus.kt          # NotStarted, InProgress, Completed
|   |-- MaterialIssue.kt            # Entity
|   `-- Receipt.kt                  # Entity
```

---

## Domain Events

```
events/
|-- ProductionOrderReleasedEvent.kt
|-- MaterialIssuedEvent.kt
|-- ProductionConfirmedEvent.kt
|-- ProductionReceiptPostedEvent.kt
`-- ProductionOrderClosedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- ProductionOrderNotFoundException.kt
|-- InvalidOrderStateException.kt
|-- InsufficientComponentException.kt
`-- PeriodClosedException.kt
```

---

## Domain Services

```
services/
|-- BackflushService.kt             # Auto component consumption
|-- WipService.kt                   # WIP accumulation and release
`-- YieldCalculationService.kt      # Yield and scrap rules
```

---

## Key Invariants

1. **Release Required**: Components cannot be issued before release.
2. **Operation Sequence**: Operations must follow routing sequence.
3. **Receipt Integrity**: Receipt quantity cannot exceed confirmed quantity.
4. **Period Control**: No postings to closed manufacturing periods.
