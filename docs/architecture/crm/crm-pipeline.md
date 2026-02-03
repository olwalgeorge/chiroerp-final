# CRM Pipeline - ADR-043

> **Bounded Context:** `crm-pipeline`  
> **Port:** `9402`  
> **Database:** `chiroerp_crm_pipeline`  
> **Kafka Consumer Group:** `crm-pipeline-cg`

## Overview

Pipeline manages **opportunities, stages, forecasts, and win/loss tracking**. It integrates with Sales for quote/order conversion and provides forecasting insights.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Opportunities, stages, forecasts |
| **Aggregates** | Opportunity, PipelineStage, Forecast |
| **Key Events** | OpportunityCreatedEvent, OpportunityStageChangedEvent, OpportunityClosedWonEvent |
| **Integration** | Sales, Customer 360 |
| **Compliance** | Audit trail for pipeline changes |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [pipeline-domain.md](./pipeline/pipeline-domain.md) | Aggregates, entities, value objects, domain events, services |
| **Application Layer** | [pipeline-application.md](./pipeline/pipeline-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [pipeline-infrastructure.md](./pipeline/pipeline-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [pipeline-api.md](./pipeline/pipeline-api.md) | Endpoints and DTOs |
| **Events & Integration** | [pipeline-events.md](./pipeline/pipeline-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
crm-pipeline/
|-- pipeline-domain/
|-- pipeline-application/
`-- pipeline-infrastructure/
```

## Architecture Diagram

```
Lead -> Opportunity -> Stage -> Close Won/Lost -> Publish
```

## Key Business Capabilities

1. Opportunity lifecycle management.
2. Pipeline stage configuration and rules.
3. Forecasting and weighted pipeline.
4. Win/loss analysis.

## Integration Points

- **Sales**: convert opportunities to orders
- **Customer 360**: account context

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Opportunity update latency | < 300ms p95 | > 800ms |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Audit log for stage changes

## Related ADRs

- [ADR-043: CRM and Customer Management](../../adr/ADR-043-crm-customer-management.md)
- [ADR-025: Sales and Distribution](../../adr/ADR-025-sales-distribution.md)
