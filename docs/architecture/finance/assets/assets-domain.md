# Assets Domain Layer

> Part of [Finance - Fixed Assets](../finance-assets.md)

## Directory Structure

```
assets-domain/
└── src/main/kotlin/com.erp.finance.assets.domain/
    ├── model/
    ├── events/
    ├── exceptions/
    └── services/
```

---

## Aggregates & Entities

### Asset (`model/asset/`)

```
├── asset/
│   ├── Asset.kt                      # Aggregate Root
│   ├── AssetId.kt
│   ├── AssetNumber.kt
│   ├── AssetStatus.kt                # Draft, Capitalized, InService, Suspended, Disposed
│   ├── AssetType.kt                  # Tangible, Intangible, ROU, CIP
│   ├── AssetComponent.kt             # Entity
│   ├── AssetComponentId.kt
│   ├── AssetLocation.kt              # Site, building, room
│   ├── AssetCustodian.kt
│   ├── AssetTag.kt                   # Barcode/RFID
│   ├── AssetCost.kt                  # Value Object (capitalized cost)
│   ├── ResidualValue.kt
│   ├── UsefulLife.kt                 # Years or units
│   ├── DepreciationMethod.kt         # SL, DB, SYD, UOP
│   ├── AssetBook.kt                  # Per-book values
│   ├── AssetBookValue.kt             # Cost, AccumDep, NBV
│   └── AssetTransaction.kt           # Acquisition, Transfer, Revalue, Impair, Dispose
```

**Asset Aggregate**:
```kotlin
class Asset private constructor(
    val id: AssetId,
    val assetNumber: String,
    val classId: AssetClassId,
    var status: AssetStatus,
    val description: String,
    val acquisitionDate: LocalDate,
    var inServiceDate: LocalDate?,
    val currency: CurrencyCode,
    val components: MutableList<AssetComponent>,
    val books: MutableList<AssetBook>
) {
    fun capitalize(cost: Money, capitalizationDate: LocalDate): AssetCapitalizedEvent
    fun placeInService(date: LocalDate): AssetPlacedInServiceEvent
    fun postDepreciation(bookId: DepreciationBookId, period: DepreciationPeriod, amount: Money): DepreciationPostedEvent
    fun revalue(newFairValue: Money, valuationDate: LocalDate): AssetRevaluedEvent
    fun impair(amount: Money, impairmentDate: LocalDate): AssetImpairedEvent
    fun transfer(transfer: AssetTransfer): AssetTransferredEvent
    fun dispose(disposal: Disposal): AssetDisposedEvent
}
```

---

### Asset Class (`model/class/`)

```
├── class/
│   ├── AssetClass.kt                 # Aggregate Root
│   ├── AssetClassId.kt
│   ├── CapitalizationThreshold.kt
│   ├── DepreciationKey.kt
│   ├── DefaultAccounts.kt            # Asset, AccumDep, DepExp, Gain/Loss
│   ├── UsefulLifePolicy.kt
│   ├── NumberRange.kt                # Asset numbering series
│   ├── RevaluationPolicy.kt
│   └── ImpairmentPolicy.kt
```

---

### Depreciation Book (`model/depreciation/`)

```
├── depreciation/
│   ├── DepreciationBook.kt           # Aggregate Root
│   ├── DepreciationBookId.kt
│   ├── DepreciationArea.kt           # Book, Tax, Management
│   ├── DepreciationPeriod.kt
│   ├── DepreciationRun.kt            # Entity
│   ├── DepreciationRunId.kt
│   ├── DepreciationLine.kt           # Entity
│   ├── DepreciationStatus.kt         # Draft, Posted, Reversed
│   └── ProrationRule.kt
```

---

### Asset Transfer (`model/transfer/`)

```
├── transfer/
│   ├── AssetTransfer.kt              # Aggregate Root
│   ├── AssetTransferId.kt
│   ├── TransferLine.kt               # Entity
│   ├── TransferReason.kt             # Location, CostCenter, Custodian, Intercompany
│   ├── TransferStatus.kt             # Draft, Approved, Posted
│   └── EffectiveDate.kt
```

---

### Physical Inventory (`model/inventory/`)

```
├── inventory/
│   ├── PhysicalInventory.kt          # Aggregate Root
│   ├── PhysicalInventoryId.kt
│   ├── InventoryLine.kt              # Entity
│   ├── InventoryStatus.kt            # Planned, InProgress, Completed, Reconciled
│   ├── AssetCondition.kt             # Good, Damaged, Missing
│   └── InventoryDiscrepancy.kt
```

---

### Construction-in-Progress (CIP) (`model/cip/`)

```
├── cip/
│   ├── CIPProject.kt                 # Aggregate Root
│   ├── CIPProjectId.kt
│   ├── CIPProjectNumber.kt
│   ├── CIPCost.kt                    # Value Object (accumulated costs)
│   ├── CIPLine.kt                    # Entity (cost line items)
│   ├── CIPStatus.kt                  # Active, OnHold, Capitalized, Cancelled
│   ├── CapitalizationTrigger.kt      # Completion date/milestone
│   └── AssetConversion.kt            # Conversion rules to fixed asset
```

**CIPProject Aggregate**:
```kotlin
class CIPProject private constructor(
    val id: CIPProjectId,
    val projectNumber: String,
    val description: String,
    val classId: AssetClassId,
    var status: CIPStatus,
    val startDate: LocalDate,
    var completionDate: LocalDate?,
    val lines: MutableList<CIPLine>,
    val accumulatedCost: CIPCost
) {
    fun addCost(line: CIPLine): CIPCostAddedEvent
    fun capitalize(conversionDate: LocalDate): CIPCapitalizedEvent
    fun convertToAsset(assetId: AssetId): AssetCreatedFromCIPEvent
}
```

---

## Domain Events

```
events/
├── AssetCreatedEvent.kt
├── AssetCapitalizedEvent.kt
├── AssetPlacedInServiceEvent.kt
├── DepreciationPostedEvent.kt
├── DepreciationReversedEvent.kt
├── AssetTransferredEvent.kt
├── AssetDisposedEvent.kt
├── AssetRevaluedEvent.kt
├── AssetImpairedEvent.kt
├── AssetComponentAddedEvent.kt
├── AssetComponentRemovedEvent.kt
├── PhysicalInventoryCompletedEvent.kt
└── AssetMissingDetectedEvent.kt
```

---

## Domain Exceptions

```
exceptions/
├── AssetNotFoundException.kt
├── AssetAlreadyCapitalizedException.kt
├── AssetNotInServiceException.kt
├── DepreciationNotAllowedException.kt
├── PeriodClosedException.kt
├── AssetDisposedException.kt
├── CapitalizationThresholdNotMetException.kt
├── TransferNotApprovedException.kt
└── InvalidRevaluationException.kt
```

---

## Domain Services

```
services/
├── DepreciationCalculator.kt          # Method-specific depreciation
├── AssetValuationService.kt           # NBV, gain/loss, book values
├── RevaluationService.kt              # Revalue logic and postings
├── ImpairmentService.kt               # Impairment calculation
├── AssetNumberingService.kt           # Class-based numbering
├── TransferValidationService.kt       # Validation of transfers
├── ComponentizationService.kt         # Split/merge components
└── PhysicalInventoryReconcileService.kt
```

---

## Key Invariants

1. **Capitalization Threshold**: Cost must meet class threshold to capitalize.
2. **In-Service Gate**: Depreciation cannot be posted before in-service date.
3. **Book Consistency**: Accumulated depreciation <= cost minus residual value.
4. **Period Control**: No postings to closed asset periods.
5. **Transfer Approval**: Transfers require approval before posting.
6. **Disposal Integrity**: Disposed assets cannot be revalued or depreciated.
7. **Component Integrity**: Component cost sum must equal parent cost when required.
8. **Audit Trail**: Every asset transaction must produce an immutable event.
