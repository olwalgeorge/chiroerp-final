# Master Data Analytics - ADR-027

> **Bounded Context:** `mdm-analytics`  
> **Port:** `9705` (logical, part of mdm-data-quality service)  
> **Database:** `chiroerp_mdm_quality`  
> **Kafka Consumer Group:** `mdm-quality-cg`

## Overview

MDM Analytics provides **data quality KPIs, stewardship performance, and master data health dashboards**. It aggregates quality scores and change request metrics across domains.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Data quality KPIs, stewardship analytics |
| **Aggregates** | DataQualityKPI, StewardshipMetric |
| **Key Events** | DataQualityKPIUpdatedEvent, StewardSlaBreachedEvent |
| **Integration** | MDM Hub, Data Quality Rules |
| **Compliance** | KPI auditability |

## Key Capabilities

- Completeness and accuracy KPIs by domain
- Stewardship SLA tracking and alerts
- Duplicate rate and merge statistics
- Trend reporting for data quality initiatives

## Domain Model

```
DataQualityKPI
|-- domain
|-- completeness
`-- accuracy

StewardshipMetric
|-- approverRole
|-- avgApprovalTime
`-- slaBreaches
```

## Workflows

1. Quality rules publish scores.
2. KPI engine aggregates by domain and time period.
3. SLA breaches trigger stewardship alerts.
4. Reports feed enterprise analytics.

## Domain Events Published

```json
{
  "eventType": "DataQualityKPIUpdatedEvent",
  "payload": {
    "domain": "VENDOR",
    "completeness": 0.97,
    "accuracy": 0.95
  }
}
```

## Domain Events Consumed

```
- DataQualityScoreUpdatedEvent (from Data Quality Rules) -> KPI updates
- ChangeRequestApprovedEvent (from Stewardship) -> SLA metrics
```

## Integration Points

- **MDM Hub**: stewardship metrics and SLAs.
- **Analytics**: enterprise dashboards.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| DataQualityKPIUpdatedEvent | `mdm.analytics.kpi.updated` | 6 | 30d |
| StewardSlaBreachedEvent | `mdm.analytics.sla.breached` | 6 | 30d |
| DataQualityScoreUpdatedEvent (consumed) | `mdm.quality.score.updated` | 6 | 30d |
| ChangeRequestApprovedEvent (consumed) | `mdm.stewardship.changerequest.approved` | 6 | 30d |

**Consumer Group:** `mdm-quality-cg`  
**Partition Key:** `domain` (ensures all events for a domain are processed in order)

## API Endpoints (Examples)

```http
GET /api/v1/mdm/analytics/kpis?domain=CUSTOMER
```

## Error Responses

```json
{
  "errorCode": "MDM_ANALYTICS_INVALID_DOMAIN",
  "message": "Invalid domain 'XYZ'",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "providedDomain": "XYZ",
    "validDomains": ["CUSTOMER", "VENDOR", "PRODUCT", "EMPLOYEE"]
  }
}
```

```json
{
  "errorCode": "MDM_ANALYTICS_KPI_NOT_AVAILABLE",
  "message": "KPI data not yet calculated for domain",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "domain": "CUSTOMER",
    "status": "CALCULATING",
    "estimatedCompletionSeconds": 60
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

- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md)
- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
