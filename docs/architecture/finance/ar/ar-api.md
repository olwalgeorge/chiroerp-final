# AR API Reference

> Part of [Finance - Accounts Receivable](../finance-ar.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Invoices

### Create Invoice

```http
POST /api/v1/ar/invoices
Content-Type: application/json
Authorization: Bearer {token}

{
  "customerId": "cst-1001",
  "invoiceNumber": "INV-2026-004210",
  "invoiceDate": "2026-02-01",
  "dueDate": "2026-03-03",
  "currency": "USD",
  "lines": [
    {
      "description": "Subscription - February",
      "quantity": 1,
      "unitPrice": 1200.00,
      "accountId": "4000-00",
      "taxCode": "TX-STD"
    }
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": "inv-001",
  "invoiceNumber": "INV-2026-004210",
  "status": "DRAFT",
  "totalAmount": 1200.00,
  "taxAmount": 0.00,
  "openBalance": 1200.00,
  "currency": "USD"
}
```

---

### Get Invoice

```http
GET /api/v1/ar/invoices/{invoiceId}
```

**Response**: `200 OK`
```json
{
  "id": "inv-001",
  "invoiceNumber": "INV-2026-004210",
  "customerId": "cst-1001",
  "customerName": "Northwind Clinics",
  "status": "POSTED",
  "invoiceDate": "2026-02-01",
  "dueDate": "2026-03-03",
  "postingDate": "2026-02-02",
  "totalAmount": 1200.00,
  "taxAmount": 0.00,
  "openBalance": 1200.00,
  "currency": "USD",
  "lines": [
    {
      "id": "line-001",
      "lineNumber": 1,
      "description": "Subscription - February",
      "quantity": 1,
      "unitPrice": 1200.00,
      "lineTotal": 1200.00,
      "accountId": "4000-00",
      "taxCode": "TX-STD",
      "taxAmount": 0.00
    }
  ]
}
```

---

### List Invoices

```http
GET /api/v1/ar/invoices?customerId={customerId}&status={status}&fromDate={date}&toDate={date}&page=0&size=20
```

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| customerId | String | Filter by customer |
| status | String | DRAFT, POSTED, PARTIAL_PAID, PAID, CANCELLED |
| fromDate | Date | Invoice date from |
| toDate | Date | Invoice date to |
| page | Integer | Page number (0-based) |
| size | Integer | Page size (default 20, max 100) |

---

### Post Invoice

```http
POST /api/v1/ar/invoices/{invoiceId}/post
Content-Type: application/json

{
  "postingDate": "2026-02-02"
}
```

**Response**: `200 OK`
```json
{
  "id": "inv-001",
  "status": "POSTED",
  "postingDate": "2026-02-02",
  "glJournalId": "JE-2026-001122"
}
```

---

### Cancel Invoice

```http
POST /api/v1/ar/invoices/{invoiceId}/cancel
Content-Type: application/json

{
  "reason": "Duplicate invoice"
}
```

**Response**: `200 OK`
```json
{
  "id": "inv-001",
  "status": "CANCELLED",
  "cancelledAt": "2026-02-03T09:12:00Z"
}
```

---

## Payments

### Record Payment

```http
POST /api/v1/ar/payments
Content-Type: application/json

{
  "customerId": "cst-1001",
  "amount": 1200.00,
  "currency": "USD",
  "paymentMethod": "ACH",
  "bankReference": "ACH-2026-8831",
  "allocations": [
    { "invoiceId": "inv-001", "amount": 1200.00 }
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": "pay-001",
  "paymentNumber": "PMT-2026-000210",
  "status": "APPLIED",
  "amount": 1200.00,
  "currency": "USD"
}
```

---

### Apply Payment

```http
POST /api/v1/ar/payments/{paymentId}/apply
Content-Type: application/json

{
  "allocations": [
    { "invoiceId": "inv-001", "amount": 1200.00 }
  ]
}
```

**Response**: `200 OK`
```json
{
  "paymentId": "pay-001",
  "status": "APPLIED",
  "appliedAt": "2026-02-04T11:30:00Z"
}
```

---

### Reverse Payment

```http
POST /api/v1/ar/payments/{paymentId}/reverse
Content-Type: application/json

{
  "reason": "Chargeback"
}
```

**Response**: `200 OK`
```json
{
  "paymentId": "pay-001",
  "status": "REVERSED",
  "reversedAt": "2026-02-05T10:00:00Z"
}
```

---

## Credit & Dunning

### Issue Credit Memo

```http
POST /api/v1/ar/credit-memos
Content-Type: application/json

{
  "invoiceId": "inv-001",
  "amount": 150.00,
  "reason": "Pricing adjustment"
}
```

**Response**: `201 Created`
```json
{
  "creditMemoId": "cm-001",
  "status": "ISSUED",
  "amount": 150.00,
  "currency": "USD"
}
```

---

### Run Dunning

```http
POST /api/v1/ar/dunning-runs
Content-Type: application/json

{
  "asOfDate": "2026-02-28",
  "dunningLevel": "REMINDER"
}
```

**Response**: `202 Accepted`
```json
{
  "dunningRunId": "dr-001",
  "status": "IN_PROGRESS",
  "customerCount": 420
}
```

---

## Reports

### Aging Report

```http
GET /api/v1/ar/reports/aging?asOfDate=2026-02-28&customerId={customerId}
```

### Customer Statement

```http
GET /api/v1/ar/reports/customer-statement?customerId={customerId}&fromDate=2026-01-01&toDate=2026-02-28
```

### DSO Metrics

```http
GET /api/v1/ar/reports/dso?period=2026-Q1
```

### Cash Forecast

```http
GET /api/v1/ar/reports/cash-forecast?fromDate=2026-03-01&toDate=2026-03-31
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
| INVOICE_NOT_FOUND | 404 | Invoice ID does not exist |
| PAYMENT_NOT_FOUND | 404 | Payment ID does not exist |
| DUPLICATE_INVOICE | 409 | Invoice number already exists for customer |
| CUSTOMER_BLOCKED | 422 | Customer is blocked for billing |
| CREDIT_LIMIT_EXCEEDED | 422 | Customer credit limit exceeded |
| PAYMENT_EXCEEDS_BALANCE | 422 | Payment amount > open balance |
| INVALID_PAYMENT_ALLOCATION | 422 | Allocation totals do not match payment |
| PERIOD_CLOSED | 422 | Cannot post to closed period |
| DUNNING_NOT_ALLOWED | 422 | Dunning cannot run for current status |
