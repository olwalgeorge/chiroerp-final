# Controlling API Reference

> Part of [Finance - Controlling](../finance-controlling.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Cost Centers

### Create Cost Center

```http
POST /api/v1/controlling/cost-centers
Content-Type: application/json
Authorization: Bearer {token}

{
  "code": "CC-100",
  "name": "Operations",
  "managerId": "user-001"
}
```

**Response**: `201 Created`
```json
{
  "id": "cc-100",
  "status": "ACTIVE"
}
```

---

## Allocations

### Run Allocation

```http
POST /api/v1/controlling/allocations/run
Content-Type: application/json

{
  "cycleId": "cycle-001",
  "period": "2026-02",
  "postingDate": "2026-02-28"
}
```

**Response**: `201 Created`
```json
{
  "runId": "alloc-001",
  "status": "POSTED",
  "totalAllocated": 25000.00
}
```

---

## Reporting

### Cost Center Report

```http
GET /api/v1/controlling/reports/cost-center?costCenterId=cc-100&period=2026-02
```

**Response**: `200 OK`
```json
{
  "costCenterId": "cc-100",
  "period": "2026-02",
  "actual": 120000.00,
  "plan": 110000.00,
  "variance": 10000.00
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
| COST_CENTER_NOT_FOUND | 404 | Cost center ID does not exist |
| ALLOCATION_RULE_INVALID | 422 | Allocation rule is invalid |
| PERIOD_CLOSED | 422 | Cannot post in closed period |
| BUDGET_EXCEEDED | 422 | Budget control violation |
