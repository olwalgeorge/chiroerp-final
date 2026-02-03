# Controlling Domain Layer

> Part of [Finance - Controlling](../finance-controlling.md)

## Directory Structure

```
controlling-domain/
└── src/main/kotlin/com.erp.finance.controlling.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Cost Center (`model/costcenter/`)

```
├── costcenter/
│   ├── CostCenter.kt              # Aggregate Root
│   ├── CostCenterId.kt
│   ├── CostCenterCode.kt
│   ├── CostCenterStatus.kt        # Active, Blocked, Closed
│   ├── CostCenterHierarchy.kt
│   ├── ResponsibilityArea.kt
│   └── ValidityPeriod.kt
```

---

### Profit Center (`model/profitcenter/`)

```
├── profitcenter/
│   ├── ProfitCenter.kt            # Aggregate Root
│   ├── ProfitCenterId.kt
│   ├── ProfitCenterCode.kt
│   ├── ProfitCenterStatus.kt
│   └── ProfitCenterHierarchy.kt
```

---

### Internal Order (`model/order/`)

```
├── order/
│   ├── InternalOrder.kt           # Aggregate Root
│   ├── InternalOrderId.kt
│   ├── OrderType.kt               # Project, Maintenance, Marketing
│   ├── OrderStatus.kt             # Open, Closed, Settled
│   ├── SettlementRule.kt
│   └── BudgetControl.kt
```

---

### Allocation Cycle (`model/allocation/`)

```
├── allocation/
│   ├── AllocationCycle.kt         # Aggregate Root
│   ├── AllocationCycleId.kt
│   ├── AllocationRule.kt
│   ├── AllocationBasis.kt          # Headcount, Usage, Revenue
│   ├── AllocationRun.kt            # Entity
│   ├── AllocationRunId.kt
│   └── AllocationStatus.kt         # Draft, Posted, Reversed
```

---

### Activity Types (`model/activity/`)

```
├── activity/
│   ├── ActivityType.kt            # Aggregate Root
│   ├── ActivityTypeId.kt
│   ├── ActivityRate.kt
│   └── ActivityUnit.kt            # Hour, Unit, km
```

---

## Domain Events

```
events/
├── CostCenterCreatedEvent.kt
├── CostCenterBlockedEvent.kt
├── ProfitCenterCreatedEvent.kt
├── InternalOrderClosedEvent.kt
├── AllocationRunPostedEvent.kt
├── AllocationRunReversedEvent.kt
└── BudgetAllocatedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── CostCenterNotFoundException.kt
├── ProfitCenterNotFoundException.kt
├── InternalOrderNotFoundException.kt
├── AllocationRuleInvalidException.kt
├── AllocationRunAlreadyPostedException.kt
├── PeriodClosedException.kt
└── BudgetExceededException.kt
```

---

## Domain Services

```
services/
├── AllocationEngine.kt            # Execute allocation rules
├── CostCenterValidationService.kt # Status and hierarchy checks
├── ActivityRateService.kt         # Rate calculation
├── BudgetControlService.kt        # Budget enforcement
└── SettlementService.kt           # Internal order settlement
```

---

## Key Invariants

1. **Valid Cost Center**: Posting requires active cost center.
2. **Allocation Balance**: Allocation run must balance total debits/credits.
3. **Open Period**: Allocations only in open periods.
4. **Order Status**: Closed orders cannot receive postings.
5. **Budget Control**: Enforced when configured for order/cost center.
