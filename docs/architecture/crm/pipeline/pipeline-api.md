# CRM Pipeline API Reference

> Part of [CRM Pipeline](../crm-pipeline.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Opportunities

```http
POST /api/v1/crm/opportunities
Content-Type: application/json

{
  "customerId": "CUST-100",
  "name": "Renewal FY26",
  "amount": 120000.00,
  "currency": "USD",
  "expectedCloseDate": "2026-03-31"
}
```

```http
POST /api/v1/crm/opportunities/{opportunityId}/stage
Content-Type: application/json

{
  "stage": "NEGOTIATION"
}
```

```http
POST /api/v1/crm/opportunities/{opportunityId}/close
Content-Type: application/json

{
  "outcome": "WON",
  "reason": "BEST_VALUE"
}
```

---

## Forecasts

```http
POST /api/v1/crm/forecasts/snapshot
Content-Type: application/json

{
  "period": "2026-Q1",
  "ownerId": "USER-200"
}
```

```http
GET /api/v1/crm/forecasts/{forecastId}
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
| OPPORTUNITY_NOT_FOUND | 404 | Opportunity not found |
| INVALID_STAGE_TRANSITION | 409 | Stage transition not allowed |
| FORECAST_LOCKED | 423 | Forecast snapshot locked |
| VALIDATION_ERROR | 422 | Request validation failed |
