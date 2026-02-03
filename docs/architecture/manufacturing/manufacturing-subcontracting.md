# Manufacturing Subcontracting - ADR-037

> **Bounded Context:** `manufacturing-subcontracting`  
> **Port:** `9307` (logical, part of manufacturing-production service)  
> **Database:** `chiroerp_manufacturing_production`  
> **Kafka Consumer Group:** `manufacturing-production-cg`

## Overview

Subcontracting manages **external operations** where components are issued to a vendor and finished goods are received back. It coordinates with Procurement for POs and Inventory for component issues/receipts.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Subcontract orders, component issues, receipts |
| **Aggregates** | SubcontractOrder, ComponentShipment, SubcontractReceipt |
| **Key Events** | SubcontractOrderCreatedEvent, SubcontractComponentsIssuedEvent, SubcontractReceiptPostedEvent |
| **Integration** | Procurement, Inventory, Production Orders |
| **Compliance** | Traceability for external processing |

## Key Capabilities

- Create subcontract orders from production routing steps
- Issue components to vendors with tracking
- Receive finished goods and reconcile yields
- Track subcontract lead times and vendor performance

## Domain Model

```
SubcontractOrder
|-- orderId
|-- vendorId
|-- productionOrderId
|-- status: Planned | Released | InProcess | Completed
`-- components

SubcontractReceipt
|-- receiptId
|-- orderId
|-- quantity
`-- receivedAt
```

## Workflows

1. Production order releases subcontract step.
2. PO issued to vendor (Procurement).
3. Components issued from inventory.
4. Finished goods received and posted.

## Domain Events Published

```json
{
  "eventType": "SubcontractOrderCreatedEvent",
  "payload": {
    "orderId": "SUB-100",
    "vendorId": "V-200",
    "productionOrderId": "PO-100"
  }
}
```

```json
{
  "eventType": "SubcontractComponentsIssuedEvent",
  "payload": {
    "orderId": "SUB-100",
    "vendorId": "V-200",
    "componentCount": 12
  }
}
```

```json
{
  "eventType": "SubcontractReceiptPostedEvent",
  "payload": {
    "orderId": "SUB-100",
    "receiptId": "GR-500",
    "quantity": 10
  }
}
```

## Domain Events Consumed

```
- ProductionOrderReleasedEvent (from Production Orders) -> Create subcontract order
- PurchaseOrderApprovedEvent (from Procurement) -> Confirm vendor commitment
- GoodsReceivedEvent (from Procurement) -> Post receipt
```

## Integration Points

- **Procurement**: PO management and vendor coordination
- **Inventory**: component issues and receipts
- **Production Orders**: progress updates

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| SubcontractOrderCreatedEvent | `manufacturing.subcontracting.order.created` | 6 | 30d |
| SubcontractComponentsIssuedEvent | `manufacturing.subcontracting.components.issued` | 6 | 30d |
| SubcontractReceiptPostedEvent | `manufacturing.subcontracting.receipt.posted` | 6 | 30d |
| ProductionOrderReleasedEvent (consumed) | `manufacturing.production.order.released` | 6 | 30d |
| PurchaseOrderApprovedEvent (consumed) | `procurement.core.po.approved` | 6 | 30d |
| GoodsReceivedEvent (consumed) | `procurement.receiving.goods.received` | 6 | 30d |

**Consumer Group:** `manufacturing-production-cg`  
**Partition Key:** `orderId` (ensures all events for a subcontract order are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/manufacturing/subcontracting/orders
Content-Type: application/json

{
  "productionOrderId": "PO-100",
  "vendorId": "V-200",
  "components": [
    { "itemId": "COMP-01", "quantity": 5 }
  ]
}
```

## Error Responses

```json
{
  "errorCode": "SUBCONTRACT_PO_NOT_RELEASED",
  "message": "Production order 'PO-999' not found or not released",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "productionOrderId": "PO-999"
  }
}
```

```json
{
  "errorCode": "SUBCONTRACT_COMPONENT_SHORTAGE",
  "message": "Insufficient inventory to issue components",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "orderId": "SUB-100",
    "itemId": "COMP-01",
    "requiredQty": 5,
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

- Vendor traceability for external processing
- Audit trail for component issues and receipts

## References

- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
