# Manufacturing Costing API Reference

> Part of [Manufacturing Costing](../manufacturing-costing.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Cost Rollup

```http
POST /api/v1/manufacturing/costing/rollup
Content-Type: application/json

{
  "plantId": "PLANT-01",
  "costVersion": "2026-STD"
}
```

---

## WIP Posting

```http
POST /api/v1/manufacturing/costing/wip
Content-Type: application/json

{
  "orderId": "MO-100",
  "amount": 500.00
}
```

---

## Variance Posting

```http
POST /api/v1/manufacturing/costing/variance
Content-Type: application/json

{
  "orderId": "MO-100",
  "varianceType": "YIELD",
  "amount": 25.00
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
| COST_VERSION_INVALID | 422 | Cost version invalid |
| WIP_NOT_FOUND | 404 | WIP record not found |
| PERIOD_CLOSED | 422 | Period is closed |
