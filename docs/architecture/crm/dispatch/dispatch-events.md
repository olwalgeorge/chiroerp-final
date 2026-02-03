# CRM Dispatch Events & Integration

> Part of [CRM Dispatch](../crm-dispatch.md)

---

## Domain Events Published

### TechnicianAssignedEvent

**Trigger**: Technician assigned to a dispatch

```json
{
  "eventType": "TechnicianAssignedEvent",
  "eventId": "evt-4501",
  "timestamp": "2026-02-02T13:05:00Z",
  "aggregateId": "DSP-100",
  "tenantId": "tenant-001",
  "payload": {
    "dispatchId": "DSP-100",
    "serviceOrderId": "SO-100",
    "technicianId": "TECH-10",
    "status": "ASSIGNED"
  }
}
```

### DispatchConfirmedEvent

**Trigger**: Technician confirmed dispatch

```json
{
  "eventType": "DispatchConfirmedEvent",
  "payload": {
    "dispatchId": "DSP-100",
    "technicianId": "TECH-10",
    "eta": "2026-02-03T09:30:00Z"
  }
}
```

### RouteOptimizedEvent

**Trigger**: Route optimization completed

```json
{
  "eventType": "RouteOptimizedEvent",
  "payload": {
    "dispatchId": "DSP-100",
    "routeId": "ROUTE-200",
    "distanceKm": 42.5
  }
}
```

---

## Domain Events Consumed

```
- ServiceOrderScheduledEvent   (from Service Orders) -> Create dispatch request
- ServiceOrderCancelledEvent   (from Service Orders) -> Cancel dispatch
- SlaBreachedEvent             (from Service Orders) -> Escalate priority
```

---

## Avro Schemas

### TechnicianAssignedSchema.avro

```json
{
  "type": "record",
  "name": "TechnicianAssignedEvent",
  "namespace": "com.erp.crm.dispatch.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "dispatchId", "type": "string" },
    { "name": "serviceOrderId", "type": "string" },
    { "name": "technicianId", "type": "string" },
    { "name": "status", "type": "string" }
  ]
}
```

### DispatchConfirmedSchema.avro

```json
{
  "type": "record",
  "name": "DispatchConfirmedEvent",
  "namespace": "com.erp.crm.dispatch.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "dispatchId", "type": "string" },
    { "name": "technicianId", "type": "string" },
    { "name": "eta", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `crm.dispatch.assignment.created` | CRM Dispatch | Service Orders, Analytics | 6 |
| `crm.dispatch.assignment.confirmed` | CRM Dispatch | Service Orders | 6 |
| `crm.dispatch.assignment.rejected` | CRM Dispatch | Service Orders | 3 |
| `crm.dispatch.route.optimized` | CRM Dispatch | Analytics | 3 |
| `crm.dispatch.technician.status` | CRM Dispatch | Service Orders, Analytics | 6 |
