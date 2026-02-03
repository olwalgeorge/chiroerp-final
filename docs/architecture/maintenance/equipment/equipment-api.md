# Plant Maintenance Equipment API Reference

> Part of [Plant Maintenance Equipment Master](../maintenance-equipment.md)

## Base URL

```
https://api.chiroerp.com
```

---

## Create Equipment

```http
POST /api/v1/maintenance/equipment
Content-Type: application/json
Authorization: Bearer {token}

{
  "equipmentId": "EQ-100",
  "classId": "PUMP",
  "description": "Cooling pump",
  "criticality": "A"
}
```

**Response**: `201 Created`
```json
{
  "equipmentId": "EQ-100",
  "status": "ACTIVE"
}
```

---

## Assign Equipment to Location

```http
POST /api/v1/maintenance/equipment/EQ-100/assign
Content-Type: application/json

{
  "functionalLocationId": "LOC-200"
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
| EQUIPMENT_NOT_FOUND | 404 | Equipment not found |
| INVALID_STATUS | 422 | Invalid equipment status |
| LOCATION_NOT_FOUND | 404 | Functional location not found |
