# Procurement Core API Reference

> Part of [Procurement Core](../procurement-core.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Requisitions

### Submit Requisition

```http
POST /api/v1/procurement/requisitions
Content-Type: application/json
Authorization: Bearer {token}

{
  "requesterId": "USR-100",
  "costCenter": "CC-100",
  "lines": [
    { "itemId": "item-001", "quantity": 10, "unitPrice": 25.00 }
  ]
}
```

**Response**: `201 Created`
```json
{
  "requisitionId": "req-001",
  "status": "SUBMITTED"
}
```

---

## Purchase Orders

### Create Purchase Order

```http
POST /api/v1/procurement/purchase-orders
Content-Type: application/json

{
  "supplierId": "vendor-001",
  "lines": [
    { "itemId": "item-001", "quantity": 10, "unitPrice": 25.00 }
  ]
}
```

**Response**: `201 Created`
```json
{
  "poId": "PO-2026-000123",
  "status": "DRAFT"
}
```

---

### Approve Purchase Order

```http
POST /api/v1/procurement/purchase-orders/{poId}/approve
Content-Type: application/json

{
  "approverId": "USR-200"
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
| BUDGET_EXCEEDED | 422 | Requisition exceeds budget |
| APPROVAL_REQUIRED | 422 | Approval not completed |
| SUPPLIER_BLOCKED | 422 | Supplier is blocked |
