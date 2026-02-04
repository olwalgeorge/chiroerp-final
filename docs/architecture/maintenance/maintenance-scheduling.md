# Plant Maintenance Scheduling - ADR-040

> **Bounded Context:** `maintenance-scheduling`
> **Port:** `9605` (logical, part of maintenance-preventive service)
> **Database:** `chiroerp_maintenance_preventive`
> **Kafka Consumer Group:** `maintenance-preventive-cg`

## Overview

Maintenance Scheduling coordinates **work order calendars, resource availability, and shutdown windows**. It optimizes maintenance execution without disrupting production schedules.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Scheduling, calendars, resource allocation |
| **Aggregates** | MaintenanceCalendar, ScheduleWindow, ResourceAllocation |
| **Key Events** | MaintenanceSchedulePublishedEvent, MaintenanceWindowScheduledEvent |
| **Integration** | Manufacturing, Work Orders |
| **Compliance** | Approval trail for outages |

## Key Capabilities

- Calendar-based scheduling and blackout windows
- Resource allocation by skill/availability
- Shutdown planning and bundling of work orders
- Schedule change audit trail

## Domain Model

```
MaintenanceCalendar
|-- calendarId
|-- blackoutWindows
`-- workingHours

ScheduleWindow
|-- start
|-- end
`-- priority
```

## Workflows

1. Preventive plans generate candidate work orders.
2. Scheduling engine assigns windows and resources.
3. Approved schedules publish to work orders.
4. Manufacturing receives downtime windows.

## Domain Events Published

```json
{
  "eventType": "MaintenanceSchedulePublishedEvent",
  "payload": {
    "scheduleId": "SCH-100",
    "windowStart": "2026-02-10T08:00:00Z",
    "windowEnd": "2026-02-10T12:00:00Z"
  }
}
```

## Domain Events Consumed

```
- MaintenanceScheduleGeneratedEvent (from Preventive) -> Create schedule
- ProductionScheduleUpdatedEvent (from Manufacturing) -> Adjust windows
```

## Integration Points

- **Work Orders**: schedule assignments and priorities.
- **Manufacturing**: downtime windows and constraints.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| MaintenanceSchedulePublishedEvent | `maintenance.scheduling.schedule.published` | 6 | 30d |
| MaintenanceWindowScheduledEvent | `maintenance.scheduling.window.scheduled` | 6 | 30d |
| MaintenanceScheduleGeneratedEvent (consumed) | `maintenance.preventive.schedule.generated` | 6 | 30d |
| ProductionScheduleUpdatedEvent (consumed) | `manufacturing.production.schedule.updated` | 6 | 30d |

**Consumer Group:** `maintenance-preventive-cg`
**Partition Key:** `plantId` / `scheduleId` (ensures all events for a plant are processed in order)

## API Endpoints (Examples)

```http
GET /api/v1/maintenance/schedules?plantId=PLANT-01
```

## Error Responses

```json
{
  "errorCode": "SCHEDULING_PLANT_NOT_FOUND",
  "message": "Plant 'PLANT-99' not found",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "plantId": "PLANT-99"
  }
}
```

```json
{
  "errorCode": "SCHEDULING_WINDOW_CONFLICT",
  "message": "Maintenance window conflicts with production schedule",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "requestedStart": "2026-02-10T08:00:00Z",
    "requestedEnd": "2026-02-10T12:00:00Z",
    "conflictingProductionOrder": "PO-5000",
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

- Outage schedules require approval.
- Schedule changes are tracked for audit.

## References

- [ADR-040: Plant Maintenance](../../adr/ADR-040-plant-maintenance.md)
- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
