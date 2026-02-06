# ADR-043: CRM & Customer Management (Add-on)

**Status**: Accepted (Planned - Blueprint Defined)
**Date**: 2026-02-01
**Updated**: 2026-02-06 - Resolved boundary overlap between crm-activity and crm-interactions
**Deciders**: Architecture Team, Product Team
**Priority**: Medium
**Tier**: Add-on
**Tags**: crm, customer, pipeline, interactions, service

## Context
The draft architecture includes a CRM bounded context, but the ADR suite does not define CRM capabilities. A CRM module is valuable for sales pipeline management, customer interactions, and account health, but is not required for the core ERP to function. This ADR defines CRM as an optional add-on with tight integration to Sales, Finance, and Service domains.

## Decision
Adopt a **CRM & Customer Management** domain as an add-on that provides customer profiles, interaction history, and pipeline management, integrating with SD and Finance for customer lifecycle and revenue visibility.

### Bounded Context Architecture

```
crm/                                # CRM Bounded Context (ADR-043)
├── crm-shared/                     # Shared identifiers (ADR-006 compliant)
├── crm-customer360/                # Customer 360 Subdomain (Port 9451)
├── crm-pipeline/                   # Sales Pipeline Subdomain (Port 9452)
├── crm-contracts/                  # Service Contracts Subdomain (Port 9453)
├── crm-activity/                   # Activity & Interaction Tracking (Port 9454)
└── crm-account-health/             # Account Health Subdomain (Port 9455)
```

**Package Structure**: `com.chiroerp.crm.*`
**Port Range**: 9451-9459 (avoids conflict with Maintenance 9401-9411)

**Note**: Ports 9456-9459 are reserved for future expansion (Marketing Campaigns, Customer Service subdomains).

### Scope
- Customer 360 profile, contacts, and interaction history.
- Opportunity and pipeline management.
- Service contracts and entitlement tracking.
- Activity tracking (calls, emails, meetings, tasks) with interaction history.
- Account health scoring and retention workflows.
- Marketing consent and communication preferences.

**Architectural Decision**: `crm-activity` consolidates both activity tracking and interaction history capabilities to avoid subdomain fragmentation. The subdomain handles:
- **Activity Management**: Tasks, appointments, calls, meetings (planned/scheduled)
- **Interaction History**: Email threads, call logs, meeting notes (completed/historical)
- **Engagement Timeline**: Unified customer interaction timeline across channels

### Key Capabilities
- **Customer Master (CRM view)**: enriched profile tied to MDG records.
- **Pipeline Management**: stages, probability, forecast rollups.
- **Activity & Interaction Tracking**: calls, emails, meetings, tasks with full history.
- **Service Contracts**: contract lifecycle, entitlements, renewals.
- **Account Health**: churn risk signals, renewal alerts.

### Integration Points
- **Master Data (ADR-027)**: authoritative customer records.
- **Sales (ADR-025)**: quote/order lifecycle and pipeline conversion.
- **Finance (ADR-009)**: AR aging, credit exposure, payment history.
- **Field Service (ADR-042)**: service history and case linkage.

### Non-Functional Constraints / KPIs
- **Customer profile retrieval**: p95 < 300ms.
- **Pipeline update latency**: p95 < 2 seconds.
- **Data consistency with MDG**: >= 99.9%.
- **Engagement activity capture**: >= 99% within 5 minutes.

## Alternatives Considered
- **External CRM (Salesforce, HubSpot)**: rejected due to integration and licensing cost.
- **Minimal CRM inside SD only**: insufficient for pipeline management.
- **CRM as MDG extension**: rejected to avoid MDG scope creep.

## Consequences
### Positive
- Provides a customer-centric view aligned with ERP financial data.
- Improves sales forecasting and retention workflows.

### Negative / Risks
- Adds non-core complexity for organizations that only need ERP.
- Requires careful data governance to avoid duplicate customer records.

### Neutral
- Optional add-on; can be enabled for growth-stage businesses.

## Compliance
- GDPR/CCPA consent management and right-to-erasure requirements.
- Audit logging for customer data access.

## Implementation Plan
1. Customer profile and interaction model.
2. Opportunity/pipeline engine and forecasting.
3. Integration with MDG and SD.
4. Account health dashboards and reporting.
5. Consent management and compliance controls.

## References
- ADR-027 Master Data Governance
- ADR-025 Sales & Distribution
- ADR-009 Financial Accounting
- ADR-042 Field Service Operations
