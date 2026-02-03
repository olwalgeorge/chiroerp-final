# Analytics OLAP API Reference

> Part of [Analytics OLAP & Cube Engine](../analytics-olap.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Define Cube

```http
POST /api/v1/analytics/olap/cubes
Content-Type: application/json
Authorization: Bearer {token}

{
  "cubeName": "sales_cube",
  "measures": ["net_sales", "gross_margin"],
  "dimensions": ["date", "product", "region"]
}
```

**Response**: `201 Created`
```json
{
  "cubeId": "CUBE-100",
  "status": "ACTIVE"
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
| CUBE_NOT_FOUND | 404 | Cube not found |
| DIMENSION_MISSING | 422 | Missing dimension |
| REFRESH_FAILED | 500 | Cube refresh failed |
