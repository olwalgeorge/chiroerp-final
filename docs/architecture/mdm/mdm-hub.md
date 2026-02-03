# Master Data Hub - ADR-027

> **Bounded Context:** `mdm-hub`  
> **Port:** `9701`  
> **Database:** `chiroerp_mdm_hub`  
> **Kafka Consumer Group:** `mdm-hub-cg`

## Overview

Master Data Hub owns **golden records, lifecycle workflows, and domain-specific master entities** (Customer, Vendor, Product, Chart of Accounts, Cost Center). It governs create/change/retire requests and publishes mastered records to consuming domains.

## Quick Reference

| Aspect | Details |
|--------|---------|
| **Domain** | Golden records, change requests, approvals |
| **Aggregates** | MasterRecord, ChangeRequest, ApprovalWorkflow |
| **Key Events** | MasterRecordPublishedEvent, ChangeRequestApprovedEvent |
| **Integration** | All domains consume mastered data |
| **Compliance** | SOX, GDPR, ISO 8000 audit trails |

## Module Documentation

| Module | File | Description |
|--------|------|-------------|
| **Domain Layer** | [hub-domain.md](./hub/hub-domain.md) | Aggregates, entities, value objects, events, services |
| **Application Layer** | [hub-application.md](./hub/hub-application.md) | Commands, queries, ports, handlers |
| **Infrastructure Layer** | [hub-infrastructure.md](./hub/hub-infrastructure.md) | REST, persistence, messaging adapters |
| **REST API** | [hub-api.md](./hub/hub-api.md) | Endpoints and DTOs |
| **Events & Integration** | [hub-events.md](./hub/hub-events.md) | Domain events, consumed events, Avro schemas |

## Bounded Context

```
mdm-hub/
|-- hub-domain/
|-- hub-application/
`-- hub-infrastructure/
```

## Architecture Diagram

```
+-----------------------------------------------------------------------+
|                            MDM HUB                                     |
|-----------------------------------------------------------------------|
|  Change Requests -> Validation -> Approval -> Golden Record -> Publish |
+-----------------------------------------------------------------------+
```

## Key Business Capabilities

1. Golden record creation and survivorship rules.
2. Approval workflows for master data changes.
3. Domain-specific validation (Customer/Vendor/Product/COA/Cost Center).
4. Controlled retirement and dependency checks.

## Integration Points

- **Finance**: COA and cost center master data.
- **Procurement**: vendor masters and status updates.
- **Sales/CRM**: customer masters and hierarchy.
- **Inventory**: product and item master data.

## SLOs & Performance

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Master record publish latency | < 10m p95 | > 20m |
| Change request approval | < 24h p95 | > 48h |
| API availability | 99.9% | < 99.8% |

## Compliance & Audit

- Full change history and approver traceability.
- Segregation of duties between requester and approver.
- Data lineage retained per record.

## Related ADRs

- [ADR-027: Master Data Governance](../../adr/ADR-027-master-data-governance.md)
- [ADR-014: Authorization Objects & SoD](../../adr/ADR-014-authorization-objects-segregation-of-duties.md)
- [ADR-015: Data Lifecycle Management](../../adr/ADR-015-data-lifecycle-management.md)
