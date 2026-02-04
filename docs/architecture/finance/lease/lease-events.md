# Lease Events & Integration

> Part of [Finance - Lease Accounting](../finance-lease.md)

---

## Domain Events Published

### LeaseActivatedEvent

**Trigger**: Lease activated
**Consumers**: Assets, GL

```json
{
  "eventType": "LeaseActivatedEvent",
  "payload": {
    "leaseId": "lease-001",
    "activationDate": "2026-02-01",
    "rouAssetId": "rou-001"
  }
}
```

---

### LeaseAmortizationPostedEvent

**Trigger**: Lease amortization posted
**Consumers**: GL

```json
{
  "eventType": "LeaseAmortizationPostedEvent",
  "payload": {
    "leaseId": "lease-001",
    "period": "2026-02",
    "amortizationAmount": 5000.00
  }
}
```

---

## Domain Events Consumed

```
- FinancialPeriodClosedEvent (from GL) -> Lock amortization
```

---

## GL Posting Rules (Examples)

### Lease Amortization
```
Debit:  Lease Expense (6200)        $5,000
Credit: ROU Asset (1505)            $5,000
```

---

## Avro Schemas

### LeaseActivatedSchema.avro

```json
{
  "type": "record",
  "name": "LeaseActivatedEvent",
  "namespace": "com.erp.finance.lease.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "leaseId", "type": "string" },
    { "name": "activationDate", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.lease.activated` | Lease | Assets, GL | 3 |
| `finance.lease.amortization.posted` | Lease | GL | 3 |
