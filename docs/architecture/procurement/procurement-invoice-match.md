# Procurement Invoice Verification - ADR-023

> **Bounded Context:** `procurement-invoice-match`  
> **Port:** `9105` (logical, part of procurement-core service)  
> **Database:** `chiroerp_procurement_core`  
> **Kafka Consumer Group:** `procurement-core-cg`

## Overview

Invoice Verification provides **2-way and 3-way match** between PO, goods receipt, and vendor invoice. It produces match results and exceptions for AP posting and dispute resolution.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | 2-way/3-way match, GR/IR reconciliation |
| **Aggregates** | InvoiceMatch, MatchException, ToleranceRule |
| **Key Events** | InvoiceMatchCompletedEvent, MatchExceptionCreatedEvent |
| **Integration** | AP posting and GR/IR clearing |
| **Compliance** | SOX approval and variance controls |

## Key Capabilities

- 2-way match (PO vs invoice)
- 3-way match (PO vs GR vs invoice)
- Tolerance rules and variance approval
- Exception workflow and dispute resolution

## Domain Model

```
InvoiceMatch
├── matchId
├── poId
├── invoiceId
├── status: Matched | Exception
└── varianceSummary

MatchException
├── exceptionId
├── type: Price | Quantity | Tax
└── status: Open | Resolved

ToleranceRule
├── type
├── percentage
└── amount
```

## Workflows

1. Receive vendor invoice from AP.
2. Perform 2-way/3-way match.
3. If within tolerance, publish InvoiceMatchCompletedEvent.
4. If exception, publish MatchExceptionCreatedEvent for resolution.

## Domain Events Published

```json
{
  "eventType": "InvoiceMatchCompletedEvent",
  "payload": {
    "matchId": "match-001",
    "poId": "PO-2026-000123",
    "invoiceId": "INV-2026-001234",
    "status": "MATCHED"
  }
}
```

```json
{
  "eventType": "MatchExceptionCreatedEvent",
  "payload": {
    "exceptionId": "exc-001",
    "poId": "PO-2026-000123",
    "invoiceId": "INV-2026-001234",
    "type": "PRICE_VARIANCE",
    "varianceAmount": 50.00
  }
}
```

## Domain Events Consumed

```
- VendorInvoicePostedEvent (from AP) -> Start match
- GoodsReceivedEvent (from Receiving) -> 3-way match
```

## Integration Points

- **Finance/AP**: Match results and exceptions
- **Inventory**: GR data for 3-way match

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| InvoiceMatchCompletedEvent | `procurement.invoice.match.completed` | 6 | 30d |
| MatchExceptionCreatedEvent | `procurement.invoice.exception.created` | 6 | 30d |
| VendorInvoicePostedEvent (consumed) | `finance.ap.invoice.posted` | 6 | 30d |
| GoodsReceivedEvent (consumed) | `procurement.receiving.goods.received` | 6 | 30d |

**Consumer Group:** `procurement-core-cg`  
**Partition Key:** `matchId` / `invoiceId` (ensures all events for a match are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/procurement/invoice-matches
Content-Type: application/json

{
  "invoiceId": "INV-2026-001234",
  "poId": "PO-2026-000123"
}
```

## Error Responses

```json
{
  "errorCode": "INVOICE_MATCH_PO_NOT_FOUND",
  "message": "Purchase order 'PO-2026-000999' not found",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "poId": "PO-2026-000999",
    "invoiceId": "INV-2026-001234"
  }
}
```

```json
{
  "errorCode": "INVOICE_MATCH_TOLERANCE_EXCEEDED",
  "message": "Price variance exceeds tolerance threshold",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "poId": "PO-2026-000123",
    "invoiceId": "INV-2026-001234",
    "varianceAmount": 150.00,
    "toleranceAmount": 100.00,
    "requiresApproval": true
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

- Tolerance thresholds require approvals
- Audit trail for match decisions and exceptions

## References

- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md)
