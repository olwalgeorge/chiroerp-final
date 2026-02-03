# Manufacturing Shop Floor Events & Integration

> Part of [Manufacturing Shop Floor Execution](../manufacturing-shop-floor.md)

---

## Domain Events Published

### OperationCompletedEvent

```json
{
  "eventType": "OperationCompletedEvent",
  "payload": {
    "orderId": "MO-100",
    "operationId": "OP-10",
    "laborTime": 2.5,
    "machineTime": 3.0
  }
}
```

### ScrapRecordedEvent

```json
{
  "eventType": "ScrapRecordedEvent",
  "payload": {
    "orderId": "MO-100",
    "operationId": "OP-10",
    "quantity": 2,
    "reason": "SETUP_REJECT"
  }
}
```

---

## Domain Events Consumed

```
- ProductionOrderReleasedEvent (from Production Orders) -> Create dispatch
```

---

## Avro Schemas

### OperationCompletedSchema.avro

```json
{
  "type": "record",
  "name": "OperationCompletedEvent",
  "namespace": "com.erp.manufacturing.shopfloor.events",
  "fields": [
    { "name": "orderId", "type": "string" },
    { "name": "operationId", "type": "string" },
    { "name": "laborTime", "type": "double" },
    { "name": "machineTime", "type": "double" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `manufacturing.shopfloor.operation.completed` | Shop Floor | Production, Costing | 12 |
| `manufacturing.shopfloor.scrap.recorded` | Shop Floor | Costing, Analytics | 6 |
