# Plant Maintenance Preventive API Reference

> Part of [Plant Maintenance Preventive Maintenance](../maintenance-preventive.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Create Maintenance Plan

```http
POST /api/v1/maintenance/plans
Content-Type: application/json
Authorization: Bearer {token}

{
  "equipmentId": "EQ-100",
  "planType": "TIME",
  "intervalDays": 90
}
```

**Response**: `201 Created`
```json
{
  "planId": "PLAN-700",
  "status": "ACTIVE"
}
```

---

## Generate Schedule

```http
POST /api/v1/maintenance/plans/PLAN-700/schedule
Content-Type: application/json

{
  "horizonDays": 180
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
| PLAN_NOT_FOUND | 404 | Maintenance plan not found |
| PLAN_INACTIVE | 409 | Plan not active |
| COUNTER_INVALID | 422 | Counter invalid |
| SCHEDULE_CONFLICT | 409 | Schedule conflict |
