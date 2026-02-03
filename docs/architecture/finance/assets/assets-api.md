# Assets API Reference

> Part of [Finance - Fixed Assets](../finance-assets.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Assets

### Create Asset

```http
POST /api/v1/assets
Content-Type: application/json
Authorization: Bearer {token}

{
  "classId": "c8ad1c2e-8f1a-4f0c-9a07-9f8ed3155a22",
  "description": "X-Ray Machine",
  "acquisitionDate": "2026-02-01",
  "cost": 25000.00,
  "currency": "USD",
  "locationId": "LOC-100",
  "custodianId": "USER-900"
}
```

**Response**: `201 Created`
```json
{
  "id": "asset-001",
  "assetNumber": "FA-2026-000123",
  "status": "DRAFT",
  "cost": 25000.00,
  "currency": "USD"
}
```

---

### Capitalize Asset

```http
POST /api/v1/assets/{assetId}/capitalize
Content-Type: application/json

{
  "capitalizationDate": "2026-02-02",
  "costAdjustment": 500.00,
  "apInvoiceId": "INV-2026-001234"
}
```

**Response**: `200 OK`
```json
{
  "id": "asset-001",
  "status": "CAPITALIZED",
  "capitalizedCost": 25500.00,
  "glJournalId": "JE-2026-000789"
}
```

---

### Place In Service

```http
POST /api/v1/assets/{assetId}/in-service
Content-Type: application/json

{
  "inServiceDate": "2026-02-10"
}
```

**Response**: `200 OK`
```json
{
  "id": "asset-001",
  "status": "IN_SERVICE",
  "inServiceDate": "2026-02-10"
}
```

---

### Get Asset

```http
GET /api/v1/assets/{assetId}
```

**Response**: `200 OK`
```json
{
  "id": "asset-001",
  "assetNumber": "FA-2026-000123",
  "classId": "c8ad1c2e-8f1a-4f0c-9a07-9f8ed3155a22",
  "description": "X-Ray Machine",
  "status": "IN_SERVICE",
  "acquisitionDate": "2026-02-01",
  "inServiceDate": "2026-02-10",
  "cost": 25500.00,
  "accumulatedDepreciation": 425.00,
  "netBookValue": 25075.00,
  "currency": "USD",
  "location": "LOC-100",
  "custodian": "USER-900"
}
```

---

### List Assets

```http
GET /api/v1/assets?status=IN_SERVICE&classId={classId}&locationId={locationId}&page=0&size=20
```

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| status | String | DRAFT, CAPITALIZED, IN_SERVICE, DISPOSED |
| classId | UUID | Filter by class |
| locationId | String | Filter by location |
| inServiceFrom | Date | In-service date from |
| inServiceTo | Date | In-service date to |
| page | Integer | Page number (0-based) |
| size | Integer | Page size (default 20, max 100) |

---

## Depreciation

### Run Depreciation

```http
POST /api/v1/assets/depreciation-runs
Content-Type: application/json

{
  "bookId": "BOOK-IFRS",
  "period": "2026-02",
  "asOfDate": "2026-02-29"
}
```

**Response**: `201 Created`
```json
{
  "runId": "dep-001",
  "status": "DRAFT",
  "totalAssets": 120,
  "totalDepreciation": 84500.00
}
```

### Post Depreciation Run

```http
POST /api/v1/assets/depreciation-runs/{runId}/post
Content-Type: application/json

{
  "postingDate": "2026-02-29"
}
```

**Response**: `200 OK`
```json
{
  "runId": "dep-001",
  "status": "POSTED",
  "glJournalId": "JE-2026-001112"
}
```

---

## Transfers

### Create Transfer

```http
POST /api/v1/assets/transfers
Content-Type: application/json

{
  "assetId": "asset-001",
  "fromCostCenter": "CC-100",
  "toCostCenter": "CC-200",
  "fromLocation": "LOC-100",
  "toLocation": "LOC-200",
  "effectiveDate": "2026-02-15"
}
```

**Response**: `201 Created`
```json
{
  "transferId": "trf-001",
  "status": "DRAFT"
}
```

---

## Disposal

### Dispose Asset

```http
POST /api/v1/assets/{assetId}/dispose
Content-Type: application/json

{
  "disposalDate": "2026-03-01",
  "disposalType": "SALE",
  "proceeds": 12000.00,
  "reason": "Upgrade"
}
```

**Response**: `200 OK`
```json
{
  "id": "asset-001",
  "status": "DISPOSED",
  "gainLoss": 250.00,
  "glJournalId": "JE-2026-001300"
}
```

**Error Response**: `422 Unprocessable Entity`
```json
{
  "errorCode": "ASSET_NOT_IN_SERVICE",
  "message": "Asset must be in service before disposal",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-xyz789"
}
```

---

## Error Responses

### Standard Error Format (ADR-010 Compliant)

```json
{
  "errorCode": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2026-02-01T10:30:00Z",
  "requestId": "abc123",
  "details": {
    "field": "Additional context"
  }
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| ASSET_NOT_FOUND | 404 | Asset ID does not exist |
| ASSET_ALREADY_CAPITALIZED | 422 | Asset is already capitalized |
| ASSET_NOT_IN_SERVICE | 422 | Asset is not yet in service |
| ASSET_DISPOSED | 422 | Asset is already disposed |
| CAPITALIZATION_THRESHOLD_NOT_MET | 422 | Cost below class threshold |
| DEPRECIATION_NOT_ALLOWED | 422 | Depreciation not permitted for asset |
| PERIOD_CLOSED | 422 | Cannot post to closed period |
| TRANSFER_NOT_APPROVED | 422 | Transfer must be approved before posting |
| INVALID_REVALUATION | 422 | Revaluation policy prevents change |
