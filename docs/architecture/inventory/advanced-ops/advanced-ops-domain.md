# Inventory Advanced Ops Domain Layer

> Part of [Inventory Advanced Operations](../inventory-advanced-ops.md)

## Directory Structure

```
advanced-ops-domain/
└── src/main/kotlin/com.erp.inventory.advancedops.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Packaging (`model/packaging/`)

```
├── PackagingHierarchy.kt          # Aggregate Root
├── PackagingLevel.kt              # Entity (pallet/case/inner/each)
├── Gtin.kt                        # Value Object
├── TiHi.kt                        # Value Object
├── PackagingUom.kt                # Value Object
└── HandlingUnitType.kt            # Value Object
```

### Kitting (`model/kitting/`)

```
├── Kit.kt                         # Aggregate Root
├── KitComponent.kt                # Entity
├── KitAssemblyOrder.kt            # Aggregate Root
├── KitAssemblyLine.kt             # Entity
└── KitType.kt                     # STATIC, DYNAMIC, VIRTUAL, CONFIGURABLE
```

### Repack / VAS (`model/repack/`)

```
├── RepackOrder.kt                 # Aggregate Root
├── RepackLine.kt                  # Entity
├── VasOperation.kt                # Entity (labeling, gift wrap, etc.)
└── RepackVariance.kt              # Value Object
```

### Catch Weight (`model/catchweight/`)

```
├── CatchWeightItem.kt             # Aggregate Root
├── CatchWeightRecord.kt           # Entity
├── NominalWeight.kt               # Value Object
├── ActualWeight.kt                # Value Object
└── TareWeight.kt                  # Value Object
```

---

## Domain Events

```
events/
├── KitAssemblyWorkOrderCreatedEvent.kt
├── KitAssembledEvent.kt
├── KitDisassembledEvent.kt
├── RepackWorkOrderCreatedEvent.kt
├── RepackCompletedEvent.kt
├── BreakBulkWorkOrderCreatedEvent.kt
├── CatchWeightRecordedEvent.kt
└── VasOperationCompletedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── InvalidPackagingHierarchyException.kt
├── KitComponentUnavailableException.kt
├── RepackVarianceExceededException.kt
├── InvalidCatchWeightException.kt
└── UnsupportedKitTypeException.kt
```

---

## Domain Services

```
services/
├── KitAtpService.kt               # Explode kit to component ATP
├── CostRollupService.kt           # Kit/repack cost rollup
├── PackagingConversionService.kt  # Nested UoM validation
└── CatchWeightPricingService.kt   # Actual weight pricing
```

---

## Key Invariants

1. **Packaging Integrity**: Each packaging level must have a valid parent and conversion factor.
2. **Kit Availability**: Kit assembly requires all components to be reservable.
3. **Repack Accuracy**: Output quantity + variance must reconcile to input quantity.
4. **Catch Weight Validity**: Actual weight must be > 0 and within tolerance of nominal.
5. **Traceability**: Lot/serial relationships are preserved through kit/repack operations.
