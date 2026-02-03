# Analytics Warehouse API Reference

> Part of [Analytics Data Warehouse](../analytics-warehouse.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Start Warehouse Load

```http
POST /api/v1/analytics/warehouse/loads
Content-Type: application/json
Authorization: Bearer {token}

{
  "sourceSystem": "inventory",
  "batchId": "LOAD-100"
}
```

**Response**: `202 Accepted`
```json
{
  "batchId": "LOAD-100",
  "status": "STARTED"
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
| LOAD_ALREADY_RUNNING | 409 | Warehouse load already running |
| LOAD_NOT_FOUND | 404 | Load not found |
| DIMENSION_INVALID | 422 | Dimension invalid |
| FACT_PUBLISH_FAILED | 500 | Fact publish failed |
