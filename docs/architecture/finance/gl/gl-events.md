# GL Events & Integration

> Part of [Finance - General Ledger](../finance-gl.md)

---

## Domain Events Published

### JournalEntryPostedEvent

**Trigger**: Journal entry posted to ledger
**Consumers**: Reporting, BI, Audit, Downstream Compliance

```json
{
  "eventType": "JournalEntryPostedEvent",
  "eventId": "evt-gl-001",
  "timestamp": "2026-02-01T10:30:00Z",
  "aggregateId": "je-001",
  "tenantId": "tenant-001",
  "payload": {
    "journalEntryId": "je-001",
    "ledgerId": "ledger-001",
    "postingDate": "2026-02-01",
    "description": "AR Invoice INV-2026-001",
    "totalDebits": 1000.00,
    "totalCredits": 1000.00,
    "lines": [
      { "accountId": "acct-1200", "debit": 1000.00, "credit": 0.00 },
      { "accountId": "acct-4000", "debit": 0.00, "credit": 900.00 },
      { "accountId": "acct-2100", "debit": 0.00, "credit": 100.00 }
    ]
  }
}
```

---

### JournalEntryReversedEvent

**Trigger**: Journal entry reversal posted
**Consumers**: Reporting, Audit

```json
{
  "eventType": "JournalEntryReversedEvent",
  "payload": {
    "journalEntryId": "je-001",
    "reversalEntryId": "je-002",
    "reason": "Correction",
    "reversalDate": "2026-02-02"
  }
}
```

---

### FinancialPeriodClosedEvent

**Trigger**: Accounting period hard-closed
**Consumers**: AP, AR, Assets, Tax, Treasury

```json
{
  "eventType": "FinancialPeriodClosedEvent",
  "payload": {
    "periodId": "per-2026-02",
    "ledgerId": "ledger-001",
    "closeType": "HARD",
    "closedAt": "2026-03-02T02:00:00Z"
  }
}
```

---

### TrialBalanceGeneratedEvent

**Trigger**: Trial balance snapshot created
**Consumers**: Reporting, FP&A

```json
{
  "eventType": "TrialBalanceGeneratedEvent",
  "payload": {
    "ledgerId": "ledger-001",
    "asOfDate": "2026-02-28",
    "totalDebits": 500000.00,
    "totalCredits": 500000.00
  }
}
```

---

## Domain Events Consumed

```
- VendorInvoicePostedEvent     (from AP)        -> Create AP journal entry
- PaymentSentEvent             (from AP)        -> Post cash disbursement
- InvoicePostedEvent           (from AR)        -> Create AR journal entry
- PaymentReceivedEvent         (from AR)        -> Post cash receipt
- AssetCapitalizedEvent        (from Assets)    -> Capitalize asset
- DepreciationPostedEvent      (from Assets)    -> Post depreciation
- TaxCalculatedEvent           (from Tax)       -> Post tax liabilities
- BankStatementImportedEvent   (from Treasury)  -> Reconcile cash accounts
```

---

## GL Posting Rules (Examples)

### AR Invoice Posted
```
Debit:  Accounts Receivable (1200)   $1,000
Credit: Revenue (4000)                 $900
Credit: Sales Tax Payable (2100)       $100
```

### AP Invoice Posted
```
Debit:  Expense/Inventory (6100)     $1,000
Credit: Accounts Payable (2100)       $1,000
```

### Asset Capitalization
```
Debit:  Fixed Assets (1500)          $25,500
Credit: Accounts Payable (2100)       $25,500
```

---

## Avro Schemas

### JournalEntryPostedSchema.avro

```json
{
  "type": "record",
  "name": "JournalEntryPostedEvent",
  "namespace": "com.erp.finance.gl.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "journalEntryId", "type": "string" },
    { "name": "ledgerId", "type": "string" },
    { "name": "postingDate", "type": "int", "logicalType": "date" },
    { "name": "totalDebits", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "totalCredits", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "lines", "type": { "type": "array", "items": "JournalLineRecord" } }
  ]
}
```

### FinancialPeriodClosedSchema.avro

```json
{
  "type": "record",
  "name": "FinancialPeriodClosedEvent",
  "namespace": "com.erp.finance.gl.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "periodId", "type": "string" },
    { "name": "ledgerId", "type": "string" },
    { "name": "closeType", "type": "string" },
    { "name": "closedAt", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.gl.journal.posted` | GL | Reporting, Audit | 12 |
| `finance.gl.journal.reversed` | GL | Reporting, Audit | 6 |
| `finance.gl.period.closed` | GL | AP, AR, Assets, Tax | 6 |
| `finance.gl.trial-balance.generated` | GL | Reporting, FP&A | 3 |
