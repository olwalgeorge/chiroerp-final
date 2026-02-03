# Master Data Stewardship Workflows - ADR-027

> **Bounded Context:** `mdm-stewardship`  
> **Port:** `9703` (logical, part of mdm-hub service)  
> **Database:** `chiroerp_mdm_hub`  
> **Kafka Consumer Group:** `mdm-hub-cg`

## Overview

Stewardship Workflows manage **role-based approvals, task queues, and audit trails** for master data changes. It enforces segregation of duties and tracks approval SLAs.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Approval workflows, stewardship tasks |
| **Aggregates** | StewardTask, ApprovalQueue |
| **Key Events** | ChangeRequestAssignedEvent, ChangeRequestApprovedEvent |
| **Integration** | MDM Hub, Authorization |
| **Compliance** | SOX, SoD enforcement |

## Key Capabilities

- Role-based approval routing
- SLA tracking for change requests
- Escalations for overdue approvals
- Audit trail for approval decisions

## Domain Model

```
StewardTask
|-- taskId
|-- changeRequestId
`-- status

ApprovalQueue
|-- queueId
|-- role
`-- pendingCount
```

## Workflows

1. MDM Hub creates change request.
2. Stewardship assigns task to approver group.
3. Approver accepts/rejects with justification.
4. MDM Hub publishes approved changes.

## Domain Events Published

```json
{
  "eventType": "ChangeRequestAssignedEvent",
  "payload": {
    "changeRequestId": "CR-100",
    "assigneeRole": "DATA_STEWARD"
  }
}
```

```json
{
  "eventType": "ChangeRequestApprovedEvent",
  "payload": {
    "changeRequestId": "CR-100",
    "approvedBy": "user-10"
  }
}
```

## Domain Events Consumed

```
- ChangeRequestCreatedEvent (from MDM Hub) -> Create stewardship task
```

## Integration Points

- **Authorization**: role checks and SoD rules.
- **MDM Hub**: change request lifecycle.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| ChangeRequestAssignedEvent | `mdm.stewardship.changerequest.assigned` | 6 | 30d |
| ChangeRequestApprovedEvent | `mdm.stewardship.changerequest.approved` | 6 | 30d |
| ChangeRequestCreatedEvent (consumed) | `mdm.hub.changerequest.created` | 6 | 30d |

**Consumer Group:** `mdm-hub-cg`  
**Partition Key:** `changeRequestId` (ensures all events for a change request are processed in order)

## API Endpoints (Examples)

```http
GET /api/v1/mdm/stewardship/tasks?status=PENDING
```

## Error Responses

```json
{
  "errorCode": "STEWARDSHIP_TASK_NOT_FOUND",
  "message": "Task 'TASK-999' not found",
  "timestamp": "2026-02-03T10:30:00Z",
  "requestId": "req-abc123",
  "details": {
    "taskId": "TASK-999"
  }
}
```

```json
{
  "errorCode": "STEWARDSHIP_INSUFFICIENT_PRIVILEGES",
  "message": "User does not have approval privileges for this domain",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "userId": "user-500",
    "requiredRole": "DATA_STEWARD",
    "domain": "CUSTOMER"
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

- Approver identity and timestamp required.
- Segregation of duties enforced.

## References

- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md)
- [ADR-014: Authorization Objects & SoD](../../adr/ADR-014-authorization-objects-segregation-of-duties.md)
