# Intercompany API Reference

> Part of [Finance - Intercompany](../finance-intercompany.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Transactions

### Create Intercompany Transaction

```http
POST /api/v1/intercompany/transactions
Content-Type: application/json

{
  "agreementId": "agr-001",
  "type": "SERVICE",
  "amount": 5000.00,
  "currency": "USD"
}
```

**Response**: `201 Created`
```json
{
  "id": "ic-001",
  "status": "DRAFT"
}
```

---

## Netting

### Run Netting

```http
POST /api/v1/intercompany/netting/run
Content-Type: application/json

{
  "period": "2026-02",
  "counterparties": ["ENT-001", "ENT-002"]
}
```

**Response**: `201 Created`
```json
{
  "batchId": "net-001",
  "status": "COMPLETED"
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
| AGREEMENT_NOT_FOUND | 404 | Agreement ID does not exist |
| COUNTERPARTY_MISMATCH | 422 | Counterparty legs do not balance |
| PERIOD_CLOSED | 422 | Cannot post to closed period |
