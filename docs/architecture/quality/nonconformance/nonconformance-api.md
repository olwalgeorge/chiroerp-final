# Quality Nonconformance API Reference

> Part of [Quality Nonconformance](../quality-nonconformance.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Create Nonconformance

```http
POST /api/v1/quality/nonconformances
Content-Type: application/json
Authorization: Bearer {token}

{
  "sourceLotId": "LOT-900",
  "ncType": "MATERIAL",
  "severity": "MAJOR",
  "description": "Surface defect"
}
```

**Response**: `201 Created`
```json
{
  "ncId": "NC-1000",
  "status": "OPEN"
}
```

---

## Determine Disposition

```http
POST /api/v1/quality/nonconformances/NC-1000/disposition
Content-Type: application/json

{
  "dispositionType": "REWORK",
  "notes": "Rework at station 3"
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
| NC_NOT_FOUND | 404 | Nonconformance not found |
| DISPOSITION_REQUIRED | 409 | Disposition required before closure |
| INVALID_SEVERITY | 422 | Severity invalid |
| QUALITY_COST_INVALID | 422 | Quality cost invalid |
