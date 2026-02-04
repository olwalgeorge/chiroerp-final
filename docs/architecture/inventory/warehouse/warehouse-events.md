# Warehouse Execution Events & Integration

> Part of [Inventory Warehouse Execution](../inventory-warehouse.md)

---

## Domain Events Published

### WaveReleasedEvent

**Trigger**: Wave released for picking
**Consumers**: Analytics, Labor, Operations

```json
{
  "eventType": "WaveReleasedEvent",
  "eventId": "evt-3001",
  "timestamp": "2026-02-02T12:00:00Z",
  "aggregateId": "wave-001",
  "tenantId": "tenant-001",
  "payload": {
    "waveId": "wave-001",
    "warehouseId": "WH-01",
    "priority": "EXPRESS",
    "orderCount": 2
  }
}
```

### TaskCompletedEvent

**Trigger**: Task completed (pick/putaway/replenishment/kit/repack)
**Consumers**: Inventory Core, Analytics, Advanced Ops

```json
{
  "eventType": "TaskCompletedEvent",
  "payload": {
    "taskId": "task-001",
    "taskType": "PICK",
    "itemId": "item-001",
    "quantity": 5,
    "sourceBin": "BIN-A-01",
    "targetBin": "PACK-01",
    "completedBy": "USR-100",
    "completedAt": "2026-02-02T12:10:00Z"
  }
}
```

**Note**: `taskType` supports execution variants such as `PUTAWAY`, `REPLENISH`, `KIT_ASSEMBLY`, `REPACK`, and `BREAK_BULK` in addition to standard pick tasks.

### PutawayConfirmedEvent

**Trigger**: Putaway confirmed
**Consumers**: Inventory Core

```json
{
  "eventType": "PutawayConfirmedEvent",
  "payload": {
    "taskId": "task-010",
    "itemId": "item-001",
    "quantity": 20,
    "targetBin": "BIN-B-02"
  }
}
```

---

## Domain Events Consumed

```
- ReservationCreatedEvent            (from Inventory Core)      -> Create pick tasks
- InventorySnapshotEvent             (from Inventory Core)      -> Refresh availability
- GoodsReceiptNoticeEvent            (from Procurement)         -> Create putaway tasks
- SalesOrderReleasedEvent            (from Sales)               -> Prioritize waves
- KitAssemblyWorkOrderCreatedEvent   (from Inventory Advanced Ops) -> Create kit assembly tasks
- RepackWorkOrderCreatedEvent        (from Inventory Advanced Ops) -> Create repack tasks
- BreakBulkWorkOrderCreatedEvent     (from Inventory Advanced Ops) -> Create break-bulk tasks
```

---

## Inventory Core Integration Events

| Interface | Direction | Description |
|-----------|-----------|-------------|
| InventorySnapshot | Core -> WMS | Current stock by location/lot/serial |
| ReservationEvent | Core -> WMS | Allocation requests for fulfillment |
| GoodsMovementConfirm | WMS -> Core | Confirmed picks, putaways, transfers |
| CycleCountResult | WMS -> Core | Count results for variance processing |
| ATPQuery | WMS -> Core | Real-time availability check |
| TaskComplete | WMS -> Core | Execution confirmations for stock update |

---

## Avro Schemas

### TaskCompletedSchema.avro

```json
{
  "type": "record",
  "name": "TaskCompletedEvent",
  "namespace": "com.erp.inventory.warehouse.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "taskId", "type": "string" },
    { "name": "taskType", "type": "string" },
    { "name": "itemId", "type": "string" },
    { "name": "quantity", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "sourceBin", "type": ["null", "string"], "default": null },
    { "name": "targetBin", "type": ["null", "string"], "default": null }
  ]
}
```

### WaveReleasedSchema.avro

```json
{
  "type": "record",
  "name": "WaveReleasedEvent",
  "namespace": "com.erp.inventory.warehouse.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "waveId", "type": "string" },
    { "name": "warehouseId", "type": "string" },
    { "name": "priority", "type": "string" },
    { "name": "orderCount", "type": "int" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `inventory.warehouse.wave.released` | WMS | Analytics, Labor | 6 |
| `inventory.warehouse.task.completed` | WMS | Inventory Core, Analytics | 12 |
| `inventory.warehouse.putaway.confirmed` | WMS | Inventory Core | 6 |
| `inventory.warehouse.pick.confirmed` | WMS | Inventory Core | 6 |
| `inventory.warehouse.replenishment.completed` | WMS | Inventory Core | 6 |
| `inventory.warehouse.return.dispositioned` | WMS | Inventory Core, Analytics | 3 |
