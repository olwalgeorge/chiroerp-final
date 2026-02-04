# Plant Maintenance Analytics - ADR-040

> **Bounded Context:** `maintenance-analytics`
> **Port:** `9607` (logical, part of maintenance-preventive service)
> **Database:** `chiroerp_maintenance_preventive`
> **Kafka Consumer Group:** `maintenance-preventive-cg`

## Overview

Maintenance Analytics provides **MTBF, MTTR, availability, and cost KPIs** for equipment reliability and maintenance performance.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Reliability KPIs, downtime analysis |
| **Aggregates** | MaintenanceKPI, AvailabilityMetric |
| **Key Events** | MaintenanceKPICalculatedEvent, DowntimeRecordedEvent |
| **Integration** | Manufacturing, Finance/CO, Analytics |
| **Compliance** | KPI auditability |

## Key Capabilities

- MTBF/MTTR calculations
- Availability and downtime metrics
- Maintenance cost variance analysis
- KPI dashboards and alerts

## Domain Model

```
MaintenanceKPI
|-- metric
|-- value
`-- period

AvailabilityMetric
|-- equipmentId
|-- uptime
`-- downtime
```

## Workflows

1. Work order confirmations and downtime feed KPI engine.
2. Metrics computed per equipment and plant.
3. Alerts triggered for KPI thresholds.
4. Reports exported to analytics layer.

## Domain Events Published

```json
{
  "eventType": "MaintenanceKPICalculatedEvent",
  "payload": {
    "metric": "MTBF",
    "value": 1200,
    "period": "2026-02"
  }
}
```

```json
{
  "eventType": "DowntimeRecordedEvent",
  "payload": {
    "equipmentId": "EQ-100",
    "durationMinutes": 120,
    "reasonCode": "FAILURE"
  }
}
```

## Domain Events Consumed

```
- WorkOrderCompletedEvent (from Work Orders) -> KPI updates
- BreakdownReportedEvent (from Breakdown) -> Downtime metrics
```

## Integration Points

- **Manufacturing**: OEE and production impact.
- **Finance/CO**: maintenance cost reporting.
- **Analytics**: enterprise dashboards.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| MaintenanceKPICalculatedEvent | `maintenance.analytics.kpi.calculated` | 6 | 30d |
| DowntimeRecordedEvent | `maintenance.analytics.downtime.recorded` | 6 | 30d |
| WorkOrderCompletedEvent (consumed) | `maintenance.workorders.order.completed` | 6 | 30d |
| BreakdownReportedEvent (consumed) | `maintenance.breakdown.reported` | 6 | 30d |

**Consumer Group:** `maintenance-preventive-cg`
**Partition Key:** `equipmentId` / `plantId` (ensures all events for equipment are processed in order)

## API Endpoints (Examples)

```http
GET /api/v1/maintenance/analytics/kpis?metric=MTBF&period=2026-02
```

## Error Responses

```json
{
  "errorCode": "MAINTENANCE_ANALYTICS_INVALID_METRIC",
  "message": "Invalid metric type 'XYZ'",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "providedMetric": "XYZ",
    "validMetrics": ["MTBF", "MTTR", "AVAILABILITY", "COST_VARIANCE"]
  }
}
```

```json
{
  "errorCode": "MAINTENANCE_ANALYTICS_PERIOD_NOT_CLOSED",
  "message": "KPI data not available for open period",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "period": "2026-02",
    "status": "OPEN",
    "nextAvailablePeriod": "2026-01"
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

- KPI calculation rules are versioned.
- Audit trail for metric changes.

## References

- [ADR-040: Plant Maintenance](../../adr/ADR-040-plant-maintenance.md)
- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
