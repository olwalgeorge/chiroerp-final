# Manufacturing Costing Domain Layer

> Part of [Manufacturing Costing](../manufacturing-costing.md)

## Directory Structure

```
costing-domain/
`-- src/main/kotlin/com.erp.manufacturing.costing.domain/
    |-- model/
    |-- events/
    |-- exceptions/
    `-- services/
```

---

## Aggregates & Entities

### Cost Estimate (`model/cost/`)

```
|-- cost/
|   |-- CostEstimate.kt             # Aggregate Root
|   |-- CostEstimateId.kt
|   |-- CostComponent.kt            # Entity (material, labor, overhead)
|   |-- CostVersion.kt
|   `-- StandardCost.kt
```

### WIP (`model/wip/`)

```
|-- wip/
|   |-- WIPBalance.kt               # Aggregate Root
|   |-- WIPBalanceId.kt
|   |-- WIPStatus.kt                # Open, Settled
|   `-- WIPLine.kt                  # Entity
```

### Variance (`model/variance/`)

```
|-- variance/
|   |-- VarianceRecord.kt           # Aggregate Root
|   |-- VarianceType.kt             # Usage, Price, Yield
|   `-- VarianceAmount.kt
```

---

## Domain Events

```
events/
|-- CostRollupCompletedEvent.kt
|-- WIPPostedEvent.kt
`-- ProductionVariancePostedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
|-- CostEstimateNotFoundException.kt
|-- InvalidCostVersionException.kt
`-- PeriodClosedException.kt
```

---

## Domain Services

```
services/
|-- CostRollupService.kt            # Rollup from BOM and routing
|-- WipSettlementService.kt         # WIP close and settlement
`-- VarianceAnalysisService.kt      # Variance calculations
```

---

## Key Invariants

1. **Cost Version Integrity**: Only one active standard cost per item.
2. **WIP Balancing**: WIP must settle to zero at order close.
3. **Variance Posting**: Variances only posted for completed orders.
