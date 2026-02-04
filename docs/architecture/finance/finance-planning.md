# Finance FP&A (Planning) - ADR-032

> **Bounded Context:** `finance-planning`
> **Port:** `8092`
> **Database:** `chiroerp_finance_planning`
> **Kafka Consumer Group:** `finance-planning-cg`

## Overview

FP&A provides budgeting, forecasting, and variance analysis. It aggregates actuals from GL and produces plan versions, scenarios, and management reports.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Budgeting, forecasting, scenarios, variance analysis |
| **Aggregates** | Budget, Forecast, PlanVersion, Scenario, VarianceReport |
| **Key Events** | BudgetApprovedEvent, ForecastPublishedEvent, VarianceCalculatedEvent |
| **GL Integration** | Actuals ingestion from GL |
| **Compliance** | Audit trail for plan approvals |

## Bounded Context

```
finance-planning/
├── planning-domain/
├── planning-application/
└── planning-infrastructure/
```

---

## Domain Layer (`planning-domain/`)

### Aggregates & Entities

```
src/main/kotlin/com.erp.finance.planning.domain/
├── model/
│   ├── budget/
│   │   ├── Budget.kt               # Aggregate Root
│   │   ├── BudgetId.kt
│   │   └── BudgetLine.kt
│   ├── forecast/
│   │   ├── Forecast.kt             # Aggregate Root
│   │   ├── ForecastId.kt
│   │   └── ForecastLine.kt
│   └── scenario/
│       ├── Scenario.kt             # Aggregate Root
│       ├── ScenarioId.kt
│       └── PlanVersion.kt
```

### Domain Events

```
├── BudgetApprovedEvent.kt
├── ForecastPublishedEvent.kt
└── VarianceCalculatedEvent.kt
```

---

## Application Layer (`planning-application/`)

### Commands

```
├── CreateBudgetCommand.kt
├── ApproveBudgetCommand.kt
├── PublishForecastCommand.kt
└── CreateScenarioCommand.kt
```

### Queries

```
├── GetBudgetQuery.kt
├── GetForecastQuery.kt
└── GetVarianceReportQuery.kt
```

### Output Ports

```
├── BudgetRepository.kt
├── ForecastRepository.kt
└── ActualsReadPort.kt
```

---

## Infrastructure Layer (`planning-infrastructure/`)

### REST Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/planning/budgets` | Create budget |
| POST | `/api/v1/planning/budgets/{id}/approve` | Approve budget |
| POST | `/api/v1/planning/forecasts` | Publish forecast |
| GET | `/api/v1/planning/reports/variance` | Variance report |

**Error Response (422 Unprocessable Entity)** - Per ADR-010:
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "Budget approval validation failed",
  "timestamp": "2026-02-10T12:10:00Z",
  "requestId": "req-planning-001",
  "details": {
    "violations": [
      {
        "field": "budgetId",
        "constraint": "Budget not found or not in DRAFT status",
        "rejectedValue": "BUD-2026-Q1"
      }
    ]
  }
}
```

### Messaging
- Consumes `JournalEntryPostedEvent` for actuals
- Publishes `BudgetApprovedEvent` and `ForecastPublishedEvent`

## Kafka Topics

| Topic | Producer | Consumers | Partitions |
|-------|----------|-----------|------------|
| `finance.planning.budget.approved` | Finance Planning | Finance/GL, Analytics | 6 |
| `finance.planning.forecast.published` | Finance Planning | Analytics, Reporting | 6 |
| `finance.planning.variance.calculated` | Finance Planning | Analytics, Reporting | 6 |

---

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Variance Report Query | < 500ms p95 | > 1s |
| Forecast Publish | < 2s p95 | > 5s |
| API Availability | 99.9% | < 99.8% |

## Compliance & Audit

- Audit trail for plan changes and approvals
- Segregation of duties for budget approvals

## Related ADRs

- [ADR-032: FP&A](../../adr/ADR-032-budgeting-planning-fpa.md) - Planning domain decisions
- [ADR-009: Financial Domain](../../adr/ADR-009-financial-accounting-domain.md) - Core financial architecture
- [ADR-003: Event-Driven Integration](../../adr/ADR-003-event-driven-integration.md) - Kafka integration patterns
