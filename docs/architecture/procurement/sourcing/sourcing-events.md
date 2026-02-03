# Procurement Sourcing Events & Integration

> Part of [Procurement Sourcing & RFQ](../procurement-sourcing.md)

---

## Domain Events Published

### RFQIssuedEvent

```json
{
  "eventType": "RFQIssuedEvent",
  "payload": {
    "rfqId": "rfq-001",
    "suppliers": ["vendor-001", "vendor-002"],
    "dueDate": "2026-02-20"
  }
}
```

### AwardGrantedEvent

```json
{
  "eventType": "AwardGrantedEvent",
  "payload": {
    "rfqId": "rfq-001",
    "supplierId": "vendor-001",
    "awardAmount": 25000.00
  }
}
```

---

## Domain Events Consumed

```
- VendorUpdatedEvent (from Supplier Mgmt) -> Ensure eligibility
```

---

## Avro Schemas

### RFQIssuedSchema.avro

```json
{
  "type": "record",
  "name": "RFQIssuedEvent",
  "namespace": "com.erp.procurement.sourcing.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "rfqId", "type": "string" },
    { "name": "dueDate", "type": "string" }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `procurement.rfq.issued` | Procurement Sourcing | Suppliers, Analytics | 3 |
| `procurement.award.granted` | Procurement Sourcing | Procurement Core | 3 |
