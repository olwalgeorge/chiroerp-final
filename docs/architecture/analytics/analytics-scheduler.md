# Analytics Report Scheduler - ADR-016

> **Bounded Context:** `analytics-scheduler`
> **Port:** `9805` (logical, part of analytics-warehouse service)
> **Database:** `chiroerp_analytics_warehouse`
> **Kafka Consumer Group:** `analytics-warehouse-cg`

## Overview

Report Scheduler manages **scheduled report execution, delivery, and retention**. It orchestrates recurring exports for finance, operations, and compliance reporting.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Report scheduling, delivery, retention |
| **Aggregates** | ReportSchedule, DeliveryTarget |
| **Key Events** | ReportScheduledEvent, ReportDeliveredEvent |
| **Integration** | Data Warehouse, Dashboard Builder |
| **Compliance** | Report retention policies |

## Key Capabilities

- Cron-based report schedules
- Delivery via email/SFTP/portal
- Retention and purge policies
- Failure retry and escalation

## Domain Model

```
ReportSchedule
|-- scheduleId
|-- cron
`-- reportId

DeliveryTarget
|-- type
`-- address
```

## Workflows

1. User schedules report with delivery targets.
2. Scheduler triggers report generation.
3. Report delivered and logged.
4. Retention policy enforces purge.

## Domain Events Published

```json
{
  "eventType": "ReportScheduledEvent",
  "payload": {
    "scheduleId": "SCH-200",
    "reportId": "RPT-10",
    "cron": "0 0 6 * * *"
  }
}
```

```json
{
  "eventType": "ReportDeliveredEvent",
  "payload": {
    "scheduleId": "SCH-200",
    "deliveryTarget": "finance@company.com"
  }
}
```

## Domain Events Consumed

```
- WarehouseLoadCompletedEvent (from Data Warehouse) -> Trigger reports
```

## Integration Points

- **Data Warehouse**: report source data.
- **Dashboard Builder**: export dashboards.
- **Compliance**: retention policies.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| ReportScheduledEvent | `analytics.scheduler.report.scheduled` | 6 | 30d |
| ReportDeliveredEvent | `analytics.scheduler.report.delivered` | 6 | 30d |
| WarehouseLoadCompletedEvent (consumed) | `analytics.warehouse.load.completed` | 6 | 30d |

**Consumer Group:** `analytics-warehouse-cg`
**Partition Key:** `scheduleId` (ensures all events for a schedule are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/analytics/reports/schedules
Content-Type: application/json

{
  "reportId": "RPT-10",
  "cron": "0 0 6 * * *",
  "targets": ["finance@company.com"]
}
```

## Error Responses

```json
{
  "errorCode": "SCHEDULER_INVALID_CRON",
  "message": "Invalid cron expression '0 0 25 * * *'",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "providedCron": "0 0 25 * * *",
    "error": "Day of month must be between 1-31"
  }
}
```

```json
{
  "errorCode": "SCHEDULER_DELIVERY_FAILED",
  "message": "Report delivery failed after 3 retries",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "scheduleId": "SCH-200",
    "deliveryTarget": "invalid@company.com",
    "lastError": "SMTP connection refused"
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

- Report delivery audit logs retained.
- Retention policies enforced by schedule type.

## References

- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
- [ADR-015: Data Lifecycle Management](../../adr/ADR-015-data-lifecycle-management.md)
