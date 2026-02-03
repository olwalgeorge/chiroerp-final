# Master Data Match & Merge - ADR-027

> **Bounded Context:** `mdm-match-merge`  
> **Port:** `9704` (logical, part of mdm-hub service)  
> **Database:** `chiroerp_mdm_hub`  
> **Kafka Consumer Group:** `mdm-hub-cg`

## Overview

Match & Merge provides **duplicate detection, survivorship rules, and merge workflows**. It consolidates records into golden masters and publishes merge results to consuming domains.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Duplicate detection, survivorship, merge workflows |
| **Aggregates** | MatchRule, MergeRule, MergeDecision |
| **Key Events** | DuplicateDetectedEvent, MasterRecordMergedEvent |
| **Integration** | MDM Hub, Data Quality |
| **Compliance** | Merge audit trail |

## Key Capabilities

- Probabilistic and deterministic matching
- Survivorship rules by field and source
- Merge approvals for high-risk records
- Cross-domain propagation of merge results

## Domain Model

```
MatchRule
|-- ruleId
|-- domain
`-- algorithm

MergeDecision
|-- mergeId
|-- survivorId
`-- mergedIds
```

## Workflows

1. Identify duplicates based on match rules.
2. Propose merge with survivorship fields.
3. Steward approves or rejects merge.
4. Publish merged master record.

## Domain Events Published

```json
{
  "eventType": "DuplicateDetectedEvent",
  "payload": {
    "domain": "CUSTOMER",
    "recordIds": ["C-100", "C-101"]
  }
}
```

```json
{
  "eventType": "MasterRecordMergedEvent",
  "payload": {
    "survivorId": "C-100",
    "mergedIds": ["C-101"]
  }
}
```

## Domain Events Consumed

```
- MasterRecordPublishedEvent (from MDM Hub) -> Update match index
```

## Integration Points

- **MDM Hub**: merge decisions and publishing.
- **Data Quality**: match rule inputs and quality thresholds.

## Kafka Topics

All events are published to and consumed from dedicated Kafka topics following the naming pattern `{domain}.{subdomain}.{entity}.{action}`:

| Event | Topic | Partitions | Retention |
|-------|-------|------------|-----------|
| DuplicateDetectedEvent | `mdm.matchmerge.duplicate.detected` | 6 | 30d |
| MasterRecordMergedEvent | `mdm.matchmerge.record.merged` | 6 | 30d |
| MasterRecordPublishedEvent (consumed) | `mdm.hub.record.published` | 6 | 30d |

**Consumer Group:** `mdm-hub-cg`  
**Partition Key:** `domain` / `recordId` (ensures all events for a record are processed in order)

## API Endpoints (Examples)

```http
POST /api/v1/mdm/matches/run?domain=CUSTOMER
```

## Error Responses

```json
{
  "errorCode": "MATCH_MERGE_INVALID_DOMAIN",
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
  "errorCode": "MATCH_MERGE_APPROVAL_REQUIRED",
  "message": "Cannot merge records: approval required for high-risk merge",
  "timestamp": "2026-02-03T10:31:00Z",
  "requestId": "req-def456",
  "details": {
    "survivorId": "C-100",
    "mergedIds": ["C-101", "C-102"],
    "riskLevel": "HIGH",
    "requiresStewardApproval": true
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

- Merge approvals required for critical domains.
- Full audit trail of merged fields.

## References

- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md)
