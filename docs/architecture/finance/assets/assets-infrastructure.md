# Assets Infrastructure Layer

> Part of [Finance - Fixed Assets](../finance-assets.md)

## Directory Structure

```
assets-infrastructure/
└── src/main/kotlin/com.erp.finance.assets.infrastructure/
    ├── adapter/
    │   ├── input/
    │   │   ├── rest/
    │   │   └── event/
    │   └── output/
    │       ├── persistence/
    │       ├── integration/
    │       ├── messaging/
    │       └── reporting/
    ├── configuration/
    └── resources/
```

---

## REST Adapters (Primary/Driving)

### Asset Resource

```
adapter/input/rest/
├── AssetResource.kt
│   ├── POST   /api/v1/assets                     -> createAsset()
│   ├── GET    /api/v1/assets/{id}                -> getAsset()
│   ├── GET    /api/v1/assets                     -> listAssets()
│   ├── POST   /api/v1/assets/{id}/capitalize     -> capitalizeAsset()
│   ├── POST   /api/v1/assets/{id}/in-service     -> placeInService()
│   ├── POST   /api/v1/assets/{id}/dispose        -> disposeAsset()
│   ├── POST   /api/v1/assets/{id}/revalue        -> revalueAsset()
│   └── POST   /api/v1/assets/{id}/impair         -> impairAsset()
```

### Asset Class Resource

```
├── AssetClassResource.kt
│   ├── POST   /api/v1/assets/classes             -> createClass()
│   ├── GET    /api/v1/assets/classes/{id}        -> getClass()
│   └── GET    /api/v1/assets/classes             -> listClasses()
```

### Depreciation Resource

```
├── DepreciationRunResource.kt
│   ├── POST   /api/v1/assets/depreciation-runs   -> runDepreciation()
│   ├── POST   /api/v1/assets/depreciation-runs/{id}/post -> postDepreciation()
│   └── GET    /api/v1/assets/depreciation-runs   -> listRuns()
```

### Transfer Resource

```
├── AssetTransferResource.kt
│   ├── POST   /api/v1/assets/transfers           -> createTransfer()
│   ├── POST   /api/v1/assets/transfers/{id}/approve -> approveTransfer()
│   └── GET    /api/v1/assets/transfers           -> listTransfers()
```

### Inventory Resource

```
├── PhysicalInventoryResource.kt
│   ├── POST   /api/v1/assets/inventories         -> createInventory()
│   ├── POST   /api/v1/assets/inventories/{id}/count -> recordCount()
│   ├── POST   /api/v1/assets/inventories/{id}/reconcile -> reconcile()
│   └── GET    /api/v1/assets/inventories         -> listInventories()
```

---

## Request/Response DTOs

### Request DTOs

```
dto/request/
├── CreateAssetRequest.kt
│   ├── classId: String
│   ├── description: String
│   ├── acquisitionDate: LocalDate
│   ├── cost: BigDecimal
│   ├── currency: String
│   ├── locationId: String?
│   └── custodianId: String?
│
├── CapitalizeAssetRequest.kt
│   ├── capitalizationDate: LocalDate
│   ├── costAdjustment: BigDecimal?
│   └── apInvoiceId: String?
│
├── PlaceInServiceRequest.kt
│   └── inServiceDate: LocalDate
│
├── RunDepreciationRequest.kt
│   ├── bookId: String
│   ├── period: String                # YYYY-MM
│   └── asOfDate: LocalDate
│
├── DisposeAssetRequest.kt
│   ├── disposalDate: LocalDate
│   ├── disposalType: String          # Sale, Scrap, Abandon
│   ├── proceeds: BigDecimal
│   └── reason: String?
│
└── CreateTransferRequest.kt
    ├── assetId: String
    ├── fromCostCenter: String
    ├── toCostCenter: String
    ├── fromLocation: String
    ├── toLocation: String
    └── effectiveDate: LocalDate
```

### Response DTOs

```
dto/response/
├── AssetDto.kt
│   ├── id, assetNumber, classId, description
│   ├── status, acquisitionDate, inServiceDate
│   ├── cost, accumulatedDepreciation, netBookValue
│   ├── currency, location, custodian
│   └── components: List<AssetComponentDto>
│
├── AssetSummaryDto.kt
│   ├── id, assetNumber, description
│   ├── status, classCode, location
│   └── netBookValue, currency
│
├── DepreciationRunDto.kt
│   ├── id, bookId, period, status
│   ├── totalAssets, totalDepreciation
│   └── postedAt
│
├── AssetTransferDto.kt
│   ├── id, assetId, status
│   ├── fromCostCenter, toCostCenter
│   ├── fromLocation, toLocation
│   └── effectiveDate
│
└── PhysicalInventoryDto.kt
    ├── id, locationId, status
    ├── plannedDate, completedDate
    └── discrepancies: List<InventoryDiscrepancyDto>
```

---

## Event Consumers

```
adapter/input/event/
├── VendorInvoiceEventConsumer.kt
│   └── Consumes: VendorInvoicePostedEvent -> Capitalize asset from AP
│
├── ProjectEventConsumer.kt
│   └── Consumes: ProjectCapitalizedEvent -> CIP to asset
│
├── InventoryEventConsumer.kt
│   └── Consumes: AssetTagScannedEvent -> Physical inventory counts
│
└── PeriodCloseEventConsumer.kt
    └── Consumes: FinancialPeriodClosedEvent -> Lock asset postings
```

---

## Persistence Adapters (Secondary/Driven)

```
adapter/output/persistence/
├── jpa/
│   ├── AssetJpaAdapter.kt
│   ├── AssetClassJpaAdapter.kt
│   ├── DepreciationBookJpaAdapter.kt
│   ├── DepreciationRunJpaAdapter.kt
│   ├── AssetTransferJpaAdapter.kt
│   ├── PhysicalInventoryJpaAdapter.kt
│   ├── entity/
│   │   ├── AssetEntity.kt
│   │   ├── AssetComponentEntity.kt
│   │   ├── AssetClassEntity.kt
│   │   ├── DepreciationBookEntity.kt
│   │   ├── DepreciationRunEntity.kt
│   │   ├── DepreciationLineEntity.kt
│   │   ├── AssetTransferEntity.kt
│   │   └── PhysicalInventoryEntity.kt
│   └── repository/
│       ├── AssetJpaRepository.kt
│       ├── AssetClassJpaRepository.kt
│       ├── DepreciationRunJpaRepository.kt
│       ├── AssetTransferJpaRepository.kt
│       └── PhysicalInventoryJpaRepository.kt
```

---

## Integration & Messaging Adapters

```
adapter/output/
├── integration/
│   ├── GLPostingAdapter.kt               # -> GL journal entries
│   ├── ProcurementAdapter.kt             # -> AP invoices
│   ├── ProjectAccountingAdapter.kt       # -> CIP costs
│   └── TaxDepreciationAdapter.kt         # -> Tax rules
│
├── messaging/
│   ├── kafka/
│   │   ├── AssetsEventPublisher.kt
│   │   ├── AssetsEventConsumer.kt
│   │   └── schema/
│   │       ├── AssetCapitalizedSchema.avro
│   │       ├── DepreciationPostedSchema.avro
│   │       └── AssetDisposedSchema.avro
│   └── outbox/
│       └── AssetsOutboxEventPublisher.kt
│
└── reporting/
    ├── AssetRegisterReadAdapter.kt
    ├── DepreciationReadAdapter.kt
    └── AssetTransactionReadAdapter.kt
```

---

## Configuration & Resources

```
configuration/
├── AssetsDependencyInjection.kt
├── PersistenceConfiguration.kt
├── MessagingConfiguration.kt
└── SchedulerConfiguration.kt

resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__create_assets_schema.sql
    ├── V2__create_asset_tables.sql
    ├── V3__create_depreciation_tables.sql
    ├── V4__create_transfer_tables.sql
    └── V5__create_inventory_tables.sql
```
