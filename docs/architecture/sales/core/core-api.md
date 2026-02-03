# Sales Core API Reference

> Part of [Sales Core](../sales-core.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Sales Orders

### Create Sales Order

```http
POST /api/v1/sales/orders
Content-Type: application/json
Authorization: Bearer {token}

{
  "customerId": "CUST-100",
  "channel": "ECOM",
  "currency": "USD",
  "lines": [
    { "sku": "SKU-1001", "quantity": 2, "uom": "EA" }
  ],
  "pricingContext": {
    "priceListId": "PL-STD",
    "promotionCodes": ["WELCOME10"]
  }
}
```

**Response**: `201 Created`
```json
{
  "orderId": "SO-10002",
  "status": "DRAFT",
  "totalAmount": 180.00,
  "currency": "USD"
}
```

---

### Submit Sales Order

```http
POST /api/v1/sales/orders/{orderId}/submit
```

---

### Approve Sales Order

```http
POST /api/v1/sales/orders/{orderId}/approve
Content-Type: application/json

{
  "approverId": "USR-200",
  "reason": "Pricing override within threshold"
}
```

---

### Allocate Sales Order

```http
POST /api/v1/sales/orders/{orderId}/allocate
```

---

### Cancel Sales Order

```http
POST /api/v1/sales/orders/{orderId}/cancel
Content-Type: application/json

{
  "reason": "Customer requested cancellation"
}
```

---

## Quotes

### Create Quote

```http
POST /api/v1/sales/quotes
Content-Type: application/json

{
  "customerId": "CUST-100",
  "currency": "USD",
  "lines": [
    { "sku": "SKU-1001", "quantity": 2, "uom": "EA" }
  ],
  "expiryDate": "2026-02-28"
}
```

---

### Convert Quote to Order

```http
POST /api/v1/sales/quotes/{quoteId}/convert
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
| ORDER_NOT_FOUND | 404 | Sales order ID does not exist |
| INVALID_ORDER_STATE | 409 | Order state does not allow this action |
| CREDIT_HOLD | 422 | Credit hold prevents submission/allocations |
| PRICING_EXPIRED | 422 | Pricing snapshot is expired |
| ALLOCATION_FAILED | 409 | Allocation could not be completed |
| TAX_CALCULATION_FAILED | 502 | Tax engine failure |
