# Plant Maintenance Breakdown Maintenance - ADR-040

> **Bounded Context:** `maintenance-breakdown`
> **Port:** `9604` (logical, part of maintenance-work-orders service)
> **Database:** `chiroerp_maintenance_work_orders`
> **Kafka Consumer Group:** `maintenance-work-orders-cg`

## Overview

Breakdown Maintenance handles **reactive repairs** triggered by equipment failures. It records downtime, raises corrective work orders, and coordinates emergency parts and labor.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Failure notifications, corrective work orders |
| **Aggregates** | BreakdownNotification, DowntimeRecord |
| **Key Events** | BreakdownReportedEvent, CorrectiveWorkOrderCreatedEvent |
| **Integration** | Work Orders, Manufacturing, Inventory |
| **Compliance** | Safety incident logging |

## Key Capabilities

- Breakdown notification and triage
- Downtime tracking and root cause tagging
- Auto-creation of corrective work orders
- Emergency parts reservation

## Domain Model

```
BreakdownNotification
|-- equipmentId
|-- severity
`-- reportedAt

DowntimeRecord
|-- startTime
|-- endTime
`-- reasonCode
```

## Workflows

1. Breakdown reported from shop floor or IoT trigger.
2. Downtime record created and severity classified.
3. Corrective work order created and dispatched.
4. Completion updates downtime and availability metrics.

## Domain Events Published

```json
{
  "eventType": "BreakdownReportedEvent",
  "payload": {
    "equipmentId": "EQ-100",
    "severity": "CRITICAL",
    "reportedAt": "2026-02-02T09:00:00Z"
  }
}
```

```json
{
  "eventType": "CorrectiveWorkOrderCreatedEvent",
  "payload": {
    "workOrderId": "WO-3000",
    "equipmentId": "EQ-100",
    "priority": "P1"
  }
}
```

## Domain Events Consumed

```
- EquipmentStatusChangedEvent (from Equipment Master) -> Failure detection
```

## Integration Points

- **Work Orders**: corrective order creation.
- **Manufacturing**: downtime impact to schedules.
- **Inventory**: emergency parts issues.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| BreakdownReportedEvent | `maintenance.breakdown.reported` | 6 | 30d |
| CorrectiveWorkOrderCreatedEvent | `maintenance.breakdown.workorder.created` | 6 | 30d |
| EquipmentStatusChangedEvent (consumed) | `maintenance.equipment.status.changed` | 6 | 30d |

**Consumer Group:** `maintenance-work-orders-cg`
**Partition Key:** `equipmentId` (ensures all events for equipment are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/maintenance/breakdowns
Content-Type: application/json

{
  "equipmentId": "EQ-100",
  "severity": "CRITICAL",
  "description": "Motor failure"
}
```

## Error Responses

```json
{
  "errorCode": "BREAKDOWN_EQUIPMENT_NOT_FOUND",
  "message": "Equipment 'EQ-999' not found",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "equipmentId": "EQ-999"
  }
}
```

```json
{
  "errorCode": "BREAKDOWN_INVALID_SEVERITY",
  "message": "Invalid severity level 'INVALID'",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "providedSeverity": "INVALID",
    "validSeverities": ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
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

- Safety incident logging and escalation.
- Downtime reason codes for audits.

## References

- [ADR-040: Plant Maintenance](../../adr/ADR-040-plant-maintenance.md)
