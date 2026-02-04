# ADR-045: Enterprise Organizational Model

**Status**: Draft (Not Implemented)
**Date**: 2026-02-03
**Deciders**: Architecture Team, Product Team
**Priority**: P0 (Critical)
**Tier**: Core
**Tags**: organization, master-data, hierarchy, authorization, cross-domain

## Context
ChiroERP domains naturally reference organizational concepts (CompanyCode in Finance, Plant in Inventory, SalesOrg in Sales). To make configuration, authorization, reporting, and integration consistent at enterprise scale, we define a **canonical organizational hierarchy** with explicit relationships, cross-domain mappings, and authorization derivation rules (aligned with patterns used by SAP/Oracle-style ERPs).

### Design Inputs
- `tenantId` is the root scope for all data and operations (ADR-005)
- Finance uses CompanyCode / CostCenter / ProfitCenter (ADR-009, ADR-028)
- Inventory uses Plant / StorageLocation (ADR-024)
- Sales uses SalesOrg / DistributionChannel / Division (ADR-025)
- Procurement uses PurchasingOrg / PurchasingGroup (ADR-023)
- This ADR defines the canonical hierarchy and mapping rules that connect these concepts across domains

### Problem Statement
How do we model enterprise organizational structures in a way that:
1. Provides a single source of truth for organizational hierarchies
2. Enables cross-domain mappings (Plant → CompanyCode, SalesOrg → CompanyCode)
3. Supports org-based authorization (user assigned to Plant → inherits CompanyCode access)
4. Handles complex structures (matrix orgs, shared services, multi-entity groups)

## Decision
Implement a **Canonical Enterprise Organizational Model** with explicit hierarchies, cross-domain assignments, and authorization derivation.

### Core Organizational Entities

#### 1. Tenant (Root Entity)
```kotlin
data class Tenant(
    val tenantId: TenantId,
    val name: String,
    val legalName: String,
    val taxId: String,
    val baseCurrency: CurrencyCode,
    val status: TenantStatus, // ACTIVE, SUSPENDED, CLOSED
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?
)
```

**Cardinality**: 1 Tenant : N CompanyCodes

---

#### 2. Company Code (Legal Entity)
```kotlin
data class CompanyCode(
    val companyCodeId: CompanyCodeId,
    val tenantId: TenantId,
    val code: String, // "1000", "2000"
    val name: String, // "ACME Inc. US", "ACME GmbH"
    val legalName: String,
    val country: CountryCode,
    val currency: CurrencyCode,
    val fiscalYearVariant: FiscalYearVariant,
    val chartOfAccounts: ChartOfAccountsId,
    val language: LanguageCode,
    val address: Address,
    val taxRegistrations: List<TaxRegistration>
)
```

**Purpose**: Independent legal entity with own balance sheet/P&L
**Cardinality**: 1 CompanyCode : N Plants, N CostCenters, N ProfitCenters

---

#### 3. Plant (Location/Site)
```kotlin
data class Plant(
    val plantId: PlantId,
    val companyCodeId: CompanyCodeId, // Assignment
    val code: String, // "P001", "WAREHOUSE-CA"
    val name: String,
    val plantType: PlantType, // MANUFACTURING, WAREHOUSE, RETAIL_STORE, SERVICE_CENTER
    val address: Address,
    val defaultCostCenter: CostCenterId?,
    val valuationArea: ValuationArea, // PLANT_LEVEL, COMPANY_CODE_LEVEL
    val storageLocations: List<StorageLocationId>
)
```

**Purpose**: Physical location for inventory, production, procurement
**Cardinality**: 1 Plant : N StorageLocations

---

#### 4. Storage Location (Sub-Location)
```kotlin
data class StorageLocation(
    val storageLocationId: StorageLocationId,
    val plantId: PlantId, // Assignment
    val code: String, // "SL01", "AISLE-A"
    val name: String,
    val locationType: StorageLocationType, // GENERAL, BLOCKED, QUARANTINE, RETURNS
    val warehouseManaged: Boolean
)
```

**Purpose**: Bin/aisle within a plant for stock management

---

#### 5. Cost Center (Cost Collector)
```kotlin
data class CostCenter(
    val costCenterId: CostCenterId,
    val companyCodeId: CompanyCodeId, // Assignment
    val controllingAreaId: ControllingAreaId?, // Optional for advanced controlling
    val code: String, // "CC-SALES-US", "CC-MKTG-EU"
    val name: String,
    val costCenterCategory: CostCenterCategory, // PRODUCTION, SALES, ADMIN, SERVICE
    val responsible: UserId,
    val validFrom: LocalDate,
    val validTo: LocalDate?
)
```

**Purpose**: Cost accounting dimension (overhead allocation)
**Cardinality**: 1 CostCenter : N Users (responsible), N Assets, N Activities

---

#### 6. Profit Center (Profitability Segment)
```kotlin
data class ProfitCenter(
    val profitCenterId: ProfitCenterId,
    val companyCodeId: CompanyCodeId,
    val controllingAreaId: ControllingAreaId?,
    val code: String, // "PC-PROD-US", "PC-SERVICES-EU"
    val name: String,
    val segment: SegmentId?, // For segment reporting (IFRS 8)
    val responsible: UserId
)
```

**Purpose**: Internal P&L reporting unit (product line, business unit, geography)

---

#### 7. Sales Organization
```kotlin
data class SalesOrganization(
    val salesOrgId: SalesOrganizationId,
    val companyCodeId: CompanyCodeId, // Assignment
    val code: String, // "SO-US", "SO-EMEA"
    val name: String,
    val currency: CurrencyCode,
    val distributionChannels: List<DistributionChannelId>,
    val divisions: List<DivisionId>
)
```

**Purpose**: Sales hierarchy for pricing, credit, order processing
**Cardinality**: 1 SalesOrg : N DistributionChannels : N Divisions

---

#### 8. Purchasing Organization
```kotlin
data class PurchasingOrganization(
    val purchasingOrgId: PurchasingOrganizationId,
    val companyCodeId: CompanyCodeId?, // Can be cross-company (shared services)
    val code: String, // "PO-US", "PO-CENTRAL"
    val name: String,
    val purchasingGroups: List<PurchasingGroupId>
)
```

**Purpose**: Procurement structure for vendor management, sourcing

---

### Organizational Hierarchy Model

#### Entity Relationships
```
Tenant
 └─ CompanyCode (1:N)
     ├─ Plant (1:N)
     │   └─ StorageLocation (1:N)
     ├─ CostCenter (1:N)
     ├─ ProfitCenter (1:N)
     ├─ SalesOrganization (1:N)
     │   ├─ DistributionChannel (1:N)
     │   └─ Division (1:N)
     └─ PurchasingOrganization (1:N)
         └─ PurchasingGroup (1:N)
```

#### Cross-Domain Assignments
```kotlin
data class PlantAssignment(
    val plantId: PlantId,
    val companyCodeId: CompanyCodeId, // Required
    val defaultCostCenterId: CostCenterId?, // Optional
    val defaultProfitCenterId: ProfitCenterId?, // Optional
    val validFrom: LocalDate,
    val validTo: LocalDate?
)

data class SalesOrgAssignment(
    val salesOrgId: SalesOrganizationId,
    val companyCodeId: CompanyCodeId,
    val defaultPlantId: PlantId?, // For order fulfillment
    val creditControlAreaId: CreditControlAreaId?
)

data class PurchasingOrgAssignment(
    val purchasingOrgId: PurchasingOrganizationId,
    val companyCodeId: CompanyCodeId?,
    val plantIds: List<PlantId> // Plants serviced by this PO
)
```

### Organizational Authorization Model

#### Org-Based Access Control
```kotlin
data class UserOrgAssignment(
    val userId: UserId,
    val orgAssignments: List<OrgAssignment>
)

data class OrgAssignment(
    val orgType: OrgType, // COMPANY_CODE, PLANT, COST_CENTER, SALES_ORG
    val orgId: String, // "1000", "P001", "SO-US"
    val accessLevel: AccessLevel, // READ, WRITE, APPROVE
    val inheritDownstream: Boolean // If true, user gets access to child orgs
)

// Example: User assigned to CompanyCode 1000 with inherit=true
// → Automatically gets access to all Plants, CostCenters, SalesOrgs under 1000
```

#### Authorization Derivation Rules
```kotlin
interface OrgAuthorizationService {
    // "Does user have access to this plant?"
    fun hasPlantAccess(userId: UserId, plantId: PlantId): Boolean {
        // 1. Check direct plant assignment
        if (userHasDirectAssignment(userId, PLANT, plantId)) return true

        // 2. Check inherited from CompanyCode
        val plant = plantRepository.findById(plantId)
        if (userHasAssignment(userId, COMPANY_CODE, plant.companyCodeId, inheritDownstream=true)) {
            return true
        }

        // 3. Check tenant-level admin
        return userHasTenantAdminRole(userId)
    }
}
```

### Multi-Entity Complexity Patterns

#### Pattern 1: Shared Services
```
Tenant: Global Corp
├─ CompanyCode 1000 (US Legal Entity)
│   ├─ Plant P001 (US Warehouse)
│   └─ CostCenter CC-US-ADMIN
├─ CompanyCode 2000 (Germany Legal Entity)
│   ├─ Plant P002 (DE Warehouse)
│   └─ CostCenter CC-DE-ADMIN
└─ PurchasingOrg PO-GLOBAL (no CompanyCode assignment)
    └─ Services plants P001, P002 (cross-company)
```

#### Pattern 2: Matrix Organization
```
Product Line Profit Centers (horizontal)
├─ PC-PRODUCT-A
└─ PC-PRODUCT-B

Geographic Profit Centers (vertical)
├─ PC-REGION-EMEA
└─ PC-REGION-APAC

→ Sales Order assigned to both (dual profit center allocation)
```

#### Pattern 3: Intercompany Relationships
```kotlin
data class IntercompanyRelationship(
    val buyingCompanyCode: CompanyCodeId,
    val sellingCompanyCode: CompanyCodeId,
    val pricingAgreement: PricingAgreementId,
    val paymentTerms: PaymentTermsId,
    val automaticElimination: Boolean
)
```

### Domain Integration Patterns

#### Finance Domain
```kotlin
// GL Posting requires CompanyCode
data class JournalEntry(
    val companyCodeId: CompanyCodeId, // Required
    val costCenterId: CostCenterId?, // Optional
    val profitCenterId: ProfitCenterId?, // Optional
    val lines: List<JournalEntryLine>
)
```

#### Inventory Domain
```kotlin
// Stock movement requires Plant + StorageLocation
data class MaterialDocument(
    val plantId: PlantId, // Required
    val storageLocationId: StorageLocationId, // Required
    val movementType: MovementType,
    val companyCodeId: CompanyCodeId // Derived from Plant assignment
)
```

#### Sales Domain
```kotlin
// Sales Order requires SalesOrg + DistributionChannel + Division
data class SalesOrder(
    val salesOrgId: SalesOrganizationId,
    val distributionChannelId: DistributionChannelId,
    val divisionId: DivisionId,
    val soldToPartyId: CustomerId,
    val plantId: PlantId, // For fulfillment
    val companyCodeId: CompanyCodeId // Derived from SalesOrg assignment
)
```

### Master Data Distribution

#### Organizational Data as Master Data
```kotlin
// Organizational entities are master data governed by MDM domain
sealed class OrgEntity {
    abstract val orgId: OrgId
    abstract val tenantId: TenantId
    abstract val effectiveFrom: LocalDate
    abstract val effectiveTo: LocalDate?
}

// MDM Hub publishes organizational changes
data class OrgEntityChangedEvent(
    val eventType: String = "OrgEntityChangedEvent",
    val eventId: EventId,
    val timestamp: Instant,
    val tenantId: TenantId,
    val payload: OrgEntityChangePayload
)
```

#### Change Propagation Rules
```
OrgEntityChangedEvent (MDM Hub)
 ↓
Finance Service (updates CompanyCode cache)
 ↓
Inventory Service (updates Plant → CompanyCode mapping)
 ↓
Sales Service (updates SalesOrg → CompanyCode mapping)
```

### Time-Dependent Organizational Data

#### Effective Dating
```kotlin
data class OrgEntityVersion(
    val orgId: OrgId,
    val version: Int,
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?,
    val changeType: ChangeType, // CREATED, UPDATED, CLOSED
    val snapshot: OrgEntity // Immutable snapshot
)

// Example: Plant P001 reassigned from CompanyCode 1000 → 2000 on 2026-07-01
PlantVersion(
    plantId = "P001",
    version = 1,
    effectiveFrom = 2026-01-01,
    effectiveTo = 2026-06-30,
    snapshot = Plant(plantId="P001", companyCodeId="1000", ...)
)

PlantVersion(
    plantId = "P001",
    version = 2,
    effectiveFrom = 2026-07-01,
    effectiveTo = null,
    snapshot = Plant(plantId="P001", companyCodeId="2000", ...)
)
```

**Reporting Rule**: Use org version effective at transaction date (not current).

### Configuration Integration

#### Org-Based Configuration (ADR-044 Integration)
```kotlin
// Pricing schema can vary by SalesOrg
val pricingSchema = configRepository.getPricingSchema(
    tenantId = order.tenantId,
    salesOrgId = order.salesOrgId // Org-specific override
)

// Posting rules vary by CompanyCode
val postingRule = configRepository.getPostingRule(
    tenantId = invoice.tenantId,
    companyCodeId = invoice.companyCodeId,
    transactionType = INVOICE_POST
)
```

## Alternatives Considered

### 1. Domain-Specific Org Models Only
**Pros**: Simple; each domain owns its org structure
**Cons**: Duplication and inconsistent semantics; weak cross-domain mappings and authorization derivation
**Decision**: Rejected in favor of a canonical organizational model

### 2. Flat Org Model (No Hierarchy)
**Pros**: Simple to implement
**Cons**: Cannot model complex enterprises, no inheritance
**Decision**: Rejected (enterprise requirement)

### 3. Flexible/Generic Hierarchy (JSON-based)
**Pros**: Maximum flexibility
**Cons**: No type safety, hard to query, performance issues
**Decision**: Rejected (type safety critical for ERP)

### 4. External Org Management System
**Pros**: Offload complexity
**Cons**: Integration complexity, latency, not suitable for transactional data
**Decision**: Rejected (org data is core master data)

## Consequences

### Positive
- ✅ **Single Source of Truth**: Canonical org model across all domains
- ✅ **Cross-Domain Consistency**: Plant → CompanyCode mappings enforced
- ✅ **Org-Based Authorization**: User assignments propagate hierarchically
- ✅ **Multi-Entity Support**: Handles complex group structures
- ✅ **Auditability**: Time-dependent org changes tracked
- ✅ **SAP-Grade Capability**: Matches SAP's org model sophistication

### Negative
- ❌ **Complexity**: Organizational model is a large domain itself
- ❌ **Migration Burden**: Existing deployments need org data migration
- ❌ **Performance**: Hierarchical authorization checks add latency
- ❌ **Learning Curve**: Admins need training on org structure concepts

### Neutral
- Organizational changes require careful change management (affects many domains)
- Time-dependent org data increases storage requirements
- Some flexibility lost vs generic hierarchy (trade-off for type safety)

## Compliance

### Audit Requirements
- All org structure changes logged with approval workflow
- Historical org structure preserved for regulatory reporting
- Segregation of duties: Org creation vs assignment

### Data Residency
- CompanyCode can specify data residency region
- Cross-border data transfer rules enforced at org level

## Performance Standards

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Org Lookup Latency | < 10ms p95 | > 20ms |
| Authorization Check Latency | < 20ms p95 | > 50ms |
| Org Hierarchy Query | < 50ms p95 | > 100ms |
| Org Cache Hit Rate | > 98% | < 95% |

## Implementation Plan

### Phase 1: Foundation (Months 1-2)
- ✅ Core org entity models (Tenant, CompanyCode, Plant, CostCenter)
- ✅ Org repository with tenant scoping
- ✅ Time-dependent versioning
- ✅ Basic MDM integration

### Phase 2: Cross-Domain Assignments (Month 3)
- ✅ Plant → CompanyCode assignment
- ✅ SalesOrg → CompanyCode assignment
- ✅ PurchasingOrg → Plant assignment
- ✅ Domain service integration (Finance, Inventory, Sales)

### Phase 3: Authorization (Months 4-5)
- ✅ Org-based access control model
- ✅ Authorization derivation service
- ✅ Integration with ADR-014 (SoD)
- ✅ User org assignment UI

### Phase 4: Advanced Features (Month 6)
- ✅ Shared services patterns
- ✅ Matrix organization support
- ✅ Intercompany relationships
- ✅ Org-based configuration (ADR-044 integration)

## References

### Related ADRs
- ADR-005: Multi-Tenancy (tenant as root entity)
- ADR-014: Authorization Objects & SoD (org-based authorization)
- ADR-027: Master Data Governance (org data as master data)
- ADR-044: Configuration & Rules Framework (org-based config)
- ADR-009: Financial Accounting Domain (CompanyCode, CostCenter)
- ADR-024: Inventory Management (Plant, StorageLocation)
- ADR-025: Sales & Distribution (SalesOrg structure)

### Industry References
- SAP Enterprise Structure (Client, Company Code, Plant, SalesOrg)
- Oracle Fusion Org Structures (Legal Entity, Business Unit, Inventory Org)
- Workday Organizations (Company, Cost Center, Business Unit)

---

**Note**: This ADR establishes the canonical organizational foundation for multi-entity operations, org-based authorization, and org-scoped configuration inheritance across domains.
