# Plant Maintenance Preventive Events & Integration

> Part of [Plant Maintenance Preventive Maintenance](../maintenance-preventive.md)

---

## Domain Events Published

### MaintenancePlanCreatedEvent

```json
{
  "eventType": "MaintenancePlanCreatedEvent",
  "payload": {
    "planId": "PLAN-700",
    "equipmentId": "EQ-100",
    "planType": "TIME"
  }
}
```

### MaintenanceScheduleGeneratedEvent

```json
{
  "eventType": "MaintenanceScheduleGeneratedEvent",
  "payload": {
    "planId": "PLAN-700",
    "scheduleCount": 6
  }
}
```

---

## Domain Events Consumed

```
- EquipmentAssignedEvent (from Equipment Master) -> Create plan
```

---

## Avro Schemas

### MaintenancePlanCreatedSchema.avro

```json
{
  "type": "record",
  "name": "MaintenancePlanCreatedEvent",
  "namespace": "com.erp.maintenance.preventive.events",
  "fields": [
    { "name": "planId", "type": "string" },
    { "name": "equipmentId", "type": "string" },
    { "name": "planType", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `maintenance.preventive.plan.created` | Preventive | Work Orders, Scheduling | 3 |
| `maintenance.preventive.schedule.generated` | Preventive | Scheduling | 3 |
