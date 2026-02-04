# CRM Account Health - ADR-043

> **Bounded Context:** `crm-account-health`
> **Port:** `9407` (logical, part of crm-service-orders service)
> **Database:** `chiroerp_crm_service_orders`
> **Kafka Consumer Group:** `crm-service-orders-cg`

## Overview

Account Health computes **risk scores and churn signals** using financial, service, and engagement data. It supports proactive account management and renewal workflows.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Health scores, risk signals |
| **Aggregates** | HealthScore, RiskSignal |
| **Key Events** | AccountHealthUpdatedEvent, AtRiskAccountFlaggedEvent |
| **Integration** | Finance/AR, Service Orders, Pipeline |
| **Compliance** | Audit trail for scoring changes |

## Key Capabilities

- Health score calculation by account
- Risk signal tracking and alerts
- Renewal and retention workflows

## Domain Model

```
HealthScore
|-- customerId
|-- score
`-- updatedAt

RiskSignal
|-- signalId
|-- customerId
`-- severity
```

## Workflows

1. Ingest financial and service signals.
2. Compute score and flag at-risk accounts.
3. Publish signals to Pipeline and Customer 360.

## Domain Events Published

```json
{
  "eventType": "AccountHealthUpdatedEvent",
  "payload": {
    "customerId": "CUST-100",
    "score": 62,
    "severity": "HIGH"
  }
}
```

## Domain Events Consumed

```
- CustomerBlockedEvent (from Finance/AR) -> Increase risk
- ServiceOrderCompletedEvent (from Service Orders) -> Improve score
- ActivityLoggedEvent (from Activity) -> Engagement signal
```

## Integration Points

- **Pipeline**: renewal and retention
- **Customer 360**: health indicators

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| AccountHealthUpdatedEvent | `crm.account.health.updated` | 6 | 30d |
| AtRiskAccountFlaggedEvent | `crm.account.risk.flagged` | 6 | 30d |
| CustomerBlockedEvent (consumed) | `finance.ar.customer.blocked` | 6 | 30d |
| ServiceOrderCompletedEvent (consumed) | `crm.service.order.completed` | 6 | 30d |
| ActivityLoggedEvent (consumed) | `crm.activity.log.created` | 6 | 30d |

**Consumer Group:** `crm-service-orders-cg`
**Partition Key:** `customerId` (ensures all events for a customer are processed in order)

## API Endpoints (Examples)

```http
GET /api/v1/crm/account-health/{customerId}
```

## Error Responses

```json
{
  "errorCode": "ACCOUNT_HEALTH_CUSTOMER_NOT_FOUND",
  "message": "Customer 'CUST-999' not found",
  "timestamp": "2026-02-02T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "customerId": "CUST-999"
  }
}
```

```json
{
  "errorCode": "ACCOUNT_HEALTH_SCORE_UNAVAILABLE",
  "message": "Health score calculation in progress",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "customerId": "CUST-100",
    "status": "CALCULATING",
    "estimatedCompletionSeconds": 30
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

- Documented scoring model

## References

- [ADR-043: CRM and Customer Management](../../adr/ADR-043-crm-customer-management.md)
