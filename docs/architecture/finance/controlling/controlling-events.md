# Controlling Events & Integration

> Part of [Finance - Controlling](../finance-controlling.md)

---

## Domain Events Published

### AllocationRunPostedEvent

**Trigger**: Allocation run posted
**Consumers**: General Ledger, Reporting

```json
{
  "eventType": "AllocationRunPostedEvent",
  "payload": {
    "runId": "alloc-001",
    "cycleId": "cycle-001",
    "period": "2026-02",
    "totalAllocated": 25000.00,
    "glEntries": [
      { "accountId": "6100-00", "debit": 15000.00, "credit": 0.00 },
      { "accountId": "6100-01", "debit": 0.00, "credit": 15000.00 }
    ]
  }
}
```

---

### CostCenterCreatedEvent

**Trigger**: Cost center created
**Consumers**: HR, Reporting

```json
{
  "eventType": "CostCenterCreatedEvent",
  "payload": {
    "costCenterId": "cc-100",
    "code": "CC-100",
    "name": "Operations"
  }
}
```

---

### InternalOrderClosedEvent

**Trigger**: Internal order closed
**Consumers**: Reporting, Audit

```json
{
  "eventType": "InternalOrderClosedEvent",
  "payload": {
    "orderId": "ord-001",
    "closedAt": "2026-02-28"
  }
}
```

---

## Domain Events Consumed

```
- JournalEntryPostedEvent      (from GL)        -> Update actuals
- AssetTransferredEvent        (from Assets)    -> Update cost center mapping
- FinancialPeriodClosedEvent   (from GL/Close)  -> Lock allocations
```

---

## GL Posting Rules (Examples)

### Allocation Posting
```
Debit:  Cost Center A Expense (6100-00)   $15,000
Credit: Cost Center B Expense (6100-01)   $15,000
```

---

## Avro Schemas

### AllocationRunPostedSchema.avro

```json
{
  "type": "record",
  "name": "AllocationRunPostedEvent",
  "namespace": "com.erp.finance.controlling.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "runId", "type": "string" },
    { "name": "cycleId", "type": "string" },
    { "name": "period", "type": "string" },
    { "name": "totalAllocated", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "glEntries", "type": { "type": "array", "items": "GLEntryRecord" } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.controlling.allocation.posted` | Controlling | GL, Reporting | 6 |
| `finance.controlling.cost-center.created` | Controlling | HR, Reporting | 3 |
| `finance.controlling.order.closed` | Controlling | Reporting, Audit | 3 |
