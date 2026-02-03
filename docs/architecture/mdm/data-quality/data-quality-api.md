# Master Data Quality API Reference

> Part of [Master Data Quality Rules](../mdm-data-quality.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Create Validation Rule

```http
POST /api/v1/mdm/quality/rules
Content-Type: application/json
Authorization: Bearer {token}

{
  "domain": "CUSTOMER",
  "ruleType": "COMPLETENESS",
  "expression": "address != null"
}
```

**Response**: `201 Created`
```json
{
  "ruleId": "RULE-10",
  "status": "ACTIVE"
}
```

---

## Get Data Quality Score

```http
GET /api/v1/mdm/quality/scores/M-100
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
| RULE_NOT_FOUND | 404 | Validation rule not found |
| INVALID_RULE | 422 | Rule expression invalid |
| DOMAIN_NOT_SUPPORTED | 422 | Domain not supported |
