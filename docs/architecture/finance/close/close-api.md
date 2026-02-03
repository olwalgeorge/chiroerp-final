# Close API Reference

> Part of [Finance - Period Close](../finance-close.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Close Runs

### Start Close Run

```http
POST /api/v1/close/runs
Content-Type: application/json

{
  "ledgerId": "ledger-001",
  "periodId": "per-2026-02"
}
```

**Response**: `201 Created`
```json
{
  "runId": "close-001",
  "status": "IN_PROGRESS"
}
```

---

### Finalize Close Run

```http
POST /api/v1/close/runs/{runId}/finalize
Content-Type: application/json
```

**Response**: `200 OK`
```json
{
  "runId": "close-001",
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
| CLOSE_RUN_NOT_FOUND | 404 | Close run does not exist |
| TASK_DEPENDENCY | 422 | Task dependency not satisfied |
| PERIOD_CLOSED | 422 | Period already closed |
