# CRM Parts Consumption - ADR-042

> **Bounded Context:** `crm-parts-consumption`
> **Port:** `9408` (logical, part of crm-service-orders service)
> **Database:** `chiroerp_crm_service_orders`
> **Kafka Consumer Group:** `crm-service-orders-cg`

## Overview

Parts Consumption records **parts issued and returned** for service orders. It integrates with Inventory for stock movements and with Finance for cost tracking.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Parts usage, returns |
| **Aggregates** | PartsUsage, PartsReturn |
| **Key Events** | PartsConsumedEvent, PartsReturnedEvent |
| **Integration** | Inventory, Service Orders |
| **Compliance** | Audit trail for parts usage |

## Key Capabilities

- Issue parts to service orders
- Return unused parts to inventory
- Cost capture by order

## Domain Model

```
PartsUsage
|-- usageId
|-- serviceOrderId
`-- items

PartsReturn
|-- returnId
|-- serviceOrderId
`-- items
```

## Workflows

1. Technician requests parts for service order.
2. Parts issued and recorded.
3. Unused parts returned and reconciled.

## Domain Events Published

```json
{
  "eventType": "PartsConsumedEvent",
  "payload": {
    "serviceOrderId": "SO-200",
    "itemId": "PART-10",
    "quantity": 2
  }
}
```

```json
{
  "eventType": "PartsReturnedEvent",
  "payload": {
    "serviceOrderId": "SO-200",
    "itemId": "PART-10",
    "quantity": 1
  }
}
```

## Domain Events Consumed

```
- ServiceOrderScheduledEvent (from Service Orders) -> Reserve parts
- StockIssuedEvent (from Inventory) -> Confirm issue
```

## Integration Points

- **Inventory**: stock movements
- **Service Orders**: cost capture

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| PartsConsumedEvent | `crm.parts.consumption.consumed` | 6 | 30d |
| PartsReturnedEvent | `crm.parts.consumption.returned` | 6 | 30d |
| ServiceOrderScheduledEvent (consumed) | `crm.service.order.scheduled` | 6 | 30d |
| StockIssuedEvent (consumed) | `inventory.stock.issued` | 6 | 30d |

**Consumer Group:** `crm-service-orders-cg`
**Partition Key:** `serviceOrderId` (ensures all events for a service order are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/crm/service-orders/{serviceOrderId}/parts/consume
Content-Type: application/json

{
  "itemId": "PART-10",
  "quantity": 2
}
```

## Error Responses

```json
{
  "errorCode": "PARTS_SERVICE_ORDER_NOT_FOUND",
  "message": "Service order 'SO-999' not found",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "serviceOrderId": "SO-999"
  }
}
```

```json
{
  "errorCode": "PARTS_INSUFFICIENT_STOCK",
  "message": "Insufficient stock to issue parts",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "itemId": "PART-10",
    "requestedQty": 5,
    "availableQty": 2
  }
}
```

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| API availability | 99.7% | < 99.5% |
| Command latency | < 1s p95 | > 3s |
| Event processing lag | < 30s p95 | > 2m |

## Compliance & Controls

- Parts usage audit log

## References

- [ADR-042: Field Service Operations](../../adr/ADR-042-field-service-operations.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
