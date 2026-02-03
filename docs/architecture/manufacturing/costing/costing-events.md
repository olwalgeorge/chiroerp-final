# Manufacturing Costing Events & Integration

> Part of [Manufacturing Costing](../manufacturing-costing.md)

---

## Domain Events Published

### CostRollupCompletedEvent

```json
{
  "eventType": "CostRollupCompletedEvent",
  "payload": {
    "costVersion": "2026-STD",
    "itemCount": 1200
  }
}
```

### WIPPostedEvent

```json
{
  "eventType": "WIPPostedEvent",
  "payload": {
    "orderId": "MO-100",
    "amount": 500.00
  }
}
```

### ProductionVariancePostedEvent

```json
{
  "eventType": "ProductionVariancePostedEvent",
  "payload": {
    "orderId": "MO-100",
    "varianceType": "YIELD",
    "amount": 25.00
  }
}
```

---

## Domain Events Consumed

```
- ProductionConfirmedEvent (from Production Orders) -> Update WIP
- ProductionReceiptPostedEvent (from Production Orders) -> Settle WIP
- BOMPublishedEvent (from BOM Mgmt) -> Trigger rollup
```

---

## GL Posting Rules (Examples)

### WIP Posting
```
Debit:  WIP (1500)                 $500
Credit: Labor/Overhead (5000)      $500
```

### Variance Posting
```
Debit:  Production Variance (5200) $25
Credit: WIP (1500)                 $25
```

---

## Avro Schemas

### WipPostedSchema.avro

```json
{
  "type": "record",
  "name": "WIPPostedEvent",
  "namespace": "com.erp.manufacturing.costing.events",
  "fields": [
    { "name": "orderId", "type": "string" },
    { "name": "amount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `manufacturing.costing.wip.posted` | Costing | Controlling | 6 |
| `manufacturing.costing.variance.posted` | Costing | Controlling | 6 |
| `manufacturing.costing.rollup.completed` | Costing | BOM Mgmt, Analytics | 6 |
