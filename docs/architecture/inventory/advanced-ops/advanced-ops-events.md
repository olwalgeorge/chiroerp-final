# Inventory Advanced Ops Events & Integration

> Part of [Inventory Advanced Operations](../inventory-advanced-ops.md)

---

## Domain Events Published

### KitAssemblyWorkOrderCreatedEvent

**Trigger**: Kit assembly order created
**Consumers**: Warehouse Execution (WMS)

```json
{
  "eventType": "KitAssemblyWorkOrderCreatedEvent",
  "eventId": "evt-advops-001",
  "timestamp": "2026-02-02T12:00:00Z",
  "tenantId": "tenant-001",
  "payload": {
    "assemblyOrderId": "kao-001",
    "kitId": "kit-001",
    "quantity": 10,
    "warehouseId": "WH-01",
    "priority": "STANDARD"
  }
}
```

### RepackWorkOrderCreatedEvent

**Trigger**: Repack order created
**Consumers**: Warehouse Execution (WMS)

```json
{
  "eventType": "RepackWorkOrderCreatedEvent",
  "eventId": "evt-advops-002",
  "timestamp": "2026-02-02T12:05:00Z",
  "tenantId": "tenant-001",
  "payload": {
    "repackOrderId": "rpo-001",
    "sourceItemId": "item-001",
    "targetItemId": "item-001",
    "quantity": 100,
    "reason": "DAMAGED_CASE"
  }
}
```

### KitAssembledEvent

**Trigger**: Kit assembly completed
**Consumers**: Inventory Core, Finance/GL, Analytics

```json
{
  "eventType": "KitAssembledEvent",
  "eventId": "evt-advops-003",
  "timestamp": "2026-02-02T13:00:00Z",
  "tenantId": "tenant-001",
  "payload": {
    "assemblyOrderId": "kao-001",
    "kitId": "kit-001",
    "quantity": 10,
    "costRollup": 250.00
  }
}
```

### RepackCompletedEvent

**Trigger**: Repack order completed
**Consumers**: Inventory Core, Finance/GL

```json
{
  "eventType": "RepackCompletedEvent",
  "eventId": "evt-advops-004",
  "timestamp": "2026-02-02T13:30:00Z",
  "tenantId": "tenant-001",
  "payload": {
    "repackOrderId": "rpo-001",
    "quantityCompleted": 100,
    "variance": 2
  }
}
```

### CatchWeightRecordedEvent

**Trigger**: Actual weight captured
**Consumers**: Inventory Core, Sales (pricing)

```json
{
  "eventType": "CatchWeightRecordedEvent",
  "eventId": "evt-advops-005",
  "timestamp": "2026-02-02T14:00:00Z",
  "tenantId": "tenant-001",
  "payload": {
    "itemId": "item-010",
    "lotNumber": "LOT-2026-02",
    "nominalWeight": 1.0,
    "actualWeight": 1.03,
    "tareWeight": 0.02
  }
}
```

---

## Domain Events Consumed

```
- TaskCompletedEvent (from Warehouse Execution) -> Complete kit/repack work orders
- StockReceivedEvent (from Inventory Core) -> Update kit/repack availability
- ReservationCreatedEvent (from Inventory Core) -> Validate component reservations
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `inventory.advanced-ops.kit.workorder.created` | Advanced Ops | WMS | 6 |
| `inventory.advanced-ops.repack.workorder.created` | Advanced Ops | WMS | 6 |
| `inventory.advanced-ops.kit.assembled` | Advanced Ops | Core, Finance, Analytics | 6 |
| `inventory.advanced-ops.repack.completed` | Advanced Ops | Core, Finance | 6 |
| `inventory.advanced-ops.catchweight.recorded` | Advanced Ops | Core, Sales | 6 |
