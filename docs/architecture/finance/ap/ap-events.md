# AP Events & Integration

> Part of [Finance - Accounts Payable](../finance-ap.md)

---

## Domain Events Published

### Invoice Events

#### VendorInvoicePostedEvent

**Trigger**: Invoice posted to GL  
**Consumers**: General Ledger, Controlling, Treasury

```json
{
  "eventType": "VendorInvoicePostedEvent",
  "eventId": "evt-001",
  "timestamp": "2026-02-01T10:30:00Z",
  "aggregateId": "invoice-001",
  "tenantId": "tenant-001",
  "payload": {
    "invoiceId": "invoice-001",
    "vendorId": "vendor-001",
    "invoiceNumber": "INV-2026-001234",
    "totalAmount": 1000.00,
    "taxAmount": 70.00,
    "withholdingAmount": 0.00,
    "currency": "USD",
    "postingDate": "2026-02-01",
    "dueDate": "2026-03-03",
    "glEntries": [
      { "accountId": "6100-00", "debit": 930.00, "credit": 0.00 },
      { "accountId": "1500-00", "debit": 70.00, "credit": 0.00 },
      { "accountId": "2100-00", "debit": 0.00, "credit": 1000.00 }
    ]
  }
}
```

#### VendorInvoiceCancelledEvent

**Trigger**: Invoice cancelled  
**Consumers**: General Ledger (reversal), Procurement

```json
{
  "eventType": "VendorInvoiceCancelledEvent",
  "payload": {
    "invoiceId": "invoice-001",
    "vendorId": "vendor-001",
    "reason": "Duplicate invoice",
    "cancelledBy": "user-001",
    "cancelledAt": "2026-02-02T09:00:00Z"
  }
}
```

#### VendorInvoiceMatchedEvent

**Trigger**: Invoice matched with PO/GRN  
**Consumers**: Procurement, Audit

```json
{
  "eventType": "VendorInvoiceMatchedEvent",
  "payload": {
    "invoiceId": "invoice-001",
    "poId": "PO-2026-000123",
    "grnId": "GRN-2026-000456",
    "matchStatus": "FULL_MATCH",
    "priceVariance": 0.00,
    "quantityVariance": 0
  }
}
```

---

### Payment Events

#### PaymentSentEvent

**Trigger**: Payment executed  
**Consumers**: General Ledger, Treasury, Vendor Portal

```json
{
  "eventType": "PaymentSentEvent",
  "payload": {
    "paymentId": "payment-001",
    "paymentNumber": "PMT-2026-000567",
    "vendorId": "vendor-001",
    "amount": 1000.00,
    "currency": "USD",
    "paymentMethod": "ACH",
    "paymentDate": "2026-02-15",
    "bankAccount": "ACCT-001",
    "bankReference": "ACH-2026-001234",
    "allocations": [
      { "invoiceId": "invoice-001", "amount": 500.00 },
      { "invoiceId": "invoice-002", "amount": 500.00 }
    ],
    "glEntries": [
      { "accountId": "2100-00", "debit": 1000.00, "credit": 0.00 },
      { "accountId": "1000-00", "debit": 0.00, "credit": 1000.00 }
    ]
  }
}
```

#### PaymentClearedEvent

**Trigger**: Payment cleared by bank  
**Consumers**: Treasury, Bank Reconciliation

```json
{
  "eventType": "PaymentClearedEvent",
  "payload": {
    "paymentId": "payment-001",
    "clearedDate": "2026-02-17",
    "bankStatementRef": "STMT-2026-001234"
  }
}
```

#### PaymentReturnedEvent

**Trigger**: Payment returned/bounced  
**Consumers**: General Ledger (reversal), Treasury, Collections

```json
{
  "eventType": "PaymentReturnedEvent",
  "payload": {
    "paymentId": "payment-001",
    "returnReason": "INSUFFICIENT_FUNDS",
    "returnDate": "2026-02-18",
    "bankReference": "RTN-2026-001"
  }
}
```

---

### Payment Run Events

#### PaymentRunApprovedEvent

**Trigger**: Payment run approved  
**Consumers**: Treasury, Notifications

```json
{
  "eventType": "PaymentRunApprovedEvent",
  "payload": {
    "paymentRunId": "run-001",
    "approvedBy": "user-001",
    "approvedAt": "2026-02-14T10:30:00Z",
    "totalAmount": 50000.00,
    "vendorCount": 15,
    "invoiceCount": 45
  }
}
```

#### PaymentRunExecutedEvent

**Trigger**: Payment run execution completed  
**Consumers**: Treasury, Bank Integration, Audit

```json
{
  "eventType": "PaymentRunExecutedEvent",
  "payload": {
    "paymentRunId": "run-001",
    "executedAt": "2026-02-15T06:00:00Z",
    "batches": [
      { "batchId": "batch-001", "paymentMethod": "ACH", "amount": 45000.00, "count": 40 },
      { "batchId": "batch-002", "paymentMethod": "WIRE", "amount": 5000.00, "count": 5 }
    ],
    "bankFiles": [
      { "fileType": "NACHA", "fileName": "ACH-20260215.txt" },
      { "fileType": "SWIFT", "fileName": "WIRE-20260215.xml" }
    ]
  }
}
```

---

### Matching Events

#### MatchExceptionCreatedEvent

**Trigger**: Variance exceeds tolerance during matching  
**Consumers**: Procurement, Workflow, Notifications

```json
{
  "eventType": "MatchExceptionCreatedEvent",
  "payload": {
    "exceptionId": "exc-001",
    "invoiceId": "invoice-001",
    "poId": "PO-2026-000123",
    "grnId": "GRN-2026-000456",
    "exceptionType": "PRICE_VARIANCE",
    "invoiceAmount": 1050.00,
    "poAmount": 1000.00,
    "varianceAmount": 50.00,
    "variancePercent": 5.0
  }
}
```

---

### Vendor Events

#### VendorBlockedEvent

**Trigger**: Vendor blocked for payments  
**Consumers**: Procurement, Sales, CRM

```json
{
  "eventType": "VendorBlockedEvent",
  "payload": {
    "vendorId": "vendor-001",
    "reason": "Quality issues",
    "blockedBy": "user-001",
    "blockedAt": "2026-02-01T14:00:00Z"
  }
}
```

---

## Domain Events Consumed

### From Procurement

#### PurchaseOrderApprovedEvent

**Source**: Procurement  
**Action**: Enable invoice matching against PO

```json
{
  "eventType": "PurchaseOrderApprovedEvent",
  "payload": {
    "poId": "PO-2026-000123",
    "vendorId": "vendor-001",
    "totalAmount": 1000.00,
    "lines": [
      { "lineId": "line-001", "itemId": "ITEM-001", "quantity": 10, "unitPrice": 100.00 }
    ]
  }
}
```

#### GoodsReceivedEvent

**Source**: Warehouse/Inventory  
**Action**: Enable 3-way matching

```json
{
  "eventType": "GoodsReceivedEvent",
  "payload": {
    "grnId": "GRN-2026-000456",
    "poId": "PO-2026-000123",
    "receivedDate": "2026-01-30",
    "lines": [
      { "lineId": "line-001", "itemId": "ITEM-001", "quantityReceived": 10 }
    ]
  }
}
```

---

### From Sales (Commissions)

#### CommissionPayableCreatedEvent

**Source**: Sales Commissions  
**Action**: Create AP vendor invoice for commission payout

```json
{
  "eventType": "CommissionPayableCreatedEvent",
  "payload": {
    "statementId": "COM-2026-02",
    "repId": "REP-88",
    "totalAmount": 1850.00,
    "vendorId": "VEND-REP-88",
    "paymentTerms": "NET15",
    "currency": "USD",
    "dueDate": "2026-03-15"
  }
}
```

---

### From Treasury

#### BankStatementImportedEvent

**Source**: Treasury  
**Action**: Auto-clear payments

```json
{
  "eventType": "BankStatementImportedEvent",
  "payload": {
    "statementId": "STMT-2026-001",
    "bankAccount": "ACCT-001",
    "statementDate": "2026-02-17",
    "transactions": [
      { "reference": "ACH-2026-001234", "amount": -1000.00, "date": "2026-02-17" }
    ]
  }
}
```

---

### From Master Data

#### VendorCreatedEvent / VendorUpdatedEvent

**Source**: Master Data Governance (MDG)  
**Action**: Sync vendor master data

```json
{
  "eventType": "VendorCreatedEvent",
  "payload": {
    "vendorId": "vendor-001",
    "vendorCode": "V-001234",
    "name": "Acme Supplies Inc.",
    "paymentTerms": "NET30",
    "defaultPaymentMethod": "ACH",
    "currency": "USD"
  }
}
```

---

## GL Integration (Journal Entries)

### Invoice Posted → GL

**Scenario**: Standard expense invoice posted

```
Journal Entry: JE-2026-000789
Date: 2026-02-01
Description: Vendor Invoice INV-2026-001234 - Acme Supplies

  Account                          Debit       Credit
  ─────────────────────────────────────────────────────
  6100-00 Office Supplies Exp     $930.00
  1500-00 Input Tax (VAT)          $70.00
  2100-00 Accounts Payable                    $1,000.00
  ─────────────────────────────────────────────────────
  TOTAL                         $1,000.00    $1,000.00
```

### Invoice with Withholding Tax

**Scenario**: Service invoice with 10% withholding

```
Journal Entry: JE-2026-000790
Date: 2026-02-01
Description: Vendor Invoice INV-2026-001235 - Consulting Services

  Account                          Debit       Credit
  ─────────────────────────────────────────────────────
  6200-00 Professional Services $1,000.00
  2100-00 Accounts Payable                      $900.00
  2150-00 Withholding Tax Payable               $100.00
  ─────────────────────────────────────────────────────
  TOTAL                         $1,000.00    $1,000.00
```

### Payment Sent → GL

**Scenario**: ACH payment to vendor

```
Journal Entry: JE-2026-000795
Date: 2026-02-15
Description: Payment PMT-2026-000567 - Acme Supplies

  Account                          Debit       Credit
  ─────────────────────────────────────────────────────
  2100-00 Accounts Payable      $1,000.00
  1000-00 Cash - Operating Bank               $1,000.00
  ─────────────────────────────────────────────────────
  TOTAL                         $1,000.00    $1,000.00
```

### Payment with Early Payment Discount

**Scenario**: 2/10 Net 30 discount taken

```
Journal Entry: JE-2026-000796
Date: 2026-02-10
Description: Payment with 2% discount - Acme Supplies

  Account                          Debit       Credit
  ─────────────────────────────────────────────────────
  2100-00 Accounts Payable      $1,000.00
  1000-00 Cash - Operating Bank                 $980.00
  7100-00 Purchase Discounts                     $20.00
  ─────────────────────────────────────────────────────
  TOTAL                         $1,000.00    $1,000.00
```

### Inventory Invoice → GL

**Scenario**: Inventory purchase (perpetual inventory)

```
Journal Entry: JE-2026-000800
Date: 2026-02-01
Description: Inventory Purchase - PO-2026-000123

  Account                          Debit       Credit
  ─────────────────────────────────────────────────────
  1400-00 Inventory - Raw Matl  $5,000.00
  1500-00 Input Tax (VAT)         $350.00
  2100-00 Accounts Payable                    $5,350.00
  ─────────────────────────────────────────────────────
  TOTAL                         $5,350.00    $5,350.00
```

---

## Avro Schemas

### VendorInvoicePostedSchema.avro

```json
{
  "type": "record",
  "name": "VendorInvoicePostedEvent",
  "namespace": "com.erp.finance.ap.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "invoiceId", "type": "string" },
    { "name": "vendorId", "type": "string" },
    { "name": "invoiceNumber", "type": "string" },
    { "name": "totalAmount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" },
    { "name": "postingDate", "type": "int", "logicalType": "date" },
    { "name": "dueDate", "type": "int", "logicalType": "date" },
    { "name": "glEntries", "type": { "type": "array", "items": "GLEntryRecord" } }
  ]
}
```

### PaymentSentSchema.avro

```json
{
  "type": "record",
  "name": "PaymentSentEvent",
  "namespace": "com.erp.finance.ap.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "paymentId", "type": "string" },
    { "name": "vendorId", "type": "string" },
    { "name": "amount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" },
    { "name": "paymentMethod", "type": { "type": "enum", "name": "PaymentMethod", "symbols": ["CHECK", "ACH", "WIRE", "CARD"] } },
    { "name": "paymentDate", "type": "int", "logicalType": "date" },
    { "name": "bankReference", "type": ["null", "string"], "default": null },
    { "name": "allocations", "type": { "type": "array", "items": "PaymentAllocationRecord" } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.ap.invoice.posted` | AP | GL, Controlling | 12 |
| `finance.ap.invoice.cancelled` | AP | GL, Procurement | 6 |
| `finance.ap.invoice.matched` | AP | Procurement, Audit | 6 |
| `finance.ap.payment.sent` | AP | GL, Treasury | 12 |
| `finance.ap.payment.cleared` | AP | Treasury | 6 |
| `finance.ap.payment.returned` | AP | GL, Treasury | 6 |
| `finance.ap.match.exception` | AP | Workflow, Notifications | 6 |
| `finance.ap.vendor.blocked` | AP | Procurement, Sales | 6 |
