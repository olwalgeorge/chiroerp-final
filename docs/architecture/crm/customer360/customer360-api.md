# CRM Customer 360 API Reference

> Part of [CRM Customer 360](../crm-customer360.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Customer Profiles

```http
POST /api/v1/crm/customers
Content-Type: application/json

{
  "name": "Acme Corp",
  "status": "ACTIVE",
  "contacts": [
    { "name": "Jane Doe", "email": "jane@acme.com", "role": "BILLING" }
  ]
}
```

```http
GET /api/v1/crm/customers/{customerId}
```

```http
POST /api/v1/crm/customers/{customerId}/merge
Content-Type: application/json

{
  "sourceCustomerId": "CUST-OLD"
}
```

---

## Consent

```http
POST /api/v1/crm/customers/{customerId}/consent
Content-Type: application/json

{
  "type": "EMAIL",
  "status": "GRANTED"
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
| CUSTOMER_NOT_FOUND | 404 | Customer not found |
| DUPLICATE_CUSTOMER | 409 | Duplicate customer detected |
| CONSENT_VIOLATION | 422 | Consent policy violation |
