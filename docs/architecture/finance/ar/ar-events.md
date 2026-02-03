# AR Events & Integration

> Part of [Finance - Accounts Receivable](../finance-ar.md)

---

## Domain Events Published

### Invoice Events

#### InvoicePostedEvent

**Trigger**: Invoice posted to GL  
**Consumers**: General Ledger, Controlling, Revenue

```json
{
  "eventType": "InvoicePostedEvent",
  "eventId": "evt-1001",
  "timestamp": "2026-02-02T10:30:00Z",
  "aggregateId": "inv-001",
  "tenantId": "tenant-001",
  "payload": {
    "invoiceId": "inv-001",
    "customerId": "cst-1001",
    "invoiceNumber": "INV-2026-004210",
    "totalAmount": 1200.00,
    "taxAmount": 0.00,
    "currency": "USD",
    "postingDate": "2026-02-02",
    "dueDate": "2026-03-03",
    "glEntries": [
      { "accountId": "1200-00", "debit": 1200.00, "credit": 0.00 },
      { "accountId": "4000-00", "debit": 0.00, "credit": 1200.00 }
    ]
  }
}
```

#### InvoiceCancelledEvent

**Trigger**: Invoice cancelled  
**Consumers**: General Ledger (reversal), Sales

```json
{
  "eventType": "InvoiceCancelledEvent",
  "payload": {
    "invoiceId": "inv-001",
    "customerId": "cst-1001",
    "reason": "Duplicate invoice",
    "cancelledBy": "user-100",
    "cancelledAt": "2026-02-03T09:12:00Z"
  }
}
```

---

### Payment Events

#### PaymentReceivedEvent

**Trigger**: Payment recorded  
**Consumers**: General Ledger, Treasury

```json
{
  "eventType": "PaymentReceivedEvent",
  "payload": {
    "paymentId": "pay-001",
    "paymentNumber": "PMT-2026-000210",
    "customerId": "cst-1001",
    "amount": 1200.00,
    "currency": "USD",
    "paymentMethod": "ACH",
    "paymentDate": "2026-02-04",
    "bankReference": "ACH-2026-8831",
    "allocations": [
      { "invoiceId": "inv-001", "amount": 1200.00 }
    ],
    "glEntries": [
      { "accountId": "1000-00", "debit": 1200.00, "credit": 0.00 },
      { "accountId": "1200-00", "debit": 0.00, "credit": 1200.00 }
    ]
  }
}
```

#### PaymentReversedEvent

**Trigger**: Payment reversed (chargeback, bounce)  
**Consumers**: General Ledger, Treasury, Collections

```json
{
  "eventType": "PaymentReversedEvent",
  "payload": {
    "paymentId": "pay-001",
    "reason": "Chargeback",
    "reversedAt": "2026-02-05T10:00:00Z"
  }
}
```

---

### Credit & Dunning Events

#### CreditMemoIssuedEvent

**Trigger**: Credit memo issued  
**Consumers**: General Ledger, Sales

```json
{
  "eventType": "CreditMemoIssuedEvent",
  "payload": {
    "creditMemoId": "cm-001",
    "invoiceId": "inv-001",
    "customerId": "cst-1001",
    "amount": 150.00,
    "reason": "Pricing adjustment",
    "issuedAt": "2026-02-06T08:15:00Z"
  }
}
```

#### DunningNoticeGeneratedEvent

**Trigger**: Dunning run completed  
**Consumers**: Notifications, CRM

```json
{
  "eventType": "DunningNoticeGeneratedEvent",
  "payload": {
    "dunningRunId": "dr-001",
    "customerId": "cst-1001",
    "dunningLevel": "REMINDER",
    "asOfDate": "2026-02-28",
    "pastDueAmount": 1200.00
  }
}
```

---

### Credit Control Events

#### CustomerBlockedEvent

**Trigger**: Customer blocked due to credit policy  
**Consumers**: Sales, CRM

```json
{
  "eventType": "CustomerBlockedEvent",
  "payload": {
    "customerId": "cst-1001",
    "reason": "Credit limit exceeded",
    "blockedAt": "2026-02-07T14:00:00Z"
  }
}
```

---

## Domain Events Consumed

```
- SalesOrderFulfilledEvent    (from Sales)     -> Create invoice
- ContractRenewalEvent        (from Contracts) -> Subscription billing
- BankStatementImportedEvent  (from Treasury)  -> Auto-apply payments
- RebateIssuedEvent           (from Sales Rebates) -> Create credit memo
```

---

### From Sales Rebates

#### RebateIssuedEvent

**Source**: Sales Rebates  
**Action**: Create AR credit memo for rebate settlement

```json
{
  "eventType": "RebateIssuedEvent",
  "payload": {
    "claimId": "REB-2026-02",
    "customerId": "cst-205",
    "amount": 420.00,
    "settlementMethod": "CREDIT_MEMO",
    "creditMemoId": "CM-2026-REB-205",
    "currency": "USD"
  }
}
```

---

## GL Posting Rules (Examples)

### Invoice Posted
```
Debit:  Accounts Receivable (1200)     $1,000
Credit: Revenue (4000)                    $900
Credit: Sales Tax Payable (2100)          $100
```

### Payment Received
```
Debit:  Cash/Bank (1000)               $1,000
Credit: Accounts Receivable (1200)     $1,000
```

### Credit Memo Issued
```
Debit:  Sales Returns (4100)             $100
Credit: Accounts Receivable (1200)       $100
```

### Write-Off Approved
```
Debit:  Bad Debt Expense (6500)          $500
Credit: Accounts Receivable (1200)       $500
```

---

## Avro Schemas

### InvoicePostedSchema.avro

```json
{
  "type": "record",
  "name": "InvoicePostedEvent",
  "namespace": "com.erp.finance.ar.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "invoiceId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "invoiceNumber", "type": "string" },
    { "name": "postingDate", "type": "int", "logicalType": "date" },
    { "name": "totalAmount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" },
    { "name": "glEntries", "type": { "type": "array", "items": "GLEntryRecord" } }
  ]
}
```

### PaymentReceivedSchema.avro

```json
{
  "type": "record",
  "name": "PaymentReceivedEvent",
  "namespace": "com.erp.finance.ar.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "paymentId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "paymentDate", "type": "int", "logicalType": "date" },
    { "name": "amount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" },
    { "name": "glEntries", "type": { "type": "array", "items": "GLEntryRecord" } }
  ]
}
```

### DunningNoticeGeneratedSchema.avro

```json
{
  "type": "record",
  "name": "DunningNoticeGeneratedEvent",
  "namespace": "com.erp.finance.ar.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "dunningRunId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "dunningLevel", "type": "string" },
    { "name": "pastDueAmount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "asOfDate", "type": "int", "logicalType": "date" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.ar.invoice.posted` | AR | GL, Controlling, Revenue | 12 |
| `finance.ar.invoice.cancelled` | AR | GL, Sales | 6 |
| `finance.ar.payment.received` | AR | GL, Treasury | 12 |
| `finance.ar.payment.reversed` | AR | GL, Treasury | 6 |
| `finance.ar.credit.memo.issued` | AR | GL, Sales | 6 |
| `finance.ar.dunning.notice.generated` | AR | Notifications, CRM | 3 |
| `finance.ar.customer.blocked` | AR | Sales, CRM | 3 |
| `finance.ar.writeoff.approved` | AR | GL | 3 |
