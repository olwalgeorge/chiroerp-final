# Quality Inspection Planning API Reference

> Part of [Quality Inspection Planning](../quality-inspection-planning.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Create Inspection Plan

```http
POST /api/v1/quality/inspection-plans
Content-Type: application/json
Authorization: Bearer {token}

{
  "planName": "INCOMING-STD",
  "itemId": "ITEM-100",
  "version": "v1",
  "effectiveFrom": "2026-02-01"
}
```

**Response**: `201 Created`
```json
{
  "planId": "PLAN-100",
  "status": "ACTIVE"
}
```

---

## Add Characteristic

```http
POST /api/v1/quality/inspection-plans/PLAN-100/characteristics
Content-Type: application/json

{
  "name": "Weight",
  "specMin": 9.5,
  "specMax": 10.5,
  "unit": "KG"
}
```

---

## Update Sampling Plan

```http
PUT /api/v1/quality/inspection-plans/PLAN-100/sampling
Content-Type: application/json

{
  "planType": "AQL",
  "aqlLevel": "1.0",
  "sampleSize": 5
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
| PLAN_NOT_FOUND | 404 | Inspection plan not found |
| INVALID_CHARACTERISTIC | 422 | Spec limits invalid |
| SAMPLING_PLAN_INVALID | 422 | Sampling plan invalid |
| TRIGGER_RULE_CONFLICT | 409 | Trigger rule conflict |
