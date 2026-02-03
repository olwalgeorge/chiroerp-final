# Manufacturing MRP Events & Integration

> Part of [Manufacturing MRP](../manufacturing-mrp.md)

---

## Domain Events Published

### PlannedOrderCreatedEvent

```json
{
  "eventType": "PlannedOrderCreatedEvent",
  "payload": {
    "plannedOrderId": "PO-200",
    "itemId": "FG-100",
    "quantity": 100,
    "dueDate": "2026-02-20",
    "supplyType": "IN_HOUSE"
  }
}
```

### MRPRunCompletedEvent

```json
{
  "eventType": "MRPRunCompletedEvent",
  "payload": {
    "runId": "MRP-100",
    "plantId": "PLANT-01",
    "plannedOrderCount": 42
  }
}
```

---

## Domain Events Consumed

```
- SalesOrderAllocatedEvent (from Sales) -> Demand signal
- StockAdjustedEvent (from Inventory) -> Refresh availability
- BOMPublishedEvent (from BOM Mgmt) -> Rebuild plan
```

---

## Avro Schemas

### PlannedOrderCreatedSchema.avro

```json
{
  "type": "record",
  "name": "PlannedOrderCreatedEvent",
  "namespace": "com.erp.manufacturing.mrp.events",
  "fields": [
    { "name": "plannedOrderId", "type": "string" },
    { "name": "itemId", "type": "string" },
    { "name": "quantity", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "dueDate", "type": "string" },
    { "name": "supplyType", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `manufacturing.mrp.planned-order.created` | MRP | Production, Procurement | 12 |
| `manufacturing.mrp.run.completed` | MRP | Analytics | 6 |
