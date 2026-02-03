# CRM Contracts Events & Integration

> Part of [CRM Contracts](../crm-contracts.md)

---

## Domain Events Published

### ContractActivatedEvent

**Trigger**: Contract activated

```json
{
  "eventType": "ContractActivatedEvent",
  "eventId": "evt-4401",
  "timestamp": "2026-02-02T12:30:00Z",
  "aggregateId": "CON-100",
  "tenantId": "tenant-001",
  "payload": {
    "contractId": "CON-100",
    "customerId": "CUST-100",
    "planId": "PLAN-1",
    "startDate": "2026-02-01",
    "endDate": "2027-01-31"
  }
}
```

### EntitlementConsumedEvent

**Trigger**: Entitlement usage recorded

```json
{
  "eventType": "EntitlementConsumedEvent",
  "payload": {
    "contractId": "CON-100",
    "entitlementId": "ENT-100",
    "units": 1,
    "remaining": 9
  }
}
```

### ContractRenewedEvent

**Trigger**: Contract renewed

```json
{
  "eventType": "ContractRenewedEvent",
  "payload": {
    "contractId": "CON-100",
    "renewalId": "REN-200",
    "newEndDate": "2028-01-31"
  }
}
```

---

## Domain Events Consumed

```
- OpportunityClosedWonEvent    (from Pipeline)      -> Create renewal offer
- ServiceOrderCreatedEvent     (from Service Orders) -> Validate coverage
- FinancialPeriodClosedEvent   (from Period Close)  -> Freeze renewals
```

---

## Avro Schemas

### ContractActivatedSchema.avro

```json
{
  "type": "record",
  "name": "ContractActivatedEvent",
  "namespace": "com.erp.crm.contracts.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "contractId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "planId", "type": "string" },
    { "name": "startDate", "type": "string" },
    { "name": "endDate", "type": "string" }
  ]
}
```

### EntitlementConsumedSchema.avro

```json
{
  "type": "record",
  "name": "EntitlementConsumedEvent",
  "namespace": "com.erp.crm.contracts.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "contractId", "type": "string" },
    { "name": "entitlementId", "type": "string" },
    { "name": "units", "type": "int" },
    { "name": "remaining", "type": "int" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `crm.contracts.activated` | CRM Contracts | Service Orders, Customer 360 | 3 |
| `crm.contracts.renewed` | CRM Contracts | Customer 360, Analytics | 3 |
| `crm.contracts.entitlement.consumed` | CRM Contracts | Service Orders | 6 |
| `crm.contracts.expired` | CRM Contracts | Service Orders, Analytics | 3 |
