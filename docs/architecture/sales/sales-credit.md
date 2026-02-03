# Sales Credit Management - ADR-025

> **Bounded Context:** `sales-credit`  
> **Port:** `9205` (logical, part of sales-core service)  
> **Database:** `chiroerp_sales_core`  
> **Kafka Consumer Group:** `sales-core-cg`

## Overview

Credit Management enforces **customer credit limits, holds, and release workflows**. It integrates with AR for balances and prevents order fulfillment when credit risk thresholds are exceeded.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Credit limits, holds, releases |
| **Aggregates** | CreditAccount, CreditHold |
| **Key Events** | CreditHoldPlacedEvent, CreditReleasedEvent |
| **Integration** | AR balance checks and approvals |
| **Compliance** | SoD controls for overrides |

## Key Capabilities

- Credit limit checks during order creation
- Automated holds and release workflow
- Override approvals with audit trail

## Domain Model

```
CreditAccount
├── customerId
├── creditLimit
├── currentExposure
└── status: Good | Hold | Blocked

CreditHold
├── holdId
├── orderId
└── reason
```

## Workflows

1. Order submitted for approval.
2. Credit check evaluates exposure vs limit.
3. If exceeded, hold placed and approval required.
4. Credit hold released after payment or approval.

## Domain Events Published

```json
{
  "eventType": "CreditHoldPlacedEvent",
  "payload": {
    "orderId": "SO-10002",
    "customerId": "CUST-100",
    "reason": "LIMIT_EXCEEDED"
  }
}
```

## Domain Events Consumed

```
- CreditLimitExceededEvent (from Finance/AR) -> Place hold
- CustomerBlockedEvent (from Finance/AR) -> Block orders
```

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `sales.credit.credit-hold.placed` | Sales Credit | Sales Core, Finance/AR | 6 |
| `sales.credit.credit-hold.released` | Sales Credit | Sales Core, Finance/AR | 6 |

## Integration Points

- **Finance/AR**: Balance and credit exposure
- **Sales Core**: Order approval workflow

## API Endpoints (Examples)

```http
POST /api/v1/sales/credit-holds/{orderId}/release
Content-Type: application/json

{
  "approvedBy": "USR-200",
  "reason": "Manual override"
}
```

**Error Response (403 Forbidden)** - Per ADR-010:
```json
{
  "errorCode": "AUTHORIZATION_FAILED",
  "message": "User lacks permission to release credit hold",
  "timestamp": "2026-02-10T09:30:00Z",
  "requestId": "req-credit-001",
  "details": {
    "requiredPermission": "CREDIT_HOLD_RELEASE",
    "orderId": "SO-10002"
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

- Override approvals with SoD enforcement
- Audit trail for credit decisions

## References

- [ADR-025: Sales & Distribution](../../adr/ADR-025-sales-distribution.md)
- [ADR-009: Financial Accounting Domain](../../adr/ADR-009-financial-accounting-domain.md)
- [ADR-014: Authorization & SoD](../../adr/ADR-014-authorization-objects-sod.md)
