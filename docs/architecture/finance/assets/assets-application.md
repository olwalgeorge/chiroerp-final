# Assets Application Layer

> Part of [Finance - Fixed Assets](../finance-assets.md)

## Directory Structure

```
assets-application/
└── src/main/kotlin/com.erp.finance.assets.application/
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

### Asset Commands

```
port/input/command/
├── CreateAssetCommand.kt
│   └── classId, description, acquisitionDate, cost, currency, vendorRef?
│
├── CapitalizeAssetCommand.kt
│   └── assetId, capitalizationDate, costAdjustment?, apInvoiceId?
│
├── PlaceInServiceCommand.kt
│   └── assetId, inServiceDate
│
├── AddAssetComponentCommand.kt
│   └── assetId, componentDescription, cost, usefulLife
│
├── RemoveAssetComponentCommand.kt
│   └── assetId, componentId, reason
│
└── AdjustUsefulLifeCommand.kt
    └── assetId, newUsefulLife, effectiveDate, reason
```

### Transfer Commands

```
├── CreateAssetTransferCommand.kt
│   └── assetId, fromCostCenter, toCostCenter, fromLocation, toLocation, effectiveDate
│
├── ApproveAssetTransferCommand.kt
│   └── transferId, approvedBy
│
└── PostAssetTransferCommand.kt
    └── transferId, postingDate
```

### Depreciation Commands

```
├── RunDepreciationCommand.kt
│   └── bookId, period, asOfDate
│
├── PostDepreciationCommand.kt
│   └── runId, postingDate
│
└── ReverseDepreciationCommand.kt
    └── runId, reversalDate, reason
```

### Disposal & Valuation Commands

```
├── DisposeAssetCommand.kt
│   └── assetId, disposalDate, disposalType, proceeds, reason
│
├── RevalueAssetCommand.kt
│   └── assetId, valuationDate, newFairValue, reason
│
└── ImpairAssetCommand.kt
    └── assetId, impairmentDate, impairmentAmount, reason
```

### Physical Inventory Commands

```
├── CreatePhysicalInventoryCommand.kt
│   └── locationId, plannedDate, assetIds[]
│
├── RecordInventoryCountCommand.kt
│   └── inventoryId, assetId, condition, scannedTag?
│
└── ReconcileInventoryCommand.kt
    └── inventoryId, reconciledBy, reconciliationDate
```

---

## Queries (Read Operations)

### Asset Queries

```
port/input/query/
├── GetAssetByIdQuery.kt
│   └── assetId -> AssetDto
│
├── GetAssetsQuery.kt
│   └── classId?, status?, locationId?, inServiceFrom?, inServiceTo? -> List<AssetSummaryDto>
│
├── GetAssetRegisterQuery.kt
│   └── asOfDate, classId?, locationId? -> AssetRegisterDto
│
└── GetAssetTransactionsQuery.kt
    └── assetId, dateRange? -> List<AssetTransactionDto>
```

### Depreciation Queries

```
├── GetDepreciationScheduleQuery.kt
│   └── assetId, bookId -> DepreciationScheduleDto
│
├── GetDepreciationRunByIdQuery.kt
│   └── runId -> DepreciationRunDto
│
└── GetDepreciationRunsQuery.kt
    └── bookId, period?, status? -> List<DepreciationRunSummaryDto>
```

### Transfer & Inventory Queries

```
├── GetAssetTransferByIdQuery.kt
│   └── transferId -> AssetTransferDto
│
├── GetAssetTransfersQuery.kt
│   └── status?, dateRange? -> List<AssetTransferSummaryDto>
│
├── GetPhysicalInventoryByIdQuery.kt
│   └── inventoryId -> PhysicalInventoryDto
│
└── GetPhysicalInventoriesQuery.kt
    └── status?, locationId? -> List<PhysicalInventorySummaryDto>
```

---

## Output Ports (Driven Ports)

### Repository Ports

```
port/output/
├── AssetRepository.kt
│   ├── save(asset: Asset)
│   ├── findById(id: AssetId): Asset?
│   └── findByNumber(assetNumber: String): Asset?
│
├── AssetClassRepository.kt
│   ├── findById(id: AssetClassId): AssetClass?
│   └── findByCode(code: String): AssetClass?
│
├── DepreciationBookRepository.kt
│   ├── findById(id: DepreciationBookId): DepreciationBook?
│   └── findByArea(area: DepreciationArea): DepreciationBook?
│
├── DepreciationRunRepository.kt
│   ├── save(run: DepreciationRun)
│   └── findByPeriod(bookId: DepreciationBookId, period: DepreciationPeriod): DepreciationRun?
│
├── AssetTransferRepository.kt
│   └── save(transfer: AssetTransfer)
│
└── PhysicalInventoryRepository.kt
    └── save(inventory: PhysicalInventory)
```

### Read Model Ports

```
├── AssetReadRepository.kt              # Denormalized asset summary
├── AssetRegisterReadRepository.kt      # Asset register projection
├── DepreciationReadRepository.kt       # Depreciation history
└── AssetTransactionReadRepository.kt   # Asset movement history
```

### Integration Ports

```
├── GeneralLedgerPort.kt                # GL journal posting
│   └── postJournal(entry: JournalEntry): JournalEntryId
│
├── ProcurementPort.kt                  # Capitalization from AP/PO
│   └── getVendorInvoice(invoiceId: VendorInvoiceId): VendorInvoice
│
├── ProjectAccountingPort.kt            # CIP integration
│   └── getProjectCosts(projectId: ProjectId): Money
│
├── TaxDepreciationPort.kt              # Tax depreciation rules
│   └── getTaxRules(assetClassId: AssetClassId): TaxRuleSet
│
├── NotificationPort.kt                 # Inventory and approval notifications
│   └── notify(event: NotificationEvent)
│
├── AuditTrailPort.kt
│   └── log(event: AuditEvent)
│
└── EventPublisherPort.kt
    └── publish(event: DomainEvent)
```

---

## Command Handlers

```
service/command/
├── AssetCommandHandler.kt
│   ├── handle(CreateAssetCommand): AssetId
│   ├── handle(CapitalizeAssetCommand): void
│   ├── handle(PlaceInServiceCommand): void
│   └── handle(AdjustUsefulLifeCommand): void
│
├── ComponentCommandHandler.kt
│   ├── handle(AddAssetComponentCommand): void
│   └── handle(RemoveAssetComponentCommand): void
│
├── TransferCommandHandler.kt
│   ├── handle(CreateAssetTransferCommand): AssetTransferId
│   ├── handle(ApproveAssetTransferCommand): void
│   └── handle(PostAssetTransferCommand): void
│
├── DepreciationCommandHandler.kt
│   ├── handle(RunDepreciationCommand): DepreciationRunId
│   ├── handle(PostDepreciationCommand): void
│   └── handle(ReverseDepreciationCommand): void
│
├── ValuationCommandHandler.kt
│   ├── handle(RevalueAssetCommand): void
│   └── handle(ImpairAssetCommand): void
│
└── DisposalCommandHandler.kt
    └── handle(DisposeAssetCommand): void
```

---

## Query Handlers

```
service/query/
├── AssetQueryHandler.kt
│   ├── handle(GetAssetByIdQuery): AssetDto
│   ├── handle(GetAssetsQuery): List<AssetSummaryDto>
│   └── handle(GetAssetRegisterQuery): AssetRegisterDto
│
├── DepreciationQueryHandler.kt
│   ├── handle(GetDepreciationScheduleQuery): DepreciationScheduleDto
│   └── handle(GetDepreciationRunsQuery): List<DepreciationRunSummaryDto>
│
├── TransferQueryHandler.kt
│   └── handle(GetAssetTransfersQuery): List<AssetTransferSummaryDto>
│
└── InventoryQueryHandler.kt
    └── handle(GetPhysicalInventoriesQuery): List<PhysicalInventorySummaryDto>
```
