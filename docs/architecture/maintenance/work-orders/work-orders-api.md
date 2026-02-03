# Plant Maintenance Work Orders API Reference

> Part of [Plant Maintenance Work Orders](../maintenance-work-orders.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Create Work Order

```http
POST /api/v1/maintenance/work-orders
Content-Type: application/json
Authorization: Bearer {token}

{
  "equipmentId": "EQ-100",
  "type": "PREVENTIVE",
  "priority": "P2",
  "description": "Quarterly inspection"
}
```

**Response**: `201 Created`
```json
{
  "workOrderId": "WO-3000",
  "status": "PLANNED"
}
```

---

## Confirm Work Order

```http
POST /api/v1/maintenance/work-orders/WO-3000/confirm
Content-Type: application/json

{
  "laborHours": 2.5,
  "partsUsed": [
    { "partId": "PART-10", "quantity": 2 }
  ]
}
```

---

## Error Responses

### Standard Error Format

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "details": {
    "field": "Additional context"
  },
  "timestamp": "2026-02-02T10:30:00Z",
  "traceId": "abc123"
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| WORK_ORDER_NOT_FOUND | 404 | Work order not found |
| INVALID_STATUS | 409 | Invalid status transition |
| PARTS_UNAVAILABLE | 409 | Required parts unavailable |
| COST_POST_FAILED | 500 | Cost posting failed |
