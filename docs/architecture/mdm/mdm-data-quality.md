# Master Data Quality Rules - ADR-027

> **Bounded Context:** `mdm-data-quality`
> **Port:** `9702`
> **Database:** `chiroerp_mdm_quality`
> **Kafka Consumer Group:** `mdm-quality-cg`

## Overview

Data Quality Rules owns **validation rules, completeness scoring, and reference integrity checks** for master data. It evaluates change requests and produces data quality scores for stewardship dashboards.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Validation rules, data quality scores |
| **Aggregates** | ValidationRule, DataQualityScore |
| **Key Events** | ValidationRuleActivatedEvent, DataQualityScoreUpdatedEvent |
| **Integration** | MDM Hub, Analytics |
| **Compliance** | ISO 8000 data quality |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [data-quality-domain.md](./data-quality/data-quality-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [data-quality-application.md](./data-quality/data-quality-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [data-quality-infrastructure.md](./data-quality/data-quality-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [data-quality-api.md](./data-quality/data-quality-api.md) | Endpoints and DTOs |
| **Events & Integration** | [data-quality-events.md](./data-quality/data-quality-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
mdm-data-quality/
|-- data-quality-domain/
|-- data-quality-application/
`-- data-quality-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                        DATA QUALITY RULES                              |
|-----------------------------------------------------------------------|
|  Rules -> Validation -> Score -> Stewardship Dashboard                 |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Validation rules per domain (format, completeness, reference integrity).
2. Data quality scoring and trend tracking.
3. Automated rule evaluation on change requests.
4. Quality threshold alerts and remediation tasks.

## Integration Points

- **MDM Hub**: validate change requests.
- **Analytics**: data quality KPIs.
- **All domains**: consume quality indicators.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Validation execution | < 1s p95 | > 3s |
| Quality score update | < 2s p95 | > 5s |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Rule changes require approval.
- Audit trail for rule evaluation outcomes.

## Related ADRs

- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md)
- [ADR-016: Analytics & Reporting](../../adr/ADR-016-analytics-reporting-architecture.md)
