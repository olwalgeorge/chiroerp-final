# Analytics KPI API Reference

> Part of [Analytics KPI Engine](../analytics-kpi.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Define KPI

```http
POST /api/v1/analytics/kpis
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "InventoryTurns",
  "formula": "cogs / avg_inventory",
  "thresholds": {
    "warning": 4,
    "critical": 2
  }
}
```

**Response**: `201 Created`
```json
{
  "kpiId": "KPI-100",
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
| KPI_NOT_FOUND | 404 | KPI not found |
| INVALID_FORMULA | 422 | KPI formula invalid |
| THRESHOLD_INVALID | 422 | Threshold invalid |
