# GL API Reference

> Part of [Finance - General Ledger](../finance-gl.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Ledgers

### Create Ledger

```http
POST /api/v1/gl/ledgers
Content-Type: application/json
Authorization: Bearer {token}

{
  "ledgerCode": "PRIMARY",
  "name": "Primary Ledger",
  "baseCurrency": "USD",
  "fiscalCalendar": "CAL-2026"
}
```

**Response**: `201 Created`
```json
{
  "id": "ledger-001",
  "ledgerCode": "PRIMARY",
  "status": "ACTIVE",
  "baseCurrency": "USD"
}
```

---

## Chart of Accounts

### Create Chart of Accounts

```http
POST /api/v1/gl/chart-of-accounts
Content-Type: application/json

{
  "ledgerId": "ledger-001",
  "name": "Corporate COA"
}
```

**Response**: `201 Created`
```json
{
  "id": "coa-001",
  "name": "Corporate COA"
}
```

### Add Account

```http
POST /api/v1/gl/chart-of-accounts/{coaId}/accounts
Content-Type: application/json

{
  "accountNumber": "1200-00",
  "name": "Accounts Receivable",
  "type": "ASSET",
  "postingRule": "BALANCE_SHEET",
  "parentAccountId": null
}
```

**Response**: `201 Created`
```json
{
  "id": "acct-1200",
  "accountNumber": "1200-00",
  "status": "ACTIVE"
}
```

---

## Journal Entries

### Create Journal Entry

```http
POST /api/v1/gl/journal-entries
Content-Type: application/json

{
  "ledgerId": "ledger-001",
  "postingDate": "2026-02-01",
  "description": "AR Invoice INV-2026-001",
  "lines": [
    { "accountId": "acct-1200", "debit": 1000.00, "credit": 0.00 },
    { "accountId": "acct-4000", "debit": 0.00, "credit": 900.00 },
    { "accountId": "acct-2100", "debit": 0.00, "credit": 100.00 }
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": "je-001",
  "status": "DRAFT",
  "totalDebits": 1000.00,
  "totalCredits": 1000.00
}
```

### Post Journal Entry

```http
POST /api/v1/gl/journal-entries/{journalEntryId}/post
Content-Type: application/json
```

**Response**: `200 OK`
```json
{
  "id": "je-001",
  "status": "POSTED",
  "postingDate": "2026-02-01"
}
```

---

## Periods

### Close Accounting Period

```http
POST /api/v1/gl/periods/{periodId}/close
Content-Type: application/json

{
  "closeType": "HARD"
}
```

**Response**: `200 OK`
```json
{
  "periodId": "per-2026-02",
  "status": "CLOSED",
  "closedAt": "2026-03-02T02:00:00Z"
}
```

---

## Reporting

### Trial Balance

```http
GET /api/v1/gl/trial-balance?ledgerId=ledger-001&asOfDate=2026-02-28
```

**Response**: `200 OK`
```json
{
  "asOfDate": "2026-02-28",
  "totalDebits": 500000.00,
  "totalCredits": 500000.00,
  "lines": [
    { "accountNumber": "1000-00", "debit": 250000.00, "credit": 0.00 },
    { "accountNumber": "2000-00", "debit": 0.00, "credit": 250000.00 }
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
  "timestamp": "2026-02-01T10:30:00Z",
  "traceId": "abc123"
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| LEDGER_NOT_FOUND | 404 | Ledger ID does not exist |
| ACCOUNT_NOT_FOUND | 404 | Account ID does not exist |
| ACCOUNT_INACTIVE | 422 | Account is inactive or blocked |
| UNBALANCED_JOURNAL_ENTRY | 422 | Debits do not equal credits |
| PERIOD_CLOSED | 422 | Cannot post to closed period |
| INVALID_POSTING_DATE | 422 | Posting date outside open period |
| JOURNAL_ENTRY_NOT_FOUND | 404 | Journal entry not found |
