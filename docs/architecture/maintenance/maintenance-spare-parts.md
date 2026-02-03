# Plant Maintenance Spare Parts - ADR-040

> **Bounded Context:** `maintenance-spare-parts`  
> **Port:** `9606` (logical, part of maintenance-work-orders service)  
> **Database:** `chiroerp_maintenance_work_orders`  
> **Kafka Consumer Group:** `maintenance-work-orders-cg`

## Overview

Spare Parts Management handles **parts catalogs, reservations, and critical spares** for maintenance work orders. It integrates with Inventory for stock, reservations, and issues.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Spare parts catalog, reservations, critical spares |
| **Aggregates** | SparePart, EquipmentBOM, PartReservation |
| **Key Events** | SparePartReservedEvent, SparePartIssuedEvent |
| **Integration** | Inventory, Procurement |
| **Compliance** | Critical spares availability |

## Key Capabilities

- Spare parts catalog linked to equipment BOMs
- Reservation and issue tracking for work orders
- Critical spares planning and min/max levels
- Preferred supplier links for parts

## Domain Model

```
SparePart
|-- partId
|-- description
`-- criticality

PartReservation
|-- reservationId
|-- workOrderId
`-- quantity
```

## Workflows

1. Work order requests parts.
2. Inventory reservation created.
3. Parts issued and consumed.
4. Reorder triggered if below threshold.

## Domain Events Published

```json
{
  "eventType": "SparePartReservedEvent",
  "payload": {
    "reservationId": "RES-200",
    "partId": "PART-10",
    "quantity": 2
  }
}
```

```json
{
  "eventType": "SparePartIssuedEvent",
  "payload": {
    "workOrderId": "WO-3000",
    "partId": "PART-10",
    "quantity": 2
  }
}
```

## Domain Events Consumed

```
- ReservationCreatedEvent (from Inventory) -> Confirm reservation
- StockIssuedEvent (from Inventory) -> Track parts consumption
```

## Integration Points

- **Inventory**: reservations and stock issues.
- **Procurement**: reorders and supplier sourcing.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| SparePartReservedEvent | `maintenance.spareparts.reserved` | 6 | 30d |
| SparePartIssuedEvent | `maintenance.spareparts.issued` | 6 | 30d |
| ReservationCreatedEvent (consumed) | `inventory.reservation.created` | 6 | 30d |
| StockIssuedEvent (consumed) | `inventory.stock.issued` | 6 | 30d |

**Consumer Group:** `maintenance-work-orders-cg`  
**Partition Key:** `workOrderId` / `partId` (ensures all events for a work order are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/maintenance/parts/reservations
Content-Type: application/json

{
  "workOrderId": "WO-3000",
  "partId": "PART-10",
  "quantity": 2
}
```

## Error Responses

```json
{
  "errorCode": "SPAREPART_WORK_ORDER_NOT_FOUND",
  "message": "Work order 'WO-9999' not found",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "workOrderId": "WO-9999"
  }
}
```

```json
{
  "errorCode": "SPAREPART_INSUFFICIENT_STOCK",
  "message": "Insufficient stock to reserve part",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "partId": "PART-10",
    "requestedQty": 5,
    "availableQty": 2,
    "isCriticalPart": true
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

- Critical spares have minimum availability targets.
- Reservation changes are audited.

## References

- [ADR-040: Plant Maintenance](../../adr/ADR-040-plant-maintenance.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
