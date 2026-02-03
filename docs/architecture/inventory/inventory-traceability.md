# Inventory Traceability (Lot & Serial) - ADR-024

> **Bounded Context:** `inventory-traceability`  
> **Port:** `9007` (logical, part of inventory-core service)  
> **Database:** `chiroerp_inventory_core`  
> **Kafka Consumer Group:** `inventory-core-cg`

## Overview

Traceability manages **lot, serial, and expiry tracking** for regulated and high-value goods. It ensures FEFO allocation, recall readiness, and audit-ready trace chains from receipt to shipment.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Lot/batch registry, serial tracking, expiry and recalls |
| **Aggregates** | Lot, Serial, TraceChain, RecallCase |
| **Key Events** | LotCreatedEvent, SerialRegisteredEvent, LotExpiredEvent, RecallInitiatedEvent |
| **Integration** | Core inventory allocations and WMS execution |
| **Compliance** | FDA/FSMA, medical device traceability, audit retention |

## Key Capabilities

- Lot and batch creation at receipt
- Serial number registry with uniqueness enforcement
- Expiry and FEFO allocation rules
- Recall case management and trace chains
- Audit-ready traceability reports

## Domain Model

```
Lot
├── lotNumber
├── itemId
├── manufactureDate
├── expiryDate
└── status: Active | Expired | Recalled

Serial
├── serialNumber
├── itemId
├── lotNumber?
└── status: Active | Returned | Scrapped

TraceChain
├── sourceDocument
├── movementHistory
└── destinationDocument

RecallCase
├── recallId
├── lotNumbers
├── reason
└── status: Open | InProgress | Closed
```

## Workflows

### Lot Creation and Allocation
1. Goods receipt creates a Lot with expiry and attributes.
2. FEFO engine prioritizes lots by earliest expiry.
3. Allocation enforces lot/serial rules per item class.

### Recall Management
1. Recall initiated for a lot or serial range.
2. TraceChain identifies impacted shipments and locations.
3. Blocks further allocations and notifies downstream systems.

## Domain Events Published

### LotCreatedEvent

```json
{
  "eventType": "LotCreatedEvent",
  "payload": {
    "lotNumber": "LOT-2026-02",
    "itemId": "item-001",
    "manufactureDate": "2026-02-01",
    "expiryDate": "2027-02-01"
  }
}
```

### SerialRegisteredEvent

```json
{
  "eventType": "SerialRegisteredEvent",
  "payload": {
    "serialNumber": "SN-000001",
    "itemId": "item-001",
    "lotNumber": "LOT-2026-02"
  }
}
```

### RecallInitiatedEvent

```json
{
  "eventType": "RecallInitiatedEvent",
  "payload": {
    "recallId": "recall-001",
    "lotNumbers": ["LOT-2026-02"],
    "reason": "Supplier contamination alert"
  }
}
```

## Domain Events Consumed

```
- GoodsReceivedEvent (from Procurement) -> Create lot/serial records
- ShipmentConfirmedEvent (from Sales/WMS) -> Update trace chain
- InventoryAdjustmentEvent (from Core) -> Reflect scrapped/expired lots
```

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `inventory.traceability.lot.created` | Inventory Traceability | Inventory Core, Quality | 6 |
| `inventory.traceability.serial.registered` | Inventory Traceability | Inventory Core, Quality | 6 |
| `inventory.traceability.lot.expired` | Inventory Traceability | Inventory Core, WMS | 6 |
| `inventory.traceability.recall.initiated` | Inventory Traceability | Quality, Compliance | 6 |

## Integration Points

- **Inventory Core**: FEFO allocation and stock status updates
- **Warehouse Execution**: Lot/serial-aware picking and confirmation
- **Quality Management**: Quarantine and disposition workflows

## API Endpoints (Examples)

```http
POST /api/v1/inventory/lots
Content-Type: application/json

{
  "itemId": "item-001",
  "lotNumber": "LOT-2026-02",
  "manufactureDate": "2026-02-01",
  "expiryDate": "2027-02-01"
}
```

```http
POST /api/v1/inventory/serials
Content-Type: application/json

{
  "itemId": "item-001",
  "serialNumber": "SN-000001",
  "lotNumber": "LOT-2026-02"
}
```

```http
GET /api/v1/inventory/lots/{lotNumber}/trace
```

```http
POST /api/v1/inventory/recalls
Content-Type: application/json

{
  "lotNumbers": ["LOT-2026-02"],
  "reason": "Supplier contamination alert"
}
```

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Recall creation validation failed",
  "timestamp": "2026-02-10T16:10:00Z",
  "requestId": "req-trace-001",
  "details": {
    "violations": [
      {
        "field": "lotNumbers",
        "constraint": "Lot not found or already recalled",
        "rejectedValue": ["LOT-2026-02"]
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

- Audit retention for lot/serial movements
- FEFO enforced for perishable goods
- Recall traceability within regulatory SLA windows

## References

- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md)
- [ADR-010: REST Validation Standard](../../adr/ADR-010-rest-validation-standard.md)
