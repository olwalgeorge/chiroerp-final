# Manufacturing Capacity Planning - ADR-037

> **Bounded Context:** `manufacturing-capacity`
> **Port:** `9306` (logical, part of manufacturing-production service)
> **Database:** `chiroerp_manufacturing_production`
> **Kafka Consumer Group:** `manufacturing-production-cg`

## Overview

Capacity Planning provides **work center capacity calendars, constraints, and leveling** to ensure feasible production schedules. It evaluates load vs available capacity and produces adjustments for Production Orders and Shop Floor.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Capacity calendars, constraints, leveling |
| **Aggregates** | WorkCenterCapacity, CapacityCalendar, Constraint |
| **Key Events** | CapacityPlanCreatedEvent, CapacityConstraintDetectedEvent |
| **Integration** | Production Orders, Shop Floor, MRP |
| **Compliance** | Audit trail for overrides |

## Key Capabilities

- Capacity calendars by work center and shift
- Load vs capacity analysis
- Bottleneck detection and alerts
- Manual leveling and override approvals

## Domain Model

```
WorkCenterCapacity
|-- workCenterId
|-- calendarId
|-- availableHours
`-- utilization

CapacityConstraint
|-- constraintId
|-- workCenterId
|-- date
`-- severity
```

## Workflows

1. MRP creates planned orders with rough-cut capacity.
2. Capacity engine evaluates load by work center.
3. Constraints generate alerts and re-sequencing suggestions.
4. Approved changes update production order schedules.

## Domain Events Published

```json
{
  "eventType": "CapacityPlanCreatedEvent",
  "payload": {
    "plantId": "PLANT-01",
    "horizonDays": 30,
    "workCenterCount": 12
  }
}
```

```json
{
  "eventType": "CapacityConstraintDetectedEvent",
  "payload": {
    "workCenterId": "WC-10",
    "date": "2026-02-12",
    "severity": "HIGH",
    "overloadHours": 12
  }
}
```

## Domain Events Consumed

```
- ProductionOrderReleasedEvent (from Production Orders) -> Recalculate load
- RoutingUpdatedEvent (from BOM Mgmt) -> Rebuild capacity plans
- FinancialPeriodClosedEvent (from Period Close) -> Freeze schedules
```

## Integration Points

- **Production Orders**: schedule adjustments
- **Shop Floor**: dispatch priorities
- **MRP**: feedback to planning

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| CapacityPlanCreatedEvent | `manufacturing.capacity.plan.created` | 6 | 30d |
| CapacityConstraintDetectedEvent | `manufacturing.capacity.constraint.detected` | 6 | 30d |
| ProductionOrderReleasedEvent (consumed) | `manufacturing.production.order.released` | 6 | 30d |
| RoutingUpdatedEvent (consumed) | `manufacturing.bom.routing.updated` | 6 | 30d |
| FinancialPeriodClosedEvent (consumed) | `finance.period.close.completed` | 6 | 30d |

**Consumer Group:** `manufacturing-production-cg`
**Partition Key:** `plantId` / `workCenterId` (ensures all events for a plant are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/manufacturing/capacity/plan
Content-Type: application/json

{
  "plantId": "PLANT-01",
  "horizonDays": 30
}
```

## Error Responses

```json
{
  "errorCode": "CAPACITY_PLANT_NOT_FOUND",
  "message": "Plant 'PLANT-99' not found",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "plantId": "PLANT-99"
  }
}
```

```json
{
  "errorCode": "CAPACITY_CONSTRAINT_APPROVAL_REQUIRED",
  "message": "Cannot override capacity constraint without approval",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "workCenterId": "WC-10",
    "date": "2026-02-12",
    "overloadHours": 12,
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

- Approval required for manual capacity overrides
- Audit trail for schedule changes

## References

- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
