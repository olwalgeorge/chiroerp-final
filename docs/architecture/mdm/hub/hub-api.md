# Master Data Hub API Reference

> Part of [Master Data Hub](../mdm-hub.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Create Master Record

```http
POST /api/v1/mdm/masters
Content-Type: application/json
Authorization: Bearer {token}

{
  "domain": "CUSTOMER",
  "attributes": {
    "name": "Acme Medical",
    "taxId": "99-9999999"
  }
}
```

**Response**: `201 Created`
```json
{
  "masterId": "M-100",
  "status": "ACTIVE"
}
```

---

## Submit Change Request

```http
POST /api/v1/mdm/changes
Content-Type: application/json

{
  "masterId": "M-100",
  "changeType": "UPDATE",
  "changes": {
    "address": "100 Main St"
  }
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
| MASTER_NOT_FOUND | 404 | Master record not found |
| CHANGE_REQUEST_NOT_FOUND | 404 | Change request not found |
| APPROVAL_REQUIRED | 409 | Approval required |
| DOMAIN_INVALID | 422 | Master domain invalid |
