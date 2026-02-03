# Manufacturing Analytics - ADR-037

> **Bounded Context:** `manufacturing-analytics`  
> **Port:** `9308` (logical, part of manufacturing-production service)  
> **Database:** `chiroerp_manufacturing_production`  
> **Kafka Consumer Group:** `manufacturing-production-cg`

## Overview

Manufacturing Analytics provides **OEE, yield, throughput, and WIP variance** reporting. It aggregates production events into KPI datasets for dashboards and operational reviews.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | OEE, yield, throughput, WIP variance |
| **Aggregates** | ProductionMetric, KPIReport, OeeSnapshot |
| **Key Events** | ProductionKpiPublishedEvent, OeeCalculatedEvent |
| **Integration** | Production Orders, Shop Floor, Costing |
| **Compliance** | KPI audit trail and data retention |

## Key Capabilities

- OEE calculation by work center and line
- Yield and scrap trend analysis
- WIP variance dashboards
- Bottleneck and throughput reporting

## Domain Model

```
ProductionMetric
|-- metricId
|-- metricType: OEE | Yield | Throughput | WIP
|-- value
`-- period

KPIReport
|-- reportId
|-- plantId
|-- period
`-- metrics
```

## Workflows

1. Consume confirmations and scrap events.
2. Aggregate metrics by period and plant.
3. Publish KPI reports for dashboards.

## Domain Events Published

```json
{
  "eventType": "ProductionKpiPublishedEvent",
  "payload": {
    "plantId": "PLANT-01",
    "period": "2026-02",
    "oee": 0.87,
    "yield": 0.98
  }
}
```

```json
{
  "eventType": "OeeCalculatedEvent",
  "payload": {
    "plantId": "PLANT-01",
    "workCenterId": "WC-10",
    "period": "2026-02",
    "oee": 0.87
  }
}
```

## Domain Events Consumed

```
- ProductionConfirmedEvent (from Production Orders) -> Update throughput
- ScrapRecordedEvent (from Shop Floor) -> Update yield
- WIPPostedEvent (from Costing) -> Update variance
```

## Integration Points

- **Analytics Platform**: publish KPI datasets
- **Manufacturing**: operational dashboards

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| ProductionKpiPublishedEvent | `manufacturing.analytics.kpi.published` | 6 | 30d |
| OeeCalculatedEvent | `manufacturing.analytics.oee.calculated` | 6 | 30d |
| ProductionConfirmedEvent (consumed) | `manufacturing.production.order.confirmed` | 6 | 30d |
| ScrapRecordedEvent (consumed) | `manufacturing.shopfloor.scrap.recorded` | 6 | 30d |
| WIPPostedEvent (consumed) | `finance.costing.wip.posted` | 6 | 30d |

**Consumer Group:** `manufacturing-production-cg`  
**Partition Key:** `plantId` / `workCenterId` (ensures all events for a plant are processed in order)

## API Endpoints (Examples)

```http
GET /api/v1/manufacturing/analytics/oee?plantId=PLANT-01&period=2026-02
```

## Error Responses

```json
{
  "errorCode": "ANALYTICS_PLANT_NOT_FOUND",
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
  "errorCode": "ANALYTICS_PERIOD_NOT_CLOSED",
  "message": "KPI data not available for open period",
  "timestamp": "2026-02-02T10:31:00Z",
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

- Data retention for KPI history
- Audit trail for KPI recalculation

## References

- [ADR-037: Manufacturing & Production](../../adr/ADR-037-manufacturing-production.md)
- [ADR-016: Analytics & Reporting Architecture](../../adr/ADR-016-analytics-reporting-architecture.md)
