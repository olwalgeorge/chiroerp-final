# Inventory Advanced Ops API Reference

> Part of [Inventory Advanced Operations](../inventory-advanced-ops.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Packaging Hierarchy

### Create Packaging Hierarchy

```http
POST /api/v1/inventory/advanced-ops/packaging/hierarchies
Content-Type: application/json

{
  "itemId": "item-001",
  "levels": [
    { "level": "EACH", "uom": "EA", "gtin": "00012345600012" },
    { "level": "CASE", "uom": "CS", "conversion": 12, "gtin": "00012345600029" },
    { "level": "PALLET", "uom": "PL", "conversion": 480, "gtin": "00012345600036" }
  ],
  "tiHi": { "ti": 10, "hi": 4 }
}
```

---

## Kitting

### Create Kit

```http
POST /api/v1/inventory/advanced-ops/kits
Content-Type: application/json

{
  "kitSku": "KIT-1001",
  "kitType": "DYNAMIC",
  "components": [
    { "itemId": "item-001", "quantity": 1 },
    { "itemId": "item-002", "quantity": 2 }
  ]
}
```

### Create Kit Assembly Order

```http
POST /api/v1/inventory/advanced-ops/kits/assembly-orders
Content-Type: application/json

{
  "kitId": "kit-001",
  "quantity": 10,
  "warehouseId": "WH-01",
  "priority": "STANDARD"
}
```

---

## Repack Operations

### Create Repack Order

```http
POST /api/v1/inventory/advanced-ops/repack-orders
Content-Type: application/json

{
  "sourceItemId": "item-001",
  "targetItemId": "item-001",
  "quantity": 100,
  "reason": "DAMAGED_CASE"
}
```

---

## Catch Weight

### Record Catch Weight

```http
POST /api/v1/inventory/advanced-ops/catch-weight/records
Content-Type: application/json

{
  "itemId": "item-010",
  "lotNumber": "LOT-2026-02",
  "nominalWeight": 1.0,
  "actualWeight": 1.03,
  "tareWeight": 0.02
}
```

---

## Error Responses

### Standard Error Format

```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Packaging conversion is invalid",
  "details": {
    "field": "levels[1].conversion",
    "constraint": "must_be_multiple_of_base"
  },
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-advops-001"
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| KIT_NOT_FOUND | 404 | Kit ID does not exist |
| PACKAGING_INVALID | 422 | Packaging hierarchy invalid |
| COMPONENT_UNAVAILABLE | 422 | Kit component stock unavailable |
| REPACK_VARIANCE_HIGH | 422 | Repack variance exceeds tolerance |
| CATCH_WEIGHT_INVALID | 422 | Actual weight out of tolerance |
