# Inventory Core Events & Integration

> Part of [Inventory Core](../inventory-core.md)

---

## Domain Events Published

### StockReceivedEvent

**Trigger**: Receipt posted to stock ledger  
**Consumers**: Finance/GL, WMS, Analytics

```json
{
  "eventType": "StockReceivedEvent",
  "eventId": "evt-2001",
  "timestamp": "2026-02-02T10:20:00Z",
  "aggregateId": "mov-001",
  "tenantId": "tenant-001",
  "payload": {
    "itemId": "item-001",
    "locationId": "LOC-100",
    "quantity": 100,
    "uom": "EA",
    "lotNumber": "LOT-2026-02",
    "movementType": "RECEIPT",
    "cost": 1.25,
    "currency": "USD",
    "glEntries": [
      { "accountId": "1400-00", "debit": 125.00, "credit": 0.00 },
      { "accountId": "2200-00", "debit": 0.00, "credit": 125.00 }
    ]
  }
}
```

### StockIssuedEvent

**Trigger**: Issue posted to stock ledger  
**Consumers**: Finance/GL, Sales, Manufacturing

```json
{
  "eventType": "StockIssuedEvent",
  "payload": {
    "itemId": "item-001",
    "locationId": "LOC-100",
    "quantity": 5,
    "uom": "EA",
    "movementType": "ISSUE",
    "reference": "SO-10002"
  }
}
```

### ReservationCreatedEvent

**Trigger**: Reservation created  
**Consumers**: WMS, Sales

```json
{
  "eventType": "ReservationCreatedEvent",
  "payload": {
    "reservationId": "res-001",
    "itemId": "item-001",
    "locationId": "LOC-100",
    "quantity": 10,
    "channel": "ECOM",
    "reference": "SO-10002"
  }
}
```

---

## Domain Events Consumed

```
- GoodsReceivedEvent         (from Procurement) -> Receive stock
- SalesOrderAllocatedEvent   (from Sales)       -> Create reservation
- ProductionReceiptEvent     (from Manufacturing) -> Receive finished goods
- WmsTaskCompletedEvent      (from WMS)         -> Confirm movement
```

---

## GL Posting Rules (Examples)

### Goods Receipt
```
Debit:  Inventory (1400)             $125
Credit: GR/IR Clearing (2200)        $125
```

### Stock Issue (COGS)
```
Debit:  COGS (5000)                  $25
Credit: Inventory (1400)             $25
```

### Cycle Count Adjustment (Shortage)
```
Debit:  Shrinkage Expense (6550)     $10
Credit: Inventory (1400)             $10
```

---

## Avro Schemas

### StockReceivedSchema.avro

```json
{
  "type": "record",
  "name": "StockReceivedEvent",
  "namespace": "com.erp.inventory.core.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "itemId", "type": "string" },
    { "name": "locationId", "type": "string" },
    { "name": "quantity", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "uom", "type": "string" },
    { "name": "movementType", "type": "string" },
    { "name": "cost", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

### ReservationCreatedSchema.avro

```json
{
  "type": "record",
  "name": "ReservationCreatedEvent",
  "namespace": "com.erp.inventory.core.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "reservationId", "type": "string" },
    { "name": "itemId", "type": "string" },
    { "name": "locationId", "type": "string" },
    { "name": "quantity", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "channel", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `inventory.core.stock.received` | Inventory Core | GL, WMS, Analytics | 12 |
| `inventory.core.stock.issued` | Inventory Core | GL, Sales, Manufacturing | 12 |
| `inventory.core.stock.adjusted` | Inventory Core | GL, Analytics | 6 |
| `inventory.core.reservation.created` | Inventory Core | WMS, Sales | 12 |
| `inventory.core.reservation.released` | Inventory Core | WMS, Sales | 6 |
| `inventory.core.cyclecount.posted` | Inventory Core | GL, Analytics | 6 |
