# Manufacturing Production Orders Events & Integration

> Part of [Manufacturing Production Orders](../manufacturing-production.md)

---

## Domain Events Published

### ProductionOrderReleasedEvent

```json
{
  "eventType": "ProductionOrderReleasedEvent",
  "payload": {
    "orderId": "MO-100",
    "itemId": "FG-100",
    "quantity": 100
  }
}
```

### MaterialIssuedEvent

```json
{
  "eventType": "MaterialIssuedEvent",
  "payload": {
    "orderId": "MO-100",
    "componentId": "RM-10",
    "quantity": 25
  }
}
```

### ProductionConfirmedEvent

```json
{
  "eventType": "ProductionConfirmedEvent",
  "payload": {
    "orderId": "MO-100",
    "operationId": "OP-10",
    "laborTime": 2.5,
    "machineTime": 3.0
  }
}
```

### ProductionReceiptPostedEvent

```json
{
  "eventType": "ProductionReceiptPostedEvent",
  "payload": {
    "orderId": "MO-100",
    "receiptId": "GR-500",
    "quantity": 90
  }
}
```

---

## Domain Events Consumed

```
- PlannedOrderCreatedEvent (from MRP) -> Create order
- CapacityConstraintDetectedEvent (from Capacity) -> Reschedule
- RoutingUpdatedEvent (from BOM Mgmt) -> Update operations
```

---

## GL Posting Rules (Examples)

### Production Receipt
```
Debit:  Inventory (1400)           $900
Credit: WIP (1500)                 $900
```

---

## Avro Schemas

### ProductionOrderReleasedSchema.avro

```json
{
  "type": "record",
  "name": "ProductionOrderReleasedEvent",
  "namespace": "com.erp.manufacturing.production.events",
  "fields": [
    { "name": "orderId", "type": "string" },
    { "name": "itemId", "type": "string" },
    { "name": "quantity", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `manufacturing.production.order.released` | Production Orders | Shop Floor, Inventory | 12 |
| `manufacturing.production.material.issued` | Production Orders | Inventory, Costing | 12 |
| `manufacturing.production.confirmed` | Production Orders | Costing, Analytics | 12 |
| `manufacturing.production.receipt.posted` | Production Orders | Inventory, Costing | 12 |
