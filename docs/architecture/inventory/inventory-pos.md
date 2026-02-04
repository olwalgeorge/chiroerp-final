# Inventory POS Synchronization - ADR-024

> **Bounded Context:** `inventory-pos`
> **Port:** `9003` (logical, part of inventory-core service)
> **Database:** `chiroerp_inventory_core`
> **Kafka Consumer Group:** `inventory-core-cg`

## Overview

POS Synchronization keeps **store-level stock** accurate by consuming POS sales, returns, voids, and offline batches. It supports store transfers, ship-from-store, and shrinkage reconciliation while posting adjustments to the stock ledger and GL.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Store stock, POS event ingestion, reconciliation |
| **Aggregates** | StoreLocation, StoreStock, POSBatch, Reconciliation |
| **Key Events** | POSSalePostedEvent, POSReturnPostedEvent, StoreStockReconciledEvent |
| **Integration** | Core inventory updates and GL posting |
| **Compliance** | Audit trail, cash/stock reconciliation |

## Key Capabilities

- POS sales, returns, and void ingestion
- Offline POS batch reconciliation
- Store-to-store transfer tracking
- Shrinkage and variance reporting
- Ship-from-store availability updates

## Domain Model

```
StoreLocation
├── storeId
├── locationId
└── timezone

StoreStock
├── storeId
├── itemId
├── onHand
├── reserved
└── inTransit

POSBatch
├── batchId
├── storeId
├── postedAt
└── events: List<POSLineEvent>

Reconciliation
├── reconciliationId
├── storeId
├── date
├── varianceAmount
└── status: Open | Approved | Posted
```

## Workflows

### POS Sale Event Flow
1. POS publishes sale event (online or batch).
2. Inventory POS validates and posts stock issue.
3. Reservation or allocation updated if applicable.
4. GL posting generated for sales and COGS.

### Offline Reconciliation
1. POS batch uploaded at close-of-day.
2. Variances compared to system stock.
3. Approved adjustments posted with reason codes.

## Domain Events Published

### POSSalePostedEvent

```json
{
  "eventType": "POSSalePostedEvent",
  "payload": {
    "storeId": "STORE-01",
    "saleId": "SALE-1001",
    "itemId": "item-001",
    "quantity": 2,
    "amount": 50.00,
    "currency": "USD"
  }
}
```

### StoreStockReconciledEvent

```json
{
  "eventType": "StoreStockReconciledEvent",
  "payload": {
    "storeId": "STORE-01",
    "date": "2026-02-10",
    "varianceAmount": -25.00,
    "reason": "SHRINKAGE"
  }
}
```

## Domain Events Consumed

```
- POSSaleEvent (from POS) -> Issue stock
- POSReturnEvent (from POS) -> Receive stock
- StoreTransferEvent (from Inventory Core) -> Update store on-hand
```

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `inventory.pos.pos-sale.posted` | Inventory POS | Inventory Core, Analytics | 6 |
| `inventory.pos.pos-return.posted` | Inventory POS | Inventory Core, Analytics | 6 |
| `inventory.pos.store-stock.reconciled` | Inventory POS | Finance/GL, Analytics | 6 |

## Integration Points

- **POS Systems**: Event ingestion (online + offline)
- **Inventory Core**: Stock ledger updates and reservations
- **Finance/GL**: Sales and shrinkage postings

## API Endpoints (Examples)

```http
POST /api/v1/inventory/stores/{storeId}/pos-batches
Content-Type: application/json

{
  "batchId": "BATCH-2026-02-10",
  "postedAt": "2026-02-10T23:00:00Z",
  "events": [
    { "type": "SALE", "itemId": "item-001", "quantity": 2, "amount": 50.00 }
  ]
}
```

```http
POST /api/v1/inventory/stores/{storeId}/reconcile
Content-Type: application/json

{
  "date": "2026-02-10",
  "varianceAmount": -25.00,
  "reason": "SHRINKAGE"
}
```

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "POS batch validation failed",
  "timestamp": "2026-02-10T23:05:00Z",
  "requestId": "req-pos-001",
  "details": {
    "violations": [
      {
        "field": "events[0].quantity",
        "constraint": "Quantity must be greater than zero",
        "rejectedValue": 0
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

- Audit trail for POS batches and adjustments
- Cash/stock reconciliation at store close
- Segregation of duties for reconciliation approvals

## References

- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md)
- [ADR-010: REST Validation Standard](../../adr/ADR-010-rest-validation-standard.md)
