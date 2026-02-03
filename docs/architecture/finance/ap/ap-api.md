# AP API Reference

> Part of [Finance - Accounts Payable](../finance-ap.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Vendor Invoices

### Create Vendor Invoice

```http
POST /api/v1/ap/invoices
Content-Type: application/json
Authorization: Bearer {token}

{
  "vendorId": "550e8400-e29b-41d4-a716-446655440000",
  "invoiceNumber": "INV-2026-001234",
  "invoiceDate": "2026-02-01",
  "dueDate": "2026-03-03",
  "currency": "USD",
  "lines": [
    {
      "description": "Office Supplies",
      "quantity": 10,
      "unitPrice": 25.00,
      "accountId": "6100-00",
      "costCenter": "CC-100",
      "taxCode": "TX-STD"
    }
  ],
  "poReferences": ["PO-2026-000123"]
}
```

**Response**: `201 Created`
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "invoiceNumber": "INV-2026-001234",
  "status": "DRAFT",
  "matchStatus": "UNMATCHED",
  "totalAmount": 267.50,
  "taxAmount": 17.50,
  "netPayable": 267.50
}
```

---

### Get Vendor Invoice

```http
GET /api/v1/ap/invoices/{invoiceId}
```

**Response**: `200 OK`
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "invoiceNumber": "INV-2026-001234",
  "vendorId": "550e8400-e29b-41d4-a716-446655440000",
  "vendorName": "Acme Supplies Inc.",
  "type": "STANDARD",
  "status": "POSTED",
  "matchStatus": "FULL_MATCH",
  "invoiceDate": "2026-02-01",
  "dueDate": "2026-03-03",
  "postingDate": "2026-02-02",
  "totalAmount": 267.50,
  "taxAmount": 17.50,
  "withholdingAmount": 0.00,
  "netPayable": 267.50,
  "openBalance": 267.50,
  "currency": "USD",
  "lines": [
    {
      "id": "line-001",
      "lineNumber": 1,
      "description": "Office Supplies",
      "quantity": 10,
      "unitPrice": 25.00,
      "lineTotal": 250.00,
      "accountId": "6100-00",
      "accountName": "Office Supplies Expense",
      "costCenter": "CC-100",
      "taxCode": "TX-STD",
      "taxAmount": 17.50
    }
  ],
  "poReferences": ["PO-2026-000123"],
  "grnReferences": ["GRN-2026-000456"]
}
```

---

### List Vendor Invoices

```http
GET /api/v1/ap/invoices?vendorId={vendorId}&status={status}&fromDate={date}&toDate={date}&page=0&size=20
```

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| vendorId | UUID | Filter by vendor |
| status | String | DRAFT, PENDING_MATCH, MATCHED, POSTED, PAID, CANCELLED |
| matchStatus | String | UNMATCHED, PARTIAL_MATCH, FULL_MATCH, EXCEPTION |
| fromDate | Date | Invoice date from |
| toDate | Date | Invoice date to |
| page | Integer | Page number (0-based) |
| size | Integer | Page size (default 20, max 100) |

---

### Post Vendor Invoice

```http
POST /api/v1/ap/invoices/{invoiceId}/post
Content-Type: application/json

{
  "postingDate": "2026-02-02",
  "overrideMatchCheck": false
}
```

**Response**: `200 OK`
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "status": "POSTED",
  "postingDate": "2026-02-02",
  "glJournalId": "JE-2026-000789"
}
```

**Error Response**: `422 Unprocessable Entity`
```json
{
  "error": "INVOICE_NOT_MATCHED",
  "message": "Invoice requires 3-way match before posting",
  "matchStatus": "UNMATCHED"
}
```

---

### Perform 3-Way Match

```http
POST /api/v1/ap/invoices/{invoiceId}/match
Content-Type: application/json

{
  "poId": "PO-2026-000123",
  "grnId": "GRN-2026-000456"
}
```

**Response**: `200 OK`
```json
{
  "invoiceId": "660e8400-e29b-41d4-a716-446655440001",
  "matchStatus": "FULL_MATCH",
  "matchResult": {
    "priceVariance": 0.00,
    "quantityVariance": 0,
    "withinTolerance": true
  }
}
```

**With Variance Exception**: `200 OK`
```json
{
  "invoiceId": "660e8400-e29b-41d4-a716-446655440001",
  "matchStatus": "EXCEPTION",
  "matchResult": {
    "priceVariance": 15.00,
    "quantityVariance": 0,
    "withinTolerance": false
  },
  "exception": {
    "id": "exc-001",
    "type": "PRICE_VARIANCE",
    "varianceAmount": 15.00,
    "status": "PENDING_RESOLUTION"
  }
}
```

---

## Payments

### Record Payment

```http
POST /api/v1/ap/payments
Content-Type: application/json

{
  "vendorId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 267.50,
  "currency": "USD",
  "paymentMethod": "ACH",
  "bankReference": "ACH-2026-001234",
  "allocations": [
    {
      "invoiceId": "660e8400-e29b-41d4-a716-446655440001",
      "amount": 267.50
    }
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "paymentNumber": "PMT-2026-000567",
  "status": "PENDING",
  "amount": 267.50,
  "paymentDate": "2026-02-15"
}
```

---

### Apply Payment to Invoices

```http
POST /api/v1/ap/payments/{paymentId}/apply
Content-Type: application/json

{
  "allocations": [
    { "invoiceId": "invoice-001", "amount": 100.00 },
    { "invoiceId": "invoice-002", "amount": 167.50 }
  ]
}
```

---

### Reverse Payment

```http
POST /api/v1/ap/payments/{paymentId}/reverse
Content-Type: application/json

{
  "reason": "Duplicate payment",
  "reversalDate": "2026-02-16"
}
```

---

## Payment Runs

### Create Payment Run

```http
POST /api/v1/ap/payment-runs
Content-Type: application/json

{
  "runDate": "2026-02-15",
  "paymentMethod": "ACH",
  "vendorIds": ["vendor-001", "vendor-002"],
  "maxDueDate": "2026-02-20",
  "minAmount": 100.00
}
```

---

### Generate Payment Proposals

```http
POST /api/v1/ap/payment-runs/{paymentRunId}/proposals
```

**Response**: `200 OK`
```json
{
  "paymentRunId": "run-001",
  "status": "PROPOSED",
  "proposals": [
    {
      "vendorId": "vendor-001",
      "vendorName": "Acme Supplies",
      "invoiceCount": 3,
      "totalAmount": 1500.00,
      "invoices": [
        { "invoiceId": "inv-001", "amount": 500.00, "dueDate": "2026-02-15" },
        { "invoiceId": "inv-002", "amount": 750.00, "dueDate": "2026-02-18" },
        { "invoiceId": "inv-003", "amount": 250.00, "dueDate": "2026-02-20" }
      ]
    }
  ],
  "summary": {
    "totalAmount": 5000.00,
    "vendorCount": 5,
    "invoiceCount": 15
  }
}
```

---

### Approve Payment Run

```http
POST /api/v1/ap/payment-runs/{paymentRunId}/approve
```

**Response**: `200 OK`
```json
{
  "paymentRunId": "run-001",
  "status": "APPROVED",
  "approvedBy": "user-001",
  "approvedAt": "2026-02-14T10:30:00Z"
}
```

---

### Execute Payment Run

```http
POST /api/v1/ap/payment-runs/{paymentRunId}/execute
```

**Response**: `202 Accepted`
```json
{
  "paymentRunId": "run-001",
  "status": "EXECUTING",
  "batchCount": 2,
  "batches": [
    { "batchId": "batch-001", "paymentMethod": "ACH", "amount": 4500.00 },
    { "batchId": "batch-002", "paymentMethod": "WIRE", "amount": 500.00 }
  ]
}
```

---

## Match Exceptions

### List Match Exceptions

```http
GET /api/v1/ap/matching/exceptions?status=PENDING&vendorId={vendorId}
```

---

### Resolve Match Exception

```http
POST /api/v1/ap/matching/exceptions/{exceptionId}/resolve
Content-Type: application/json

{
  "resolution": "ACCEPT",
  "comment": "Price increase approved by procurement"
}
```

**Resolution Options**:
| Resolution | Description |
|------------|-------------|
| ACCEPT | Accept variance and allow posting |
| REJECT | Reject invoice, return to vendor |
| ADJUST | Adjust invoice amount to match PO |

---

## Reports

### AP Aging Report

```http
GET /api/v1/ap/reports/aging?asOfDate=2026-02-01&vendorId={vendorId}
```

**Response**: `200 OK`
```json
{
  "asOfDate": "2026-02-01",
  "totalBalance": 125000.00,
  "buckets": {
    "current": 45000.00,
    "1-30": 35000.00,
    "31-60": 25000.00,
    "61-90": 15000.00,
    "over90": 5000.00
  },
  "vendorDetails": [
    {
      "vendorId": "vendor-001",
      "vendorName": "Acme Supplies",
      "totalBalance": 5000.00,
      "current": 3000.00,
      "1-30": 2000.00,
      "31-60": 0.00,
      "61-90": 0.00,
      "over90": 0.00
    }
  ]
}
```

---

### Cash Requirements Forecast

```http
GET /api/v1/ap/reports/cash-requirements?fromDate=2026-02-01&toDate=2026-02-28
```

**Response**: `200 OK`
```json
{
  "fromDate": "2026-02-01",
  "toDate": "2026-02-28",
  "totalRequired": 85000.00,
  "byDate": [
    { "date": "2026-02-05", "amount": 15000.00 },
    { "date": "2026-02-10", "amount": 25000.00 },
    { "date": "2026-02-15", "amount": 30000.00 },
    { "date": "2026-02-20", "amount": 15000.00 }
  ],
  "byVendor": [
    { "vendorId": "vendor-001", "vendorName": "Acme", "amount": 25000.00 },
    { "vendorId": "vendor-002", "vendorName": "Beta Corp", "amount": 35000.00 }
  ]
}
```

---

### Vendor Statement

```http
GET /api/v1/ap/reports/vendor-statement?vendorId={vendorId}&fromDate=2026-01-01&toDate=2026-02-28
```

---

### Days Payable Outstanding (DPO)

```http
GET /api/v1/ap/reports/dpo?period=2026-Q1
```

**Response**: `200 OK`
```json
{
  "period": "2026-Q1",
  "dpo": 45.2,
  "trend": [
    { "period": "2025-Q4", "dpo": 42.5 },
    { "period": "2025-Q3", "dpo": 40.1 },
    { "period": "2025-Q2", "dpo": 38.8 }
  ],
  "benchmark": 45.0
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
| INVOICE_NOT_FOUND | 404 | Invoice ID does not exist |
| VENDOR_BLOCKED | 422 | Vendor is blocked for payments |
| INVOICE_NOT_MATCHED | 422 | Invoice requires matching before posting |
| TOLERANCE_EXCEEDED | 422 | Match variance exceeds tolerance |
| DUPLICATE_INVOICE | 409 | Invoice number already exists for vendor |
| PERIOD_CLOSED | 422 | Cannot post to closed period |
| PAYMENT_EXCEEDS_BALANCE | 422 | Payment amount > open balance |
| PAYMENT_RUN_ALREADY_EXECUTED | 422 | Cannot modify executed payment run |
| INSUFFICIENT_FUNDS | 422 | Bank account has insufficient funds |
