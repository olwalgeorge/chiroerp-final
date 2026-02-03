# Procurement Sourcing API Reference

> Part of [Procurement Sourcing & RFQ](../procurement-sourcing.md)

## Base URL

```
https://api.chiroerp.com
```

---

## RFQs

### Issue RFQ

```http
POST /api/v1/procurement/rfqs
Content-Type: application/json
Authorization: Bearer {token}

{
  "title": "Medical Supplies Q1",
  "suppliers": ["vendor-001", "vendor-002"],
  "dueDate": "2026-02-20",
  "lines": [
    { "itemId": "item-001", "quantity": 100 }
  ]
}
```

---

## Quotes

### Submit Quote

```http
POST /api/v1/procurement/rfqs/{rfqId}/quotes
Content-Type: application/json

{
  "supplierId": "vendor-001",
  "lines": [
    { "itemId": "item-001", "unitPrice": 2.50, "leadTimeDays": 7 }
  ]
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
| RFQ_CLOSED | 422 | RFQ is closed for submissions |
| QUOTE_LATE | 422 | Quote submitted after deadline |
| SUPPLIER_BLOCKED | 422 | Supplier is blocked |
