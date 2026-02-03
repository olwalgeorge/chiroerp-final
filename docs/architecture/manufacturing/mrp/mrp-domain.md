# Manufacturing MRP Domain Layer

> Part of [Manufacturing MRP](../manufacturing-mrp.md)

## Directory Structure

```
mrp-domain/
`-- src/main/kotlin/com.erp.manufacturing.mrp.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### MRP Plan (`model/plan/`)

```
|-- plan/
|   |-- MRPPlan.kt                 # Aggregate Root
|   |-- MRPPlanId.kt
|   |-- PlanningHorizon.kt
|   |-- DemandSignal.kt            # Entity
|   |-- SupplyElement.kt           # Entity
|   |-- PeggingLink.kt             # Entity
|   `-- PlanningStatus.kt          # Draft, Running, Completed
```

### Planned Order (`model/order/`)

```
|-- order/
|   |-- PlannedOrder.kt            # Aggregate Root
|   |-- PlannedOrderId.kt
|   |-- PlannedOrderStatus.kt      # Created, Released, Converted
|   |-- RequirementDate.kt
|   `-- SupplyType.kt              # InHouse, External
```

---

## Domain Events

```
events/
|-- MRPRunStartedEvent.kt
|-- MRPRunCompletedEvent.kt
|-- PlannedOrderCreatedEvent.kt
|-- PlannedOrderConvertedEvent.kt
`-- PlanningExceptionRaisedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- PlanningHorizonInvalidException.kt
|-- DemandSignalMissingException.kt
|-- BomExplosionException.kt
`-- PlannedOrderNotFoundException.kt
```

---

## Domain Services

```
services/
|-- NettingService.kt             # Net requirements calculation
|-- PeggingService.kt             # Demand to supply mapping
`-- ExceptionService.kt           # Shortage and delay exceptions
```

---

## Key Invariants

1. **No Negative Supply**: Planned orders must not result in negative supply.
2. **Pegging Integrity**: Each planned order must link to at least one demand signal.
3. **Horizon Validity**: Requirements must fall within the planning horizon.
4. **BOM Consistency**: BOM explosion must resolve all components.
