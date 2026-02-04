# Analytics Embedded Analytics - ADR-016

> **Bounded Context:** `analytics-embedded`
> **Port:** `9806` (logical, part of analytics-olap service)
> **Database:** `chiroerp_analytics_olap`
> **Kafka Consumer Group:** `analytics-olap-cg`

## Overview

Embedded Analytics provides **contextual insights inside transactional screens** (e.g., AR aging on customer, inventory turns on item). It exposes lightweight queries and cached projections for in-app KPIs.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | In-app KPIs, contextual insights |
| **Aggregates** | EmbeddedWidget, InsightCache |
| **Key Events** | EmbeddedWidgetPublishedEvent, InsightCacheRefreshedEvent |
| **Integration** | OLAP, KPI Engine |
| **Compliance** | Tenant data isolation |

## Key Capabilities

- Contextual widgets embedded in ERP screens
- Cache refresh for low-latency KPIs
- Tenant- and role-based access control
- Lightweight query templates

## Domain Model

```
EmbeddedWidget
|-- widgetId
|-- context
`-- queryRef

InsightCache
|-- cacheKey
|-- lastRefresh
`-- payload
```

## Workflows

1. Widget is embedded in a domain screen.
2. Cached KPI fetched for current context.
3. Cache refreshed on KPI or cube updates.

## Domain Events Published

```json
{
  "eventType": "EmbeddedWidgetPublishedEvent",
  "payload": {
    "widgetId": "W-10",
    "context": "CUSTOMER_DETAIL"
  }
}
```

## Domain Events Consumed

```
- KpiCalculatedEvent (from KPI Engine) -> Refresh cache
- CubeRefreshedEvent (from OLAP) -> Refresh cache
```

## Integration Points

- **KPI Engine**: KPI values for widgets.
- **OLAP**: aggregated context queries.
- **ERP Screens**: embedded UI placement.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| EmbeddedWidgetPublishedEvent | `analytics.embedded.widget.published` | 6 | 30d |
| InsightCacheRefreshedEvent | `analytics.embedded.cache.refreshed` | 6 | 30d |
| KpiCalculatedEvent (consumed) | `analytics.kpi.calculated` | 6 | 30d |
| CubeRefreshedEvent (consumed) | `analytics.olap.cube.refreshed` | 6 | 30d |

**Consumer Group:** `analytics-olap-cg`
**Partition Key:** `widgetId` / `cacheKey` (ensures all events for a widget are processed in order)

## API Endpoints (Examples)

```http
GET /api/v1/analytics/embedded/widgets?context=CUSTOMER_DETAIL
```

## Error Responses

```json
{
  "errorCode": "EMBEDDED_WIDGET_NOT_FOUND",
  "message": "No widgets configured for context 'INVALID_CONTEXT'",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "providedContext": "INVALID_CONTEXT",
    "availableContexts": ["CUSTOMER_DETAIL", "ITEM_DETAIL", "ORDER_DETAIL"]
  }
}
```

```json
{
  "errorCode": "EMBEDDED_CACHE_EXPIRED",
  "message": "Insight cache expired, refresh in progress",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "cacheKey": "CUSTOMER_AR_AGING_100",
    "lastRefresh": "2026-02-02T10:00:00Z",
    "estimatedRefreshSeconds": 30
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

- Tenant isolation for embedded queries.
- Audit trail for widget publishing.

## References

- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
