# Manufacturing MRP API Reference

> Part of [Manufacturing MRP](../manufacturing-mrp.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Run MRP

```http
POST /api/v1/manufacturing/mrp/runs
Content-Type: application/json
Authorization: Bearer {token}

{
  "plantId": "PLANT-01",
  "horizonDays": 30,
  "includeSafetyStock": true
}
```

**Response**: `202 Accepted`
```json
{
  "runId": "MRP-100",
  "status": "RUNNING"
}
```

---

## Planned Orders

```http
GET /api/v1/manufacturing/mrp/planned-orders?plantId=PLANT-01
```

```http
POST /api/v1/manufacturing/mrp/planned-orders/convert
Content-Type: application/json

{
  "plannedOrderId": "PO-200",
  "targetType": "PRODUCTION_ORDER"
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
| MRP_RUN_IN_PROGRESS | 409 | MRP run already running |
| PLANNED_ORDER_NOT_FOUND | 404 | Planned order does not exist |
| INVALID_HORIZON | 422 | Planning horizon is invalid |
| BOM_EXPLOSION_FAILED | 500 | BOM could not be exploded |
