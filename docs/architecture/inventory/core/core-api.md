# Inventory Core API Reference

> Part of [Inventory Core](../inventory-core.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Items

### Create Item

```http
POST /api/v1/inventory/items
Content-Type: application/json
Authorization: Bearer {token}

{
  "sku": "SKU-1001",
  "description": "3M N95 Mask",
  "baseUom": "EA",
  "attributes": {
    "size": "M",
    "color": "White"
  }
}
```

**Response**: `201 Created`
```json
{
  "id": "item-001",
  "sku": "SKU-1001",
  "status": "ACTIVE",
  "baseUom": "EA"
}
```

---

### Get Item

```http
GET /api/v1/inventory/items/{itemId}
```

---

## Stock Movements

### Post Stock Movement

```http
POST /api/v1/inventory/stock-movements
Content-Type: application/json

{
  "itemId": "item-001",
  "locationId": "LOC-100",
  "quantity": 100,
  "uom": "EA",
  "movementType": "RECEIPT",
  "cost": 1.25,
  "lotNumber": "LOT-2026-02"
}
```

**Response**: `201 Created`
```json
{
  "movementId": "mov-001",
  "status": "POSTED",
  "postingDate": "2026-02-02",
  "glJournalId": "JE-2026-002010"
}
```

---

### Get Stock Ledger

```http
GET /api/v1/inventory/stock-ledger?itemId={itemId}&fromDate={date}&toDate={date}
```

---

### Get Availability (ATP)

```http
GET /api/v1/inventory/availability?itemId={itemId}&channel={channel}
```

---

## Reservations

### Create Reservation

```http
POST /api/v1/inventory/reservations
Content-Type: application/json

{
  "itemId": "item-001",
  "locationId": "LOC-100",
  "quantity": 10,
  "channel": "ECOM",
  "reference": "SO-10002"
}
```

**Response**: `201 Created`
```json
{
  "reservationId": "res-001",
  "status": "PENDING",
  "reservedQuantity": 10
}
```

---

### Confirm Reservation

```http
POST /api/v1/inventory/reservations/{reservationId}/confirm
```

---

### Release Reservation

```http
POST /api/v1/inventory/reservations/{reservationId}/release
Content-Type: application/json

{
  "reason": "Order cancelled"
}
```

---

## Cycle Counts

### Start Cycle Count

```http
POST /api/v1/inventory/cycle-counts
Content-Type: application/json

{
  "locationId": "LOC-100",
  "itemIds": ["item-001", "item-002"],
  "scheduledDate": "2026-02-05"
}
```

---

### Post Cycle Count

```http
POST /api/v1/inventory/cycle-counts/{cycleCountId}/post
Content-Type: application/json

{
  "lines": [
    { "itemId": "item-001", "countedQuantity": 98 },
    { "itemId": "item-002", "countedQuantity": 50 }
  ],
  "varianceReason": "COUNT_VARIANCE"
}
```

---

## Error Responses

### Standard Error Format

```json
{
  "errorCode": "ERROR_CODE",
  "message": "Human-readable error message",
  "details": {
    "field": "Additional context"
  },
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-inv-001"
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| ITEM_NOT_FOUND | 404 | Item ID does not exist |
| LOCATION_NOT_FOUND | 404 | Location ID does not exist |
| INSUFFICIENT_STOCK | 422 | Available quantity is insufficient |
| INVALID_UOM | 422 | UoM conversion missing or invalid |
| LOT_EXPIRED | 422 | Lot is expired and cannot be issued |
| PERIOD_CLOSED | 422 | Cannot post to closed inventory period |
| RESERVATION_NOT_FOUND | 404 | Reservation ID does not exist |
