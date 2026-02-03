# CRM Contracts API Reference

> Part of [CRM Contracts](../crm-contracts.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Contracts

```http
POST /api/v1/crm/contracts
Content-Type: application/json

{
  "customerId": "CUST-100",
  "planId": "PLAN-1",
  "startDate": "2026-02-01",
  "endDate": "2027-01-31"
}
```

```http
POST /api/v1/crm/contracts/{contractId}/activate
```

```http
POST /api/v1/crm/contracts/{contractId}/renew
Content-Type: application/json

{
  "termMonths": 12,
  "autoRenew": true
}
```

---

## Entitlements

```http
POST /api/v1/crm/contracts/{contractId}/entitlements/consume
Content-Type: application/json

{
  "entitlementId": "ENT-100",
  "units": 1
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
| CONTRACT_NOT_FOUND | 404 | Contract not found |
| CONTRACT_NOT_ACTIVE | 409 | Contract not active |
| ENTITLEMENT_EXCEEDED | 409 | Entitlement balance exceeded |
| RENEWAL_WINDOW_CLOSED | 423 | Renewal window closed |
