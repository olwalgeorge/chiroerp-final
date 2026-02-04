# Quality Supplier Quality - ADR-039

> **Bounded Context:** `quality-supplier-quality`
> **Port:** `9505` (logical, part of quality-execution service)
> **Database:** `chiroerp_quality_execution`
> **Kafka Consumer Group:** `quality-execution-cg`

## Overview

Supplier Quality tracks **vendor performance, incoming inspection history, and approved supplier status**. It produces scorecards and drives dynamic inspection frequency based on quality outcomes.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Supplier scorecards, ASL, quality history |
| **Aggregates** | SupplierScorecard, ApprovedSupplierList, SupplierQualityHistory |
| **Key Events** | VendorQualityScoreUpdatedEvent, ASLStatusChangedEvent |
| **Integration** | Procurement sourcing, Inventory receiving |
| **Compliance** | Supplier qualification and audit trail |

## Key Capabilities

- Vendor scorecards (PPM, delivery, defects)
- Approved Supplier List (ASL) governance
- Dynamic modification rules for inspection frequency
- Quality alerts for supplier degradation

## Domain Model

```
SupplierScorecard
|-- supplierId
|-- ppmDefects
|-- onTimeDelivery
`-- overallRating

ASLRecord
|-- supplierId
|-- status
`-- effectiveFrom
```

## Workflows

1. Incoming inspection results update supplier quality history.
2. Scorecard recalculation updates supplier rating.
3. ASL status changes notify Procurement and Receiving.
4. Trigger rules adjust inspection frequency based on trends.

## Domain Events Published

```json
{
  "eventType": "VendorQualityScoreUpdatedEvent",
  "payload": {
    "supplierId": "SUP-100",
    "overallRating": "A",
    "ppmDefects": 120
  }
}
```

```json
{
  "eventType": "ASLStatusChangedEvent",
  "payload": {
    "supplierId": "SUP-100",
    "status": "APPROVED"
  }
}
```

## Domain Events Consumed

```
- InspectionCompletedEvent (from Quality Execution) -> Update scorecard
- NonconformanceCreatedEvent (from Nonconformance) -> Supplier escalation
```

## Integration Points

- **Procurement**: sourcing decisions and supplier blocking.
- **Inventory**: receiving inspection history.
- **Quality Execution**: inspection outcomes.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| VendorQualityScoreUpdatedEvent | `quality.supplier.score.updated` | 6 | 30d |
| ASLStatusChangedEvent | `quality.supplier.asl.changed` | 6 | 30d |
| InspectionCompletedEvent (consumed) | `quality.execution.inspection.completed` | 6 | 30d |
| NonconformanceCreatedEvent (consumed) | `quality.nonconformance.created` | 6 | 30d |

**Consumer Group:** `quality-execution-cg`
**Partition Key:** `supplierId` (ensures all events for a supplier are processed in order)

## API Endpoints (Examples)

```http
GET /api/v1/quality/suppliers/SUP-100/scorecard
```

## Error Responses

```json
{
  "errorCode": "SUPPLIER_QUALITY_SUPPLIER_NOT_FOUND",
  "message": "Supplier 'SUP-999' not found",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "supplierId": "SUP-999"
  }
}
```

```json
{
  "errorCode": "SUPPLIER_QUALITY_ASL_APPROVAL_REQUIRED",
  "message": "Cannot change ASL status without approval",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "supplierId": "SUP-100",
    "currentStatus": "PENDING",
    "requestedStatus": "APPROVED",
    "approvalRequired": true
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

- ASL changes require approval and audit trail.
- Supplier rating changes recorded with justification.

## References

- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md)
- [ADR-023: Procurement](../../adr/ADR-023-procurement.md)
