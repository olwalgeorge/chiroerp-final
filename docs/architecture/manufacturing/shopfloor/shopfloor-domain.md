# Manufacturing Shop Floor Domain Layer

> Part of [Manufacturing Shop Floor Execution](../manufacturing-shop-floor.md)

## Directory Structure

```
shopfloor-domain/
`-- src/main/kotlin/com.erp.manufacturing.shopfloor.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Dispatch (`model/dispatch/`)

```
|-- dispatch/
|   |-- WorkOrderDispatch.kt        # Aggregate Root
|   |-- DispatchId.kt
|   |-- DispatchStatus.kt           # Queued, InProgress, Completed
|   `-- DispatchRule.kt             # Priority rules
```

### Operation Confirmation (`model/confirmation/`)

```
|-- confirmation/
|   |-- OperationConfirmation.kt    # Aggregate Root
|   |-- ConfirmationId.kt
|   |-- LaborTicket.kt              # Entity
|   |-- MachineTime.kt              # Value Object
|   `-- ConfirmationStatus.kt
```

### Scrap (`model/scrap/`)

```
|-- scrap/
|   |-- ScrapRecord.kt              # Aggregate Root
|   |-- ScrapReason.kt
|   `-- ScrapQuantity.kt
```

---

## Domain Events

```
events/
|-- OperationStartedEvent.kt
|-- OperationCompletedEvent.kt
|-- ScrapRecordedEvent.kt
`-- DowntimeLoggedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- DispatchNotFoundException.kt
|-- InvalidOperationStateException.kt
`-- WorkCenterUnavailableException.kt
```

---

## Domain Services

```
services/
|-- DispatchingService.kt           # Sequencing and priorities
|-- TimeCaptureService.kt           # Labor/machine time capture
`-- ScrapAnalysisService.kt         # Scrap classification rules
```

---

## Key Invariants

1. **Dispatch Order**: Operations must respect dispatch priority.
2. **Confirmation Integrity**: Completed operations must have time captured.
3. **Scrap Traceability**: Scrap records require reason codes.
