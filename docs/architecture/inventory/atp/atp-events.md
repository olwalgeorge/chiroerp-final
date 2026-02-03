# Inventory ATP Events & Integration

> Part of [Inventory ATP & Allocation](../inventory-atp.md)

---

## Domain Events Published

### AvailabilityCalculatedEvent

**Trigger**: Availability calculated for item/channel  
**Consumers**: Sales, Ecommerce

```json
{
  "eventType": "AvailabilityCalculatedEvent",
  "payload": {
    "itemId": "item-001",
    "channel": "ECOM",
    "available": 120,
    "reserved": 30
  }
}
```

### AllocationCommittedEvent

**Trigger**: Allocation committed  
**Consumers**: Sales, WMS

```json
{
  "eventType": "AllocationCommittedEvent",
  "payload": {
    "allocationId": "alloc-001",
    "itemId": "item-001",
    "channel": "ECOM",
    "quantity": 5
  }
}
```

---

## Domain Events Consumed

```
- StockAvailabilityChangedEvent (from Inventory Core) -> Refresh ATP cache
- ReservationCreatedEvent (from Inventory Core) -> Update allocations
```

---

## Avro Schemas

### AvailabilityCalculatedSchema.avro

```json
{
  "type": "record",
  "name": "AvailabilityCalculatedEvent",
  "namespace": "com.erp.inventory.atp.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "itemId", "type": "string" },
    { "name": "channel", "type": "string" },
    { "name": "available", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "reserved", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

### AllocationCommittedSchema.avro

```json
{
  "type": "record",
  "name": "AllocationCommittedEvent",
  "namespace": "com.erp.inventory.atp.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "allocationId", "type": "string" },
    { "name": "itemId", "type": "string" },
    { "name": "channel", "type": "string" },
    { "name": "quantity", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `inventory.atp.availability.calculated` | Inventory ATP | Sales, Ecommerce | 6 |
| `inventory.atp.allocation.committed` | Inventory ATP | Sales, WMS | 6 |
| `inventory.atp.allocation.released` | Inventory ATP | Sales, WMS | 3 |
