# Manufacturing Production Orders API Reference

> Part of [Manufacturing Production Orders](../manufacturing-production.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Production Orders

```http
POST /api/v1/manufacturing/production/orders
Content-Type: application/json

{
  "itemId": "FG-100",
  "quantity": 100,
  "dueDate": "2026-02-20",
  "routingId": "R-200"
}
```

```http
POST /api/v1/manufacturing/production/orders/{orderId}/release
```

```http
POST /api/v1/manufacturing/production/orders/{orderId}/issue
Content-Type: application/json

{
  "componentId": "RM-10",
  "quantity": 25
}
```

```http
POST /api/v1/manufacturing/production/orders/{orderId}/confirm
Content-Type: application/json

{
  "operationId": "OP-10",
  "laborTime": 2.5,
  "machineTime": 3.0
}
```

```http
POST /api/v1/manufacturing/production/orders/{orderId}/receipt
Content-Type: application/json

{
  "quantity": 90
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
| PRODUCTION_ORDER_NOT_FOUND | 404 | Production order not found |
| INVALID_ORDER_STATE | 409 | Order state does not allow action |
| INSUFFICIENT_COMPONENTS | 422 | Components not available |
| PERIOD_CLOSED | 422 | Manufacturing period closed |
