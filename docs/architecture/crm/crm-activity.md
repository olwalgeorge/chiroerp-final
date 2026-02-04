# CRM Activity - ADR-043

> **Bounded Context:** `crm-activity`
> **Port:** `9406` (logical, part of crm-service-orders service)
> **Database:** `chiroerp_crm_service_orders`
> **Kafka Consumer Group:** `crm-service-orders-cg`

## Overview

Activity records **calls, emails, meetings, and tasks** for customer interactions. It provides a unified activity timeline for Customer 360 and Pipeline users.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Activities, tasks, interaction history |
| **Aggregates** | ActivityLog, Task, Interaction |
| **Key Events** | ActivityLoggedEvent, TaskCompletedEvent |
| **Integration** | Customer 360, Pipeline |
| **Compliance** | Audit trail for customer interactions |

## Key Capabilities

- Log calls, emails, meetings
- Task reminders and completion
- Attachments and notes

## Domain Model

```
ActivityLog
|-- activityId
|-- customerId
|-- type: Call | Email | Meeting
`-- timestamp

Task
|-- taskId
|-- ownerId
`-- status
```

## Workflows

1. User logs interaction against a customer.
2. Activity timeline updates Customer 360.
3. Pipeline records engagement metrics.

## Domain Events Published

```json
{
  "eventType": "ActivityLoggedEvent",
  "payload": {
    "activityId": "ACT-100",
    "customerId": "CUST-100",
    "type": "CALL"
  }
}
```

## Domain Events Consumed

```
- CustomerProfileUpdatedEvent (from Customer 360) -> Refresh timeline
```

## Integration Points

- **Customer 360**: timeline display
- **Pipeline**: engagement scoring

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| ActivityLoggedEvent | `crm.activity.log.created` | 6 | 30d |
| TaskCompletedEvent | `crm.activity.task.completed` | 6 | 30d |
| CustomerProfileUpdatedEvent (consumed) | `crm.customer.profile.updated` | 6 | 30d |

**Consumer Group:** `crm-service-orders-cg`
**Partition Key:** `customerId` (ensures all events for a customer are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/crm/activities
Content-Type: application/json

{
  "customerId": "CUST-100",
  "type": "CALL",
  "notes": "Discussed renewal"
}
```

## Error Responses

```json
{
  "errorCode": "ACTIVITY_CUSTOMER_NOT_FOUND",
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
  "errorCode": "ACTIVITY_INVALID_TYPE",
  "message": "Invalid activity type 'INVALID'",
  "timestamp": "2026-02-02T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "providedType": "INVALID",
    "validTypes": ["CALL", "EMAIL", "MEETING", "TASK"]
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

- Audit trail for interactions

## References

- [ADR-043: CRM and Customer Management](../../adr/ADR-043-crm-customer-management.md)
