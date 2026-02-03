# Master Data Governance Domain Architecture

This directory contains hexagonal architecture specifications for Master Data Governance (MDG) subdomains.

## Subdomain Index

| Subdomain | ADR | Port | Tier | File | Description |
|-----------|-----|------|------|------|-------------|
| **MDM Hub** | ADR-027 | 9701 | Core | [mdm-hub.md](./mdm-hub.md) | Golden records, change requests, approvals |
| **Data Quality Rules** | ADR-027 | 9702 | Core | [mdm-data-quality.md](./mdm-data-quality.md) | Validation rules, quality scoring |
| **Stewardship Workflows** | ADR-027 | 9703 | Core | [mdm-stewardship.md](./mdm-stewardship.md) | Approval queues and SoD |
| **Match & Merge** | ADR-027 | 9704 | Core | [mdm-match-merge.md](./mdm-match-merge.md) | Duplicate detection, survivorship |
| **MDM Analytics** | ADR-027 | 9705 | Core | [mdm-analytics.md](./mdm-analytics.md) | KPI dashboards and trend analysis |

## Documentation Structure

### Complex Subdomains (with subfolders)
- **MDM Hub** (`/hub/`) - 5 detailed files (domain, application, infrastructure, api, events)
- **Data Quality Rules** (`/data-quality/`) - 5 detailed files

### Simple Subdomains (inline documentation)
- **Stewardship Workflows** - Approvals and task queues
- **Match & Merge** - Duplicate detection and merge decisions
- **MDM Analytics** - Quality KPIs and stewardship metrics

## Port Allocation

| Port | Service | Database | Status |
|------|---------|----------|--------|
| 9701 | mdm-hub | chiroerp_mdm_hub | Implemented |
| 9702 | mdm-data-quality | chiroerp_mdm_quality | Implemented |
| 9703 | mdm-stewardship | chiroerp_mdm_hub (shared) | Implemented |
| 9704 | mdm-match-merge | chiroerp_mdm_hub (shared) | Implemented |
| 9705 | mdm-analytics | chiroerp_mdm_quality (shared) | Implemented |

**Note:** Inline modules share their parent service database and run as part of the core service. Port numbers are logical identifiers for documentation purposes.

## Integration Map

```
+---------------------------------------------------------------------------+
|                        MASTER DATA GOVERNANCE                              |
|---------------------------------------------------------------------------|
|                                                                           |
|  Stewardship -> Approval -> Golden Record -> Publish -> All Domains       |
|        ^             |                     |                              |
|        |             v                     v                              |
|  Match/Merge ----> Hub <-------------- Data Quality                        |
|                                                                           |
+---------------------------------------------------------------------------+
```

## Key Integration Points

### Downstream Consumers (Publish Events)
- **All Domains** <- `MasterRecordPublishedEvent`
- **Stewardship** <- `ChangeRequestApprovedEvent`
- **Analytics** <- `DataQualityScoreUpdatedEvent`

### Upstream Dependencies (Consume Events)
- **All Domains** -> `ChangeRequestSubmittedEvent` (change requests)

## References

### Related ADRs
- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md)
- [ADR-014: Authorization Objects & SoD](../../adr/ADR-014-authorization-objects-segregation-of-duties.md)
- [ADR-015: Data Lifecycle Management](../../adr/ADR-015-data-lifecycle-management.md)

### Related Domains
- [Finance Architecture](../finance/README.md)
- [Inventory Architecture](../inventory/README.md)
- [Procurement Architecture](../procurement/README.md)
- [Sales Architecture](../sales/README.md)
- [Manufacturing Architecture](../manufacturing/README.md)
- [CRM Architecture](../crm/README.md)

---

## Phase 1 Status: Complete

**5 modules implemented:**
- MDM Hub
- Data Quality Rules
- Stewardship Workflows
- Match & Merge
- MDM Analytics

**Total documentation:** 16 files (2 overview + 10 detailed subfolder files + 3 inline + 1 README)
