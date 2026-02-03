# Analytics Dashboard Builder - ADR-016

> **Bounded Context:** `analytics-dashboard`  
> **Port:** `9804` (logical, part of analytics-olap service)  
> **Database:** `chiroerp_analytics_olap`  
> **Kafka Consumer Group:** `analytics-olap-cg`

## Overview

Dashboard Builder provides **dashboard definitions, widgets, and sharing** for operational and executive reporting. It consumes KPI and cube data for visualization.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Dashboards, widgets, sharing |
| **Aggregates** | Dashboard, Widget, DashboardShare |
| **Key Events** | DashboardPublishedEvent, DashboardSharedEvent |
| **Integration** | OLAP, KPI Engine |
| **Compliance** | Access control audit trail |

## Key Capabilities

- Dashboard creation and layout management
- Widget library (KPI, charts, tables)
- Sharing and access control
- Export to PDF/CSV

## Domain Model

```
Dashboard
|-- dashboardId
|-- title
`-- widgets[]

Widget
|-- widgetId
|-- type
`-- queryRef
```

## Workflows

1. User creates dashboard and selects widgets.
2. Widgets query KPI and cube data.
3. Dashboard published and shared.
4. Access controlled by role and tenant.

## Domain Events Published

```json
{
  "eventType": "DashboardPublishedEvent",
  "payload": {
    "dashboardId": "DB-100",
    "title": "Executive Overview"
  }
}
```

## Domain Events Consumed

```
- KpiCalculatedEvent (from KPI Engine) -> Refresh widgets
- CubeRefreshedEvent (from OLAP) -> Refresh dashboards
```

## Integration Points

- **KPI Engine**: KPI widgets.
- **OLAP**: dimensional charts.
- **Analytics**: report exports.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| DashboardPublishedEvent | `analytics.dashboard.published` | 6 | 30d |
| DashboardSharedEvent | `analytics.dashboard.shared` | 6 | 30d |
| KpiCalculatedEvent (consumed) | `analytics.kpi.calculated` | 6 | 30d |
| CubeRefreshedEvent (consumed) | `analytics.olap.cube.refreshed` | 6 | 30d |

**Consumer Group:** `analytics-olap-cg`  
**Partition Key:** `dashboardId` (ensures all events for a dashboard are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/analytics/dashboards
Content-Type: application/json

{
  "title": "Executive Overview",
  "widgets": ["KPI_SALES", "KPI_MARGIN"]
}
```

## Error Responses

```json
{
  "errorCode": "DASHBOARD_INVALID_WIDGET",
  "message": "Widget 'INVALID_KPI' not found",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "providedWidget": "INVALID_KPI",
    "availableWidgets": ["KPI_SALES", "KPI_MARGIN", "KPI_PROFIT"]
  }
}
```

```json
{
  "errorCode": "DASHBOARD_SHARE_PERMISSION_DENIED",
  "message": "User does not have permission to share dashboard",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "dashboardId": "DB-100",
    "userId": "user-500",
    "requiredRole": "DASHBOARD_ADMIN"
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

- Dashboard shares audited.
- Role-based access enforced.

## References

- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
