# Assets Events & Integration

> Part of [Finance - Fixed Assets](../finance-assets.md)

---

## Domain Events Published

### AssetCapitalizedEvent

**Trigger**: Asset capitalization posted to GL
**Consumers**: General Ledger, Controlling, Treasury

```json
{
  "eventType": "AssetCapitalizedEvent",
  "eventId": "evt-asset-001",
  "timestamp": "2026-02-02T10:30:00Z",
  "aggregateId": "asset-001",
  "tenantId": "tenant-001",
  "payload": {
    "assetId": "asset-001",
    "assetNumber": "FA-2026-000123",
    "classId": "class-med-equip",
    "capitalizationDate": "2026-02-02",
    "capitalizedCost": 25500.00,
    "currency": "USD",
    "sourceDocument": "INV-2026-001234",
    "glEntries": [
      { "accountId": "1500-00", "debit": 25500.00, "credit": 0.00 },
      { "accountId": "2100-00", "debit": 0.00, "credit": 25500.00 }
    ]
  }
}
```

---

### AssetPlacedInServiceEvent

**Trigger**: Asset becomes depreciable
**Consumers**: Depreciation Scheduler, Reporting

```json
{
  "eventType": "AssetPlacedInServiceEvent",
  "payload": {
    "assetId": "asset-001",
    "inServiceDate": "2026-02-10",
    "usefulLifeMonths": 60,
    "depreciationMethod": "STRAIGHT_LINE"
  }
}
```

---

### DepreciationPostedEvent

**Trigger**: Depreciation run posted
**Consumers**: General Ledger, Controlling

```json
{
  "eventType": "DepreciationPostedEvent",
  "payload": {
    "runId": "dep-001",
    "bookId": "BOOK-IFRS",
    "period": "2026-02",
    "postingDate": "2026-02-29",
    "totalDepreciation": 84500.00,
    "glEntries": [
      { "accountId": "6100-00", "debit": 84500.00, "credit": 0.00 },
      { "accountId": "1590-00", "debit": 0.00, "credit": 84500.00 }
    ]
  }
}
```

---

### AssetTransferredEvent

**Trigger**: Asset transfer posted
**Consumers**: Controlling, HR/Facilities

```json
{
  "eventType": "AssetTransferredEvent",
  "payload": {
    "transferId": "trf-001",
    "assetId": "asset-001",
    "fromCostCenter": "CC-100",
    "toCostCenter": "CC-200",
    "fromLocation": "LOC-100",
    "toLocation": "LOC-200",
    "effectiveDate": "2026-02-15"
  }
}
```

---

### AssetDisposedEvent

**Trigger**: Asset disposal posted
**Consumers**: General Ledger, Tax, Reporting

```json
{
  "eventType": "AssetDisposedEvent",
  "payload": {
    "assetId": "asset-001",
    "disposalDate": "2026-03-01",
    "disposalType": "SALE",
    "proceeds": 12000.00,
    "netBookValue": 11750.00,
    "gainLoss": 250.00,
    "currency": "USD",
    "glEntries": [
      { "accountId": "1000-00", "debit": 12000.00, "credit": 0.00 },
      { "accountId": "1590-00", "debit": 8450.00, "credit": 0.00 },
      { "accountId": "1500-00", "debit": 0.00, "credit": 25500.00 },
      { "accountId": "7190-00", "debit": 0.00, "credit": 250.00 }
    ]
  }
}
```

---

### AssetRevaluedEvent

**Trigger**: Asset revaluation posted
**Consumers**: General Ledger, Reporting

```json
{
  "eventType": "AssetRevaluedEvent",
  "payload": {
    "assetId": "asset-001",
    "valuationDate": "2026-03-15",
    "oldFairValue": 23000.00,
    "newFairValue": 26000.00,
    "revaluationAmount": 3000.00,
    "glEntries": [
      { "accountId": "1500-00", "debit": 3000.00, "credit": 0.00 },
      { "accountId": "3400-00", "debit": 0.00, "credit": 3000.00 }
    ]
  }
}
```

---

### AssetImpairedEvent

**Trigger**: Asset impairment posted
**Consumers**: General Ledger, Reporting

```json
{
  "eventType": "AssetImpairedEvent",
  "payload": {
    "assetId": "asset-001",
    "impairmentDate": "2026-03-20",
    "impairmentAmount": 1500.00,
    "glEntries": [
      { "accountId": "7200-00", "debit": 1500.00, "credit": 0.00 },
      { "accountId": "1500-00", "debit": 0.00, "credit": 1500.00 }
    ]
  }
}
```

---

### PhysicalInventoryCompletedEvent

**Trigger**: Physical inventory reconciliation completed
**Consumers**: Audit, Facilities

```json
{
  "eventType": "PhysicalInventoryCompletedEvent",
  "payload": {
    "inventoryId": "inv-001",
    "locationId": "LOC-100",
    "completedDate": "2026-02-28",
    "missingAssetIds": ["asset-009", "asset-014"],
    "damagedAssetIds": ["asset-021"]
  }
}
```

---

## Domain Events Consumed

```
- VendorInvoicePostedEvent     (from AP)        -> Capitalize asset from vendor invoice
- PurchaseOrderApprovedEvent   (from Procurement) -> Create CIP or asset draft
- ProjectCapitalizedEvent      (from Projects)  -> CIP to asset conversion
- FinancialPeriodClosedEvent   (from Close)     -> Lock asset postings for period
- AssetTagScannedEvent         (from Inventory) -> Physical inventory updates
```

---

## GL Posting Rules (Examples)

### Capitalization
```
Debit:  Fixed Assets (1500)            $25,500
Credit: Accounts Payable (2100)        $25,500
```

### Depreciation
```
Debit:  Depreciation Expense (6100)    $84,500
Credit: Accumulated Depreciation (1590) $84,500
```

### Disposal (Sale)
```
Debit:  Cash/Bank (1000)               $12,000
Debit:  Accumulated Depreciation (1590) $8,450
Credit: Fixed Assets (1500)            $25,500
Credit: Gain on Disposal (7190)          $250
```

---

## Avro Schemas

### AssetCapitalizedSchema.avro

```json
{
  "type": "record",
  "name": "AssetCapitalizedEvent",
  "namespace": "com.erp.finance.assets.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "assetId", "type": "string" },
    { "name": "assetNumber", "type": "string" },
    { "name": "classId", "type": "string" },
    { "name": "capitalizationDate", "type": "int", "logicalType": "date" },
    { "name": "capitalizedCost", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" },
    { "name": "glEntries", "type": { "type": "array", "items": "GLEntryRecord" } }
  ]
}
```

### DepreciationPostedSchema.avro

```json
{
  "type": "record",
  "name": "DepreciationPostedEvent",
  "namespace": "com.erp.finance.assets.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "runId", "type": "string" },
    { "name": "bookId", "type": "string" },
    { "name": "period", "type": "string" },
    { "name": "postingDate", "type": "int", "logicalType": "date" },
    { "name": "totalDepreciation", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "glEntries", "type": { "type": "array", "items": "GLEntryRecord" } }
  ]
}
```

### AssetDisposedSchema.avro

```json
{
  "type": "record",
  "name": "AssetDisposedEvent",
  "namespace": "com.erp.finance.assets.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "assetId", "type": "string" },
    { "name": "disposalDate", "type": "int", "logicalType": "date" },
    { "name": "disposalType", "type": "string" },
    { "name": "proceeds", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "netBookValue", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "gainLoss", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" },
    { "name": "glEntries", "type": { "type": "array", "items": "GLEntryRecord" } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.assets.capitalized` | Assets | GL, Controlling | 12 |
| `finance.assets.in_service` | Assets | Reporting, Scheduling | 6 |
| `finance.assets.depreciation.posted` | Assets | GL, Controlling | 12 |
| `finance.assets.transferred` | Assets | Controlling, Facilities | 6 |
| `finance.assets.disposed` | Assets | GL, Tax, Reporting | 6 |
| `finance.assets.revalued` | Assets | GL, Reporting | 6 |
| `finance.assets.impaired` | Assets | GL, Reporting | 6 |
| `finance.assets.inventory.completed` | Assets | Audit, Facilities | 3 |
