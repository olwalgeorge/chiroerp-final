# ADR-043: CRM & Customer Management (Add-on)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-01
**Deciders**: Architecture Team, Product Team
**Priority**: Medium
**Tier**: Add-on
**Tags**: crm, customer, pipeline, interactions, service

## Context
The draft architecture includes a CRM bounded context, but the ADR suite does not define CRM capabilities. A CRM module is valuable for sales pipeline management, customer interactions, and account health, but is not required for the core ERP to function. This ADR defines CRM as an optional add-on with tight integration to Sales, Finance, and Service domains.

## Decision
Adopt a **CRM & Customer Management** domain as an add-on that provides customer profiles, interaction history, and pipeline management, integrating with SD and Finance for customer lifecycle and revenue visibility.

### Scope
- Customer 360 profile, contacts, and interaction history.
- Opportunity and pipeline management.
- Account health scoring and retention workflows.
- Marketing consent and communication preferences.

### Key Capabilities
- **Customer Master (CRM view)**: enriched profile tied to MDG records.
- **Pipeline Management**: stages, probability, forecast rollups.
- **Activity Tracking**: calls, emails, meetings, tasks.
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
