# Manufacturing BOM API Reference

> Part of [Manufacturing BOM Management](../manufacturing-bom.md)

## Base URL

```
https://api.chiroerp.com
```

---

## BOMs

```http
POST /api/v1/manufacturing/boms
Content-Type: application/json

{
  "itemId": "FG-100",
  "revision": "A",
  "items": [
    { "componentId": "RM-10", "quantity": 2 }
  ]
}
```

```http
POST /api/v1/manufacturing/boms/{bomId}/publish
```

```http
GET /api/v1/manufacturing/boms/{bomId}
```

---

## Routings

```http
POST /api/v1/manufacturing/routings
Content-Type: application/json

{
  "itemId": "FG-100",
  "operations": [
    { "sequence": 10, "workCenterId": "WC-10", "setupTime": 1.0, "runTime": 2.5 }
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
| BOM_NOT_FOUND | 404 | BOM not found |
| BOM_CYCLE_DETECTED | 422 | BOM has a cyclic reference |
| ROUTING_NOT_FOUND | 404 | Routing not found |
