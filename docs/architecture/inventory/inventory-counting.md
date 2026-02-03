# Inventory Cycle Counting - ADR-024

> **Bounded Context:** `inventory-counting`  
> **Port:** `9004` (logical, part of inventory-core service)  
> **Database:** `chiroerp_inventory_core`  
> **Kafka Consumer Group:** `inventory-core-cg`

## Overview

Cycle Counting provides **continuous stock verification** to maintain high inventory accuracy without full physical shutdowns. It schedules counts by ABC class, executes counts with approvals, and posts variances back to the stock ledger and GL.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Cycle count scheduling, execution, variance approval |
| **Aggregates** | CycleCount, CycleCountLine, VarianceApproval |
| **Key Events** | CycleCountScheduledEvent, CycleCountCompletedEvent, StockAdjustedEvent |
| **GL Integration** | Variance posted (DR/CR Inventory, offset Shrinkage) |
| **Compliance** | SOX controls, audit trail for adjustments |

## Key Capabilities

- ABC-based scheduling (A weekly, B monthly, C quarterly)
- Count execution with blind counts and recounts
- Variance thresholds and approval workflow
- Stock adjustment posting with reason codes
- Accuracy KPIs and shrinkage reporting

## Domain Model

```
CycleCount
├── id
├── locationId
├── status: Planned | InProgress | Completed | Approved | Posted
├── scheduledDate
├── lines: List<CycleCountLine>
└── varianceSummary

CycleCountLine
├── itemId
├── systemQuantity
├── countedQuantity
├── variance
└── varianceReason
```

## Workflows

### Scheduled Cycle Count
1. ABC scheduler creates a CycleCount for a location.
2. Count tasks assigned to counters (blind counts).
3. Counted quantities captured via mobile or scanner.
4. Variances beyond threshold trigger approval.
5. Approved variances post StockAdjusted and GL entries.

### Spot Count
1. Triggered by exception (negative stock, customer issue).
2. Single-item count with immediate review.
3. Adjustment posted after approval.

## Domain Events Published

### CycleCountScheduledEvent

```json
{
  "eventType": "CycleCountScheduledEvent",
  "payload": {
    "cycleCountId": "cc-001",
    "locationId": "LOC-100",
    "scheduledDate": "2026-02-10",
    "strategy": "ABC-A"
  }
}
```

### CycleCountCompletedEvent

```json
{
  "eventType": "CycleCountCompletedEvent",
  "payload": {
    "cycleCountId": "cc-001",
    "locationId": "LOC-100",
    "totalLines": 120,
    "varianceLines": 6,
    "completedAt": "2026-02-10T15:30:00Z"
  }
}
```

### StockAdjustedEvent

```json
{
  "eventType": "StockAdjustedEvent",
  "payload": {
    "adjustmentId": "adj-001",
    "itemId": "item-001",
    "locationId": "LOC-100",
    "quantityDelta": -2,
    "reason": "COUNT_VARIANCE",
    "glEntries": [
      { "accountId": "6550-00", "debit": 25.00, "credit": 0.00 },
      { "accountId": "1400-00", "debit": 0.00, "credit": 25.00 }
    ]
  }
}
```

## Domain Events Consumed

```
- FinancialPeriodClosedEvent (from Finance Close) -> Block adjustments
- InventoryAccuracyPolicyUpdatedEvent (from Governance) -> Update thresholds
```

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `inventory.counting.cycle-count.scheduled` | Inventory Counting | Inventory Core, Analytics | 6 |
| `inventory.counting.cycle-count.completed` | Inventory Counting | Inventory Core, Analytics | 6 |
| `inventory.counting.stock.adjusted` | Inventory Counting | Finance/GL, Analytics | 6 |

## Integration Points

- **Inventory Core**: Stock ledger adjustments and reservations
- **Finance/GL**: Variance postings and audit trail
- **Analytics**: Accuracy KPIs and shrinkage reporting

## API Endpoints (Examples)

```http
POST /api/v1/inventory/cycle-counts
Content-Type: application/json

{
  "locationId": "LOC-100",
  "strategy": "ABC-A",
  "scheduledDate": "2026-02-10"
}
```

```http
POST /api/v1/inventory/cycle-counts/{cycleCountId}/complete
Content-Type: application/json

{
  "lines": [
    { "itemId": "item-001", "countedQuantity": 98 },
    { "itemId": "item-002", "countedQuantity": 50 }
  ]
}
```

```http
POST /api/v1/inventory/cycle-counts/{cycleCountId}/approve
Content-Type: application/json

{
  "approvedBy": "USR-200",
  "varianceReason": "COUNT_VARIANCE"
}
```

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Cycle count approval validation failed",
  "timestamp": "2026-02-10T16:00:00Z",
  "requestId": "req-count-001",
  "details": {
    "violations": [
      {
        "field": "cycleCountId",
        "constraint": "Cycle count must be in COMPLETED status",
        "rejectedValue": "cc-001"
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

- Segregation of duties for count approval
- Immutable audit trail for adjustments
- Period-close enforcement for variance posting

## References

- [ADR-024: Inventory Management](../../adr/ADR-024-inventory-management.md)
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md)
- [ADR-010: REST Validation Standard](../../adr/ADR-010-rest-validation-standard.md)
