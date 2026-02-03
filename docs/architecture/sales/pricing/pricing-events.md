# Sales Pricing Events & Integration

> Part of [Sales Pricing & Promotions](../sales-pricing.md)

---

## Domain Events Published

### PriceCalculatedEvent

**Trigger**: Pricing evaluated for order or quote

```json
{
  "eventType": "PriceCalculatedEvent",
  "eventId": "evt-3101",
  "timestamp": "2026-02-02T12:15:00Z",
  "aggregateId": "order-quote-100",
  "tenantId": "tenant-001",
  "payload": {
    "customerId": "CUST-100",
    "channel": "ECOM",
    "currency": "USD",
    "subtotal": 200.00,
    "discount": 20.00,
    "total": 180.00
  }
}
```

### PriceOverrideApprovedEvent

**Trigger**: Pricing override approval

```json
{
  "eventType": "PriceOverrideApprovedEvent",
  "payload": {
    "overrideId": "OVR-200",
    "orderId": "SO-10002",
    "approvedBy": "USR-200",
    "approvedPrice": 90.00
  }
}
```

---

## Domain Events Consumed

```
- SalesOrderSubmittedEvent      (from Sales Core) -> Validate pricing snapshot
- CustomerTierUpdatedEvent      (from CRM)        -> Refresh segment rules
- ExchangeRateUpdatedEvent      (from Finance)    -> Update FX cache
```

---

## Avro Schemas

### PriceCalculatedSchema.avro

```json
{
  "type": "record",
  "name": "PriceCalculatedEvent",
  "namespace": "com.erp.sales.pricing.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "channel", "type": "string" },
    { "name": "currency", "type": "string" },
    { "name": "subtotal", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "discount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "total", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

### PriceOverrideApprovedSchema.avro

```json
{
  "type": "record",
  "name": "PriceOverrideApprovedEvent",
  "namespace": "com.erp.sales.pricing.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "overrideId", "type": "string" },
    { "name": "orderId", "type": "string" },
    { "name": "approvedBy", "type": "string" },
    { "name": "approvedPrice", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } }
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `sales.pricing.price.calculated` | Sales Pricing | Sales Core, Analytics | 12 |
| `sales.pricing.override.approved` | Sales Pricing | Sales Core | 6 |
| `sales.pricing.price.list.updated` | Sales Pricing | Sales Core, Cache | 6 |
