# Sales Pricing API Reference

> Part of [Sales Pricing & Promotions](../sales-pricing.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Calculate Price

```http
POST /api/v1/sales/pricing/calculate
Content-Type: application/json
Authorization: Bearer {token}

{
  "customerId": "CUST-100",
  "channel": "ECOM",
  "currency": "USD",
  "items": [
    { "sku": "SKU-1001", "quantity": 2 }
  ],
  "promotionCodes": ["WELCOME10"]
}
```

**Response**: `200 OK`
```json
{
  "totalAmount": 180.00,
  "currency": "USD",
  "breakdown": {
    "subtotal": 200.00,
    "discount": 20.00
  }
}
```

---

## Price Lists

### Create Price List

```http
POST /api/v1/sales/price-lists
Content-Type: application/json

{
  "name": "Standard USD",
  "currency": "USD",
  "items": [
    { "sku": "SKU-1001", "price": 100.00 }
  ]
}
```

---

### Get Price List

```http
GET /api/v1/sales/price-lists/{priceListId}
```

---

## Promotions

### Create Promotion

```http
POST /api/v1/sales/promotions
Content-Type: application/json

{
  "code": "WELCOME10",
  "type": "PERCENTAGE",
  "rules": [
    { "minQty": 1, "discountPercent": 10 }
  ],
  "effectiveDates": {
    "from": "2026-02-01",
    "to": "2026-03-01"
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
| PRICE_LIST_NOT_FOUND | 404 | Price list does not exist |
| PROMOTION_EXPIRED | 422 | Promotion is expired or inactive |
| INVALID_CURRENCY | 422 | Currency does not match price list |
| OVERRIDE_NOT_AUTHORIZED | 403 | Override requires approval |
| PRICING_CONFIGURATION_ERROR | 500 | Pricing rules are misconfigured |
