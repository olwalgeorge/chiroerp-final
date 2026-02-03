# Inventory Valuation API Reference

> Part of [Inventory Valuation & Costing](../inventory-valuation.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Valuation Runs

### Run Valuation

```http
POST /api/v1/inventory/valuation/runs
Content-Type: application/json
Authorization: Bearer {token}

{
  "period": "2026-02",
  "method": "WAC",
  "includeLandedCost": true
}
```

**Response**: `202 Accepted`
```json
{
  "runId": "val-001",
  "status": "RUNNING"
}
```

---

### Post Valuation

```http
POST /api/v1/inventory/valuation/runs/{runId}/post
Content-Type: application/json

{
  "postingDate": "2026-02-28"
}
```

**Response**: `200 OK`
```json
{
  "runId": "val-001",
  "status": "POSTED",
  "glJournalId": "JE-2026-009001"
}
```

---

## Landed Cost

### Create Landed Cost

```http
POST /api/v1/inventory/valuation/landed-costs
Content-Type: application/json

{
  "sourceDocument": "PO-2026-000123",
  "allocationRule": "BY_VALUE",
  "charges": [
    { "type": "FREIGHT", "amount": 250.00 },
    { "type": "DUTY", "amount": 75.00 }
  ]
}
```

**Response**: `201 Created`
```json
{
  "landedCostId": "lc-001",
  "totalCharge": 325.00,
  "status": "CREATED"
}
```

---

### Allocate Landed Cost

```http
POST /api/v1/inventory/valuation/landed-costs/{landedCostId}/allocate
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
| VALUATION_RUN_IN_PROGRESS | 409 | Another valuation run is active |
| PERIOD_CLOSED | 422 | Cannot post to closed period |
| INVALID_COST_METHOD | 422 | Unsupported valuation method |
| LANDED_COST_NOT_FOUND | 404 | Landed cost ID does not exist |
