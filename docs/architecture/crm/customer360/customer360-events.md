# CRM Customer 360 Events & Integration

> Part of [CRM Customer 360](../crm-customer360.md)

---

## Domain Events Published

### CustomerProfileCreatedEvent

**Trigger**: New customer profile created

```json
{
  "eventType": "CustomerProfileCreatedEvent",
  "eventId": "evt-4101",
  "timestamp": "2026-02-02T09:15:00Z",
  "aggregateId": "CUST-100",
  "tenantId": "tenant-001",
  "payload": {
    "customerId": "CUST-100",
    "name": "Acme Corp",
    "status": "ACTIVE",
    "primaryContactId": "CONT-10",
    "segment": "ENTERPRISE"
  }
}
```

### CustomerProfileUpdatedEvent

**Trigger**: Profile attributes changed

```json
{
  "eventType": "CustomerProfileUpdatedEvent",
  "payload": {
    "customerId": "CUST-100",
    "changedFields": ["segment", "status"],
    "updatedBy": "user-200"
  }
}
```

### CustomerMergedEvent

**Trigger**: Two profiles merged into a golden record

```json
{
  "eventType": "CustomerMergedEvent",
  "eventId": "evt-4103",
  "timestamp": "2026-02-02T10:05:00Z",
  "aggregateId": "CUST-100",
  "tenantId": "tenant-001",
  "payload": {
    "targetCustomerId": "CUST-100",
    "sourceCustomerId": "CUST-OLD",
    "mergeReason": "DUPLICATE"
  }
}
```

### ConsentUpdatedEvent

**Trigger**: Consent updated

```json
{
  "eventType": "ConsentUpdatedEvent",
  "payload": {
    "customerId": "CUST-100",
    "consentType": "EMAIL",
    "status": "GRANTED"
  }
}
```

---

## Domain Events Consumed

```
- LeadCreatedEvent               (from Sales)       -> Create initial profile
- ServiceOrderCreatedEvent       (from Service Orders) -> Attach service history
- ContractActivatedEvent         (from Contracts)   -> Update entitlement view
```

---

## Avro Schemas

### CustomerProfileCreatedSchema.avro

```json
{
  "type": "record",
  "name": "CustomerProfileCreatedEvent",
  "namespace": "com.erp.crm.customer360.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "name", "type": "string" },
    { "name": "status", "type": "string" },
    { "name": "segment", "type": "string" }
  ]
}
```

### CustomerMergedSchema.avro

```json
{
  "type": "record",
  "name": "CustomerMergedEvent",
  "namespace": "com.erp.crm.customer360.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "targetCustomerId", "type": "string" },
    { "name": "sourceCustomerId", "type": "string" },
    { "name": "mergeReason", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `crm.customer360.profile.created` | Customer 360 | Pipeline, Service Orders, Analytics | 6 |
| `crm.customer360.profile.updated` | Customer 360 | Pipeline, Contracts, Analytics | 6 |
| `crm.customer360.profile.merged` | Customer 360 | All CRM modules | 6 |
| `crm.customer360.consent.updated` | Customer 360 | Communication, Marketing | 3 |
