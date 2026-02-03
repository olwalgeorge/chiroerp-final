# Manufacturing Shop Floor API Reference

> Part of [Manufacturing Shop Floor Execution](../manufacturing-shop-floor.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Dispatch Queue

```http
POST /api/v1/manufacturing/shopfloor/dispatch
Content-Type: application/json

{
  "orderId": "MO-100",
  "workCenterId": "WC-10"
}
```

```http
GET /api/v1/manufacturing/shopfloor/dispatch?workCenterId=WC-10
```

---

## Operation Execution

```http
POST /api/v1/manufacturing/shopfloor/operations/start
Content-Type: application/json

{
  "orderId": "MO-100",
  "operationId": "OP-10"
}
```

```http
POST /api/v1/manufacturing/shopfloor/operations/complete
Content-Type: application/json

{
  "orderId": "MO-100",
  "operationId": "OP-10",
  "laborTime": 2.5,
  "machineTime": 3.0
}
```

---

## Scrap

```http
POST /api/v1/manufacturing/shopfloor/scrap
Content-Type: application/json

{
  "orderId": "MO-100",
  "operationId": "OP-10",
  "quantity": 2,
  "reason": "SETUP_REJECT"
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
| DISPATCH_NOT_FOUND | 404 | Dispatch not found |
| INVALID_OPERATION_STATE | 409 | Operation state invalid |
| WORKCENTER_UNAVAILABLE | 422 | Work center not available |
