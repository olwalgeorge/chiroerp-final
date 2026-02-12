# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records documenting significant architectural choices for the ERP platform.

## Key Documentation
- **[MVP Implementation Roadmap](../MVP-IMPLEMENTATION-ROADMAP.md)**: Trackable tenancy-identity + finance implementation execution plan

## ADR Index

| ADR | Title | Status | Date | Impact | Tier |
|-----|-------|--------|------|--------|------|
| [ADR-001](ADR-001-modular-cqrs.md) | Modular CQRS Implementation | Draft (Not Implemented) | - | High | Core |
| [ADR-002](ADR-002-database-per-context.md) | Database Per Bounded Context | Draft (Not Implemented) | - | High | Core |
| [ADR-003](ADR-003-event-driven-integration.md) | Event-Driven Integration Between Contexts | Accepted (In Progress) | 2026-02-12 | High | Core |
| [ADR-004](ADR-004-api-gateway-pattern.md) | API Gateway Pattern | Draft (Not Implemented) | - | Medium | Core |
| [ADR-005](ADR-005-multi-tenancy-isolation.md) | Multi-Tenancy Data Isolation Strategy | Draft (Not Implemented) | - | High | Core |
| [ADR-006](ADR-006-platform-shared-governance.md) | Platform-Shared Governance Rules | Draft (Not Implemented) | 2025-11-06 | Critical | Core |
| [ADR-007](ADR-007-authn-authz-strategy.md) | Authentication & Authorization Strategy | Draft (Not Implemented) | 2025-11-08 | High | Core |
| [ADR-008](ADR-008-cicd-network-resilience.md) | CI/CD Pipeline Architecture & Network Resilience | **Accepted (Partially Implemented)** | 2026-02-12 | **High** | Core |
| [ADR-009](ADR-009-financial-accounting-domain.md) | Financial Accounting Domain Strategy | Draft (Not Implemented) | 2025-11-13 | High | Core |
| [ADR-010](ADR-010-rest-validation-standard.md) | REST Validation Standard - Enterprise Grade | Accepted (In Progress) | 2026-02-12 | High | Core |
| [ADR-011](ADR-011-saga-pattern-compensating-transactions.md) | Saga Pattern & Compensating Transactions | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-012](ADR-012-tenant-customization-framework.md) | Tenant Customization Framework | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-013](ADR-013-external-integration-patterns-b2b-edi.md) | External Integration Patterns (B2B / EDI) | Draft (Not Implemented) | 2026-02-01 | High | Advanced |
| [ADR-014](ADR-014-authorization-objects-sod.md) | Authorization Objects & Segregation of Duties | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-015](ADR-015-data-lifecycle-management.md) | Data Lifecycle Management (Archiving, Retention, Backup) | Draft (Not Implemented) | 2026-02-01 | High | Advanced |
| [ADR-016](ADR-016-analytics-reporting-architecture.md) | Analytics & Reporting Architecture | Draft (Not Implemented) | 2026-02-01 | High | Advanced |
| [ADR-017](ADR-017-performance-standards-monitoring.md) | Performance Standards & Monitoring | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-018](ADR-018-deployment-operations-strategy.md) | Deployment & Operations Strategy | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-019](ADR-019-testing-standards-e2e-uat-regression.md) | Testing Standards (E2E, UAT, Regression) | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-020](ADR-020-event-driven-architecture-hybrid-policy.md) | Event-Driven Architecture Hybrid Policy | Draft (Not Implemented) | 2025-11-09 | High | Core |
| [ADR-021](ADR-021-fixed-asset-accounting.md) | Fixed Asset Accounting (FI-AA) | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-022](ADR-022-revenue-recognition.md) | Revenue Recognition (ASC 606 / IFRS 15) | Draft (Not Implemented) | 2026-02-01 | High | Advanced |
| [ADR-023](ADR-023-procurement.md) | Procurement (MM-PUR) | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-024](ADR-024-inventory-management.md) | Inventory Management (MM-IM) | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-025](ADR-025-sales-distribution.md) | Sales & Distribution (SD) | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-026](ADR-026-treasury-cash-management.md) | Treasury & Cash Management (TR-CM) | Draft (Not Implemented) | 2026-02-01 | Medium | Advanced |
| [ADR-027](ADR-027-master-data-governance.md) | Master Data Governance (MDG) | Draft (Not Implemented) | 2026-02-01 | Medium | Advanced |
| [ADR-028](ADR-028-controlling-management-accounting.md) | Controlling / Management Accounting (CO) | Draft (Not Implemented) | 2026-02-01 | Critical | Advanced |
| [ADR-029](ADR-029-intercompany-accounting.md) | Intercompany Accounting (IC) | Draft (Not Implemented) | 2026-02-01 | Critical | Advanced |
| [ADR-030](ADR-030-tax-engine-compliance.md) | Tax Engine & Compliance | Draft (Not Implemented) | 2026-02-01 | High | Core |
| [ADR-031](ADR-031-period-close-orchestration.md) | Period Close Orchestration | Draft (Not Implemented) | 2026-02-01 | High | Advanced |
| [ADR-032](ADR-032-budgeting-planning-fpa.md) | Budgeting & Planning (FP&A) | Draft (Not Implemented) | 2026-02-01 | Medium | Advanced |
| [ADR-033](ADR-033-lease-accounting-ifrs16.md) | Lease Accounting (IFRS 16 / ASC 842) | Draft (Not Implemented) | 2026-02-01 | Medium | Add-on |
| [ADR-034](ADR-034-hr-integration-payroll-events.md) | HR Integration & Payroll Events | Draft (Not Implemented) | 2026-02-01 | Medium | Core |
| [ADR-035](ADR-035-esg-sustainability-reporting.md) | ESG & Sustainability Reporting | Draft (Not Implemented) | 2026-02-02 | Low | Add-on |
| [ADR-036](ADR-036-project-accounting.md) | Project Accounting (PS) | Draft (Not Implemented) | 2026-02-02 | Low | Add-on |
| [ADR-037](ADR-037-manufacturing-production.md) | Manufacturing & Production (PP) | Accepted (Planned - Blueprint Defined) | 2026-02-01 | Medium | Add-on |
| [ADR-038](ADR-038-warehouse-execution-wms.md) | Warehouse Execution System (WES/WMS) | Draft (Not Implemented) | 2026-02-01 | Medium | Add-on |
| [ADR-039](ADR-039-quality-management.md) | Quality Management (QM) | Accepted (Production-Ready; Implementation Starting Q2 2027) | 2026-02-05 | Low | Add-on |
| [ADR-040](ADR-040-plant-maintenance.md) | Plant Maintenance (PM) | Draft (Not Implemented) | 2026-02-02 | Low | Add-on |
| [ADR-041](ADR-041-marketplace-domain.md) | Marketplace Domain | Draft (Not Implemented) | 2026-02-01 | Medium | Add-on |
| [ADR-042](ADR-042-field-service-operations.md) | Field Service Operations | Accepted (Production-Ready; Implementation Starting Q2 2027) | 2026-02-05 | Medium | Add-on |
| [ADR-043](ADR-043-crm-customer-management.md) | CRM & Customer Management | Accepted (Planned - Blueprint Defined) | 2026-02-01 | Medium | Add-on |
| [ADR-044](ADR-044-configuration-rules-framework.md) | Configuration & Rules Framework | Draft (Not Implemented) | 2026-02-03 | Critical | Core |
| [ADR-045](ADR-045-enterprise-organizational-model.md) | Enterprise Organizational Model | Draft (Not Implemented) | 2026-02-03 | Critical | Core |
| [ADR-046](ADR-046-workflow-approval-engine.md) | Workflow & Approval Engine | Draft (Not Implemented) | 2026-02-03 | High | Core |
| [ADR-047](ADR-047-localization-regulatory-framework.md) | Localization & Regulatory Framework | Draft (Not Implemented) | 2026-02-03 | High | Core |
| [ADR-048](ADR-048-frontend-architecture.md) | Frontend Architecture | Draft (Not Implemented) | 2026-02-03 | High | Core |
| [ADR-049](ADR-049-extensibility-model-enhancement.md) | Extensibility Model Enhancement | Draft (Not Implemented) | 2026-02-03 | High | Core |
| [ADR-050](ADR-050-public-sector-government.md) | Public Sector & Government Accounting | Accepted (Production-Ready; Implementation Starting Q2 2027) | 2026-02-06 | Low | Add-on |
| [ADR-051](ADR-051-insurance-policy-claims.md) | Insurance Policy & Claims Management | Accepted (Production-Ready; Implementation Starting Q3 2027) | 2026-02-06 | Low | Add-on |
| [ADR-052](ADR-052-contingent-workforce-professional-services.md) | Contingent Workforce & Professional Services Management | Draft (Not Implemented) | 2026-02-03 | Medium | Add-on |
| [ADR-053](ADR-053-fleet-management.md) | Fleet Management | Accepted (Planned - Blueprint Defined) | 2026-02-03 | Low | Add-on |
| [ADR-054](ADR-054-travel-expense-management.md) | Travel & Expense Management (T&E) | Draft (Not Implemented) | 2026-02-03 | High | Advanced |
| [ADR-055](ADR-055-workforce-scheduling-labor-management.md) | Workforce Scheduling & Labor Management (WFM) | Draft (Not Implemented) | 2026-02-03 | Medium | Add-on |

## Tiering Model

This ADR suite is organized into **Core**, **Advanced**, and **Add-on** tiers to support different business niches and scales.

### Tier Definitions

#### **Core Tier** (25 ADRs)
Base platform and essential ERP domains required for all editions.

**Characteristics:**
- Always enabled for all tenants
- Included in base subscription pricing
- Foundational capabilities (FI/GL, Procurement, Inventory, Sales, HR Integration)
- Architecture/platform services (CQRS, API Gateway, Multi-tenancy, Authorization)
- Priority: P1 (Critical/High)

**Examples:** ADR-009 (Financial Accounting), ADR-021 (Fixed Assets), ADR-023 (Procurement), ADR-024 (Inventory), ADR-025 (Sales & Distribution), ADR-030 (Tax Engine), ADR-034 (HR Integration)

#### **Advanced Tier** (12 ADRs)
Enterprise-grade capabilities for complex operations, compliance, and multi-entity needs.

**Characteristics:**
- Optional upgrade from Core
- Enabled per tenant via feature flags
- Premium pricing tier (subscription upgrade or usage-based)
- Supports enterprise complexity (multi-entity consolidation, advanced compliance, FP&A)
- Priority: P1-P2 (High-Medium)

**Examples:** ADR-022 (Revenue Recognition ASC 606), ADR-028 (Controlling/Management Accounting), ADR-029 (Intercompany Accounting), ADR-031 (Period Close Orchestration), ADR-032 (Budgeting & Planning), ADR-054 (Travel & Expense Management)

#### **Add-on Tier** (18 ADRs)
Specialized or industry-specific modules enabled as optional extensions.

**Characteristics:**
- Licensed separately (à la carte pricing)
- Enabled per tenant based on industry/use case
- Domain-specific capabilities (Manufacturing, Fleet, Field Service, Public Sector)
- Prerequisites documented in each ADR (typically requires Core modules)
- Priority: P2-P3 (Medium-Low, Optional)
- Phased implementation common (Foundation → Advanced Features)

**Examples:** ADR-033 (Lease Accounting), ADR-037 (Manufacturing), ADR-038 (WMS), ADR-040 (Plant Maintenance), ADR-042 (Field Service), ADR-050 (Public Sector), ADR-052 (Contingent Workforce), ADR-053 (Fleet Management), ADR-055 (Workforce Scheduling)

### Licensing Model

| Tier | Licensing | Pricing | Activation |
|------|-----------|---------|------------|
| **Core** | Included | Base subscription | Always on |
| **Advanced** | Opt-in upgrade | Premium tier or per-module | Feature flag per tenant |
| **Add-on** | À la carte | Separate license per module | Feature flag + license check |

**Pricing Examples:**
- **Core**: Base ERP subscription (user-based or transaction volume)
- **Advanced**: +30-50% premium for full Advanced suite, or per-module pricing
- **Add-on**: Industry-specific pricing (e.g., per vehicle for Fleet, per contractor for Contingent Workforce, per asset for Plant Maintenance)

### Technical Enablement

**Feature Flags:**
```kotlin
data class TenantConfiguration(
    val tenantId: TenantId,
    val enabledModules: Set<ModuleId>,
    val tier: SubscriptionTier // CORE, ADVANCED, ENTERPRISE
)

enum class ModuleId {
    // Core (always enabled)
    FINANCIAL_ACCOUNTING, FIXED_ASSETS, PROCUREMENT, INVENTORY, SALES_DISTRIBUTION,

    // Advanced (opt-in)
    REVENUE_RECOGNITION, CONTROLLING, INTERCOMPANY, PERIOD_CLOSE, BUDGETING, TRAVEL_EXPENSE,

    // Add-ons (licensed separately)
    LEASE_ACCOUNTING, MANUFACTURING, WMS, PLANT_MAINTENANCE, FIELD_SERVICE,
    FLEET_MANAGEMENT, CONTINGENT_WORKFORCE, WORKFORCE_SCHEDULING, CRM, PUBLIC_SECTOR
}
```

**Tenant Provisioning:**
- Core modules: Auto-enabled during tenant creation
- Advanced modules: Enabled via Admin UI or API (requires subscription upgrade)
- Add-on modules: Enabled after license purchase + prerequisites validation

**Runtime Checks:**
```kotlin
@PreAuthorize("hasModule('PLANT_MAINTENANCE')")
fun createMaintenanceWorkOrder(request: CreateWorkOrderRequest): WorkOrder

// Or via service layer
if (!tenantConfig.hasModule(ModuleId.PLANT_MAINTENANCE)) {
    throw ModuleNotEnabledException("Plant Maintenance requires separate license")
}
```

### Implementation Sequence

**Phase 1: Core Foundation (Months 1-12)**
1. Platform services (ADR-001 to ADR-020)
2. Core financial (ADR-009, ADR-021, ADR-030)
3. Core supply chain (ADR-023, ADR-024, ADR-025)
4. Core HR (ADR-034)

**Phase 2: Advanced Capabilities (Months 13-24)**
1. Revenue Recognition (ADR-022) for SaaS/subscription customers
2. Controlling & Management Accounting (ADR-028)
3. Intercompany & Consolidation (ADR-029)
4. Period Close & FP&A (ADR-031, ADR-032)
5. Travel & Expense (ADR-054)

**Phase 3: Add-on Modules (Months 25+, based on demand)**
1. **Manufacturing vertical** (ADR-037, ADR-038, ADR-039, ADR-040)
2. **Service vertical** (ADR-042, ADR-043, ADR-052)
3. **Specialized compliance** (ADR-033 Lease, ADR-050 Public Sector)
4. **Operations optimization** (ADR-053 Fleet, ADR-055 Workforce Scheduling)

**Guideline:** Build Core first (all customers need it), then Advanced (enterprise demand), then Add-ons (vertical/industry-specific).

### Feature-Level Tiering

Some domains include both Core and Advanced features inside a single ADR. See the **Feature Tiering (Core vs Advanced)** section in these ADRs:
- ADR-009 (Financial Accounting): Core = single-entity ledger, Advanced = multi-entity consolidation
- ADR-021 (Fixed Assets): Core = depreciation/disposal, Advanced = asset transfers/impairment/revaluation
- ADR-023 (Procurement): Core = PO management, Advanced = supplier collaboration/contract management
- ADR-024 (Inventory): Core = stock ledger/valuation, Advanced = ATP/multi-location optimization
- ADR-025 (Sales & Distribution): Core = order-to-cash, Advanced = rebates/commissions/ATP
- ADR-030 (Tax Engine): Core = basic tax calculation, Advanced = vertex/avalara integration
- ADR-034 (HR Integration): Core = employee master/payroll events, Advanced = full T&E (ADR-054)
- ADR-014 (Authorization & SoD): Core = RBAC, Advanced = dynamic SoD enforcement

### Canonical Example

**ADR-024 (Inventory MM-IM)** provides core inventory accounting and controls, while **ADR-038 (WES/WMS)** delivers warehouse execution as an add-on.

- **Core (ADR-024)**: Stock ledger, goods movements, valuation, cycle counting, basic location management
- **Add-on (ADR-038)**: Directed putaway/picking, wave management, RF scanning, cross-docking, automation integration

**Implementation Path:** Start with ADR-024 (Core Inventory) → Add WMS (ADR-038) when warehouse complexity requires optimization.

## What is an ADR?

An Architecture Decision Record (ADR) captures an important architectural decision made along with its context and consequences.

### When to Create an ADR

Create an ADR when:
- Making decisions with **system-wide impact**
- Choosing between **significant alternatives**
- Establishing **patterns** or **conventions**
- Making **irreversible** or **costly-to-change** decisions
- Resolving **contentious** technical debates

### ADR Template

```markdown
# ADR-XXX: [Title]

**Status:** [Proposed | Accepted | Deprecated | Superseded]
**Date:** YYYY-MM-DD
**Context:** [Which phase/component this affects]

## Decision
[What is the decision?]

## Context
[What is the issue we're trying to solve?]

## Alternatives Considered
[What other options did we evaluate?]

## Consequences
### Positive
- [Benefits]

### Negative
- [Drawbacks]

### Neutral
- [Trade-offs]

## Compliance
[How will we enforce this decision?]

## Related ADRs
- [Links to related ADRs]
```

## ADR Lifecycle

```
Proposed → Accepted → [Active]
                    ↓
         Deprecated | Superseded
```

- **Proposed:** Under discussion, not yet enforced
- **Accepted:** Approved by team, being implemented
- **Active:** Fully implemented and enforced
- **Deprecated:** No longer recommended, but not removed
- **Superseded:** Replaced by a newer ADR

## Critical ADRs Requiring Enforcement (Planned)

### 🔴 ADR-006: Platform-Shared Governance (Draft - Not Implemented)

**Why Critical:** Prevents the "distributed monolith" anti-pattern that would destroy bounded context autonomy.

**Enforcement Mechanisms (Planned):**
1. ⬜ **ArchUnit Tests:** `tests/arch/PlatformSharedGovernanceRules.kt` (automated in CI)
2. ⬜ **CI Pipeline:** Fails builds on violations (`.github/workflows/ci.yml`)
3. ⬜ **Weekly Audit:** Automated monitoring every Monday (`.github/workflows/governance-audit.yml`)
4. ⬜ **Local Script:** `./scripts/audit-platform-shared.ps1` for dev feedback
5. ⬜ **Code Review Checklist:** Manual review for platform-shared PRs

**Current Status:** Not implemented (planning phase)

**Next Actions:**
1. Create ArchUnit governance tests
2. Wire tests into CI pipeline
3. Add weekly governance audit workflow
4. Schedule team workshop on bounded context principles

## References

- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions) by Michael Nygard
- *Domain-Driven Design* by Eric Evans
- *Building Evolutionary Architectures* by Neal Ford, Rebecca Parsons, Patrick Kua
