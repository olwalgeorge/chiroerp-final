# Quality Analytics - ADR-039

> **Bounded Context:** `quality-analytics`
> **Port:** `9507` (logical, part of quality-execution service)
> **Database:** `chiroerp_quality_execution`
> **Kafka Consumer Group:** `quality-execution-cg`

## Overview

Quality Analytics provides **PPM, yield, SPC charts, and cost-of-quality metrics**. It aggregates inspection outcomes, nonconformances, and CAPA effectiveness to support continuous improvement.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | PPM, FPY, SPC, Pareto analysis |
| **Aggregates** | QualityKPI, SPCChart, DefectPareto |
| **Key Events** | QualityKPICalculatedEvent, ControlLimitExceededEvent |
| **Integration** | Manufacturing, Procurement, Analytics |
| **Compliance** | Auditability of KPI calculations |

## Key Capabilities

- PPM and yield calculations by plant, product, supplier
- SPC control charts with threshold alerts
- Pareto analysis for defect types
- Cost-of-quality reporting

## Domain Model

```
QualityKPI
|-- kpiId
|-- metric
|-- value
`-- period

SPCChart
|-- chartId
|-- characteristicId
|-- ucl
`-- lcl
```

## Workflows

1. Inspection results and NCs are aggregated per period.
2. KPI engine calculates yield and defect rates.
3. SPC charts detect out-of-control processes.
4. Alerts trigger investigations or CAPA.

## Domain Events Published

```json
{
  "eventType": "QualityKPICalculatedEvent",
  "payload": {
    "metric": "PPM",
    "value": 180,
    "period": "2026-02"
  }
}
```

```json
{
  "eventType": "ControlLimitExceededEvent",
  "payload": {
    "characteristicId": "CH-10",
    "ucl": 5.0,
    "lcl": 1.0,
    "observed": 5.4
  }
}
```

## Domain Events Consumed

```
- InspectionCompletedEvent (from Quality Execution) -> Update KPIs
- NonconformanceCreatedEvent (from Nonconformance) -> Defect analysis
- CAPAClosedEvent (from CAPA) -> Effectiveness scoring
```

## Integration Points

- **Manufacturing**: process stability and yield reporting.
- **Procurement**: supplier PPM metrics.
- **Analytics**: enterprise dashboards.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| QualityKPICalculatedEvent | `quality.analytics.kpi.calculated` | 6 | 30d |
| ControlLimitExceededEvent | `quality.analytics.control.exceeded` | 6 | 30d |
| InspectionCompletedEvent (consumed) | `quality.execution.inspection.completed` | 6 | 30d |
| NonconformanceCreatedEvent (consumed) | `quality.nonconformance.created` | 6 | 30d |
| CAPAClosedEvent (consumed) | `quality.capa.closed` | 6 | 30d |

**Consumer Group:** `quality-execution-cg`
**Partition Key:** `plantId` / `productId` (ensures all events for a plant/product are processed in order)

## API Endpoints (Examples)

```http
GET /api/v1/quality/analytics/kpis?metric=PPM&period=2026-02
```

## Error Responses

```json
{
  "errorCode": "QUALITY_ANALYTICS_INVALID_METRIC",
  "message": "Invalid metric type 'XYZ'",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "providedMetric": "XYZ",
    "validMetrics": ["PPM", "FPY", "YIELD", "SCRAP_RATE"]
  }
}
```

```json
{
  "errorCode": "QUALITY_ANALYTICS_PERIOD_NOT_CLOSED",
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

- KPI calculation rules are versioned.
- Audit trail for metric changes.

## References

- [ADR-039: Quality Management](../../adr/ADR-039-quality-management.md)
- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
