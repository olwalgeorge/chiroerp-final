# Sales Returns & RMA - ADR-025

> **Bounded Context:** `sales-returns`
> **Port:** `9204` (logical, part of sales-core service)
> **Database:** `chiroerp_sales_core`
> **Kafka Consumer Group:** `sales-core-cg`

## Overview

Returns & RMA handles **return authorization, inspection, restocking, and refunds/credit notes**. It coordinates with Inventory and Finance/AR for reversals and credit memos.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | RMA, returns, restock, refunds |
| **Aggregates** | ReturnOrder, RMA, Refund |
| **Key Events** | RMAApprovedEvent, ReturnReceivedEvent, CreditMemoRequestedEvent |
| **Integration** | Inventory restock and AR credit notes |
| **Compliance** | SOX approvals for credits |

## Key Capabilities

- RMA creation and approval
- Inspection and disposition (restock/scrap)
- Refunds and credit notes

## Domain Model

```
ReturnOrder
├── returnId
├── orderId
├── status: Requested | Approved | Received | Closed
└── lines

RMA
├── rmaId
├── returnId
└── approvedBy
```

## Workflows

1. Customer requests return.
2. RMA approved and issued.
3. Return received and inspected.
4. Credit memo requested and issued by AR.

## Domain Events Published

```json
{
  "eventType": "RMAApprovedEvent",
  "payload": {
    "rmaId": "RMA-100",
    "orderId": "SO-10002"
  }
}
```

```json
{
  "eventType": "ReturnReceivedEvent",
  "payload": {
    "returnId": "RET-100",
    "orderId": "SO-10002",
    "receivedAt": "2026-02-05T14:10:00Z"
  }
}
```

```json
{
  "eventType": "CreditMemoRequestedEvent",
  "payload": {
    "returnId": "RET-100",
    "orderId": "SO-10002",
    "amount": 120.00
  }
}
```

## Domain Events Consumed

```
- ShipmentConfirmedEvent (from Fulfillment) -> Allow return window
- CreditMemoIssuedEvent (from Finance/AR) -> Close return
```

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `sales.returns.rma.approved` | Sales Returns | Inventory, Analytics | 6 |
| `sales.returns.return.received` | Sales Returns | Inventory, Quality | 6 |
| `sales.returns.credit-memo.requested` | Sales Returns | Finance/AR | 6 |

## Integration Points

- **Inventory**: Restock or disposal
- **Finance/AR**: Credit memo issuance

## API Endpoints (Examples)

```http
POST /api/v1/sales/returns
Content-Type: application/json

{
  "orderId": "SO-10002",
  "reason": "Damaged"
}
```

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Return request validation failed",
  "timestamp": "2026-02-05T10:00:00Z",
  "requestId": "req-returns-001",
  "details": {
    "violations": [
      {
        "field": "orderId",
        "constraint": "Return window expired",
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

- Approval required for high-value refunds
- Audit trail for return disposition

## References

- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md)
