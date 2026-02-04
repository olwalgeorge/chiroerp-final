# Procurement Receiving & Inspection - ADR-023

> **Bounded Context:** `procurement-receiving`
> **Port:** `9104` (logical, part of procurement-core service)
> **Database:** `chiroerp_procurement_core`
> **Kafka Consumer Group:** `procurement-core-cg`

## Overview

Receiving manages **goods receipt, service entry, and inspection**. It validates deliveries against POs, creates GR events for Inventory, and captures discrepancies and returns.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Goods receipt, service entry, inspection |
| **Aggregates** | GoodsReceipt, ServiceEntry, InspectionResult |
| **Key Events** | GoodsReceivedEvent, GoodsReceiptPostedEvent, VendorReturnCreatedEvent |
| **Integration** | Inventory stock updates and AP matching |
| **Compliance** | Audit trail, quality inspection controls |

## Key Capabilities

- Goods receipt with quantity and quality checks
- Service entry sheets for services procurement
- Discrepancy handling and vendor returns

## Domain Model

```
GoodsReceipt
├── receiptId
├── poId
├── status: Draft | Posted | Cancelled
└── lines

ServiceEntry
├── entryId
├── poId
└── status

InspectionResult
├── receiptId
├── outcome: Accepted | Rejected | Partial
└── notes
```

## Workflows

1. Receive goods against PO.
2. Perform inspection and record results.
3. Post receipt and publish GoodsReceivedEvent.
4. If rejected, create vendor return.

## Domain Events Published

```json
{
  "eventType": "GoodsReceivedEvent",
  "payload": {
    "receiptId": "gr-001",
    "poId": "PO-2026-000123",
    "locationId": "LOC-100",
    "lines": [
      { "itemId": "item-001", "quantity": 100 }
    ]
  }
}
```

## Domain Events Consumed

```
- PurchaseOrderApprovedEvent (from Procurement Core) -> Expect receipt
```

## Integration Points

- **Inventory**: Stock ledger updates
- **Finance/AP**: GR/IR matching
- **Quality Management**: Inspection results

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| GoodsReceivedEvent | `procurement.receiving.goods.received` | 6 | 30d |
| GoodsReceiptPostedEvent | `procurement.receiving.receipt.posted` | 6 | 30d |
| VendorReturnCreatedEvent | `procurement.receiving.return.created` | 6 | 30d |
| PurchaseOrderApprovedEvent (consumed) | `procurement.core.po.approved` | 6 | 30d |

**Consumer Group:** `procurement-core-cg`
**Partition Key:** `receiptId` / `poId` (ensures all events for a receipt are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/procurement/receipts
Content-Type: application/json

{
  "poId": "PO-2026-000123",
  "locationId": "LOC-100",
  "lines": [
    { "itemId": "item-001", "quantity": 100 }
  ]
}
```

## Error Responses

```json
{
  "errorCode": "RECEIVING_PO_NOT_FOUND",
  "message": "Purchase order 'PO-2026-000999' not found or not approved",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "poId": "PO-2026-000999"
  }
}
```

```json
{
  "errorCode": "RECEIVING_QUANTITY_EXCEEDED",
  "message": "Receipt quantity exceeds remaining PO quantity",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "poId": "PO-2026-000123",
    "itemId": "item-001",
    "remainingQty": 80,
    "attemptedQty": 100
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

- Inspection required for regulated goods
- Audit trail for receipt posting and reversals

## References

- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
