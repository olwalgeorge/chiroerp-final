# Lease API Reference

> Part of [Finance - Lease Accounting](../finance-lease.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Lease Contracts

### Create Lease

```http
POST /api/v1/lease/contracts
Content-Type: application/json

{
  "vendorId": "vendor-001",
  "termStart": "2026-02-01",
  "termEnd": "2029-01-31",
  "paymentAmount": 5000.00,
  "paymentFrequency": "MONTHLY"
}
```

**Response**: `201 Created`
```json
{
  "id": "lease-001",
  "status": "DRAFT"
}
```

---

### Activate Lease

```http
POST /api/v1/lease/contracts/{leaseId}/activate
Content-Type: application/json
```

**Response**: `200 OK`
```json
{
  "id": "lease-001",
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
  "timestamp": "2026-02-01T10:30:00Z",
  "traceId": "abc123"
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| LEASE_NOT_FOUND | 404 | Lease ID does not exist |
| INVALID_LEASE_TERM | 422 | Lease term invalid |
| PERIOD_CLOSED | 422 | Cannot post to closed period |
