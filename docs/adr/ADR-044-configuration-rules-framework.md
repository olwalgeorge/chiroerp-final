# ADR-044: Configuration & Rules Framework

**Status**: Draft (Not Implemented)
**Date**: 2026-02-03
**Deciders**: Architecture Team, Product Team
**Priority**: P0 (Critical)
**Tier**: Core
**Tags**: configuration, rules-engine, customization, scalability

## Context
ChiroERP is designed to serve multiple industries and deployment scales with domain-driven boundaries and event-driven integration. A core characteristic of general-purpose ERP is that business policy (pricing, posting determination, tax determination, tolerances, approvals) is expressed primarily as **versioned configuration** rather than bespoke code per tenant. This ADR defines a configuration & rules framework that domains can rely on to externalize policy as data while remaining auditable and upgrade-safe.

### Design Goals
- Externalize business policy into versioned configuration (not per-tenant code forks)
- Support tenant/org inheritance (global → tenant → company → org unit overrides)
- Ensure deterministic evaluation, traceability, and auditability of outcomes
- Provide validation, simulation, and migration tools for safe change management
- Keep “simple rules” simple (low operational overhead) while allowing advanced cases where needed

### Problem Statement
How do we enable administrators to configure business rules (pricing, posting, tax, approvals, tolerances) without code changes, while maintaining consistency, auditability, and upgrade safety?

## Decision
Implement a **Configuration & Rules Framework** that enables data-driven customization across all domains.

### Core Capabilities

#### 1. Pricing Condition Technique (SAP-Style)
Multi-step pricing calculation with configurable conditions:

```kotlin
// Configuration Model
data class PricingSchema(
    val schemaId: PricingSchemaId,
    val name: String,
    val steps: List<PricingStep>
)

data class PricingStep(
    val stepNumber: Int,
    val conditionType: ConditionType, // BASE_PRICE, DISCOUNT, SURCHARGE, TAX
    val calculationType: CalculationType, // PERCENTAGE, FIXED_AMOUNT, FORMULA
    val source: PricingSource, // MASTER_DATA, CONDITION_RECORD, CUSTOM_LOGIC
    val mandatory: Boolean,
    val accumulationType: AccumulationType // ADD, SUBTRACT, MULTIPLY
)

// Example: Standard B2B Pricing Schema
steps = [
    PricingStep(1, BASE_PRICE, FIXED_AMOUNT, PRICE_LIST, mandatory=true, ADD),
    PricingStep(2, CUSTOMER_DISCOUNT, PERCENTAGE, CONDITION_RECORD, mandatory=false, SUBTRACT),
    PricingStep(3, VOLUME_DISCOUNT, PERCENTAGE, CONDITION_RECORD, mandatory=false, SUBTRACT),
    PricingStep(4, FREIGHT, FIXED_AMOUNT, MASTER_DATA, mandatory=false, ADD),
    PricingStep(5, SALES_TAX, PERCENTAGE, TAX_ENGINE, mandatory=true, ADD)
]
```

**Condition Records** (time-bound, hierarchical):
```kotlin
data class ConditionRecord(
    val conditionType: ConditionType,
    val validFrom: LocalDate,
    val validTo: LocalDate,
    val customerGroup: CustomerGroupId?,
    val productCategory: ProductCategoryId?,
    val region: RegionId?,
    val value: BigDecimal,
    val priority: Int // For conflict resolution
)
```

#### 2. Posting Rule Determination
Configurable GL account assignment logic:

```kotlin
data class PostingRule(
    val ruleId: PostingRuleId,
    val transactionType: TransactionType, // GOODS_RECEIPT, INVOICE, PAYMENT
    val conditions: List<RuleCondition>,
    val accountAssignments: List<AccountAssignment>
)

data class RuleCondition(
    val field: String, // "materialType", "vendorGroup", "companyCode"
    val operator: ConditionOperator, // EQUALS, IN, BETWEEN, MATCHES
    val value: String
)

data class AccountAssignment(
    val accountType: AccountType, // DEBIT, CREDIT
    val accountDetermination: AccountDetermination, // FIXED, DERIVED, MASTER_DATA
    val accountNumber: String?,
    val derivationLogic: String? // "materialMaster.inventoryAccount"
)

// Example: Goods Receipt Posting Rule
PostingRule(
    transactionType = GOODS_RECEIPT,
    conditions = [
        RuleCondition("materialType", EQUALS, "RAW_MATERIAL"),
        RuleCondition("companyCode", EQUALS, "1000")
    ],
    accountAssignments = [
        AccountAssignment(DEBIT, MASTER_DATA, null, "materialMaster.inventoryAccount"),
        AccountAssignment(CREDIT, FIXED, "191100", null) // GR/IR Clearing
    ]
)
```

#### 3. Tax Determination Engine
Jurisdiction-based tax calculation:

```kotlin
data class TaxDeterminationRule(
    val taxCode: TaxCode,
    val taxType: TaxType, // SALES_TAX, VAT, WITHHOLDING, GST
    val jurisdictionRules: List<JurisdictionRule>,
    val calculationMethod: TaxCalculationMethod // RATE_TABLE, FORMULA, EXTERNAL_SERVICE
)

data class JurisdictionRule(
    val jurisdictionId: JurisdictionId,
    val priority: Int,
    val conditions: List<RuleCondition>, // customerLocation, productTaxCategory, shipToLocation
    val taxRate: BigDecimal,
    val compoundTax: Boolean, // Tax on tax (e.g., Canadian PST on GST)
    val exemptionRules: List<ExemptionRule>
)

// Example: U.S. Multi-State Sales Tax
TaxDeterminationRule(
    taxCode = "SALES_TAX_US",
    jurisdictionRules = [
        JurisdictionRule(
            jurisdictionId = "US_CA",
            conditions = [RuleCondition("shipToState", EQUALS, "CA")],
            taxRate = 7.25,
            exemptionRules = [
                ExemptionRule("RESALE_CERTIFICATE", checkCustomerMaster=true)
            ]
        )
    ]
)
```

#### 4. Approval & Tolerance Configuration
Multi-level approval workflows with dynamic routing:

```kotlin
data class ApprovalSchema(
    val schemaId: ApprovalSchemaId,
    val documentType: DocumentType, // PURCHASE_ORDER, INVOICE, JOURNAL_ENTRY
    val approvalLevels: List<ApprovalLevel>
)

data class ApprovalLevel(
    val levelNumber: Int,
    val conditions: List<RuleCondition>, // amount > 10000, vendor.riskClass = HIGH
    val approverDetermination: ApproverDetermination, // ROLE, ORG_HIERARCHY, SPECIFIC_USER
    val approverRole: String?,
    val escalationTimeHours: Int?,
    val allowDelegation: Boolean
)

data class ToleranceGroup(
    val groupId: ToleranceGroupId,
    val documentType: DocumentType,
    val tolerances: List<Tolerance>
)

data class Tolerance(
    val fieldName: String, // "priceVariance", "quantityVariance"
    val lowerLimit: BigDecimal,
    val upperLimit: BigDecimal,
    val warningThreshold: BigDecimal,
    val blockingThreshold: BigDecimal
)

// Example: Purchase Order Approval
ApprovalSchema(
    documentType = PURCHASE_ORDER,
    approvalLevels = [
        ApprovalLevel(1, [RuleCondition("amount", GREATER_THAN, "1000")], ROLE, "PURCHASING_MANAGER"),
        ApprovalLevel(2, [RuleCondition("amount", GREATER_THAN, "50000")], ROLE, "CFO")
    ]
)
```

#### 5. Document Type & Numbering Configuration
```kotlin
data class DocumentType(
    val documentTypeId: DocumentTypeId,
    val name: String,
    val numberRange: NumberRange,
    val fieldConfiguration: FieldConfiguration,
    val defaultValues: Map<String, Any>,
    val validationRules: List<ValidationRule>
)

data class NumberRange(
    val prefix: String, // "PO-", "INV-"
    val suffix: String?, // "-2026"
    val currentNumber: Long,
    val increment: Int,
    val paddingLength: Int
)

// Example: Purchase Order Document Type
DocumentType(
    documentTypeId = "PO_STANDARD",
    name = "Standard Purchase Order",
    numberRange = NumberRange("PO-", "-2026", 100000, 1, 6),
    fieldConfiguration = FieldConfiguration(
        requiredFields = ["vendor", "companyCode", "purchasingOrg"],
        readOnlyFields = ["createdBy", "createdAt"],
        hiddenFields = []
    )
)
```

### Architecture Components

#### Configuration Storage
```
configuration/
├── domain/
│   ├── PricingSchema.kt
│   ├── PostingRule.kt
│   ├── TaxDeterminationRule.kt
│   ├── ApprovalSchema.kt
│   ├── ToleranceGroup.kt
│   └── DocumentType.kt
├── repository/
│   └── ConfigurationRepository.kt (tenant-scoped)
├── engine/
│   ├── PricingEngine.kt
│   ├── PostingEngine.kt
│   ├── TaxEngine.kt
│   └── ApprovalEngine.kt
└── api/
    └── ConfigurationManagementAPI.kt
```

#### Rule Evaluation Engine
```kotlin
interface RuleEngine<T, R> {
    fun evaluate(context: T, rules: List<Rule>): R
    fun validateRules(rules: List<Rule>): ValidationResult
}

class PricingEngine : RuleEngine<PricingContext, PricingResult> {
    override fun evaluate(context: PricingContext, rules: List<Rule>): PricingResult {
        val schema = configRepository.getPricingSchema(context.schemaId)
        var price = BigDecimal.ZERO

        schema.steps.sortedBy { it.stepNumber }.forEach { step ->
            val conditionValue = evaluateCondition(step, context)
            price = applyStep(price, step, conditionValue)
        }

        return PricingResult(price, appliedConditions, breakdownByStep)
    }
}
```

### Integration Points

#### Domain Services Use Configuration
```kotlin
@ApplicationScoped
class CreateSalesOrderUseCase {
    @Inject
    lateinit var pricingEngine: PricingEngine

    @Inject
    lateinit var taxEngine: TaxEngine

    fun execute(command: CreateSalesOrderCommand): SalesOrderId {
        // Use configuration instead of hardcoded logic
        val pricingResult = pricingEngine.evaluate(
            PricingContext(
                customerId = command.customerId,
                productId = command.productId,
                quantity = command.quantity,
                orderDate = command.orderDate
            ),
            configRepository.getPricingSchema(command.tenantId)
        )

        val taxResult = taxEngine.calculate(
            TaxContext(
                shipToAddress = command.shipToAddress,
                productCategory = product.taxCategory,
                netAmount = pricingResult.netPrice
            )
        )

        // Create order with calculated values
    }
}
```

### Configuration UI Requirements

#### Admin Interface
- **Pricing Configuration Workbench**: Visual pricing schema designer
- **Posting Rule Designer**: Condition builder with account picker
- **Tax Jurisdiction Manager**: Geographic tax rate maintenance
- **Approval Flow Designer**: Drag-and-drop workflow builder
- **Tolerance Maintenance**: Threshold configuration by document type

#### Tenant Self-Service
- Limited configuration scope (pricing discounts, approval limits)
- Cannot modify core posting rules or tax logic (compliance risk)
- Configuration change audit trail

### Versioning & Change Management

```kotlin
data class ConfigurationVersion(
    val configId: ConfigId,
    val version: Int,
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?,
    val changeDescription: String,
    val approvedBy: UserId,
    val configuration: Configuration // Immutable snapshot
)

// Example: Pricing Schema Version History
v1: Effective 2026-01-01 to 2026-06-30 (Q1/Q2 pricing)
v2: Effective 2026-07-01 to null (Q3+ pricing, 5% volume discount added)
```

**Historical Reporting**: Use configuration version in effect at transaction time (not current version).

### Multi-Tenancy Considerations

#### Shared vs Tenant-Specific
- **Shared Configuration**: Tax jurisdiction rules (regulatory, same for all tenants)
- **Tenant-Specific**: Pricing schemas, posting rules, approval workflows
- **Override Model**: Tenant config overrides shared defaults

#### Configuration Inheritance
```
Global Default Schema
  ↓ (overrides)
Tenant Schema (tenant-001)
  ↓ (overrides)
Company Code Schema (tenant-001, company 1000)
  ↓ (overrides)
Sales Org Schema (tenant-001, sales org 0001)
```

## Alternatives Considered

### 1. Drools Rule Engine
**Pros**: Mature, RETE algorithm, DMN support
**Cons**: JVM memory overhead, learning curve, overkill for simple conditions
**Decision**: Use for complex decision tables (credit scoring), not for all rules

### 2. External Rules Service (e.g., Amazon Fraud Detector)
**Pros**: Managed service, ML-powered
**Cons**: Vendor lock-in, latency, not suitable for ERP deterministic logic
**Decision**: Rejected

### 3. Groovy/Script-Based Rules
**Pros**: Flexibility, can handle any logic
**Cons**: Security risk, hard to audit, performance unpredictable
**Decision**: Limit to specific extension points (ADR-012), not core config

### 4. Code-First Policies (No Shared Configuration Framework)
**Pros**: Low initial complexity; straightforward implementation in domain code
**Cons**: Policy variation management becomes expensive (upgrades, testing, and tenant drift); reduces self-service and operational scalability
**Decision**: Rejected in favor of a configuration-first model

## Consequences

### Positive
- ✅ **Scalable Multi-Tenancy**: 80-90% of customer variation handled via config
- ✅ **Self-Service**: Customers configure pricing/approvals without vendor involvement
- ✅ **Upgrade Safety**: Configuration survives code upgrades (data-driven)
- ✅ **Auditability**: All rule changes tracked with approval workflow
- ✅ **Time-to-Value**: Faster implementations (configure vs code)
- ✅ **SAP-Grade Capability**: Matches SAP's condition technique and customizing

### Negative
- ❌ **Complexity**: Configuration framework itself is a large domain
- ❌ **Performance**: Rule evaluation adds latency (mitigate with caching)
- ❌ **Learning Curve**: Admins need training on configuration concepts
- ❌ **Testing**: Must test all rule combinations (combinatorial explosion risk)

### Neutral
- Configuration UI becomes a major product surface area
- Need configuration migration tools for upgrades
- Some rules may still require code (10-20% edge cases)

## Compliance

### Audit Requirements
- All configuration changes logged with user, timestamp, before/after snapshot
- Approval workflow for sensitive configurations (posting rules, tax rates)
- Configuration export for regulatory audits (SOX, GDPR)

### Authorization
- Configuration read: All tenant users
- Configuration write: Admin role only
- Critical config changes: Dual approval (maker-checker)

## Performance Standards

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Rule Evaluation Latency | < 50ms p95 | > 100ms |
| Configuration Cache Hit Rate | > 95% | < 90% |
| Configuration Load Time | < 200ms | > 500ms |
| Pricing Calculation (10 steps) | < 100ms | > 200ms |

## Implementation Plan

### Phase 1: Foundation (Months 1-2)
- ✅ Configuration domain model
- ✅ Configuration repository (tenant-scoped)
- ✅ Versioning & audit trail
- ✅ Basic rule evaluation engine

### Phase 2: Pricing & Tax (Months 3-4)
- ✅ Pricing condition technique implementation
- ✅ Tax determination engine
- ✅ Integration with Sales & AR domains
- ✅ Pricing configuration UI (basic)

### Phase 3: Posting & Approvals (Months 5-6)
- ✅ Posting rule engine
- ✅ Approval workflow engine
- ✅ Integration with Finance & Procurement
- ✅ Configuration workbench UI

### Phase 4: Advanced Features (Months 7-8)
- ✅ Tolerance management
- ✅ Document type configuration
- ✅ Configuration inheritance
- ✅ Migration tools

## References

### Related ADRs
- ADR-012: Tenant Customization Framework (high-level strategy)
- ADR-014: Authorization Objects & SoD (configuration authorization)
- ADR-010: REST Validation Standard (rule validation)
- ADR-045: Enterprise Organizational Model (org-based rule determination)

### Industry References
- SAP Customizing (IMG - Implementation Guide)
- Oracle Fusion Rules Engine
- Workday Business Process Framework
- Stripe Billing Rules Engine

### External Resources
- [Martin Fowler: Rules Engine Pattern](https://martinfowler.com/bliki/RulesEngine.html)
- [DMN (Decision Model and Notation) Spec](https://www.omg.org/dmn/)
- [Drools Documentation](https://docs.drools.org/)

---

**Note**: This ADR establishes the configuration-first foundation required for multi-tenant ERP deployments, upgrade-safe customization, and auditable policy changes.
