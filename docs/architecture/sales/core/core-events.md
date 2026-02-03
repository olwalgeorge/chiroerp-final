# Sales Core Events & Integration

> Part of [Sales Core](../sales-core.md)

---

## Domain Events Published

### SalesOrderCreatedEvent

**Trigger**: Sales order created

```json
{
  "eventType": "SalesOrderCreatedEvent",
  "eventId": "evt-3001",
  "timestamp": "2026-02-02T12:10:00Z",
  "aggregateId": "SO-10002",
  "tenantId": "tenant-001",
  "payload": {
    "orderId": "SO-10002",
    "customerId": "CUST-100",
    "channel": "ECOM",
    "totalAmount": 180.00,
    "currency": "USD"
  }
}
```

### SalesOrderAllocatedEvent

**Trigger**: Allocation completed

```json
{
  "eventType": "SalesOrderAllocatedEvent",
  "payload": {
    "orderId": "SO-10002",
    "reservationId": "res-001",
    "allocationStatus": "ALLOCATED"
  }
}
```

### SalesOrderFulfilledEvent

**Trigger**: Shipment confirmed and order ready for billing

```json
{
  "eventType": "SalesOrderFulfilledEvent",
  "payload": {
    "orderId": "SO-10002",
    "shipmentId": "ship-001",
    "customerId": "CUST-100",
    "amount": 180.00,
    "currency": "USD",
    "postingDate": "2026-02-02"
  }
}
```

---

## Domain Events Consumed

```
- PriceCalculatedEvent           (from Sales Pricing) -> Attach pricing snapshot
- ReservationCreatedEvent        (from Inventory)     -> Mark allocation
- ShipmentConfirmedEvent         (from Fulfillment)   -> Emit SalesOrderFulfilled
- CreditLimitExceededEvent       (from Finance/AR)    -> Place order hold
- CustomerBlockedEvent           (from Finance/AR)    -> Block orders
- FinancialPeriodClosedEvent     (from Period Close)  -> Prevent posting
```

---

## GL Posting Rules (Examples)

### Sales Order Fulfilled
```
Debit:  Accounts Receivable (1200)     $180
Credit: Revenue (4000)                 $180
```

---

## Avro Schemas

### SalesOrderCreatedSchema.avro

```json
{
  "type": "record",
  "name": "SalesOrderCreatedEvent",
  "namespace": "com.erp.sales.core.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "orderId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "channel", "type": "string" },
    { "name": "totalAmount", "type": { "type": "bytes", "logicalType": "decimal", "precision": 18, "scale": 2 } },
    { "name": "currency", "type": "string" }
  ]
}
```

### SalesOrderFulfilledSchema.avro

```json
{
  "type": "record",
  "name": "SalesOrderFulfilledEvent",
  "namespace": "com.erp.sales.core.events",
  "fields": [
    { "name": "eventId", "type": "string" },
    { "name": "timestamp", "type": "long", "logicalType": "timestamp-millis" },
    { "name": "tenantId", "type": "string" },
    { "name": "orderId", "type": "string" },
    { "name": "shipmentId", "type": "string" },
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
| `sales.core.order.created` | Sales Core | Pricing, Analytics | 12 |
| `sales.core.order.allocated` | Sales Core | Inventory, Fulfillment | 12 |
| `sales.core.order.fulfilled` | Sales Core | Finance/AR, Revenue | 12 |
| `sales.core.order.cancelled` | Sales Core | Inventory, Analytics | 6 |
