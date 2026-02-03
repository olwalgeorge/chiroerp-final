# Inventory Valuation Domain Layer

> Part of [Inventory Valuation & Costing](../inventory-valuation.md)

## Directory Structure

```
valuation-domain/
└── src/main/kotlin/com.erp.inventory.valuation.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Cost Layer (`model/cost/`)

```
├── cost/
│   ├── CostLayer.kt                 # Aggregate Root
│   ├── CostLayerId.kt
│   ├── CostMethod.kt                # FIFO, WAC, Standard
│   ├── CostComponent.kt             # Entity (material, freight, duty)
│   └── CostSource.kt                # PO, Transfer, Adjustment
```

---

### Valuation Run (`model/valuation/`)

```
├── valuation/
│   ├── ValuationRun.kt              # Aggregate Root
│   ├── ValuationRunId.kt
│   ├── ValuationStatus.kt           # Draft, Running, Completed, Posted
│   ├── ValuationPeriod.kt
│   └── ValuationResult.kt           # Entity
```

---

### Landed Cost (`model/landed/`)

```
├── landed/
│   ├── LandedCost.kt                # Aggregate Root
│   ├── LandedCostId.kt
│   ├── AllocationRule.kt            # ByValue, ByQuantity, ByWeight
│   ├── CostCharge.kt                # Entity
│   └── AllocationLine.kt            # Entity
```

---

### FX Revaluation (`model/fx/`)

```
├── fx/
│   ├── FxRevaluation.kt             # Aggregate Root
│   ├── FxRevaluationId.kt
│   ├── FxRate.kt
│   └── FxGainLoss.kt                # Entity
```

---

## Domain Events

```
events/
├── ValuationRunCompletedEvent.kt
├── ValuationPostedEvent.kt
├── LandedCostAllocatedEvent.kt
├── FxRevaluationPostedEvent.kt
└── CostLayerAdjustedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── InvalidCostMethodException.kt
├── ValuationRunInProgressException.kt
├── LandedCostAllocationException.kt
└── PeriodClosedException.kt
```

---

## Domain Services

```
services/
├── CostingEngine.kt                 # FIFO/WAC/Standard calculations
├── LandedCostAllocator.kt           # Allocation rules
├── FxRevaluationService.kt          # FX gains/losses
└── ValuationPostingService.kt       # GL posting orchestration
```

---

## Key Invariants

1. **Cost Layer Immutability**: Posted cost layers cannot be edited.
2. **Single Valuation Per Period**: Only one posted run per period.
3. **Allocation Balance**: Landed cost allocations must sum to total charges.
4. **Period Control**: Valuation cannot post to closed periods.
