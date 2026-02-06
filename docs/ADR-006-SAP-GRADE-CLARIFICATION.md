# ADR-006 SAP-Grade Pattern Clarification

**Date**: February 6, 2026  
**Status**: ✅ **COMPLIANT** (Clarification Document)  
**Context**: Architecture audit questioned whether `org-model` and `workflow-model` violate ADR-006 governance rules

---

## Executive Summary

✅ **org-model** and **workflow-model** are **CORRECTLY PLACED** in `platform-shared/`

✅ **ADR-006 is COMPLIANT** with SAP-grade architecture patterns

✅ **No architectural changes needed** - only documentation clarification

---

## Question Raised

During architecture audit, the question was raised:
> "Is org and workflow rightly in platform-shared given the governance policy?"

**Initial Concern**: These modules contain entities like `OrganizationUnit`, `CompanyCode`, `WorkflowDefinition` that **appear** to be domain models, which ADR-006 forbids in platform-shared.

**Resolution**: These are **configuration metadata** (SAP IMG pattern), **NOT domain models**. ADR-006 explicitly allows this in Section 5.

---

## SAP-Grade Classification

### **Tier 0: Core Platform Infrastructure** (SAP IMG Equivalent)

| ChiroERP Component | SAP S/4HANA Equivalent | Classification | Purpose |
|-------------------|------------------------|----------------|---------|
| **org-model/** | **IMG → Enterprise Structure**<br>(Company Code, Plant, Cost Center, Org Mgmt) | **Tier 0 - Configuration Metadata** | Defines organizational structures that domains interpret with their own semantics |
| **workflow-model/** | **SAP Business Workflow (SWF)**<br>(Workflow Templates, Work Items, BPEL) | **Tier 0 - Process Infrastructure** | Provides process orchestration primitives that domains configure with business rules |

---

## Key Distinction: Configuration Metadata vs. Domain Models

### ✅ **Configuration Metadata** (ALLOWED in platform-shared)

**Characteristics:**
- Defines **structure** without **business semantics**
- Multiple domains **interpret differently**
- **Metadata** that domains **consume/configure**, not execute
- Like **SAP IMG** = data/configuration, not business logic

**Examples from org-model:**
```kotlin
// ✅ Configuration metadata - multiple interpretations
data class CompanyCode(
    val code: String,        // "1000"
    val name: String,        // "ACME Inc."
    val currency: String     // "USD"
)

// How domains interpret this SAME structure:
// - Finance: P&L entity with chart of accounts
// - Procurement: Contracting party for vendor relationships  
// - Tax Engine: Tax jurisdiction for tax determination
// - Inventory: Valuation area for material pricing
// → SAME STRUCTURE, DIFFERENT SEMANTICS per domain
```

```kotlin
// ✅ Configuration metadata
data class Plant(
    val code: String,
    val companyCode: String,
    val address: Address
)

// How domains interpret this SAME structure:
// - Finance: Valuation area for P&L reporting
// - Inventory: Physical location for stock management
// - Manufacturing: Production site for production orders
// - Procurement: Receiving location for purchase orders
// → SAME STRUCTURE, DIFFERENT SEMANTICS per domain
```

**Examples from workflow-model:**
```kotlin
// ✅ Process infrastructure - domains add business rules
data class WorkflowDefinition(
    val steps: List<ApprovalStep>,
    val routes: List<WorkflowRoute>
)

// How domains configure this SAME structure:
// - P2P: 3-level approval for >$10K (procurement business rule)
// - O2C: Credit check before shipment (sales business rule)
// - HR: Leave approval with delegation (HR business rule)
// - Manufacturing: Production order release with material check
// → SAME STRUCTURE, DIFFERENT BUSINESS RULES per domain
```

---

### ❌ **Domain Models** (FORBIDDEN in platform-shared)

**Characteristics:**
- Defines **behavior** with **business semantics**
- Single domain **owns meaning**
- **Business logic** embedded or implied
- Domain-specific, not reusable across contexts

**Examples (correctly forbidden by ADR-006):**
```kotlin
// ❌ FORBIDDEN: Domain model with business semantics
data class CustomerAddress(
    val street: String,
    val city: String,
    val country: String
)

// Why forbidden: DIFFERENT SEMANTICS per usage
// - Finance: Tax address (strict validation for tax reporting)
// - Sales: Shipping address (delivery instructions allowed)
// - CRM: Contact address (may be incomplete/outdated)
// → Cannot share because semantics CONFLICT, not just differ
```

```kotlin
// ❌ FORBIDDEN: Business logic
class TaxCalculator {
    fun calculateVAT(amount: BigDecimal, country: String): BigDecimal {
        // Business rules specific to Finance context
    }
}

// Why forbidden: Finance-specific business logic
// Not infrastructure, not metadata - belongs in finance-management context
```

---

## SAP Pattern Match: 100% Alignment

### **SAP Architecture Pattern:**

```
┌─────────────────────────────────────────────────────┐
│ SAP IMG (Implementation Guide)                      │
│ = Configuration Layer (Tier 0)                      │
├─────────────────────────────────────────────────────┤
│ ├── Enterprise Structure                            │ ← org-model equivalent
│ │   ├── Define Company Code       (metadata)        │
│ │   ├── Assign Plant to Company   (relationship)    │
│ │   └── Create Cost Center        (metadata)        │
│ ├── Authorization Management                        │ ← derived from org-model
│ │   └── Authorization Objects      (org checks)     │
│ └── Workflow Configuration                          │ ← workflow-model equivalent
│     └── Define Workflow Templates  (metadata)       │
└─────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────┐
│ SAP Application Modules = Domain Layer              │
├─────────────────────────────────────────────────────┤
│ ├── FI (Finance)                                    │ ← interprets Company Code as P&L entity
│ ├── MM (Materials Management)                       │ ← interprets Plant as inventory location
│ ├── PP (Production Planning)                        │ ← interprets Plant as production site
│ └── SD (Sales & Distribution)                       │ ← interprets SalesOrg with CompanyCode mapping
└─────────────────────────────────────────────────────┘

Key Principle: IMG is NOT in any module - it's PLATFORM configuration
               Modules CONSUME IMG configuration with their own semantics
```

### **ChiroERP Mirrors This Exactly:**

```
┌─────────────────────────────────────────────────────┐
│ platform-shared/ = SAP IMG Equivalent                │
│ = Configuration Layer (Tier 0)                      │
├─────────────────────────────────────────────────────┤
│ ├── org-model/                                      │ ← Enterprise Structure
│ │   ├── OrganizationUnit          (metadata)        │
│ │   ├── CompanyCode                (metadata)        │
│ │   ├── Plant                      (metadata)        │
│ │   └── CostCenter                 (metadata)        │
│ └── workflow-model/                                 │ ← Workflow Templates
│     ├── WorkflowDefinition         (metadata)        │
│     └── ApprovalStep                (metadata)        │
└─────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────┐
│ bounded-contexts/ = SAP Modules Equivalent          │
├─────────────────────────────────────────────────────┤
│ ├── financial-management/                           │ ← interprets CompanyCode as P&L
│ ├── inventory-management/                           │ ← interprets Plant as valuation area
│ ├── procurement-management/                         │ ← configures P2P workflow with business rules
│ └── sales-management/                               │ ← configures O2C workflow with business rules
└─────────────────────────────────────────────────────┘

Key Principle: platform-shared is NOT a domain - it's CONFIGURATION infrastructure
               Bounded contexts CONSUME configuration with their own semantics
```

---

## Gap-to-SAP-Grade Roadmap Context

The **Gap-to-SAP-Grade Implementation Plan** explicitly identifies organizational and workflow models as **Phase 1 Critical Gaps**:

### **Phase 1 - Platform Foundation (Months 1-3)**

**Gap #2: Operational Org Model** (Priority: 🔴 CRITICAL)
> "Consistent authorization + scope inheritance"
> 
> **What's Missing**: Manual permissions → security gaps
> 
> **What's Needed**: Authorization scope filters sales orders by user's org unit (100% of queries org-filtered)

**Deliverables (Lines 488-566)**:
- Org hierarchy API (CRUD + traversal)
- **Authorization scope derivation** (user → allowed org units)
- **Org-based data filtering** (sales orders, inventory, invoices)
- Reporting line API (manager chain)
- Org chart UI (visualization + drill-down)

**Success Metric**: 100% of Finance/Sales/Inventory queries filtered by user's org scope

---

### **Why This Is Platform-Shared (Like SAP IMG)**

1. **Cross-Domain Authorization Foundation** (Like SAP PFCG)
   - Finance checks `CompanyCode` authorization
   - Inventory checks `Plant` authorization
   - Sales checks `SalesOrg` authorization
   - **All derive from org-model hierarchy**

2. **Configuration Layer, Not Business Logic** (Like SAP IMG)
   - org-model defines **structure** (CompanyCode "1000" exists)
   - Domains define **semantics** (Finance: CompanyCode = P&L entity; Inventory: Plant = valuation area)
   - **Metadata vs. domain models**

3. **Workflow = Process Orchestration Infrastructure** (Like SAP Business Workflow)
   - workflow-model defines **process primitives** (WorkflowDefinition, ApprovalStep)
   - Domains define **business rules** (P2P: 3-level approval for >$10K; O2C: credit check before shipment)
   - **Infrastructure vs. business logic**

---

## ADR-006 Compliance Analysis

### **Section 5 of ADR-006 Explicitly Allows This:**

```kotlin
// From ADR-006 Section 5: Platform Configuration Metadata (Phase 0 Addition)

// ✅ Organizational model metadata (ADR-045) - SAP Enterprise Structure Equivalent
data class CompanyCode(val code: String, val name: String, val currency: String)
data class Plant(val code: String, val companyCode: String, val address: Address)
data class OrgUnit(val id: UUID, val code: String, val type: OrgUnitType)

// ✅ Workflow infrastructure (ADR-046) - SAP Business Workflow Equivalent
data class WorkflowDefinition(val steps: List<ApprovalStep>)
data class ApprovalContext(val documentType: String, val amount: BigDecimal)
```

**Rationale (Updated February 6, 2026)**:
> "These are **configuration metadata** and **process infrastructure** that enable the SAP-grade configurability strategy:
> 1. Configuration Metadata defines **structure** without **business semantics** - domains interpret differently
> 2. Like **SAP IMG Enterprise Structure** - metadata consumed by modules, not shared domain models"

---

## ADR References Confirming This Pattern

### **ADR-045: Enterprise Organizational Model**

**Lines 11-12**:
> "To make configuration, authorization, reporting, and integration consistent at enterprise scale, we define a **canonical organizational hierarchy** with explicit relationships, cross-domain mappings, and authorization derivation rules (**aligned with patterns used by SAP/Oracle-style ERPs**)."

**Lines 523-525** (References):
- SAP Enterprise Structure (Client, Company Code, Plant, SalesOrg)
- Oracle Fusion Org Structures (Legal Entity, Business Unit, Inventory Org)
- Workday Organizations (Company, Cost Center, Business Unit)

---

## Architectural Principles Satisfied

### ✅ **Bounded Context Autonomy Preserved**

Each bounded context **interprets** org-model structures with its own semantics:

```kotlin
// Financial-Management Context
class FinancialPostingService(private val orgService: OrgHierarchyService) {
    fun postToGeneralLedger(transaction: Transaction) {
        val companyCode = orgService.resolveCompanyCode(transaction.orgUnitId)
        // Interprets as: P&L entity with chart of accounts
        val chartOfAccounts = companyCode.chartOfAccounts
        // Post using Finance semantics
    }
}

// Inventory-Management Context
class InventoryValuationService(private val orgService: OrgHierarchyService) {
    fun valuateMaterial(material: Material) {
        val plant = orgService.resolvePlant(material.orgUnitId)
        // Interprets as: Valuation area for material pricing
        val valuationArea = plant.valuationArea
        // Value using Inventory semantics
    }
}

// ✅ Both use SAME org-model structure, DIFFERENT semantics
// ✅ org-model has NO business logic - pure metadata
// ✅ Contexts remain autonomous - they OWN their interpretations
```

---

## Conclusion

### ✅ **Final Verdict: COMPLIANT**

| Question | Answer | Rationale |
|----------|--------|-----------|
| Are org-model and workflow-model correctly placed in platform-shared? | **✅ YES** | They are configuration metadata (SAP IMG pattern), not domain models |
| Does this violate ADR-006 governance rules? | **✅ NO** | ADR-006 Section 5 explicitly allows "Platform Configuration Metadata" |
| Does this match SAP-grade architecture? | **✅ YES** | 100% alignment with SAP IMG Enterprise Structure and Business Workflow patterns |
| Do bounded contexts remain autonomous? | **✅ YES** | Each context interprets metadata with its own semantics - no shared business logic |
| Is this Phase 0 compliant? | **✅ YES** | Gap-to-SAP-Grade Roadmap Phase 1 identifies this as critical platform foundation |

---

## Recommendations

### ✅ **No Architectural Changes Needed**

The current structure is **correct and compliant**.

### ✅ **Documentation Updated**

- **ADR-006** - Enhanced Section 5 with clearer explanation of configuration metadata vs. domain models
- **This Document** - Provides comprehensive clarification for future reference

### ✅ **Education Material**

For team onboarding, emphasize:
1. **Configuration metadata** (multiple interpretations) vs. **domain models** (single semantic meaning)
2. **SAP IMG pattern** - platform configuration consumed by domains
3. **"Structure without semantics"** - org-model defines WHAT exists, domains define WHAT IT MEANS

---

## References

- [ADR-006: Platform-Shared Governance Rules](./adr/ADR-006-platform-shared-governance.md) - Section 5 (Updated)
- [ADR-045: Enterprise Organizational Model](./adr/ADR-045-enterprise-organizational-model.md) - SAP/Oracle pattern alignment
- [ADR-046: Workflow & Approval Engine](./adr/ADR-046-workflow-approval-engine.md) - Workflow infrastructure
- [Gap-to-SAP-Grade Roadmap](./architecture/gap-to-sap-grade-roadmap.md) - Phase 1 platform foundation
- [COMPLETE_STRUCTURE.txt](../COMPLETE_STRUCTURE.txt) - Lines 258-500 (platform-shared structure)

---

**Status**: ✅ **RESOLVED** - No violations, documentation clarified  
**Date**: February 6, 2026  
**Reviewed By**: Architecture Audit Team
