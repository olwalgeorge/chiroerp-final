# CRM Pipeline Events & Integration

> Part of [CRM Pipeline](../crm-pipeline.md)

---

## Domain Events Published

### OpportunityCreatedEvent

**Trigger**: Opportunity created

```json
{
  "eventType": "OpportunityCreatedEvent",
  "eventId": "evt-4201",
  "timestamp": "2026-02-02T11:10:00Z",
  "aggregateId": "OPP-100",
  "tenantId": "tenant-001",
  "payload": {
    "opportunityId": "OPP-100",
    "customerId": "CUST-100",
    "amount": 120000.00,
    "currency": "USD",
    "stage": "QUALIFICATION"
  }
}
```

### OpportunityStageChangedEvent

**Trigger**: Stage updated

```json
{
  "eventType": "OpportunityStageChangedEvent",
  "payload": {
    "opportunityId": "OPP-100",
    "fromStage": "QUALIFICATION",
    "toStage": "NEGOTIATION"
  }
}
```

### OpportunityClosedWonEvent

**Trigger**: Opportunity closed as won

```json
{
  "eventType": "OpportunityClosedWonEvent",
  "payload": {
    "opportunityId": "OPP-100",
    "customerId": "CUST-100",
    "amount": 120000.00,
    "currency": "USD",
    "closeDate": "2026-02-02"
  }
}
```

---

## Domain Events Consumed

```
- CustomerProfileUpdatedEvent  (from Customer 360) -> Sync account data
- QuoteCreatedEvent            (from Sales)        -> Attach pricing snapshot
- CreditLimitExceededEvent     (from Finance/AR)   -> Block close
```

---

## Avro Schemas

### OpportunityCreatedSchema.avro

```json
{
  "type": "record",
  "name": "OpportunityCreatedEvent",
  "namespace": "com.erp.crm.pipeline.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "opportunityId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "amount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" },
    { "name": "stage", "type": "string" }
  ]
}
```

### OpportunityClosedWonSchema.avro

```json
{
  "type": "record",
  "name": "OpportunityClosedWonEvent",
  "namespace": "com.erp.crm.pipeline.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "opportunityId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "amount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" },
    { "name": "closeDate", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `crm.pipeline.opportunity.created` | CRM Pipeline | Sales, Analytics | 6 |
| `crm.pipeline.opportunity.stage.changed` | CRM Pipeline | Analytics | 6 |
| `crm.pipeline.opportunity.closed.won` | CRM Pipeline | Sales, Contracts | 6 |
| `crm.pipeline.opportunity.closed.lost` | CRM Pipeline | Analytics | 3 |
| `crm.pipeline.forecast.snapshot.created` | CRM Pipeline | Analytics | 3 |
