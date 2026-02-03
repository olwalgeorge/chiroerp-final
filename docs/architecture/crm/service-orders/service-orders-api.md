# CRM Service Orders API Reference

> Part of [CRM Service Orders](../crm-service-orders.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Service Orders

```http
POST /api/v1/crm/service-orders
Content-Type: application/json

{
  "customerId": "CUST-100",
  "locationId": "LOC-10",
  "priority": "HIGH",
  "description": "On-site repair"
}
```

```http
POST /api/v1/crm/service-orders/{serviceOrderId}/schedule
Content-Type: application/json

{
  "scheduleWindow": {
    "start": "2026-02-03T09:00:00Z",
    "end": "2026-02-03T12:00:00Z"
  }
}
```

```http
POST /api/v1/crm/service-orders/{serviceOrderId}/complete
Content-Type: application/json

{
  "resolutionCode": "RESOLVED"
}
```

---

## Work Logs

```http
POST /api/v1/crm/service-orders/{serviceOrderId}/work-logs
Content-Type: application/json

{
  "technicianId": "TECH-10",
  "workPerformed": "Replaced failed pump"
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
| SERVICE_ORDER_NOT_FOUND | 404 | Service order not found |
| INVALID_STATUS | 409 | Invalid service order status |
| SLA_VIOLATION | 422 | SLA violation detected |
| PARTS_NOT_AVAILABLE | 409 | Required parts not available |
