# Quality Execution API Reference

> Part of [Quality Execution](../quality-execution.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Create Inspection Lot

```http
POST /api/v1/quality/inspection-lots
Content-Type: application/json
Authorization: Bearer {token}

{
  "sourceType": "INCOMING",
  "referenceId": "GR-1001",
  "itemId": "ITEM-100",
  "quantity": 50,
  "lotNumber": "LOT-2026-01"
}
```

**Response**: `201 Created`
```json
{
  "inspectionLotId": "LOT-900",
  "status": "CREATED"
}
```

---

## Record Result

```http
POST /api/v1/quality/inspection-lots/LOT-900/results
Content-Type: application/json

{
  "characteristicId": "CH-01",
  "value": 9.8
}
```

---

## Usage Decision

```http
POST /api/v1/quality/inspection-lots/LOT-900/decision
Content-Type: application/json

{
  "decisionType": "ACCEPT",
  "reason": "Within spec"
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
| LOT_NOT_FOUND | 404 | Inspection lot not found |
| RESULT_OUT_OF_RANGE | 422 | Result outside spec limits |
| DECISION_REQUIRED | 409 | Results required before decision |
| STOCK_BLOCK_FAILED | 500 | Inventory block failed |
