# CRM Service Orders Events & Integration

> Part of [CRM Service Orders](../crm-service-orders.md)

---

## Domain Events Published

### ServiceOrderCreatedEvent

**Trigger**: Service order created

```json
{
  "eventType": "ServiceOrderCreatedEvent",
  "eventId": "evt-4301",
  "timestamp": "2026-02-02T12:00:00Z",
  "aggregateId": "SO-100",
  "tenantId": "tenant-001",
  "payload": {
    "serviceOrderId": "SO-100",
    "customerId": "CUST-100",
    "priority": "HIGH",
    "status": "REQUESTED"
  }
}
```

### ServiceOrderScheduledEvent

**Trigger**: Service order scheduled

```json
{
  "eventType": "ServiceOrderScheduledEvent",
  "payload": {
    "serviceOrderId": "SO-100",
    "scheduleStart": "2026-02-03T09:00:00Z",
    "scheduleEnd": "2026-02-03T12:00:00Z"
  }
}
```

### ServiceOrderCompletedEvent

**Trigger**: Work completed

```json
{
  "eventType": "ServiceOrderCompletedEvent",
  "payload": {
    "serviceOrderId": "SO-100",
    "completionTime": "2026-02-03T11:45:00Z",
    "resolutionCode": "RESOLVED"
  }
}
```

### ServiceOrderBilledEvent

**Trigger**: Billing approved

```json
{
  "eventType": "ServiceOrderBilledEvent",
  "payload": {
    "serviceOrderId": "SO-100",
    "customerId": "CUST-100",
    "amount": 450.00,
    "currency": "USD"
  }
}
```

---

## Domain Events Consumed

```
- TechnicianAssignedEvent      (from Dispatch)   -> Mark dispatched
- PartsConsumedEvent           (from Inventory)  -> Attach parts usage
- ContractEntitlementValidatedEvent (from Contracts) -> Confirm coverage
- FinancialPeriodClosedEvent   (from Period Close) -> Prevent billing
```

---

## GL Posting Rules (Examples)

### Service Order Billed
```
Debit:  Accounts Receivable (1200)     $450
Credit: Service Revenue (4100)         $450
```

---

## Avro Schemas

### ServiceOrderCreatedSchema.avro

```json
{
  "type": "record",
  "name": "ServiceOrderCreatedEvent",
  "namespace": "com.erp.crm.serviceorders.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "serviceOrderId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "priority", "type": "string" },
    { "name": "status", "type": "string" }
  ]
}
```

### ServiceOrderBilledSchema.avro

```json
{
  "type": "record",
  "name": "ServiceOrderBilledEvent",
  "namespace": "com.erp.crm.serviceorders.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "serviceOrderId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "amount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `crm.serviceorders.created` | Service Orders | Dispatch, Contracts, Analytics | 6 |
| `crm.serviceorders.scheduled` | Service Orders | Dispatch | 6 |
| `crm.serviceorders.completed` | Service Orders | Finance/AR, Analytics | 6 |
| `crm.serviceorders.billed` | Service Orders | Finance/AR | 6 |
| `crm.serviceorders.sla.breached` | Service Orders | Analytics, Customer 360 | 3 |
