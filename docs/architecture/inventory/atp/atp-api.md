# Inventory ATP API Reference

> Part of [Inventory ATP & Allocation](../inventory-atp.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Availability

### Get Availability

```http
GET /api/v1/inventory/atp/availability?itemId={itemId}&channel={channel}&promiseDate=2026-03-01
```

**Response**: `200 OK`
```json
{
  "itemId": "item-001",
  "channel": "ECOM",
  "available": 120,
  "reserved": 30,
  "promiseDate": "2026-03-01"
}
```

---

## Allocations

### Commit Allocation

```http
POST /api/v1/inventory/atp/allocations
Content-Type: application/json

{
  "itemId": "item-001",
  "channel": "ECOM",
  "quantity": 5,
  "reference": "SO-10002"
}
```

**Response**: `201 Created`
```json
{
  "allocationId": "alloc-001",
  "status": "COMMITTED"
}
```

---

### Release Allocation

```http
POST /api/v1/inventory/atp/allocations/{allocationId}/release
Content-Type: application/json

{
  "reason": "Order cancelled"
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
| ALLOCATION_EXCEEDED | 422 | Allocation exceeds available stock |
| SAFETY_STOCK_VIOLATION | 422 | Safety stock cannot be breached |
| INVALID_PRIORITY_RULE | 422 | Priority rule is invalid |
