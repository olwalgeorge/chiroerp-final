# Warehouse Execution API Reference

> Part of [Inventory Warehouse Execution](../inventory-warehouse.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Waves

### Create Wave

```http
POST /api/v1/inventory/warehouse/waves
Content-Type: application/json
Authorization: Bearer {token}

{
  "warehouseId": "WH-01",
  "orderIds": ["SO-10002", "SO-10003"],
  "priority": "EXPRESS"
}
```

**Response**: `201 Created`
```json
{
  "waveId": "wave-001",
  "status": "PLANNED",
  "orderCount": 2
}
```

---

### Release Wave

```http
POST /api/v1/inventory/warehouse/waves/{waveId}/release
```

---

## Tasks

### Create Task

```http
POST /api/v1/inventory/warehouse/tasks
Content-Type: application/json

{
  "taskType": "PICK",
  "itemId": "item-001",
  "quantity": 5,
  "sourceBin": "BIN-A-01",
  "targetBin": "PACK-01"
}
```

**Supported taskType values**:
`PICK`, `PUTAWAY`, `REPLENISH`, `KIT_ASSEMBLY`, `REPACK`, `BREAK_BULK`

**Kit/Repack execution**: tasks of type `KIT_ASSEMBLY`, `REPACK`, and `BREAK_BULK` are created from Inventory Advanced Ops work orders.

**Response**: `201 Created`
```json
{
  "taskId": "task-001",
  "status": "PENDING"
}
```

---

### Complete Task

```http
POST /api/v1/inventory/warehouse/tasks/{taskId}/complete
Content-Type: application/json

{
  "actualQuantity": 5,
  "workerId": "USR-100"
}
```

**Response**: `200 OK`
```json
{
  "taskId": "task-001",
  "status": "COMPLETED"
}
```

---

## Putaway

### Confirm Putaway

```http
POST /api/v1/inventory/warehouse/putaway/confirm
Content-Type: application/json

{
  "taskId": "task-010",
  "targetBin": "BIN-B-02",
  "quantity": 20
}
```

---

## Replenishment

### Trigger Replenishment

```http
POST /api/v1/inventory/warehouse/replenishments
Content-Type: application/json

{
  "itemId": "item-001",
  "fromBin": "BIN-R-01",
  "toBin": "BIN-A-01",
  "quantity": 50
}
```

---

## Error Responses

### Standard Error Format

```json
{
  "errorCode": "ERROR_CODE",
  "message": "Human-readable error message",
  "details": {
    "field": "Additional context"
  },
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-wms-001"
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| WAVE_NOT_FOUND | 404 | Wave ID does not exist |
| TASK_NOT_FOUND | 404 | Task ID does not exist |
| BIN_NOT_FOUND | 404 | Bin ID does not exist |
| BIN_CAPACITY_EXCEEDED | 422 | Putaway exceeds bin capacity |
| TASK_ALREADY_COMPLETED | 409 | Task is already completed |
| INVALID_PICK_LOCATION | 422 | Pick source is invalid |
