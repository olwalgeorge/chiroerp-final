# Quality CAPA API Reference

> Part of [Quality CAPA Management](../quality-capa.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Initiate CAPA

```http
POST /api/v1/quality/capa
Content-Type: application/json
Authorization: Bearer {token}

{
  "ncId": "NC-1000",
  "capaType": "CORRECTIVE",
  "description": "Recurring surface defect"
}
```

**Response**: `201 Created`
```json
{
  "capaId": "CAPA-500",
  "status": "OPEN"
}
```

---

## Record Root Cause

```http
POST /api/v1/quality/capa/CAPA-500/root-cause
Content-Type: application/json

{
  "method": "FIVE_WHY",
  "statement": "Incorrect machine calibration"
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
| CAPA_NOT_FOUND | 404 | CAPA record not found |
| ROOT_CAUSE_REQUIRED | 409 | Root cause required before closure |
| ACTION_PLAN_INCOMPLETE | 409 | Actions incomplete |
| EFFECTIVENESS_REQUIRED | 409 | Effectiveness check required |
