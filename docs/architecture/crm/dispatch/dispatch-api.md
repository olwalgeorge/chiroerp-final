# CRM Dispatch API Reference

> Part of [CRM Dispatch](../crm-dispatch.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Dispatch Requests

```http
POST /api/v1/crm/dispatch/requests
Content-Type: application/json

{
  "serviceOrderId": "SO-100",
  "locationId": "LOC-10",
  "requiredSkills": ["HVAC"]
}
```

```http
POST /api/v1/crm/dispatch/{dispatchId}/assign
Content-Type: application/json

{
  "technicianId": "TECH-10"
}
```

```http
POST /api/v1/crm/dispatch/{dispatchId}/confirm
```

---

## Technicians

```http
GET /api/v1/crm/dispatch/technicians/available?locationId=LOC-10
```

```http
POST /api/v1/crm/dispatch/technicians/{technicianId}/status
Content-Type: application/json

{
  "status": "ON_ROUTE"
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
| DISPATCH_NOT_FOUND | 404 | Dispatch request not found |
| TECHNICIAN_NOT_AVAILABLE | 409 | Technician unavailable |
| ASSIGNMENT_CONFLICT | 409 | Overlapping assignment detected |
| ROUTE_OPTIMIZATION_FAILED | 500 | Route optimization failed |
