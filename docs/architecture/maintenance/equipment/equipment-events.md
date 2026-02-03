# Plant Maintenance Equipment Events & Integration

> Part of [Plant Maintenance Equipment Master](../maintenance-equipment.md)

---

## Domain Events Published

### EquipmentCreatedEvent

```json
{
  "eventType": "EquipmentCreatedEvent",
  "payload": {
    "equipmentId": "EQ-100",
    "classId": "PUMP",
    "criticality": "A"
  }
}
```

### EquipmentAssignedEvent

```json
{
  "eventType": "EquipmentAssignedEvent",
  "payload": {
    "equipmentId": "EQ-100",
    "functionalLocationId": "LOC-200"
  }
}
```

---

## Domain Events Consumed

```
- AssetRetiredEvent (from Finance Assets) -> Decommission equipment
```

---

## Avro Schemas

### EquipmentCreatedSchema.avro

```json
{
  "type": "record",
  "name": "EquipmentCreatedEvent",
  "namespace": "com.erp.maintenance.equipment.events",
  "fields": [
    { "name": "equipmentId", "type": "string" },
    { "name": "classId", "type": "string" },
    { "name": "criticality", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `maintenance.equipment.created` | Equipment Master | Work Orders, Preventive | 6 |
| `maintenance.equipment.assigned` | Equipment Master | Preventive | 6 |
