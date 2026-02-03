# Intercompany Events & Integration

> Part of [Finance - Intercompany](../finance-intercompany.md)

---

## Domain Events Published

### IntercompanyPostedEvent

**Trigger**: Intercompany transaction posted  
**Consumers**: General Ledger

```json
{
  "eventType": "IntercompanyPostedEvent",
  "payload": {
    "transactionId": "ic-001",
    "amount": 5000.00,
    "currency": "USD",
    "glEntries": [
      { "accountId": "4300-00", "debit": 0.00, "credit": 5000.00 },
      { "accountId": "2300-00", "debit": 5000.00, "credit": 0.00 }
    ]
  }
}
```

---

### NettingCompletedEvent

**Trigger**: Netting batch completed  
**Consumers**: Treasury, Reporting

```json
{
  "eventType": "NettingCompletedEvent",
  "payload": {
    "batchId": "net-001",
    "netAmount": 15000.00
  }
}
```

---

## Domain Events Consumed

```
- FinancialPeriodClosedEvent (from GL) -> Trigger eliminations
```

---

## GL Posting Rules (Examples)

### Intercompany Sale
```
Debit:  Intercompany Receivable (2300)  $5,000
Credit: Intercompany Revenue (4300)     $5,000
```

---

## Avro Schemas

### IntercompanyPostedSchema.avro

```json
{
  "type": "record",
  "name": "IntercompanyPostedEvent",
  "namespace": "com.erp.finance.intercompany.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "transactionId", "type": "string" },
    { "name": "amount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.intercompany.posted` | Intercompany | GL | 6 |
| `finance.intercompany.netting.completed` | Intercompany | Treasury, Reporting | 3 |
