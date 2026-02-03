# Quality Execution Events & Integration

> Part of [Quality Execution](../quality-execution.md)

---

## Domain Events Published

### InspectionLotCreatedEvent

```json
{
  "eventType": "InspectionLotCreatedEvent",
  "payload": {
    "inspectionLotId": "LOT-900",
    "sourceType": "INCOMING",
    "referenceId": "GR-1001",
    "itemId": "ITEM-100"
  }
}
```

### InspectionCompletedEvent

```json
{
  "eventType": "InspectionCompletedEvent",
  "payload": {
    "inspectionLotId": "LOT-900",
    "decision": "ACCEPT",
    "resultStatus": "PASS"
  }
}
```

### StockBlockedEvent

```json
{
  "eventType": "StockBlockedEvent",
  "payload": {
    "itemId": "ITEM-100",
    "lotNumber": "LOT-2026-01",
    "reason": "QUALITY_HOLD"
  }
}
```

---

## Domain Events Consumed

```
- GoodsReceivedEvent (from Procurement) -> Create inspection lot
- ProductionOrderReleasedEvent (from Manufacturing) -> Create in-process lot
- ReturnReceivedEvent (from Sales) -> Create return lot
```

---

## Avro Schemas

### InspectionCompletedSchema.avro

```json
{
  "type": "record",
  "name": "InspectionCompletedEvent",
  "namespace": "com.erp.quality.execution.events",
  "fields": [
    { "name": "inspectionLotId", "type": "string" },
    { "name": "decision", "type": "string" },
    { "name": "resultStatus", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `quality.execution.lot.created` | Quality Execution | Inspectors, Analytics | 6 |
| `quality.execution.inspection.completed` | Quality Execution | Inventory, Nonconformance | 6 |
| `quality.execution.stock.blocked` | Quality Execution | Inventory | 6 |
