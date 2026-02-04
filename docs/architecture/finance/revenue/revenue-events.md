# Revenue Events & Integration

> Part of [Finance - Revenue Recognition](../finance-revenue.md)

---

## Domain Events Published

### RevenueRecognizedEvent

**Trigger**: Revenue recognized for period
**Consumers**: General Ledger, Reporting

```json
{
  "eventType": "RevenueRecognizedEvent",
  "payload": {
    "contractId": "ctr-001",
    "period": "2026-02",
    "recognizedAmount": 1000.00,
    "glEntries": [
      { "accountId": "4000-00", "debit": 0.00, "credit": 1000.00 },
      { "accountId": "2300-00", "debit": 1000.00, "credit": 0.00 }
    ]
  }
}
```

---

### RevenueDeferredEvent

**Trigger**: Revenue deferred
**Consumers**: General Ledger

```json
{
  "eventType": "RevenueDeferredEvent",
  "payload": {
    "contractId": "ctr-001",
    "deferredAmount": 11000.00
  }
}
```

---

## Domain Events Consumed

```
- SalesOrderFulfilledEvent   (from Commerce) -> Activate contract
- InvoicePostedEvent         (from AR)       -> Trigger recognition
- FinancialPeriodClosedEvent (from GL)       -> Lock recognition
```

---

## GL Posting Rules (Examples)

### Recognized Revenue
```
Debit:  Deferred Revenue (2300)      $1,000
Credit: Revenue (4000)               $1,000
```

---

## Avro Schemas

### RevenueRecognizedSchema.avro

```json
{
  "type": "record",
  "name": "RevenueRecognizedEvent",
  "namespace": "com.erp.finance.revenue.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "contractId", "type": "string" },
    { "name": "period", "type": "string" },
    { "name": "recognizedAmount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.revenue.recognized` | Revenue | GL, Reporting | 6 |
| `finance.revenue.deferred` | Revenue | GL | 3 |
| `finance.revenue.contract.modified` | Revenue | Reporting | 3 |
