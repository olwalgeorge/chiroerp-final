# ADR-027: Master Data Governance (MDG)

**Status**: Draft (Not Implemented)  
**Date**: 2026-02-01  
**Deciders**: Architecture Team, Data Governance Team  
**Priority**: P2 (Medium)  
**Tier**: Advanced  
**Tags**: master-data, governance, data-quality, mdg

## Context
Master data (customers, vendors, products, accounts) must be consistent across contexts. This ADR defines governance processes (validation, approvals, stewardship) and golden record management to support financial correctness, compliance, and cross-domain consistency at enterprise scale.

## Decision
Implement a **Master Data Governance** capability with lifecycle workflows, validation rules, and golden record management.

### Scope
- Core domains: Customer, Vendor, Product, Chart of Accounts, Cost Centers.
- Approval workflows for create/update/merge.
- Data quality scoring and stewardship dashboards.

### Core Capabilities
- **Golden record** management with survivorship rules.
- **Duplicate detection** and merge workflows.
- **Validation rules** per domain (format, completeness, reference integrity).
- **Hierarchy management** (product/category, customer groups, GL structures).
- **Stewardship**: role-based approval and audit trails.
- **Change requests**: versioned edits with impact analysis.
- **Reference data**: controlled code lists and effective-date management.

### Data Model (Conceptual)
- `MasterRecord`, `ChangeRequest`, `MergeRule`, `ValidationRule`, `DataQualityScore`, `Hierarchy`.

### Key Workflows
- **Create/Change**: request -> validate -> approve -> publish.
- **Merge**: duplicate detection -> review -> merge -> propagate.
- **Retire**: deactivation with dependency checks.

### Integration Points
- **All bounded contexts** consuming master data.
- **Authorization/SoD**: segregation between request and approval.
- **Data lifecycle**: retention and lineage for master data changes.

### Non-Functional Constraints
- **Data quality**: 95%+ completeness for critical domains.
- **Latency**: master data propagation within 10 minutes.
- **Auditability**: full change history retained.

## Alternatives Considered
- **Decentralized master data**: rejected (inconsistent data and audit risk).
- **Manual stewardship only**: rejected (not scalable).
- **External MDM platform**: rejected initially (integration complexity).

## Consequences
### Positive
- Consistent golden records across contexts.
- Reduced downstream reconciliation issues.
- Strong auditability for master data changes.

### Negative
- Requires governance roles and stewardship processes.
- Additional workflow overhead for business users.

### Neutral
- Some low-risk domains may stay decentralized early on.

## Compliance
- **SOX**: controlled changes to vendor/customer masters.
- **GDPR**: PII quality and minimization.
- **ISO 8000**: data quality management alignment.

## Implementation Plan
- Phase 1: Define domains and approval workflows.
- Phase 2: Golden record model and duplicate detection.
- Phase 3: Stewardship dashboards and quality KPIs.
- Phase 4: Cross-context synchronization and lineage.

## References
### Related ADRs
- ADR-005: Multi-Tenancy Data Isolation Strategy
- ADR-009: Financial Accounting Domain Strategy
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-015: Data Lifecycle Management

### Internal Documentation
- `docs/data/mdg_requirements.md`

### External References
- SAP Master Data Governance (MDG)
- ISO 8000 Data Quality
