# Plant Maintenance Work Orders Events & Integration

> Part of [Plant Maintenance Work Orders](../maintenance-work-orders.md)

---

## Domain Events Published

### WorkOrderCreatedEvent

```json
{
  "eventType": "WorkOrderCreatedEvent",
  "payload": {
    "workOrderId": "WO-3000",
    "equipmentId": "EQ-100",
    "type": "PREVENTIVE"
  }
}
```

### WorkOrderCompletedEvent

```json
{
  "eventType": "WorkOrderCompletedEvent",
  "payload": {
    "workOrderId": "WO-3000",
    "completionDate": "2026-02-02"
  }
}
```

### MaintenanceCostPostedEvent

```json
{
  "eventType": "MaintenanceCostPostedEvent",
  "payload": {
    "workOrderId": "WO-3000",
    "amount": 450.00,
    "currency": "USD"
  }
}
```

---

## Domain Events Consumed

```
- MaintenanceSchedulePublishedEvent (from Scheduling) -> Create work order
- BreakdownReportedEvent (from Breakdown) -> Corrective work order
```

---

## Avro Schemas

### WorkOrderCreatedSchema.avro

```json
{
  "type": "record",
  "name": "WorkOrderCreatedEvent",
  "namespace": "com.erp.maintenance.workorders.events",
  "fields": [
    { "name": "workOrderId", "type": "string" },
    { "name": "equipmentId", "type": "string" },
    { "name": "type", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `maintenance.workorders.created` | Work Orders | Inventory, Analytics | 6 |
| `maintenance.workorders.completed` | Work Orders | Analytics | 3 |
| `maintenance.workorders.cost.posted` | Work Orders | Finance/CO | 3 |
