# Inventory Valuation Application Layer

> Part of [Inventory Valuation & Costing](../inventory-valuation.md)

## Directory Structure

```
valuation-application/
└── src/main/kotlin/com.erp.inventory.valuation.application/
    ├── port/
    │   ├── input/
    │   │   ├── command/
    │   │   └── query/
    │   └── output/
    └── service/
        ├── command/
        └── query/
```

---

## Commands (Write Operations)

### Valuation Commands

```
port/input/command/
├── RunValuationCommand.kt
│   └── period, method, includeLandedCost
│
├── PostValuationCommand.kt
│   └── valuationRunId, postingDate
│
└── ReverseValuationCommand.kt
    └── valuationRunId, reason
```

### Landed Cost Commands

```
├── CreateLandedCostCommand.kt
│   └── sourceDocument, charges[], allocationRule
│
└── AllocateLandedCostCommand.kt
    └── landedCostId
```

### FX Revaluation Commands

```
└── RunFxRevaluationCommand.kt
    └── period, currency, rateDate
```

---

## Queries (Read Operations)

```
port/input/query/
├── GetValuationRunByIdQuery.kt
│   └── valuationRunId -> ValuationRunDto
│
├── GetInventoryValuationQuery.kt
│   └── period -> InventoryValuationDto
│
└── GetCostLayersQuery.kt
    └── itemId -> List<CostLayerDto>
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── CostLayerRepository.kt
├── ValuationRunRepository.kt
├── LandedCostRepository.kt
└── FxRevaluationRepository.kt
```

### Integration Ports

```
├── InventoryCorePort.kt             # Stock movements
├── GeneralLedgerPort.kt             # GL posting
├── FxRatePort.kt                    # FX rates
└── EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
├── command/
│   ├── ValuationCommandHandler.kt
│   ├── LandedCostCommandHandler.kt
│   └── FxRevaluationCommandHandler.kt
└── query/
    ├── ValuationQueryHandler.kt
    └── CostLayerQueryHandler.kt
```
