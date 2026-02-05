# ADR-027: Master Data Governance (MDG)

**Status**: Draft (Not Implemented)
**Date**: 2026-02-05
**Deciders**: Architecture Team, Data Governance Team
**Priority**: P2 (Medium)
**Tier**: Advanced
**Tags**: master-data, governance, data-quality, mdg, hexagonal-architecture, federated-ownership

## Context
Master data (customers, vendors, products, accounts) must be consistent across contexts. This ADR defines governance processes (validation, approvals, stewardship) and golden record management to support financial correctness, compliance, and cross-domain consistency at enterprise scale.

**Critical Principle**: MDM is a **governance layer**, not a data storage layer. The actual master data lives in its owning bounded context—MDM orchestrates workflows, enforces quality rules, and tracks lineage without duplicating the source data.

## Decision
Implement a **Master Data Governance** bounded context with lifecycle workflows, validation rules, and governance capabilities following **hexagonal architecture** principles.

### Subdomain Architecture
MDM is implemented as 5 subdomains within the `mdm/` bounded context:

```
mdm/                                             # Master Data Governance (ADR-027)
├── mdm-shared/                                  # Shared types for MDM context
├── mdm-hub/                                     # Port 9701 - Golden Record Management
├── mdm-data-quality/                            # Port 9702 - Validation & Quality Scoring
├── mdm-stewardship/                             # Port 9703 - Approval Workflows & SoD
├── mdm-duplicate-detection/                     # Port 9704 - Match & Merge
└── mdm-hierarchy/                               # Port 9705 - Cross-Domain Hierarchies
```

#### 1. MDM Hub (Port 9701)
**Package**: `com.chiroerp.mdm.hub`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `MasterRecord`, `MasterRecordVersion`, `RecordAttribute`, `SurvivorshipRule`, `RecordLineage`, `ChangeRequest`, `ChangeDetail`, `ImpactAnalysis`, `ApprovalWorkflow`, `ApprovalStep` | Hub entities |
| **Domain Events** | `MasterRecordCreatedEvent`, `MasterRecordUpdatedEvent`, `MasterRecordPublishedEvent`, `MasterRecordRetiredEvent`, `ChangeRequestSubmittedEvent`, `ChangeRequestApprovedEvent`, `ChangeRequestRejectedEvent` | Hub events |
| **Input Ports** | `MasterRecordUseCase`, `ChangeRequestUseCase`, `ApprovalUseCase` | Hub use cases |
| **Output Ports** | `MasterRecordRepository`, `ChangeRequestRepository`, `MasterDataEventPublisher` | Persistence and integration |
| **Domain Services** | `SurvivorshipService`, `LineageTrackingService`, `DependencyCheckService` | Business logic |

#### 2. Data Quality (Port 9702)
**Package**: `com.chiroerp.mdm.dataquality`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `ValidationRule`, `ValidationResult`, `DataQualityScore`, `QualityDimension`, `CompletenessCheck`, `AccuracyCheck`, `ConsistencyCheck`, `TimelinessCheck` | Quality entities |
| **Domain Events** | `ValidationExecutedEvent`, `QualityScoreCalculatedEvent`, `QualityThresholdBreachedEvent` | Quality events |
| **Input Ports** | `ValidationUseCase`, `QualityScoreUseCase` | Quality use cases |
| **Output Ports** | `ValidationRuleRepository`, `QualityScoreRepository`, `QualityEventPublisher` | Persistence and integration |
| **Domain Services** | `ValidationEngine`, `QualityScoringService`, `DataProfilingService` | Business logic |

#### 3. Stewardship (Port 9703)
**Package**: `com.chiroerp.mdm.stewardship`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `Steward`, `StewardRole`, `DataDomain`, `ApprovalMatrix`, `EscalationRule`, `StewardshipTask`, `AuditTrail` | Stewardship entities |
| **Domain Events** | `TaskAssignedEvent`, `TaskCompletedEvent`, `EscalationTriggeredEvent`, `AuditRecordedEvent` | Stewardship events |
| **Input Ports** | `StewardUseCase`, `TaskUseCase`, `AuditUseCase` | Stewardship use cases |
| **Output Ports** | `StewardRepository`, `TaskRepository`, `AuditRepository`, `StewardshipEventPublisher` | Persistence and integration |
| **Domain Services** | `TaskRoutingService`, `EscalationService`, `AuditService` | Business logic |

#### 4. Duplicate Detection (Port 9704)
**Package**: `com.chiroerp.mdm.duplicatedetection`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `MatchRule`, `MatchResult`, `MatchScore`, `DuplicateCluster`, `MergeRequest`, `MergeDecision`, `SurvivorRecord` | Match & merge entities |
| **Domain Events** | `DuplicatesDetectedEvent`, `MergeRequestedEvent`, `MergeCompletedEvent`, `ClusterResolvedEvent` | Match & merge events |
| **Input Ports** | `MatchUseCase`, `MergeUseCase` | Match & merge use cases |
| **Output Ports** | `MatchRuleRepository`, `DuplicateClusterRepository`, `MergeEventPublisher` | Persistence and integration |
| **Domain Services** | `MatchingEngine`, `MergeService`, `SurvivorshipEngine` | Business logic |

#### 5. Hierarchy Management (Port 9705)
**Package**: `com.chiroerp.mdm.hierarchy`

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain Models** | `MasterHierarchy`, `HierarchyNode`, `HierarchyType`, `HierarchyVersion`, `NodeAssignment`, `EffectivePeriod` | Hierarchy entities |
| **Domain Events** | `HierarchyCreatedEvent`, `HierarchyChangedEvent`, `NodeAddedEvent`, `NodeMovedEvent`, `HierarchyVersionedEvent` | Hierarchy events |
| **Input Ports** | `HierarchyUseCase`, `NodeUseCase` | Hierarchy use cases |
| **Output Ports** | `HierarchyRepository`, `HierarchyEventPublisher` | Persistence and integration |
| **Domain Services** | `HierarchyValidationService`, `NodePathService`, `VersioningService` | Business logic |

### Inter-Subdomain Communication

| Source Subdomain | Target Subdomain | Communication | Purpose |
|------------------|------------------|---------------|---------|
| Hub (9701) | Data Quality (9702) | Command | Validate record before publish |
| Hub (9701) | Stewardship (9703) | Command | Route change request for approval |
| Hub (9701) | Duplicate Detection (9704) | Query | Check for duplicates before create |
| Data Quality (9702) | Hub (9701) | Event | Report validation results |
| Stewardship (9703) | Hub (9701) | Event | Approval decision notification |
| Duplicate Detection (9704) | Hub (9701) | Event | Merge completion notification |
| Hierarchy (9705) | Hub (9701) | Event | Hierarchy assignment changes |

### Port Assignments

| Subdomain | Port | Package |
|-----------|------|---------|
| MDM Hub | 9701 | `com.chiroerp.mdm.hub` |
| Data Quality | 9702 | `com.chiroerp.mdm.dataquality` |
| Stewardship | 9703 | `com.chiroerp.mdm.stewardship` |
| Duplicate Detection | 9704 | `com.chiroerp.mdm.duplicatedetection` |
| Hierarchy Management | 9705 | `com.chiroerp.mdm.hierarchy` |

## Federated Data Ownership Model

### Core Principle: Reference by ID, Not Duplication

MDM **governs** master data but does **not own** it. Each master data domain has a single owning bounded context:

| Master Data Domain | Owning Context | MDM Role |
|--------------------|----------------|----------|
| **Customer** | CRM Customer360 (Port 9401) | Governance, quality, approval workflows |
| **Vendor** | Procurement (ADR-023) | Governance, quality, approval workflows |
| **Material/Product** | Inventory (ADR-024) | Governance, quality, approval workflows |
| **Chart of Accounts** | Finance GL (ADR-009) | Governance, quality, approval workflows |
| **Cost Centers** | Controlling (ADR-028) | Governance, quality, approval workflows |

### What MDM Stores vs. What Owners Store

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MDM HUB (Port 9701)                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ MasterRecord                                                         │   │
│  │   - id: MasterRecordId                                              │   │
│  │   - domainType: CUSTOMER | VENDOR | PRODUCT | COA | COST_CENTER     │   │
│  │   - sourceSystem: "crm-customer360"                                 │   │
│  │   - sourceId: CustomerId (REFERENCE ONLY)                           │   │
│  │   - qualityScore: 95%                                               │   │
│  │   - lastValidatedAt: Timestamp                                      │   │
│  │   - lineage: List<SourceContribution>                               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ✅ STORES: Metadata, quality scores, lineage, change history              │
│  ❌ DOES NOT STORE: Customer name, address, phone, email (lives in CRM)    │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                      CRM CUSTOMER360 (Port 9401)                            │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Customer (Aggregate Root - SOURCE OF TRUTH)                         │   │
│  │   - id: CustomerId                                                  │   │
│  │   - name: CustomerName                                              │   │
│  │   - profile: CustomerProfile                                        │   │
│  │   - contacts: List<Contact>                                         │   │
│  │   - addresses: List<Address>                                        │   │
│  │   - hierarchy: CustomerHierarchy                                    │   │
│  │   - segment: CustomerSegment                                        │   │
│  │   - classification: A | B | C                                       │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ✅ OWNS: All customer master data attributes                              │
│  ✅ PUBLISHES: CustomerCreatedEvent, CustomerUpdatedEvent                   │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                        FINANCE AR (Accounts Receivable)                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ CustomerAccount (Local Entity - FINANCIAL VIEW ONLY)                │   │
│  │   - id: CustomerAccountId                                           │   │
│  │   - customerId: CustomerId (REFERENCE to CRM)                       │   │
│  │   - creditLimit: Money                                              │   │
│  │   - creditStatus: GOOD | WARNING | BLOCKED                          │   │
│  │   - paymentTerms: PaymentTerms                                      │   │
│  │   - agingProfile: AgingBuckets                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ✅ OWNS: Financial attributes (credit, payment terms, aging)              │
│  ✅ REFERENCES: CustomerId (no duplication of name/address)                │
│  ✅ SYNCS VIA: CustomerCreatedEvent → CreateCustomerAccountCommand         │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Consumer Contexts: Reference Pattern

Other bounded contexts that need customer/vendor/product data follow this pattern:

```kotlin
// Commerce E-Commerce - References CustomerId, does NOT duplicate
class OnlineOrder(
    val orderId: OrderId,
    val customerId: CustomerId,        // Reference to CRM (no name/address stored)
    val lines: List<OrderLine>,
    // ...
)

// For display purposes, use a read-only projection (cache)
class CustomerProjection(
    val customerId: CustomerId,        // Reference to CRM
    val displayName: String,           // Cached for display only
    val segment: CustomerSegment,      // Cached for personalization
    val lastSyncedAt: Instant          // Staleness indicator
)

// Projection refreshed by events from CRM
@EventHandler
fun on(event: CustomerUpdatedEvent) {
    customerProjectionRepository.refresh(event.customerId)
}
```

## Event-Driven Integration Flow

### Change Request Workflow

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    MASTER DATA CHANGE WORKFLOW                               │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1. USER INITIATES CHANGE                                                    │
│     └─► MDM Stewardship (9703): SubmitChangeRequestCommand                   │
│         - requestedChanges: { "creditLimit": "$50,000" }                     │
│         - targetDomain: CUSTOMER                                             │
│         - targetId: CustomerId("CUST-001")                                   │
│                                                                              │
│  2. MDM VALIDATES                                                            │
│     └─► Data Quality (9702): ValidateChangeCommand                           │
│         - Format validation                                                  │
│         - Business rule validation                                           │
│         - Completeness checks                                                │
│                                                                              │
│  3. MDM ROUTES FOR APPROVAL                                                  │
│     └─► Stewardship (9703): RouteForApprovalCommand                          │
│         - Determine approvers based on domain/amount                         │
│         - SoD: requester ≠ approver                                          │
│         - 4-eyes principle for high-risk changes                             │
│                                                                              │
│  4. APPROVAL DECISION                                                        │
│     └─► Stewardship (9703): ApproveChangeRequestCommand                      │
│         - ChangeRequestApprovedEvent published                               │
│                                                                              │
│  5. OWNING DOMAIN EXECUTES                                                   │
│     └─► Finance AR: UpdateCreditLimitCommand                                 │
│         - Owner applies the approved change                                  │
│         - CustomerAccountUpdatedEvent published                              │
│                                                                              │
│  6. MDM RECORDS LINEAGE                                                      │
│     └─► MDM Hub (9701): RecordChangeLineageCommand                           │
│         - Who changed, when, what, why                                       │
│         - Update quality score                                               │
│                                                                              │
│  7. CONSUMERS REACT                                                          │
│     └─► Commerce: RefreshCustomerProjectionCommand                           │
│     └─► Analytics: UpdateCustomerDimensionCommand                            │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Event Propagation Pattern

| Event Source | Event | Consumers | Action |
|--------------|-------|-----------|--------|
| CRM Customer360 | `CustomerCreatedEvent` | Finance AR | `CreateCustomerAccountCommand` |
| CRM Customer360 | `CustomerCreatedEvent` | MDM Hub | `RegisterMasterRecordCommand` |
| CRM Customer360 | `CustomerCreatedEvent` | Analytics | `CreateCustomerDimensionCommand` |
| CRM Customer360 | `CustomerUpdatedEvent` | Commerce | `RefreshCustomerProjectionCommand` |
| CRM Customer360 | `CustomerMergedEvent` | Finance AR | `MergeCustomerAccountsCommand` |
| CRM Customer360 | `CustomerMergedEvent` | MDM Hub | `RecordMergeLineageCommand` |
| MDM Stewardship | `ChangeRequestApprovedEvent` | CRM Customer360 | `ApplyApprovedChangeCommand` |
| MDM Data Quality | `QualityThresholdBreachedEvent` | Notification | `AlertDataStewardCommand` |

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
Implementation follows the subdomain architecture within `mdm/`:

### Phase 1: Foundation
- **mdm-shared**: Shared types (MasterDomain, RecordStatus, ChangeRequestStatus).
- **mdm-hub** (Port 9701): MasterRecord reference model, basic CRUD.
- Integration with CRM Customer360 for customer master governance.

### Phase 2: Quality & Validation
- **mdm-data-quality** (Port 9702): Validation rules engine.
- Quality scoring (completeness, accuracy, consistency, timeliness).
- Data profiling and anomaly detection.

### Phase 3: Stewardship & Workflows
- **mdm-stewardship** (Port 9703): Approval workflows with SoD.
- Steward assignment and task routing.
- Escalation rules and audit trails.

### Phase 4: Match & Merge
- **mdm-duplicate-detection** (Port 9704): Matching rules engine.
- Duplicate cluster management.
- Survivorship rules and merge workflows.

### Phase 5: Hierarchies
- **mdm-hierarchy** (Port 9705): Cross-domain hierarchy management.
- Customer groups, product categories, GL structures.
- Version control for hierarchy changes.

### Phase 6: Cross-Context Integration
- Event-driven synchronization with all consuming contexts.
- Lineage tracking across the platform.
- Quality dashboards and KPIs.

## References
### Related ADRs
- ADR-005: Multi-Tenancy Data Isolation Strategy
- ADR-009: Financial Accounting Domain Strategy (Chart of Accounts ownership)
- ADR-014: Authorization Objects & Segregation of Duties
- ADR-015: Data Lifecycle Management
- ADR-020: Accounts Receivable (CustomerAccount - financial view)
- ADR-023: Procurement (Vendor master ownership)
- ADR-024: Inventory Management (Material master ownership)
- ADR-028: Controlling (Cost Center ownership)
- ADR-043: CRM (Customer master ownership)

### Internal Documentation
- `docs/data/mdg_requirements.md`

### External References
- SAP Master Data Governance (MDG)
- ISO 8000 Data Quality
- DAMA DMBOK Data Management Body of Knowledge
