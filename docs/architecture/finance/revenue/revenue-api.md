# Revenue API Reference

> Part of [Finance - Revenue Recognition](../finance-revenue.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Contracts

### Create Contract

```http
POST /api/v1/revenue/contracts
Content-Type: application/json
Authorization: Bearer {token}

{
  "customerId": "cust-001",
  "termStart": "2026-02-01",
  "termEnd": "2027-01-31",
  "transactionPrice": 12000.00
}
```

**Response**: `201 Created`
```json
{
  "id": "ctr-001",
  "status": "ACTIVE"
}
```

---

## Revenue Recognition

### Recognize Revenue

```http
POST /api/v1/revenue/recognize
Content-Type: application/json

{
  "contractId": "ctr-001",
  "period": "2026-02"
}
```

**Response**: `200 OK`
```json
{
  "recognized": 1000.00,
  "deferred": 11000.00
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
| CONTRACT_NOT_FOUND | 404 | Contract ID does not exist |
| INVALID_ALLOCATION | 422 | Allocation rules invalid |
| PERIOD_CLOSED | 422 | Cannot recognize in closed period |
