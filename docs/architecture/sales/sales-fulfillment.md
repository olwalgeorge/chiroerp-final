# Sales Fulfillment - ADR-025

> **Bounded Context:** `sales-fulfillment`
> **Port:** `9203` (logical, part of sales-core service)
> **Database:** `chiroerp_sales_core`
> **Kafka Consumer Group:** `sales-core-cg`

## Overview

Fulfillment manages **pick/pack/ship and delivery confirmation** for sales orders. It orchestrates shipments with Inventory/WMS and publishes shipment confirmations to Finance/AR and Revenue Recognition.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Pick, pack, ship, delivery confirmation |
| **Aggregates** | Delivery, Shipment, Package |
| **Key Events** | ShipmentCreatedEvent, ShipmentConfirmedEvent, DeliveryCompletedEvent |
| **Integration** | Inventory reservations and shipment confirmations |
| **Compliance** | Audit trail and carrier compliance |

## Key Capabilities

- Shipment planning and carrier selection
- Pick/pack confirmation integration with WMS
- Partial shipments and backorders

## Domain Model

```
Delivery
├── deliveryId
├── orderId
├── status: Planned | InProgress | Completed
└── lines

Shipment
├── shipmentId
├── carrier
├── trackingNumber
└── status
```

## Workflows

1. Sales order allocated to fulfillment.
2. WMS confirms picks and packs.
3. Shipment created and confirmed.
4. ShipmentConfirmedEvent published to Sales Core; SalesOrderFulfilledEvent emitted to AR.

## Domain Events Published

```json
{
  "eventType": "ShipmentConfirmedEvent",
  "payload": {
    "shipmentId": "ship-001",
    "orderId": "SO-10002",
    "carrier": "UPS",
    "trackingNumber": "1Z999"
  }
}
```

## Domain Events Consumed

```
- SalesOrderAllocatedEvent (from Sales Core) -> Create delivery
- TaskCompletedEvent (from WMS) -> Confirm picks
```

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `sales.fulfillment.shipment.created` | Sales Fulfillment | Inventory/WMS, Analytics | 6 |
| `sales.fulfillment.shipment.confirmed` | Sales Fulfillment | Sales Core, Finance/AR | 6 |
| `sales.fulfillment.delivery.completed` | Sales Fulfillment | Revenue Recognition, Analytics | 6 |

## Integration Points

- **Inventory/WMS**: pick/pack confirmations
- **Finance/AR**: billing after SalesOrderFulfilledEvent

## API Endpoints (Examples)

```http
POST /api/v1/sales/shipments
Content-Type: application/json

{
  "orderId": "SO-10002",
  "carrier": "UPS",
  "serviceLevel": "2_DAY"
}
```

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Shipment creation validation failed",
  "timestamp": "2026-02-10T11:00:00Z",
  "requestId": "req-fulfill-001",
  "details": {
    "violations": [
      {
        "field": "orderId",
        "constraint": "Order is not allocatable",
        "rejectedValue": "SO-10002"
      }
    ]
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

- Shipment audit trail
- Carrier compliance documentation

## References

- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
