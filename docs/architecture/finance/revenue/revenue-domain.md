# Revenue Domain Layer

> Part of [Finance - Revenue Recognition](../finance-revenue.md)

## Directory Structure

```
revenue-domain/
└── src/main/kotlin/com.erp.finance.revenue.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Contract (`model/contract/`)

```
├── contract/
│   ├── Contract.kt                # Aggregate Root
│   ├── ContractId.kt
│   ├── ContractStatus.kt          # Draft, Active, Modified, Closed
│   ├── ContractTerm.kt
│   ├── TransactionPrice.kt
│   └── ContractModification.kt
```

### Performance Obligation (`model/obligation/`)

```
├── obligation/
│   ├── PerformanceObligation.kt   # Aggregate Root
│   ├── ObligationId.kt
│   ├── SatisfactionMethod.kt      # PointInTime, OverTime
│   ├── StandaloneSellingPrice.kt
│   └── AllocationResult.kt
```

### Revenue Schedule (`model/schedule/`)

```
├── schedule/
│   ├── RevenueSchedule.kt         # Aggregate Root
│   ├── ScheduleLine.kt            # Entity
│   ├── RevenueMethod.kt           # StraightLine, Usage
│   └── ScheduleStatus.kt
```

### Deferred Revenue (`model/deferred/`)

```
├── deferred/
│   ├── DeferredRevenue.kt         # Aggregate Root
│   ├── DeferredRevenueId.kt
│   └── DeferredBalance.kt
```

---

## Domain Events

```
events/
├── ContractIdentifiedEvent.kt
├── ContractModifiedEvent.kt
├── RevenueRecognizedEvent.kt
├── RevenueDeferredEvent.kt
└── PerformanceObligationSatisfiedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── ContractNotFoundException.kt
├── InvalidAllocationException.kt
├── ScheduleNotFoundException.kt
└── PeriodClosedException.kt
```

---

## Domain Services

```
services/
├── AllocationService.kt           # SSP allocation
├── RecognitionService.kt          # Revenue recognition rules
└── ScheduleGenerationService.kt   # Build schedules
```

---

## Key Invariants

1. **Allocation Integrity**: SSP allocation totals equal transaction price.
2. **Schedule Balance**: Recognized + deferred = contract value.
3. **Open Period**: Recognition only in open periods.
4. **Immutable History**: Recognized lines are immutable; adjustments create new lines.
