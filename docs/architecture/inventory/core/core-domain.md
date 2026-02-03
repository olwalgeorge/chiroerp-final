# Inventory Core Domain Layer

> Part of [Inventory Core](../inventory-core.md)

## Directory Structure

```
core-domain/
└── src/main/kotlin/com.erp.inventory.core.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Item and Variant (`model/item/`)

```
├── item/
│   ├── Item.kt                      # Aggregate Root
│   ├── ItemId.kt
│   ├── ItemType.kt                  # Stocked, NonStock, Service
│   ├── ItemStatus.kt                # Active, Blocked, Discontinued
│   ├── ItemVariant.kt               # Entity
│   ├── VariantId.kt
│   ├── Barcode.kt
│   ├── UoM.kt                        # Base unit
│   ├── UoMConversion.kt
│   └── ItemAttribute.kt             # Size, color, style
```

**Item Aggregate**:
```kotlin
class Item private constructor(
    val id: ItemId,
    val sku: String,
    val description: String,
    val status: ItemStatus,
    val baseUom: UoM,
    val variants: MutableList<ItemVariant>
) {
    fun activate(): ItemActivatedEvent
    fun discontinue(reason: String): ItemDiscontinuedEvent
    fun addVariant(variant: ItemVariant): VariantAddedEvent
}
```

---

### Location and Bin (`model/location/`)

```
├── location/
│   ├── Location.kt                  # Aggregate Root
│   ├── LocationId.kt
│   ├── LocationType.kt              # Store, Warehouse, Vendor, InTransit
│   ├── Bin.kt                       # Entity (optional for MM-IM)
│   ├── BinId.kt
│   ├── Capacity.kt
│   └── TemperatureZone.kt
```

---

### Stock Ledger (`model/stock/`)

```
├── stock/
│   ├── StockLedger.kt               # Aggregate Root
│   ├── StockMovement.kt             # Entity
│   ├── MovementType.kt              # Receipt, Issue, Transfer, Adjustment
│   ├── StockStatus.kt               # Available, Reserved, Damaged, InTransit
│   ├── StockLot.kt                  # Entity
│   ├── StockSerial.kt               # Entity
│   └── ExpiryDate.kt
```

---

### Reservation (`model/reservation/`)

```
├── reservation/
│   ├── Reservation.kt               # Aggregate Root
│   ├── ReservationId.kt
│   ├── ReservationStatus.kt         # Pending, Confirmed, Released, Cancelled
│   ├── AllocationRule.kt
│   └── ChannelPriority.kt
```

---

### Cost and Valuation (`model/valuation/`)

```
├── valuation/
│   ├── CostLayer.kt                 # Aggregate Root
│   ├── CostLayerId.kt
│   ├── ValuationMethod.kt           # FIFO, WAC, Standard
│   ├── LandedCost.kt                # Entity
│   ├── CurrencyRate.kt
│   └── ValuationRun.kt              # Aggregate Root
```

---

### Cycle Count (`model/counting/`)

```
├── counting/
│   ├── CycleCount.kt                # Aggregate Root
│   ├── CycleCountLine.kt            # Entity
│   ├── CountStatus.kt               # Planned, InProgress, Approved, Posted
│   └── VarianceReason.kt
```

---

## Domain Events

```
events/
├── StockReceivedEvent.kt
├── StockIssuedEvent.kt
├── StockTransferredEvent.kt
├── StockAdjustedEvent.kt
├── ReservationCreatedEvent.kt
├── ReservationReleasedEvent.kt
├── CycleCountPostedEvent.kt
├── InventoryValuationPostedEvent.kt
├── LotExpiredEvent.kt
└── ItemDiscontinuedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── InsufficientStockException.kt
├── InvalidMovementTypeException.kt
├── ReservationNotFoundException.kt
├── InvalidUoMConversionException.kt
├── PeriodClosedException.kt
└── LotExpiredException.kt
```

---

## Domain Services

```
services/
├── ValuationService.kt              # FIFO/WAC/Standard calculations
├── ReservationService.kt            # Allocation and release rules
├── UoMConversionService.kt          # Unit conversions with rounding
├── LotAllocationService.kt          # FEFO selection
└── CycleCountVarianceService.kt     # Variance approval rules
```

---

## Key Invariants

1. **No Negative Stock**: Stock on hand cannot drop below zero for controlled items.
2. **Reservation Integrity**: Reserved quantity cannot exceed available stock.
3. **Valuation Consistency**: Cost layers are immutable after posting.
4. **UoM Validity**: All movements must use valid conversions to base UoM.
5. **Lot Validity**: Expired lots cannot be allocated or issued.
6. **Period Control**: No postings to closed inventory periods.
