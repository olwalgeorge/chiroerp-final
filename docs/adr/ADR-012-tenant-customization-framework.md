# ADR-012: Tenant Customization Framework

**Status**: Draft (Not Implemented)
**Date**: 2026-02-01
**Deciders**: Architecture Team, Product Team
**Priority**: P1 (High)
**Tier**: Core
**Tags**: multi-tenancy, customization, configuration, extensibility, governance

## Context
ChiroERP must support tenant-specific customization comparable to SAP "Customizing" (SPRO) without forking code or weakening isolation. Tenants need to configure processes, fields, validations, approvals, numbering, and integrations while remaining upgrade-safe and compliant. This ADR defines the standardized mechanism for controlled variation (configuration + metadata extensions + governance) across tenants and organizational units.

## Decision
Adopt a **Tenant Customization Framework** that provides **configuration layering**, **metadata-driven extensions**, **workflow customization**, **UI configuration**, **validation rule extensions**, and a **safe upgrade path** with strict governance. Implementation is **not started**; this ADR defines the standard.

### Customization Types (Allowed)
1. **Configuration** (highest priority): fiscal calendars, tax rules, numbering, default values, thresholds, currencies, time zones, localization settings.
2. **Metadata Extensions**: tenant-defined custom fields on approved entities via extension tables or controlled JSONB, with validation rules.
3. **Workflow Variants**: declarative workflow definitions for approvals and exceptions; no arbitrary code execution.
4. **Validation Rule Extensions**: tenant-defined validation rules on allowed extension points.
5. **UI/Reporting**: field visibility/labels, form layouts, report templates, and localized labels.
6. **Integration Mapping**: per-tenant data mappings and transform rules for external systems.

### Not Allowed
- Code forks or tenant-specific binaries.
- Direct database schema mutations outside approved extension tables.
- Unbounded custom scripting in production (must be sandboxed and reviewed).

### Framework Design

### Configuration Layering
- **Resolution order**: Global default -> Region/Country -> Tenant -> Org Unit -> User (optional).
- **Effective dating**: Configs can be future-dated with activation windows.
- **Schema**: Key/value JSONB with explicit schema definitions and validation per key.
- **Caching**: Read-through cache with tenant + version key; cache invalidation on publish.

### Metadata Extensions (Custom Fields)
- **Ownership**: Each bounded context owns its extension tables and validation rules.
- **Storage**: `entity_ext` tables with `tenant_id`, `entity_id`, and `custom_*` fields or a structured JSONB column for low-cardinality extensions.
- **Indexing**: Index on frequently queried custom fields; avoid unbounded JSON queries.
- **Validation**: All custom fields must have defined types, constraints, and localized labels.

### Custom Fields & Extensions (Details)

#### Metadata Registry Architecture

```kotlin
// CustomFieldRegistry.kt
package com.chiroerp.platform.customization

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.Instant

@ApplicationScoped
class CustomFieldRegistry {

    @Inject
    lateinit var repository: CustomFieldRepository

    @Inject
    lateinit var validator: CustomFieldValidator

    @Inject
    lateinit var cache: CustomFieldCache

    /**
     * Register a new custom field for a tenant
     */
    fun registerCustomField(
        tenantId: String,
        entityType: EntityType,
        definition: CustomFieldDefinition
    ): CustomFieldMetadata {
        // Validate limits
        val existingCount = repository.countFieldsByTenant(tenantId, entityType)
        if (existingCount >= getMaxFieldsForEntity(entityType)) {
            throw CustomFieldLimitExceededException(
                "Tenant $tenantId has reached the limit of ${getMaxFieldsForEntity(entityType)} " +
                "custom fields for $entityType"
            )
        }

        // Validate field definition
        validator.validateDefinition(definition)

        // Check for name conflicts
        if (repository.existsByName(tenantId, entityType, definition.name)) {
            throw CustomFieldConflictException(
                "Custom field '${definition.name}' already exists for $entityType"
            )
        }

        // Create metadata
        val metadata = CustomFieldMetadata(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            entityType = entityType,
            fieldName = definition.name,
            fieldKey = generateFieldKey(definition.name),
            dataType = definition.dataType,
            constraints = definition.constraints,
            localization = definition.localization,
            indexed = definition.indexed,
            queryable = definition.queryable,
            version = 1,
            status = FieldStatus.ACTIVE,
            createdAt = Instant.now(),
            createdBy = getCurrentUser()
        )

        // Persist
        repository.save(metadata)

        // Create database schema changes if indexed
        if (metadata.indexed) {
            createIndexedField(metadata)
        }

        // Invalidate cache
        cache.invalidate(tenantId, entityType)

        return metadata
    }

    /**
     * Get all custom fields for a tenant and entity
     */
    fun getCustomFields(tenantId: String, entityType: EntityType): List<CustomFieldMetadata> {
        return cache.get(tenantId, entityType) {
            repository.findByTenantAndEntity(tenantId, entityType)
                .filter { it.status == FieldStatus.ACTIVE }
        }
    }

    /**
     * Validate custom field value
     */
    fun validateValue(
        tenantId: String,
        entityType: EntityType,
        fieldKey: String,
        value: Any?
    ): ValidationResult {
        val metadata = getCustomFields(tenantId, entityType)
            .find { it.fieldKey == fieldKey }
            ?: return ValidationResult.invalid("Unknown custom field: $fieldKey")

        return validator.validateValue(metadata, value)
    }

    private fun getMaxFieldsForEntity(entityType: EntityType): Int {
        return when (entityType) {
            EntityType.CUSTOMER, EntityType.VENDOR -> 50
            EntityType.INVOICE, EntityType.PURCHASE_ORDER -> 30
            EntityType.GL_ACCOUNT -> 20
            else -> 15
        }
    }

    private fun generateFieldKey(name: String): String {
        return "custom_" + name.lowercase()
            .replace(Regex("[^a-z0-9_]"), "_")
            .replace(Regex("_{2,}"), "_")
            .trim('_')
    }

    private fun createIndexedField(metadata: CustomFieldMetadata) {
        // Create extension table column or index on JSONB path
        when (metadata.dataType) {
            DataType.STRING, DataType.NUMBER, DataType.DATE -> {
                // Add column to extension table with index
                val sql = """
                    ALTER TABLE ${metadata.entityType.extensionTable}
                    ADD COLUMN IF NOT EXISTS ${metadata.fieldKey} ${metadata.dataType.sqlType};

                    CREATE INDEX IF NOT EXISTS idx_${metadata.fieldKey}
                    ON ${metadata.entityType.extensionTable}(tenant_id, ${metadata.fieldKey})
                    WHERE ${metadata.fieldKey} IS NOT NULL;
                """
                executeDDL(sql)
            }
            else -> {
                // Create GIN index on JSONB path for non-indexed types
                val sql = """
                    CREATE INDEX IF NOT EXISTS idx_jsonb_${metadata.fieldKey}
                    ON ${metadata.entityType.extensionTable}
                    USING gin ((custom_data -> '${metadata.fieldKey}'))
                    WHERE tenant_id = '${metadata.tenantId}';
                """
                executeDDL(sql)
            }
        }
    }
}

data class CustomFieldDefinition(
    val name: String,
    val dataType: DataType,
    val constraints: FieldConstraints,
    val localization: Map<String, LocalizedLabels>,
    val indexed: Boolean = false,
    val queryable: Boolean = false,
    val defaultValue: Any? = null
)

data class CustomFieldMetadata(
    val id: UUID,
    val tenantId: String,
    val entityType: EntityType,
    val fieldName: String,
    val fieldKey: String,
    val dataType: DataType,
    val constraints: FieldConstraints,
    val localization: Map<String, LocalizedLabels>,
    val indexed: Boolean,
    val queryable: Boolean,
    val version: Int,
    val status: FieldStatus,
    val createdAt: Instant,
    val createdBy: String,
    val modifiedAt: Instant? = null,
    val modifiedBy: String? = null
)

data class FieldConstraints(
    val required: Boolean = false,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val minValue: BigDecimal? = null,
    val maxValue: BigDecimal? = null,
    val pattern: String? = null,
    val allowedValues: List<String>? = null
)

data class LocalizedLabels(
    val label: String,
    val description: String? = null,
    val placeholder: String? = null,
    val helpText: String? = null
)

enum class DataType(val sqlType: String) {
    STRING("VARCHAR(500)"),
    TEXT("TEXT"),
    NUMBER("DECIMAL(19,4)"),
    INTEGER("BIGINT"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE"),
    DATETIME("TIMESTAMP"),
    JSON("JSONB")
}

enum class EntityType(val extensionTable: String) {
    CUSTOMER("customers_ext"),
    VENDOR("vendors_ext"),
    INVOICE("invoices_ext"),
    PURCHASE_ORDER("purchase_orders_ext"),
    GL_ACCOUNT("gl_accounts_ext"),
    SALES_ORDER("sales_orders_ext")
}

enum class FieldStatus {
    ACTIVE, DEPRECATED, DELETED
}
```

#### Extension Table Schema

```sql
-- Customer Extension Table Example
CREATE TABLE customers_ext (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,

    -- Pre-allocated indexed custom fields (populated dynamically)
    custom_field_1 VARCHAR(500),
    custom_field_2 DECIMAL(19,4),
    custom_field_3 DATE,
    -- ... up to 20 pre-allocated indexed fields

    -- JSONB for non-indexed custom fields
    custom_data JSONB,

    -- Metadata
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP,

    CONSTRAINT uk_customers_ext_tenant_customer UNIQUE(tenant_id, customer_id)
);

-- Tenant-scoped index
CREATE INDEX idx_customers_ext_tenant ON customers_ext(tenant_id);

-- GIN index for JSONB queries
CREATE INDEX idx_customers_ext_custom_data ON customers_ext USING gin(custom_data);

-- Partial indexes on commonly queried indexed fields
CREATE INDEX idx_customers_ext_field1
ON customers_ext(tenant_id, custom_field_1)
WHERE custom_field_1 IS NOT NULL;
```

#### API Exposure Pattern

```kotlin
// Customer API with Custom Fields
@Path("/api/v1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CustomerResource {

    @Inject
    lateinit var customerService: CustomerService

    @Inject
    lateinit var customFieldRegistry: CustomFieldRegistry

    @GET
    @Path("/{id}")
    fun getCustomer(
        @PathParam("id") customerId: String,
        @HeaderParam("X-Tenant-ID") tenantId: String
    ): Response {
        val customer = customerService.findById(tenantId, customerId)
            ?: return Response.status(404).build()

        // Load custom field values
        val customFields = loadCustomFields(tenantId, customer.id)

        // Build response with custom fields namespaced
        val response = CustomerResponse(
            id = customer.id,
            code = customer.code,
            name = customer.name,
            email = customer.email,
            // ... standard fields
            custom = customFields
        )

        return Response.ok(response).build()
    }

    @POST
    fun createCustomer(
        @HeaderParam("X-Tenant-ID") tenantId: String,
        request: CreateCustomerRequest
    ): Response {
        // Validate standard fields
        val violations = validator.validate(request)
        if (violations.isNotEmpty()) {
            return Response.status(400).entity(violations).build()
        }

        // Validate custom fields
        if (request.custom != null) {
            val customFieldViolations = validateCustomFields(
                tenantId,
                EntityType.CUSTOMER,
                request.custom
            )
            if (customFieldViolations.isNotEmpty()) {
                return Response.status(400).entity(customFieldViolations).build()
            }
        }

        // Create customer with custom fields
        val customer = customerService.create(tenantId, request)

        return Response.status(201)
            .entity(customer)
            .header("Location", "/api/v1/customers/${customer.id}")
            .build()
    }

    private fun validateCustomFields(
        tenantId: String,
        entityType: EntityType,
        customFields: Map<String, Any?>
    ): List<ValidationViolation> {
        val violations = mutableListOf<ValidationViolation>()

        customFields.forEach { (fieldKey, value) ->
            val result = customFieldRegistry.validateValue(
                tenantId,
                entityType,
                fieldKey,
                value
            )
            if (!result.isValid) {
                violations.addAll(result.violations)
            }
        }

        return violations
    }
}

data class CustomerResponse(
    val id: String,
    val code: String,
    val name: String,
    val email: String,
    // ... standard fields
    val custom: Map<String, Any?>? = null  // Namespaced custom fields
)

data class CreateCustomerRequest(
    val code: String,
    val name: String,
    val email: String,
    // ... standard fields
    val custom: Map<String, Any?>? = null  // Namespaced custom fields
)
```

#### Custom Field Limits by Entity

| Entity Type | Max Indexed Fields | Max JSONB Fields | Total Limit | Performance Impact |
|-------------|-------------------|------------------|-------------|-------------------|
| **Customer** | 20 | 30 | 50 | Medium |
| **Vendor** | 20 | 30 | 50 | Medium |
| **Invoice** | 15 | 15 | 30 | High (query-heavy) |
| **Purchase Order** | 15 | 15 | 30 | High (query-heavy) |
| **GL Account** | 10 | 10 | 20 | Critical (read-heavy) |
| **Sales Order** | 15 | 20 | 35 | Medium |

**Rationale**:
- **Indexed fields**: Used in filters, sorts, and reports; limited due to index maintenance cost
- **JSONB fields**: Lower-cardinality fields for display/context only; limited to prevent schema bloat
- **Total limits**: Protect against runaway customization that degrades performance

### Custom Fields & Extensions (Details)
- **Metadata registry**: Central catalog of custom fields per entity, with type, constraints, and localization keys.
- **Data model**: Prefer extension tables for high-usage fields; allow JSONB for low-cardinality or rarely queried fields.
- **Queryability**: Only indexed custom fields are allowed in global search and reporting filters.
- **API exposure**: Custom fields are surfaced through API schemas as a namespaced map (`custom.<field_key>`), with versioned contracts.
- **Limits**: Per-tenant limits on number of custom fields per entity to protect performance.

### Workflow Variants

#### Declarative Workflow DSL

```yaml
# workflow-definition.yaml
workflow:
  id: purchase_order_approval
  version: 1.0
  tenant_id: tenant-001
  entity_type: PurchaseOrder

  variables:
    - name: po_amount
      type: decimal
      source: entity.total_amount

    - name: requestor_department
      type: string
      source: entity.requestor.department

  steps:
    - id: manager_approval
      name: "Department Manager Approval"
      type: approval
      condition: "po_amount >= 1000 && po_amount < 10000"
      assignee:
        type: role
        role: DEPARTMENT_MANAGER
        department_match: requestor_department
      sla:
        duration: 24h
        escalation:
          - after: 24h
            action: notify_escalation
            recipients: [DIRECTOR]
      actions:
        - approve
        - reject
        - request_more_info

    - id: director_approval
      name: "Director Approval"
      type: approval
      condition: "po_amount >= 10000 && po_amount < 50000"
      depends_on: [manager_approval]
      assignee:
        type: role
        role: DIRECTOR
      sla:
        duration: 48h
        escalation:
          - after: 48h
            action: notify_escalation
            recipients: [CFO]

    - id: cfo_approval
      name: "CFO Approval"
      type: approval
      condition: "po_amount >= 50000"
      depends_on: [director_approval]
      assignee:
        type: role
        role: CFO
      sla:
        duration: 72h
        escalation:
          - after: 72h
            action: notify_escalation
            recipients: [CEO]
      compliance_required: true  # Cannot be skipped

    - id: procurement_review
      name: "Procurement Review"
      type: review
      depends_on: [cfo_approval]
      assignee:
        type: role
        role: PROCUREMENT_SPECIALIST
      parallel: true  # Can run alongside other steps

  completion_conditions:
    all_approvals: true
    compliance_met: true

  notification_channels:
    - email
    - slack

  audit_level: FULL  # Log all state transitions

```

#### Workflow Engine Implementation

```kotlin
// WorkflowEngine.kt
package com.chiroerp.platform.workflow

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class WorkflowEngine {

    @Inject
    lateinit var definitionRepository: WorkflowDefinitionRepository

    @Inject
    lateinit var instanceRepository: WorkflowInstanceRepository

    @Inject
    lateinit var evaluator: ConditionEvaluator

    @Inject
    lateinit var notificationService: NotificationService

    /**
     * Start a workflow instance for an entity
     */
    fun startWorkflow(
        tenantId: String,
        workflowId: String,
        entityType: EntityType,
        entityId: String,
        context: Map<String, Any>
    ): WorkflowInstance {
        // Load workflow definition
        val definition = definitionRepository.findByIdAndTenant(workflowId, tenantId)
            ?: throw WorkflowNotFoundException("Workflow $workflowId not found for tenant $tenantId")

        // Validate definition is active
        if (definition.status != WorkflowStatus.ACTIVE) {
            throw IllegalStateException("Workflow $workflowId is not active")
        }

        // Create instance
        val instance = WorkflowInstance(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            workflowId = workflowId,
            workflowVersion = definition.version,
            entityType = entityType,
            entityId = entityId,
            status = InstanceStatus.IN_PROGRESS,
            context = context,
            currentStep = null,
            startedAt = Instant.now(),
            startedBy = getCurrentUser()
        )

        instanceRepository.save(instance)

        // Evaluate and activate initial steps
        evaluateAndActivateSteps(instance, definition)

        return instance
    }

    /**
     * Process a workflow action (approve, reject, etc.)
     */
    fun processAction(
        tenantId: String,
        instanceId: UUID,
        stepId: String,
        action: WorkflowAction,
        comment: String? = null
    ): WorkflowInstance {
        val instance = instanceRepository.findById(instanceId)
            ?: throw WorkflowInstanceNotFoundException("Instance $instanceId not found")

        // Validate tenant ownership
        if (instance.tenantId != tenantId) {
            throw UnauthorizedException("Instance belongs to different tenant")
        }

        // Validate instance is in progress
        if (instance.status != InstanceStatus.IN_PROGRESS) {
            throw IllegalStateException("Instance is not in progress")
        }

        // Find step
        val step = instance.steps.find { it.id == stepId }
            ?: throw StepNotFoundException("Step $stepId not found")

        // Validate step is active
        if (step.status != StepStatus.ACTIVE) {
            throw IllegalStateException("Step is not active")
        }

        // Validate user is assigned
        val currentUser = getCurrentUser()
        if (!isUserAssignedToStep(step, currentUser)) {
            throw UnauthorizedException("User not assigned to step")
        }

        // Process action
        when (action) {
            WorkflowAction.APPROVE -> approveStep(instance, step, comment)
            WorkflowAction.REJECT -> rejectStep(instance, step, comment)
            WorkflowAction.REQUEST_MORE_INFO -> requestInfo(instance, step, comment)
        }

        // Persist
        instanceRepository.update(instance)

        // Evaluate next steps
        val definition = definitionRepository.findByIdAndTenant(
            instance.workflowId,
            instance.tenantId
        )!!
        evaluateAndActivateSteps(instance, definition)

        // Check completion
        if (isWorkflowComplete(instance, definition)) {
            completeWorkflow(instance)
        }

        return instance
    }

    private fun evaluateAndActivateSteps(
        instance: WorkflowInstance,
        definition: WorkflowDefinition
    ) {
        definition.steps.forEach { stepDef ->
            // Check if step should be activated
            val shouldActivate = evaluator.evaluateCondition(
                stepDef.condition,
                instance.context
            )

            if (!shouldActivate) return@forEach

            // Check dependencies
            val dependenciesMet = stepDef.dependsOn.all { depId ->
                instance.steps.any { it.id == depId && it.status == StepStatus.COMPLETED }
            }

            if (!dependenciesMet) return@forEach

            // Check if already exists
            if (instance.steps.any { it.id == stepDef.id }) return@forEach

            // Create and activate step
            val step = WorkflowStep(
                id = stepDef.id,
                name = stepDef.name,
                type = stepDef.type,
                assignee = resolveAssignee(stepDef.assignee, instance.context),
                status = StepStatus.ACTIVE,
                activatedAt = Instant.now(),
                sla = calculateSLA(stepDef.sla)
            )

            instance.steps.add(step)

            // Send notifications
            notificationService.notifyStepActivated(instance, step)

            // Schedule SLA reminder
            if (step.sla != null) {
                scheduleEscalation(instance, step)
            }
        }
    }

    private fun isWorkflowComplete(
        instance: WorkflowInstance,
        definition: WorkflowDefinition
    ): Boolean {
        // All required steps completed
        val allStepsCompleted = definition.steps
            .filter { !it.optional }
            .all { stepDef ->
                instance.steps.any {
                    it.id == stepDef.id && it.status == StepStatus.COMPLETED
                }
            }

        // Completion conditions met
        val conditionsMet = evaluator.evaluateCondition(
            definition.completionConditions,
            instance.context
        )

        return allStepsCompleted && conditionsMet
    }
}

data class WorkflowDefinition(
    val id: String,
    val tenantId: String,
    val name: String,
    val version: String,
    val entityType: EntityType,
    val steps: List<StepDefinition>,
    val completionConditions: Map<String, Any>,
    val status: WorkflowStatus,
    val createdAt: Instant,
    val modifiedAt: Instant? = null
)

data class WorkflowInstance(
    val id: UUID,
    val tenantId: String,
    val workflowId: String,
    val workflowVersion: String,
    val entityType: EntityType,
    val entityId: String,
    var status: InstanceStatus,
    val context: Map<String, Any>,
    val steps: MutableList<WorkflowStep> = mutableListOf(),
    val startedAt: Instant,
    val startedBy: String,
    var completedAt: Instant? = null,
    var completedBy: String? = null
)

data class WorkflowStep(
    val id: String,
    val name: String,
    val type: StepType,
    val assignee: StepAssignee,
    var status: StepStatus,
    val activatedAt: Instant,
    var completedAt: Instant? = null,
    var completedBy: String? = null,
    val sla: SLA? = null,
    var comment: String? = null
)

enum class WorkflowAction {
    APPROVE, REJECT, REQUEST_MORE_INFO
}

enum class WorkflowStatus {
    DRAFT, ACTIVE, DEPRECATED
}

enum class InstanceStatus {
    IN_PROGRESS, COMPLETED, REJECTED, CANCELLED
}

enum class StepStatus {
    PENDING, ACTIVE, COMPLETED, SKIPPED, CANCELLED
}

enum class StepType {
    APPROVAL, REVIEW, NOTIFICATION, DECISION
}
```

#### Workflow Governance & Safety

| Constraint | Rule | Enforcement |
|------------|------|-------------|
| **Compliance Steps** | Cannot be removed or skipped | Validated at definition publish time |
| **SoD Enforcement** | Maker/checker separation enforced | User assignment validation |
| **No Code Execution** | Only declarative rules allowed | Parser rejects code blocks |
| **Timeout Limits** | Max SLA duration: 30 days | Validated at definition publish |
| **Complexity Limits** | Max 20 steps per workflow | Validated at definition publish |
| **Version Control** | All changes create new version | Immutable definitions |

### Workflow Variants
- **Definition**: Declarative DSL for steps, approvers, thresholds, and SLAs.
- **Execution**: Orchestrated by a workflow engine within the owning context.
- **Constraints**: Only approved extension points; no removal of compliance-required steps.

### Rule Overrides
- **Rule engine**: Simple deterministic rules (if/then) for thresholds and routing.
- **SoD**: Enforce segregation-of-duties (maker/checker) for sensitive workflows.

### Validation Rule Extensions

#### Validation Rule Types

```kotlin
// CustomValidationRule.kt
package com.chiroerp.platform.validation

sealed class CustomValidationRule {
    abstract val id: String
    abstract val tenantId: String
    abstract val entityType: EntityType
    abstract val fieldKey: String
    abstract val message: Map<String, String>  // Localized messages
    abstract val severity: ValidationSeverity

    /**
     * Required field validation
     */
    data class RequiredRule(
        override val id: String,
        override val tenantId: String,
        override val entityType: EntityType,
        override val fieldKey: String,
        override val message: Map<String, String>,
        override val severity: ValidationSeverity = ValidationSeverity.ERROR,
        val condition: String? = null  // Optional conditional requirement
    ) : CustomValidationRule()

    /**
     * Format/Pattern validation
     */
    data class FormatRule(
        override val id: String,
        override val tenantId: String,
        override val entityType: EntityType,
        override val fieldKey: String,
        override val message: Map<String, String>,
        override val severity: ValidationSeverity = ValidationSeverity.ERROR,
        val pattern: String,  // Regex pattern
        val examples: List<String> = emptyList()
    ) : CustomValidationRule()

    /**
     * Range validation (min/max)
     */
    data class RangeRule(
        override val id: String,
        override val tenantId: String,
        override val entityType: EntityType,
        override val fieldKey: String,
        override val message: Map<String, String>,
        override val severity: ValidationSeverity = ValidationSeverity.ERROR,
        val minValue: BigDecimal? = null,
        val maxValue: BigDecimal? = null,
        val inclusive: Boolean = true
    ) : CustomValidationRule()

    /**
     * Cross-field validation
     */
    data class CrossFieldRule(
        override val id: String,
        override val tenantId: String,
        override val entityType: EntityType,
        override val fieldKey: String,
        override val message: Map<String, String>,
        override val severity: ValidationSeverity = ValidationSeverity.ERROR,
        val relatedField: String,
        val operator: ComparisonOperator,
        val condition: String
    ) : CustomValidationRule()

    /**
     * Conditional validation (if/then)
     */
    data class ConditionalRule(
        override val id: String,
        override val tenantId: String,
        override val entityType: EntityType,
        override val fieldKey: String,
        override val message: Map<String, String>,
        override val severity: ValidationSeverity = ValidationSeverity.WARNING,
        val condition: String,  // When to apply validation
        val thenRule: CustomValidationRule  // Nested rule to apply
    ) : CustomValidationRule()
}

enum class ValidationSeverity {
    ERROR,    // Blocks save
    WARNING,  // Allows save with confirmation
    INFO      // Informational only
}

enum class ComparisonOperator {
    EQUALS, NOT_EQUALS,
    GREATER_THAN, LESS_THAN,
    GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL,
    IN_LIST, NOT_IN_LIST
}
```

#### Validation Rule Engine

```kotlin
// CustomValidationEngine.kt
package com.chiroerp.platform.validation

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class CustomValidationEngine {

    @Inject
    lateinit var ruleRepository: ValidationRuleRepository

    @Inject
    lateinit var expressionEvaluator: SafeExpressionEvaluator

    @Inject
    lateinit var cache: ValidationRuleCache

    /**
     * Validate entity with custom rules
     */
    fun validate(
        tenantId: String,
        entityType: EntityType,
        entity: Map<String, Any?>,
        locale: String = "en-US"
    ): ValidationResult {
        // Load active rules for tenant and entity
        val rules = cache.get(tenantId, entityType) {
            ruleRepository.findActiveRules(tenantId, entityType)
        }

        val violations = mutableListOf<ValidationViolation>()

        // Apply each rule
        rules.forEach { rule ->
            val result = applyRule(rule, entity, locale)
            if (!result.isValid) {
                violations.addAll(result.violations)
            }
        }

        return ValidationResult(
            isValid = violations.none { it.severity == ValidationSeverity.ERROR },
            violations = violations
        )
    }

    private fun applyRule(
        rule: CustomValidationRule,
        entity: Map<String, Any?>,
        locale: String
    ): ValidationResult {
        return when (rule) {
            is CustomValidationRule.RequiredRule -> validateRequired(rule, entity, locale)
            is CustomValidationRule.FormatRule -> validateFormat(rule, entity, locale)
            is CustomValidationRule.RangeRule -> validateRange(rule, entity, locale)
            is CustomValidationRule.CrossFieldRule -> validateCrossField(rule, entity, locale)
            is CustomValidationRule.ConditionalRule -> validateConditional(rule, entity, locale)
        }
    }

    private fun validateRequired(
        rule: CustomValidationRule.RequiredRule,
        entity: Map<String, Any?>,
        locale: String
    ): ValidationResult {
        // Check condition if present
        if (rule.condition != null) {
            val conditionMet = expressionEvaluator.evaluate(rule.condition, entity)
            if (!conditionMet) {
                return ValidationResult.valid()
            }
        }

        // Check if field is present and not empty
        val value = entity[rule.fieldKey]
        if (value == null || (value is String && value.isBlank())) {
            return ValidationResult.invalid(
                ValidationViolation(
                    field = rule.fieldKey,
                    message = rule.message[locale] ?: rule.message["en-US"]
                        ?: "Field is required",
                    severity = rule.severity,
                    ruleId = rule.id
                )
            )
        }

        return ValidationResult.valid()
    }

    private fun validateFormat(
        rule: CustomValidationRule.FormatRule,
        entity: Map<String, Any?>,
        locale: String
    ): ValidationResult {
        val value = entity[rule.fieldKey]?.toString() ?: return ValidationResult.valid()

        val regex = Regex(rule.pattern)
        if (!regex.matches(value)) {
            return ValidationResult.invalid(
                ValidationViolation(
                    field = rule.fieldKey,
                    message = rule.message[locale] ?: rule.message["en-US"]
                        ?: "Field format is invalid",
                    severity = rule.severity,
                    ruleId = rule.id,
                    metadata = mapOf(
                        "pattern" to rule.pattern,
                        "examples" to rule.examples
                    )
                )
            )
        }

        return ValidationResult.valid()
    }

    private fun validateRange(
        rule: CustomValidationRule.RangeRule,
        entity: Map<String, Any?>,
        locale: String
    ): ValidationResult {
        val value = entity[rule.fieldKey]?.let {
            when (it) {
                is Number -> it.toBigDecimal()
                is String -> it.toBigDecimalOrNull()
                else -> null
            }
        } ?: return ValidationResult.valid()

        val violations = mutableListOf<ValidationViolation>()

        if (rule.minValue != null) {
            val valid = if (rule.inclusive) {
                value >= rule.minValue
            } else {
                value > rule.minValue
            }

            if (!valid) {
                violations.add(ValidationViolation(
                    field = rule.fieldKey,
                    message = rule.message[locale] ?: "Value is below minimum",
                    severity = rule.severity,
                    ruleId = rule.id,
                    metadata = mapOf("minValue" to rule.minValue)
                ))
            }
        }

        if (rule.maxValue != null) {
            val valid = if (rule.inclusive) {
                value <= rule.maxValue
            } else {
                value < rule.maxValue
            }

            if (!valid) {
                violations.add(ValidationViolation(
                    field = rule.fieldKey,
                    message = rule.message[locale] ?: "Value exceeds maximum",
                    severity = rule.severity,
                    ruleId = rule.id,
                    metadata = mapOf("maxValue" to rule.maxValue)
                ))
            }
        }

        return if (violations.isEmpty()) {
            ValidationResult.valid()
        } else {
            ValidationResult(isValid = false, violations = violations)
        }
    }

    private fun validateCrossField(
        rule: CustomValidationRule.CrossFieldRule,
        entity: Map<String, Any?>,
        locale: String
    ): ValidationResult {
        val value1 = entity[rule.fieldKey]
        val value2 = entity[rule.relatedField]

        if (value1 == null || value2 == null) {
            return ValidationResult.valid()
        }

        val conditionMet = expressionEvaluator.evaluate(
            rule.condition,
            mapOf(
                rule.fieldKey to value1,
                rule.relatedField to value2
            )
        )

        return if (conditionMet) {
            ValidationResult.valid()
        } else {
            ValidationResult.invalid(
                ValidationViolation(
                    field = rule.fieldKey,
                    message = rule.message[locale] ?: "Cross-field validation failed",
                    severity = rule.severity,
                    ruleId = rule.id,
                    metadata = mapOf("relatedField" to rule.relatedField)
                )
            )
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val violations: List<ValidationViolation> = emptyList()
) {
    companion object {
        fun valid() = ValidationResult(true, emptyList())
        fun invalid(violation: ValidationViolation) = ValidationResult(false, listOf(violation))
    }
}

data class ValidationViolation(
    val field: String,
    val message: String,
    val severity: ValidationSeverity,
    val ruleId: String,
    val metadata: Map<String, Any> = emptyMap()
)
```

#### Safe Expression Evaluator

```kotlin
// SafeExpressionEvaluator.kt
package com.chiroerp.platform.validation

import jakarta.enterprise.context.ApplicationScoped

/**
 * Safe expression evaluator that only allows whitelisted operators
 * No code execution - only declarative comparisons
 */
@ApplicationScoped
class SafeExpressionEvaluator {

    private val allowedOperators = setOf(
        "==", "!=", ">", "<", ">=", "<=",
        "&&", "||", "!",
        "in", "not in",
        "contains", "startsWith", "endsWith",
        "isEmpty", "isNotEmpty"
    )

    /**
     * Evaluate a safe expression with given context
     * Expressions are parsed and evaluated WITHOUT code execution
     */
    fun evaluate(expression: String, context: Map<String, Any?>): Boolean {
        // Parse expression into AST
        val ast = parseExpression(expression)

        // Validate only allowed operators
        validateAST(ast)

        // Evaluate AST with context
        return evaluateAST(ast, context)
    }

    private fun parseExpression(expression: String): ExpressionNode {
        // Simplified parser - production would use proper expression grammar
        // Examples:
        // "amount > 1000"
        // "status == 'PENDING' && amount > 5000"
        // "country in ['US', 'CA', 'MX']"

        // For now, return placeholder
        // Production implementation would use ANTLR or similar
        return PlaceholderNode(expression)
    }

    private fun validateAST(node: ExpressionNode) {
        when (node) {
            is BinaryOperator -> {
                if (node.operator !in allowedOperators) {
                    throw SecurityException(
                        "Operator '${node.operator}' not allowed in custom validation rules"
                    )
                }
                validateAST(node.left)
                validateAST(node.right)
            }
            is UnaryOperator -> {
                if (node.operator !in allowedOperators) {
                    throw SecurityException(
                        "Operator '${node.operator}' not allowed in custom validation rules"
                    )
                }
                validateAST(node.operand)
            }
            // Other node types...
        }
    }

    private fun evaluateAST(node: ExpressionNode, context: Map<String, Any?>): Boolean {
        // Evaluate AST nodes recursively
        // This is safe because we've validated operators and don't execute arbitrary code
        return when (node) {
            is BinaryOperator -> evaluateBinary(node, context)
            is UnaryOperator -> evaluateUnary(node, context)
            is LiteralNode -> node.value as Boolean
            is VariableNode -> context[node.name] as? Boolean ?: false
            else -> false
        }
    }
}

sealed class ExpressionNode
data class BinaryOperator(
    val operator: String,
    val left: ExpressionNode,
    val right: ExpressionNode
) : ExpressionNode()
data class UnaryOperator(val operator: String, val operand: ExpressionNode) : ExpressionNode()
data class LiteralNode(val value: Any?) : ExpressionNode()
data class VariableNode(val name: String) : ExpressionNode()
data class PlaceholderNode(val expression: String) : ExpressionNode()
```

#### Validation Rule Limits & Safety

| Constraint | Limit | Rationale |
|------------|-------|-----------|
| **Max Rules per Entity** | 30 | Prevent validation performance degradation |
| **Max Condition Depth** | 5 levels | Prevent complex, hard-to-debug rules |
| **Expression Timeout** | 100ms | Prevent runaway evaluations |
| **Allowed Operators** | Whitelist only | Prevent code injection |
| **No Dynamic Code** | Enforced | Security - only declarative rules |
| **Localization Required** | All messages | Ensure user-facing quality |

### Validation Rule Extensions
- **Scope**: Allowed for tenant-defined custom fields and approved domain extension points.
- **Types**: required/optional, format, ranges, cross-field rules, and conditional rules.
- **Execution point**: Enforced at API boundary via ADR-010 validation pipeline.
- **Safety**: Declarative rules only; no dynamic code execution in production.
- **Localization**: All rule messages must be localized via ValidationMessages bundles.

### UI/Report Customization
- **UI metadata**: Field labels, visibility, order, and required flags by tenant/role.
- **Report templates**: Versioned report definitions with preview and validation.

### UI Configuration per Tenant

#### UI Configuration Models

```kotlin
// UIConfiguration.kt
package com.chiroerp.platform.ui

/**
 * Form layout configuration
 */
data class FormLayout(
    val id: String,
    val tenantId: String,
    val entityType: EntityType,
    val screenKey: String,  // e.g., "customer_create", "invoice_edit"
    val sections: List<FormSection>,
    val version: Int = 1,
    val status: ConfigStatus = ConfigStatus.DRAFT,
    val effectiveFrom: Instant? = null,
    val effectiveTo: Instant? = null,
    val createdAt: Instant,
    val createdBy: String,
    val modifiedAt: Instant,
    val modifiedBy: String
)

data class FormSection(
    val id: String,
    val title: Map<String, String>,  // Localized titles
    val order: Int,
    val collapsible: Boolean = false,
    val defaultCollapsed: Boolean = false,
    val fields: List<FieldConfig>,
    val visibilityRule: String? = null,  // Condition for showing section
    val columns: Int = 1  // Layout columns (1, 2, or 3)
)

data class FieldConfig(
    val fieldKey: String,  // e.g., "customer.name", "custom.region_code"
    val order: Int,
    val label: Map<String, String>? = null,  // Override default label
    val placeholder: Map<String, String>? = null,
    val helpText: Map<String, String>? = null,
    val required: Boolean? = null,  // Override field requirement
    val readOnly: Boolean = false,
    val visible: Boolean = true,
    val visibilityRule: String? = null,  // Role or condition-based
    val defaultValue: Any? = null,
    val width: FieldWidth = FieldWidth.FULL,
    val inputType: InputType = InputType.AUTO  // Override input control
)

enum class FieldWidth {
    FULL,      // 100%
    HALF,      // 50%
    THIRD,     // 33%
    TWO_THIRDS // 66%
}

enum class InputType {
    AUTO,           // Infer from data type
    TEXT,           // Single-line text
    TEXTAREA,       // Multi-line text
    NUMBER,         // Number input
    DATE,           // Date picker
    DATETIME,       // Date-time picker
    SELECT,         // Dropdown
    MULTISELECT,    // Multi-select dropdown
    RADIO,          // Radio buttons
    CHECKBOX,       // Checkbox
    SWITCH,         // Toggle switch
    AUTOCOMPLETE    // Autocomplete with search
}

enum class ConfigStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}
```

#### UI Configuration Service

```kotlin
// UIConfigurationService.kt
package com.chiroerp.platform.ui

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class UIConfigurationService {

    @Inject
    lateinit var configRepository: UIConfigurationRepository

    @Inject
    lateinit var authService: AuthorizationService  // From ADR-014

    @Inject
    lateinit var cache: UIConfigCache

    /**
     * Get effective form layout for tenant and user
     * Applies role-based visibility rules
     */
    fun getFormLayout(
        tenantId: String,
        entityType: EntityType,
        screenKey: String,
        userId: String,
        roles: Set<String>,
        locale: String = "en-US"
    ): FormLayout {
        // Load base layout for tenant
        val baseLayout = cache.get(tenantId, entityType, screenKey) {
            configRepository.findPublishedLayout(tenantId, entityType, screenKey)
                ?: getDefaultLayout(entityType, screenKey)
        }

        // Apply role-based visibility
        return applyVisibilityRules(baseLayout, userId, roles, tenantId)
    }

    /**
     * Apply role-based visibility rules
     */
    private fun applyVisibilityRules(
        layout: FormLayout,
        userId: String,
        roles: Set<String>,
        tenantId: String
    ): FormLayout {
        val filteredSections = layout.sections
            .filter { section ->
                // Check section visibility
                section.visibilityRule == null ||
                    evaluateVisibilityRule(section.visibilityRule, userId, roles, tenantId)
            }
            .map { section ->
                // Filter fields by visibility
                val filteredFields = section.fields.filter { field ->
                    field.visible && (field.visibilityRule == null ||
                        evaluateVisibilityRule(field.visibilityRule, userId, roles, tenantId))
                }
                section.copy(fields = filteredFields)
            }
            .filter { it.fields.isNotEmpty() }  // Remove empty sections

        return layout.copy(sections = filteredSections)
    }

    /**
     * Evaluate visibility rule using Authorization Objects (ADR-014)
     */
    private fun evaluateVisibilityRule(
        rule: String,
        userId: String,
        roles: Set<String>,
        tenantId: String
    ): Boolean {
        // Parse rule format: "hasAuthObject:F_CUSTOMER_CREDIT_LIMIT"
        return when {
            rule.startsWith("hasAuthObject:") -> {
                val authObjectId = rule.substringAfter("hasAuthObject:")
                authService.checkAuthObject(tenantId, userId, authObjectId)
            }
            rule.startsWith("hasRole:") -> {
                val requiredRole = rule.substringAfter("hasRole:")
                roles.contains(requiredRole)
            }
            rule.startsWith("hasAnyRole:") -> {
                val requiredRoles = rule.substringAfter("hasAnyRole:").split(",")
                requiredRoles.any { it in roles }
            }
            else -> true  // Unknown rule = visible by default
        }
    }

    /**
     * Publish UI configuration
     */
    fun publishConfiguration(
        tenantId: String,
        layoutId: String,
        publishedBy: String
    ): FormLayout {
        val layout = configRepository.findById(tenantId, layoutId)
            ?: throw NotFoundException("Layout not found: $layoutId")

        // Validate configuration
        validateLayout(layout)

        // Archive current published version (if any)
        configRepository.archivePublished(tenantId, layout.entityType, layout.screenKey)

        // Publish new version
        val published = layout.copy(
            status = ConfigStatus.PUBLISHED,
            effectiveFrom = Instant.now(),
            modifiedAt = Instant.now(),
            modifiedBy = publishedBy
        )

        configRepository.save(published)

        // Invalidate cache
        cache.invalidate(tenantId, layout.entityType, layout.screenKey)

        return published
    }

    private fun validateLayout(layout: FormLayout) {
        // Validate section order uniqueness
        val sectionOrders = layout.sections.map { it.order }
        require(sectionOrders.distinct().size == sectionOrders.size) {
            "Section orders must be unique"
        }

        // Validate field order uniqueness within sections
        layout.sections.forEach { section ->
            val fieldOrders = section.fields.map { it.order }
            require(fieldOrders.distinct().size == fieldOrders.size) {
                "Field orders must be unique within section: ${section.id}"
            }
        }

        // Validate field keys reference valid fields
        layout.sections.flatMap { it.fields }.forEach { field ->
            // Check if custom field exists (if custom.*)
            if (field.fieldKey.startsWith("custom.")) {
                // Validation logic for custom fields
            }
        }

        // Validate max sections
        require(layout.sections.size <= 20) {
            "Maximum 20 sections allowed per form"
        }

        // Validate max fields per section
        layout.sections.forEach { section ->
            require(section.fields.size <= 50) {
                "Maximum 50 fields allowed per section"
            }
        }
    }

    private fun getDefaultLayout(
        entityType: EntityType,
        screenKey: String
    ): FormLayout {
        // Return built-in default layout
        // This would come from system configuration
        return FormLayout(
            id = "default_${entityType}_${screenKey}",
            tenantId = "SYSTEM",
            entityType = entityType,
            screenKey = screenKey,
            sections = emptyList(),
            status = ConfigStatus.PUBLISHED,
            createdAt = Instant.now(),
            createdBy = "SYSTEM",
            modifiedAt = Instant.now(),
            modifiedBy = "SYSTEM"
        )
    }
}
```

#### UI Configuration JSON Example

```json
{
  "id": "cust_create_layout_v2",
  "tenantId": "tenant_acme_corp",
  "entityType": "CUSTOMER",
  "screenKey": "customer_create",
  "version": 2,
  "status": "PUBLISHED",
  "sections": [
    {
      "id": "basic_info",
      "title": {
        "en-US": "Basic Information",
        "es-MX": "Información Básica"
      },
      "order": 1,
      "collapsible": false,
      "columns": 2,
      "fields": [
        {
          "fieldKey": "customer.name",
          "order": 1,
          "required": true,
          "width": "FULL"
        },
        {
          "fieldKey": "customer.taxId",
          "order": 2,
          "width": "HALF",
          "inputType": "TEXT"
        },
        {
          "fieldKey": "custom.region_code",
          "order": 3,
          "label": {
            "en-US": "Sales Region",
            "es-MX": "Región de Ventas"
          },
          "width": "HALF",
          "inputType": "SELECT"
        }
      ]
    },
    {
      "id": "credit_info",
      "title": {
        "en-US": "Credit Information",
        "es-MX": "Información de Crédito"
      },
      "order": 2,
      "collapsible": true,
      "defaultCollapsed": false,
      "visibilityRule": "hasAuthObject:F_CUSTOMER_CREDIT_LIMIT",
      "columns": 2,
      "fields": [
        {
          "fieldKey": "customer.creditLimit",
          "order": 1,
          "width": "HALF",
          "inputType": "NUMBER",
          "helpText": {
            "en-US": "Maximum credit limit for this customer",
            "es-MX": "Límite de crédito máximo para este cliente"
          }
        },
        {
          "fieldKey": "customer.paymentTerms",
          "order": 2,
          "width": "HALF",
          "inputType": "SELECT"
        }
      ]
    }
  ],
  "effectiveFrom": "2026-02-01T00:00:00Z",
  "createdAt": "2026-01-15T10:30:00Z",
  "createdBy": "admin@acmecorp.com",
  "modifiedAt": "2026-01-20T14:45:00Z",
  "modifiedBy": "admin@acmecorp.com"
}
```

#### UI Configuration Limits

| Constraint | Limit | Rationale |
|------------|-------|-----------|
| **Max Sections per Form** | 20 | Prevent overwhelming UI |
| **Max Fields per Section** | 50 | Maintain scannable sections |
| **Max Columns** | 3 | Responsive design limits |
| **Max Active Layouts** | 10 per screen | Version/tenant combinations |
| **Visibility Rule Depth** | 5 levels | Prevent complex conditionals |
| **Cache TTL** | 5 minutes | Balance freshness vs performance |

### UI Configuration per Tenant
- **Layout**: Tenant-specific form layouts (grouping, ordering, collapsible sections).
- **Visibility**: Role-based field visibility tied to authorization objects (ADR-014).
- **Defaults**: Per-tenant default values and hints without changing domain rules.
- **Auditability**: UI metadata changes are versioned and reviewable.

### Upgrade Path & Compatibility
- **Versioned schemas**: Customization definitions include schema version and migration notes.
- **Compatibility checks**: Pre-upgrade validation ensures customizations remain valid.
- **Deprecation policy**: Deprecated fields/rules remain supported through a defined window.
- **Rollback safety**: Ability to revert to last known good customization snapshot.

### Automation & Tooling

### Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Backend Framework** | Quarkus (Jakarta EE) | Core service implementation |
| **Language** | Kotlin 1.9+ | Type-safe service code |
| **Database** | PostgreSQL 15+ | Extension tables, JSONB storage |
| **Caching** | Redis 7+ | Configuration & metadata caching |
| **Expression Evaluation** | Custom Safe Evaluator | Validation & workflow conditions |
| **Workflow DSL** | YAML 1.2 | Declarative workflow definitions |
| **API** | REST (OpenAPI 3.0) | Configuration management endpoints |
| **Frontend** | React 18+ | UI designer & form renderer |
| **Testing** | JUnit 5, REST Assured, TestContainers | Comprehensive testing (ADR-019) |
| **Monitoring** | Prometheus, Grafana | Performance & usage metrics (ADR-017) |
| **Audit Storage** | PostgreSQL (separate schema) | Immutable audit trail |
| **Version Control** | Git | Configuration as code |

### Developer Tools

1. **CustomFieldCLI**: Command-line tool for bulk field registration
2. **WorkflowValidator**: Pre-publish validation tool for workflow definitions
3. **ConfigDiffTool**: Compare configuration versions and generate migration notes
4. **PerformanceProfiler**: Analyze custom field query performance
5. **AuditExporter**: Export audit logs in compliance-friendly formats
6. **MigrationSimulator**: Test upgrades with tenant customizations

### Continuous Integration

```yaml
# .github/workflows/customization-tests.yml
name: Customization Framework Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'

      - name: Run custom field tests
        run: ./gradlew test --tests "*CustomField*"

      - name: Run workflow engine tests
        run: ./gradlew test --tests "*Workflow*"

      - name: Run validation rule tests
        run: ./gradlew test --tests "*Validation*"

      - name: Run UI config tests
        run: ./gradlew test --tests "*UIConfiguration*"

      - name: Performance regression tests
        run: ./gradlew performanceTest

      - name: Security scan (expression evaluator)
        run: ./gradlew securityScan

      - name: Generate coverage report
        run: ./gradlew jacocoTestReport

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
```

### Conclusion

The Tenant Customization Framework is a **strategic investment** that positions ChiroERP as a true SAP-grade international ERP system. By providing deep customization capabilities without code forks, we enable:

### Key Success Factors

1. **Declarative-First Design**: No code execution ensures security and upgradability
2. **Performance Optimization**: Indexed columns, caching, strict limits prevent degradation
3. **Upgrade Safety**: Versioned schemas, compatibility checking, automated migration
4. **Governance Controls**: Audit trail, approval workflows, rollback capability
5. **Tenant Autonomy**: Self-service configuration reduces dev bottleneck

### Expected Outcomes

- **Tenant Satisfaction**: Tenants can configure the system to match their unique processes
- **Competitive Advantage**: Match SAP SPRO customization depth at cloud-native scale
- **Operational Efficiency**: 30-40% reduction in custom development requests
- **Revenue Growth**: Support 20% more tenants without scaling dev team
- **Market Leadership**: Differentiate from competitors with inferior customization capabilities

### SAP-Grade Alignment

This framework directly addresses the SAP-grade ERP requirements identified in our architecture assessment:

| SAP Capability | ChiroERP Equivalent | Status |
|----------------|---------------------|--------|
| **SPRO Configuration** | Configuration Layering (Global → Tenant → User) | ✅ Designed |
| **Enhancement Framework** | Custom Field Metadata Registry | ✅ Designed |
| **BRFplus Rules** | Custom Validation Rule Engine | ✅ Designed |
| **Workflow Customization** | Declarative Workflow DSL | ✅ Designed |
| **User Exits** | Not supported (by design - no code execution) | ⚠️ Intentional |
| **Authorization Objects** | Integrated with ADR-014 | ✅ Integrated |
| **Transport System** | Configuration versioning + Git | ✅ Designed |
| **Upgrade Safety** | Compatibility checker + automated migration | ✅ Designed |

### Next Steps

1. **Approval**: Stakeholder review and approval of this ADR
2. **Prioritization**: Confirm placement in product roadmap (suggested: Month 7)
3. **Staffing**: Allocate 8.25 FTE for 16-month implementation
4. **Pilot Selection**: Identify pilot tenant for Phase 8 rollout
5. **Training Plan**: Develop training curriculum for power users and support team

## Alternatives Considered

### Alternative 1: Pure RBAC-Based Customization
- **Approach**: Rely solely on role-based permissions and feature flags for tenant differentiation. No custom fields or metadata extensions.
- **Pros**:
  - Simple to implement and maintain
  - No schema changes or metadata storage required
  - Low performance overhead
- **Cons**:
  - Extremely limited flexibility (feature on/off only)
  - Cannot support custom business objects or fields
  - Forces business logic into code for tenant-specific requirements
  - No SAP-level customization capability
- **Decision**: Rejected. This approach cannot support the business requirement for SAP-grade configurability and would require custom development for each tenant-specific requirement.

### Alternative 2: Code-Based Tenant Extensions
- **Approach**: Allow tenants to upload custom code (scripts, plugins) to extend behavior. Similar to Salesforce Apex or Shopify apps.
- **Pros**:
  - Maximum flexibility for tenants
  - Can support arbitrary business logic
  - Developer ecosystem potential
- **Cons**:
  - Severe security risks (untrusted code execution)
  - Performance unpredictability (no resource limits)
  - Upgrade/migration nightmare (breaking changes in tenant code)
  - High support burden (debugging tenant code)
  - SOX/GDPR compliance risks (tenant code accessing sensitive data)
- **Decision**: Rejected. Security, compliance, and maintainability risks outweigh flexibility benefits. Not suitable for financial/healthcare ERP.

### Alternative 3: SAP SPRO Clone (Pure Transaction Codes)
- **Approach**: Full replication of SAP's Implementation Guide (IMG) and SPRO transaction-based customization. 5,000+ configuration tables.
- **Pros**:
  - Feature parity with SAP for customization depth
  - Familiar to SAP consultants
  - Proven model for complex enterprise scenarios
- **Cons**:
  - Massive development effort (18-24 months)
  - Overwhelming complexity for non-SAP users
  - Over-engineering for SaaS multi-tenant model
  - High training barrier for customers
- **Decision**: Rejected. Too complex for initial implementation. Deferred to Phase 9+ for full IMG parity if market demands.

### Alternative 4: Metadata-Only Approach (No Custom Tables)
- **Approach**: Store all customizations as JSON/YAML metadata in a single configuration table. No ALTER TABLE or schema migrations.
- **Pros**:
  - Simplest to implement
  - No database schema changes
  - Fast deployment of custom fields
- **Cons**:
  - Poor query performance (JSON filtering in WHERE clauses)
  - No database-level constraints or referential integrity
  - No indexing on custom fields
  - Limited integration with PostgreSQL features (e.g., full-text search, GIN indexes)
  - Cannot support complex data types (arrays, enums)
- **Decision**: Rejected as sole approach. Performance and integrity trade-offs unacceptable for financial data. However, metadata-driven UI configuration is retained as part of the hybrid solution.

### Alternative 5: Hybrid Framework (Selected Approach)
- **Approach**: Combine configuration (key-value settings), custom fields (ALTER TABLE via migrations), and metadata-driven UI extensions. Layered resolution with system defaults → tenant overrides.
- **Pros**:
  - Balances flexibility with performance
  - Database-level integrity for custom fields (constraints, indexes)
  - Metadata-driven UI for low-code customization
  - SAP-inspired but simplified for SaaS
  - Supports 80% of customization needs without code
  - Clear upgrade path (metadata versioning, field deprecation)
- **Cons**:
  - More complex than metadata-only approach
  - Requires schema migration tooling for custom fields
  - Moderate implementation effort (16 months, 8.25 FTE)
- **Decision**: Selected. Provides SAP-grade configurability with modern SaaS architecture. Aligns with roadmap positioning (Phase 4-5: enterprise-ready, Phase 6-8: customization layers).

## Consequences
### Positive

1. **SAP-Grade Flexibility**
   - **Tenant Autonomy**: Tenants can configure the system to match their specific business processes without code changes
   - **Reduced Custom Development**: 30-40% reduction in custom development requests
   - **Faster Onboarding**: New tenants can be configured in days instead of weeks
   - **Market Differentiation**: Compete directly with SAP SPRO customization capabilities
   - **Revenue Growth**: Support 20% more tenant customization requests without scaling dev team

2. **Upgrade Safety**
   - **Version Compatibility**: Automated compatibility checking prevents breaking changes during upgrades
   - **Migration Automation**: Generated migration scripts reduce upgrade risk
   - **Rollback Capability**: Quick rollback in case of issues (< 5 minutes)
   - **Deprecation Management**: Clear deprecation policy with advance warnings
   - **Zero Downtime**: Upgrades don't require tenant downtime for customizations

3. **Operational Efficiency**
   - **Self-Service**: Power users can make configuration changes without dev team involvement
   - **Audit Compliance**: Complete audit trail for SOX, GDPR, HIPAA compliance
   - **Governance Controls**: Maker-checker approval workflows for critical changes
   - **Performance Monitoring**: Real-time monitoring of customization impact on performance
   - **Support Reduction**: Self-service tools reduce support ticket volume by 40%

4. **Technical Excellence**
   - **Declarative Approach**: No code execution ensures security and upgradability
   - **Multi-Tenancy Safe**: Tenant isolation maintained at all customization levels
   - **Performance Optimized**: Indexed custom fields, caching, pre-allocated columns
   - **Scalable Architecture**: Support 100+ tenants with unique customizations
   - **Standards-Based**: Uses industry-standard patterns (YAML DSL, JSONB, REST API)

### Negative

1. **Implementation Complexity**
   - **Development Effort**: 8.25 FTE over 16 months is significant investment
   - **Learning Curve**: Team must learn new patterns (DSL parsing, dynamic DDL, expression evaluation)
   - **Testing Complexity**: Combinatorial explosion of customization combinations
   - **Migration Risk**: Existing tenants must be migrated carefully
   - **Mitigation**: Phased rollout, pilot tenant program, comprehensive testing, rollback capability

2. **Performance Overhead**
   - **Query Impact**: Custom fields add 5-10% query overhead even with optimization
   - **Validation Cost**: Custom validation adds 20-50ms per entity save
   - **Cache Pressure**: Configuration caching increases Redis memory usage by 10-15%
   - **Index Cost**: Each indexed custom field adds index maintenance overhead
   - **Mitigation**: Strict field limits, query optimization, cache tuning, performance monitoring (ADR-017)

3. **Maintenance Burden**
   - **Schema Evolution**: Custom field schemas must be migrated with each version
   - **Compatibility Testing**: Every release requires compatibility testing across tenant customizations
   - **Documentation**: Customization framework requires extensive documentation and training
   - **Support Complexity**: Support team must understand customization framework
   - **Mitigation**: Automated testing, versioned schemas, self-service tools, comprehensive training

4. **Design Constraints**
   - **Extension Point Limits**: Not all system aspects can be customized (must design extension points)
   - **Field Limits**: Strict limits (20-50 fields) may frustrate some tenants
   - **Workflow Complexity**: 20-step limit may be insufficient for complex approval chains
   - **UI Flexibility**: Pre-defined layouts may not satisfy all UI requirements
   - **Mitigation**: Carefully designed extension points, clear documentation of limits, escalation path for exceptions

5. **Security Concerns**
   - **Expression Injection**: Safe evaluator must prevent code injection attacks
   - **Data Exposure**: Custom fields may expose sensitive data if visibility rules not configured
   - **Privilege Escalation**: Workflow customization could bypass authorization controls
   - **Audit Bypass**: Customization approval workflows must be tamper-proof
   - **Mitigation**: Security review, safe evaluator, integration with ADR-014 auth objects, immutable audit trail

### Neutral

1. **Scope Management**
   - Some tenant requests will exceed framework capabilities and must be rejected or handled as custom development
   - Clear escalation process needed for out-of-scope requests
   - Product management must balance framework flexibility vs. complexity

2. **Training Investment**
   - Tenants require training to use customization framework effectively
   - Power users need deeper training on advanced features (workflows, validation rules)
   - Support team requires comprehensive training on troubleshooting customizations

3. **Vendor Comparison**
   - Framework will be compared to SAP SPRO, Oracle EBS DFF, NetSuite customization
   - Some features may not match SAP 1:1 (acceptable trade-off for cloud-native architecture)
   - Clear communication needed about framework capabilities vs. limitations

4. **Resource Allocation**
   - 8.25 FTE investment competes with feature development resources
   - Opportunity cost: features that won't be built during customization framework development
   - Strategic decision: framework unlocks tenant autonomy vs. specific feature requests

### Positive
- Enables SAP-grade tenant flexibility without code forks
- Upgrade-safe customization with strict governance and auditability
- Consistent customization UX across modules
- Supports regulated industries via controlled, versioned changes

### Negative / Risks
- Additional platform complexity and operational surface
- Requires careful extension-point design per context
- Customization misuse could lead to performance issues (unbounded fields)

### Neutral
- Some tenant requests will be rejected if they exceed allowed extension points

## Compliance
### Governance & Safety

### Versioning and Audit
- All customization changes are versioned, reviewed, and auditable.
- Publish pipeline with validation checks and rollback capability.
- Immutable audit log: who changed what, when, and why.

### Validation
- Schema validation on all custom configuration and metadata changes.
- Consistency checks ensure required fields and compliance rules remain intact.

### Isolation & Compliance
- Customization data stored within tenant isolation tier (row/schema/DB).
- No cross-tenant reads or shared caches without tenant scoping.
- GDPR: custom fields must be classified (PII/PHI) for retention policies.

### Regulatory Compliance

### SOX Compliance: Change Management and Audit Trail
- **Customization Audit Trail**: All configuration changes, custom field additions, and metadata modifications are logged with immutable records (who, what, when, why, version).
- **Change Control Process**: Multi-stage approval workflow for customizations affecting financial modules (e.g., GL account structures, cost center hierarchies). Requires business owner approval + IT compliance review.
- **Segregation of Duties**: Customization publishers cannot be the same users who initiate the changes. Enforced by workflow engine.
- **Rollback Capability**: All customizations are versioned. Can roll back to previous configuration version with audit trail documenting the rollback reason.
- **Evidence Retention**: Customization audit logs retained for 7 years. Quarterly reports generated for SOX 404 internal controls testing.
- **Testing Requirements**: All customizations to financial modules must pass validation tests (referential integrity, business rule consistency) before publishing.

### GDPR Compliance: Custom Field Classification
- **PII/PHI Tagging**: Custom fields must be tagged with data classification (Public, Internal, PII, PHI) at creation time. Default: Internal.
- **Purpose Limitation**: Custom fields require documented business purpose. Purpose displayed in UI when users view/edit the field.
- **Right to Access**: Custom field data included in GDPR data export (SAR - Subject Access Request). Metadata describes custom field purpose and legal basis.
- **Right to Erasure**: Custom fields tagged as PII/PHI are included in erasure workflows (right to be forgotten). Tombstone records replace deleted values.
- **Data Minimization**: Custom field creation wizard warns if field name suggests PII (e.g., "Social Security", "Date of Birth"). Requires explicit classification override.
- **Retention Policies**: Custom fields inherit retention policies based on classification. PII fields auto-archive/delete per tenant GDPR policy.

### Multi-Tenancy Isolation
- **Tenant Scoping**: All customization queries include `tenant_id` in WHERE clause. No cross-tenant reads permitted.
- **Schema Isolation**: Custom fields created via `ALTER TABLE` are prefixed with `tenant_{tenant_id}_` for schema-level tenants, or namespaced in row-level isolation.
- **Cache Isolation**: Configuration resolver caches are tenant-scoped. Cache invalidation on customization publish is tenant-specific.
- **Test Data Isolation**: Custom fields in test environments use synthetic data generators. No production PII in custom fields during testing.

### ISO 27001 Compliance: Access Control
- **Principle of Least Privilege**: Only users with `TENANT_ADMIN` or `CUSTOMIZATION_MANAGER` roles can create/modify customizations.
- **Access Logging**: All access to customization management UI logged (read, write, delete). Logs include IP address, user ID, timestamp.
- **Session Management**: Customization management sessions timeout after 30 minutes of inactivity. Re-authentication required.

### Service Level Objectives (SLOs)
- **Configuration Resolution Latency**: `<50ms` for 99th percentile (layered resolution with caching).
- **Customization Publish Time**: `<5 minutes` for configuration changes, `<30 minutes` for custom field schema migrations (includes validation, approval, deployment).
- **Audit Log Completeness**: `100%` of customization changes logged. Monthly audit log integrity checks.
- **Rollback Success Rate**: `>99%` of rollback requests complete without data loss.
- **Cache Invalidation Propagation**: `<10 seconds` for configuration changes to propagate to all service instances.
- **Custom Field Query Performance**: No more than `10%` overhead compared to standard fields (tested at P95 latency).

## Implementation Plan
### Phase 1: Configuration Service Foundation (Months 1-2)

**Objectives:**
- Establish configuration infrastructure
- Implement tenant-scoped configuration storage
- Build configuration layering and resolution

**Activities:**
1. **Configuration Storage Design** (Week 1-2)
   - Design tenant_configurations table schema
   - Implement configuration versioning
   - Create effective dating logic
   - Build configuration audit trail

2. **Configuration Resolver** (Week 3-4)
   - Implement ConfigurationResolver.kt
   - Build multi-layer resolution (Global → Region → Tenant → Org Unit → User)
   - Implement caching strategy with Redis
   - Create cache invalidation mechanism

3. **Configuration API** (Week 5-6)
   - Build REST endpoints for configuration CRUD
   - Implement configuration validation
   - Create configuration import/export
   - Build configuration diff viewer

4. **Testing & Documentation** (Week 7-8)
   - Unit tests for resolution logic
   - Integration tests for layering
   - API documentation (OpenAPI)
   - Configuration management guide

**Deliverables:**
- ✅ Configuration service operational
- ✅ Multi-layer resolution working
- ✅ Configuration API documented
- ✅ 90%+ test coverage

**Resources:**
- 2 Backend Engineers
- 1 DevOps Engineer
- 1 QA Engineer

**Success Criteria:**
- Configuration resolution < 50ms (P95)
- Cache hit rate > 95%
- Zero configuration conflicts in test

### Phase 2: Custom Field Metadata Registry (Months 3-4)

**Objectives:**
- Implement custom field registration
- Build field metadata management
- Create extension table architecture

**Activities:**
1. **Registry Service** (Week 1-3)
   - Implement CustomFieldRegistry.kt
   - Build field registration with limits
   - Create field validation framework
   - Implement field versioning

2. **Extension Tables** (Week 3-4)
   - Create extension table templates (SQL)
   - Build dynamic DDL execution
   - Implement indexed field creation
   - Set up JSONB storage for non-indexed fields

3. **API Integration** (Week 5-6)
   - Extend entity APIs with custom fields
   - Implement custom.* namespace
   - Build field value validation
   - Create field discovery endpoint

4. **Migration Tools** (Week 7-8)
   - Build field migration utility
   - Create data backfill scripts
   - Implement field deprecation workflow
   - Build field usage analytics

**Deliverables:**
- ✅ CustomFieldRegistry operational
- ✅ Extension tables for 6 core entities
- ✅ API endpoints extended
- ✅ Migration toolkit complete

**Resources:**
- 2 Backend Engineers
- 1 Database Engineer
- 1 QA Engineer

**Success Criteria:**
- Field registration < 200ms
- Query performance degradation < 10%
- Support 50 fields per entity
- Zero data loss during migration

### Phase 3: Workflow Customization DSL (Months 5-6)

**Objectives:**
- Implement declarative workflow DSL
- Build workflow execution engine
- Create approval routing logic

**Activities:**
1. **DSL Design** (Week 1-2)
   - Finalize YAML-based DSL schema
   - Implement DSL parser and validator
   - Build workflow definition storage
   - Create workflow versioning

2. **Workflow Engine** (Week 3-5)
   - Implement WorkflowEngine.kt
   - Build step evaluation and activation
   - Create assignee resolution logic
   - Implement SLA tracking and escalation

3. **Integration Points** (Week 5-6)
   - Integrate with entity lifecycle hooks
   - Build workflow trigger mechanism
   - Create workflow instance API
   - Implement notification service integration

4. **Workflow Designer UI** (Week 7-8)
   - Build visual workflow designer (React)
   - Create step configuration forms
   - Implement workflow testing UI
   - Build workflow monitoring dashboard

**Deliverables:**
- ✅ Workflow DSL operational
- ✅ Workflow engine processing workflows
- ✅ Workflow designer UI live
- ✅ Monitoring dashboard deployed

**Resources:**
- 2 Backend Engineers
- 2 Frontend Engineers
- 1 QA Engineer

**Success Criteria:**
- Workflow start time < 500ms
- Step evaluation < 100ms
- Support 20 steps per workflow
- Designer usable by non-technical users

### Phase 4: Validation Rule Extensions (Months 7-8)

**Objectives:**
- Implement custom validation rules
- Build safe expression evaluator
- Integrate with entity validation pipeline

**Activities:**
1. **Validation Engine** (Week 1-3)
   - Implement CustomValidationEngine.kt
   - Build SafeExpressionEvaluator.kt
   - Create rule type implementations
   - Implement localized error messages

2. **Rule Management** (Week 3-4)
   - Build rule registration API
   - Create rule versioning
   - Implement rule testing framework
   - Build rule usage analytics

3. **Integration** (Week 5-6)
   - Integrate with ADR-010 validation pipeline
   - Extend entity validation endpoints
   - Build validation result formatting
   - Create validation bypass for system users

4. **Testing & Documentation** (Week 7-8)
   - Unit tests for all rule types
   - Integration tests with entities
   - Performance testing (validation overhead)
   - Rule authoring guide

**Deliverables:**
- ✅ Validation engine operational
- ✅ 5 rule types supported
- ✅ Safe evaluator security-tested
- ✅ Rule authoring documented

**Resources:**
- 2 Backend Engineers
- 1 Security Engineer
- 1 QA Engineer

**Success Criteria:**
- Validation overhead < 50ms per entity
- Zero code execution vulnerabilities
- Support 30 rules per entity
- Localization for 5+ languages

### Phase 5: UI Customization Framework (Months 9-10)

**Objectives:**
- Implement form layout configuration
- Build role-based field visibility
- Create UI configuration management

**Activities:**
1. **UI Configuration Service** (Week 1-3)
   - Implement UIConfigurationService.kt
   - Build layout storage and versioning
   - Create visibility rule engine
   - Implement layout caching

2. **Frontend Integration** (Week 3-5)
   - Build dynamic form renderer (React)
   - Implement field visibility logic
   - Create section collapsing/grouping
   - Build responsive layout engine

3. **Configuration UI** (Week 5-7)
   - Build layout designer UI
   - Create field drag-and-drop
   - Implement layout preview
   - Build layout publishing workflow

4. **Testing & Documentation** (Week 7-8)
   - Browser compatibility testing
   - Accessibility testing (WCAG 2.1 AA)
   - Performance testing (render time)
   - UI configuration guide

**Deliverables:**
- ✅ UI configuration service live
- ✅ Dynamic form renderer deployed
- ✅ Layout designer usable
- ✅ WCAG 2.1 AA compliant

**Resources:**
- 1 Backend Engineer
- 3 Frontend Engineers
- 1 UX Designer
- 1 QA Engineer

**Success Criteria:**
- Form render time < 1s
- Support 20 sections, 50 fields/section
- Layout designer usable by power users
- Accessibility score 95+

### Phase 6: Governance & Audit Framework (Months 11-12)

**Objectives:**
- Implement customization audit logging
- Build approval workflows for changes
- Create rollback capability

**Activities:**
1. **Audit Infrastructure** (Week 1-2)
   - Design audit log schema
   - Implement immutable audit trail
   - Build audit query API
   - Create audit data retention policy

2. **Approval Workflows** (Week 3-4)
   - Build customization approval workflow
   - Implement maker-checker pattern
   - Create approval notification system
   - Build approval UI

3. **Rollback & Recovery** (Week 5-6)
   - Implement configuration snapshot
   - Build rollback mechanism
   - Create rollback testing
   - Implement rollback UI

4. **Compliance & Reporting** (Week 7-8)
   - Build compliance reports (SOX, GDPR)
   - Create audit export functionality
   - Implement audit search and filtering
   - Build audit dashboard

**Deliverables:**
- ✅ Audit logging operational
- ✅ Approval workflows enforced
- ✅ Rollback capability tested
- ✅ Compliance reports available

**Resources:**
- 1 Backend Engineer
- 1 Frontend Engineer
- 1 Compliance Specialist
- 1 QA Engineer

**Success Criteria:**
- 100% customization changes audited
- Rollback time < 5 minutes
- Audit query response < 2s
- Compliance reports validated

### Phase 7: Upgrade Compatibility System (Months 13-14)

**Objectives:**
- Implement version compatibility checking
- Build customization migration tools
- Create deprecation policy enforcement

**Activities:**
1. **Compatibility Framework** (Week 1-3)
   - Implement CompatibilityChecker.kt
   - Build version schema registry
   - Create breaking change detection
   - Implement compatibility validation

2. **Migration Tools** (Week 3-5)
   - Build customization snapshot
   - Create migration script generator
   - Implement automated migration testing
   - Build migration UI

3. **Deprecation Management** (Week 5-6)
   - Implement deprecation policy
   - Build deprecation warnings
   - Create migration notifications
   - Implement forced migration

4. **Testing & Documentation** (Week 7-8)
   - Upgrade simulation testing
   - Migration rollback testing
   - Performance testing (migration time)
   - Upgrade guide documentation

**Deliverables:**
- ✅ Compatibility checker operational
- ✅ Migration tools tested
- ✅ Deprecation policy enforced
- ✅ Upgrade guide published

**Resources:**
- 2 Backend Engineers
- 1 DevOps Engineer
- 1 QA Engineer

**Success Criteria:**
- Pre-upgrade validation < 5 minutes
- Zero data loss during migration
- Migration time < 2 hours for 100K records
- Breaking changes detected 100%

### Phase 8: Production Rollout & Optimization (Months 15-16)

**Objectives:**
- Deploy to production
- Monitor performance and stability
- Optimize based on real usage

**Activities:**
1. **Production Deployment** (Week 1-2)
   - Deploy to staging environment
   - Conduct load testing (10x expected load)
   - Deploy to production (blue-green)
   - Monitor for 48 hours

2. **Tenant Migration** (Week 2-4)
   - Migrate pilot tenant (Week 2)
   - Gather feedback and iterate
   - Migrate 10 tenants (Week 3)
   - Migrate remaining tenants (Week 4)

3. **Performance Optimization** (Week 5-6)
   - Analyze query performance
   - Optimize slow queries
   - Tune cache configuration
   - Implement query result caching

4. **Training & Documentation** (Week 7-8)
   - Conduct admin training sessions
   - Create video tutorials
   - Build knowledge base articles
   - Establish support runbooks

**Deliverables:**
- ✅ Production deployment successful
- ✅ All tenants migrated
- ✅ Performance optimized
- ✅ Training materials published

**Resources:**
- 2 Backend Engineers
- 1 DevOps Engineer
- 1 QA Engineer
- 1 Technical Writer
- 1 Trainer

**Success Criteria:**
- Zero critical incidents post-launch
- Performance SLOs met (ADR-017)
- User satisfaction > 80%
- Support ticket volume < 5/week

### Resource Summary

| Role | Phase 1-2 | Phase 3-4 | Phase 5-6 | Phase 7-8 | Total FTE |
|------|-----------|-----------|-----------|-----------|-----------|
| **Backend Engineers** | 2 | 4 | 3 | 3 | 3.0 |
| **Frontend Engineers** | 0 | 2 | 4 | 0 | 1.5 |
| **Database Engineers** | 0 | 1 | 0 | 0 | 0.25 |
| **DevOps Engineers** | 1 | 0 | 0 | 2 | 0.75 |
| **QA Engineers** | 1 | 1 | 2 | 2 | 1.5 |
| **Security Engineers** | 0 | 0 | 1 | 0 | 0.25 |
| **UX Designers** | 0 | 0 | 1 | 0 | 0.25 |
| **Compliance Specialists** | 0 | 0 | 1 | 0 | 0.25 |
| **Technical Writers** | 0 | 0 | 0 | 1 | 0.25 |
| **Trainers** | 0 | 0 | 0 | 1 | 0.25 |
| **TOTAL** | 4 | 8 | 12 | 9 | **8.25 FTE** |

### Success Metrics & KPIs

#### Coverage Metrics
- **Custom Field Adoption**: 80% of tenants use custom fields within 6 months
- **Workflow Customization**: 60% of tenants customize at least one workflow
- **Validation Rules**: 50% of tenants add custom validation rules
- **UI Configuration**: 70% of tenants customize at least one screen

#### Usage Metrics
- **Custom Fields per Tenant**: Target avg 20 fields across all entities
- **Workflows per Tenant**: Target avg 5 customized workflows
- **Validation Rules per Tenant**: Target avg 15 custom rules
- **UI Layouts per Tenant**: Target avg 10 customized screens

#### Performance Metrics (from ADR-017)
- **Configuration Resolution**: < 50ms (P95)
- **Custom Field Query Overhead**: < 10% vs standard fields
- **Workflow Start Time**: < 500ms (P95)
- **Validation Overhead**: < 50ms per entity (P95)
- **UI Render Time**: < 1s for 50-field form (P95)

#### Quality Metrics
- **Customization Errors**: < 2% error rate on save
- **Rollback Rate**: < 1% of published customizations
- **Support Tickets**: < 5 per week after Month 3
- **User Satisfaction**: > 80% satisfaction score

#### Business Impact Metrics
- **Implementation Velocity**: 30% reduction in custom development requests
- **Tenant Onboarding**: 20% faster tenant onboarding time
- **Upgrade Success Rate**: 100% tenants upgrade within SLA
- **Revenue Impact**: Support 20% more tenant customization requests without additional dev resources

### Risk Mitigation

| Risk | Probability | Impact | Mitigation Strategy |
|------|-------------|--------|---------------------|
| **Performance Degradation** | Medium | High | Implement strict field limits, pre-allocate columns, extensive load testing |
| **Security Vulnerabilities** | Low | Critical | Safe expression evaluator, no code execution, security review |
| **Upgrade Complexity** | High | High | Versioned schemas, compatibility checker, automated migration |
| **Data Loss** | Low | Critical | Immutable audit trail, snapshot before changes, rollback capability |
| **User Adoption** | Medium | Medium | Training programs, intuitive UI, gradual rollout |
| **Support Overhead** | Medium | Medium | Comprehensive docs, video tutorials, self-service tools |
| **Technical Debt** | Medium | Medium | Regular refactoring sprints, code reviews, technical backlog |
| **Scope Creep** | High | Medium | Strict phase gates, MVP focus, defer non-critical features |

### Implementation Plan (Not Started)

- Phase 1: Define configuration service, schema registry, and layering rules.
- Phase 2: Implement custom field metadata and extension tables for 1-2 pilot contexts.
- Phase 3: Workflow DSL + execution engine with approval steps.
- Phase 4: Validation rule extension engine aligned with ADR-010.
- Phase 5: UI/report customization tooling and admin console.
- Phase 6: Integration mapping library and sandboxed rule evaluation.

### Implementation Notes

### Prerequisites

1. **Database**: PostgreSQL 15+ with JSONB support and GIN indexes
2. **Cache**: Redis 7+ cluster with 10GB+ memory allocation
3. **Authorization**: ADR-014 Authorization Objects framework must be implemented
4. **Validation**: ADR-010 REST validation standard must be in place
5. **Monitoring**: ADR-017 performance monitoring infrastructure
6. **Testing**: ADR-019 testing standards and E2E framework

### Current Status

**Status**: Draft (Not Implemented)
**Target Start**: Month 7 (after ADR-014 Authorization Objects complete)
**Target Completion**: Month 22 (16-month implementation)
**Priority**: P1 (High) - Core differentiator for SAP-grade ERP

### Success Criteria

#### Phase Gate Criteria

1. **Phase 1-2 Complete**: Configuration service and custom field registry operational
   - Configuration resolution < 50ms (P95)
   - Custom fields support 6 core entities
   - 90%+ test coverage

2. **Phase 3-4 Complete**: Workflow and validation frameworks operational
   - Workflow designer UI usable by non-technical users
   - Safe evaluator passes security review
   - Zero code injection vulnerabilities

3. **Phase 5-6 Complete**: UI customization and governance operational
   - WCAG 2.1 AA accessibility compliance
   - Audit logging for 100% of customizations
   - Rollback time < 5 minutes

4. **Phase 7-8 Complete**: Production deployment successful
   - All tenants migrated with zero data loss
   - Performance SLOs met (ADR-017)
   - User satisfaction > 80%

#### Business Success Criteria

- **Tenant Adoption**: 80% of tenants use customization framework within 6 months
- **Development Efficiency**: 30% reduction in custom development requests
- **Onboarding Speed**: 20% faster tenant onboarding time
- **Revenue Growth**: Support 20% more tenants without scaling dev team
- **Competitive Position**: Feature parity with SAP SPRO customization depth

## References

### Related ADRs
- ADR-005: Multi-Tenancy Isolation Strategy - Tenant isolation patterns (row/schema/database levels)
- ADR-006: Platform & Shared Governance - Configuration layering and governance
- ADR-010: REST Validation Standard - Validation pipeline integration for custom rules
- ADR-011: Saga Pattern for Distributed Transactions - Workflow state management
- ADR-014: Authorization Objects & SoD - Role-based field visibility and workflow assignments
- Our equivalent: ADR-014 Authorization Objects + UI visibility rules
- ADR-017: Performance Standards & Monitoring - SLOs for customization framework
- ADR-018: Deployment & Operations Strategy - Deployment patterns for customizations
- ADR-019: Testing Standards (E2E/UAT/Regression) - Testing approach for customizations

### Internal Documentation
- Business Add-Ins (BAdIs) for code extensions
- 4. SAP Workflow (Business Workflow)
- Multi-level configuration (client, company code, plant)
- Oracle E-Business Suite: Descriptive Flexfields (DFF) and Key Flexfields (KFF)
- Field-level authorization (e.g., F_BKPF_BUK)
- GDPR (General Data Protection Regulation): Custom field classification (PII/PHI)
- Our equivalent: Configuration Layering (Global → Tenant → User)
- HIPAA (Health Insurance Portability and Accountability Act): PHI handling in custom fields
- 1. SAP SPRO (IMG)
- SOX (Sarbanes-Oxley): Audit trail requirements for financial system changes
- Append Structures for custom fields (SE11)

### External References
- 2. SAP Enhancement Framework
- 3. SAP BRFplus
- 5. SAP Transport System
- 6. SAP Authorization Objects
- Business Rule Framework for declarative rules
- Change tracking and approval
- Expression Language Security: OWASP Injection Prevention Cheat Sheet
- Expression-based decision tables
- GIN Indexes: https://www.postgresql.org/docs/15/gin-intro.html
- Implementation Guide for system configuration
- ISO 27001: Information security management for customization framework
- Microsoft Dynamics: Custom entities and fields in Dataverse
- Multi-step approval processes
- NetSuite: Custom fields, custom records, SuiteScript extensibility
- OpenAPI 3.0: https://swagger.io/specification/
- Our equivalent: Configuration versioning + Git + Audit trail
- Our equivalent: Custom Field Metadata Registry + Extension Tables
- Our equivalent: Custom Validation Rule Engine with Safe Evaluator
- Our equivalent: Declarative Workflow DSL + WorkflowEngine
- PostgreSQL JSONB: https://www.postgresql.org/docs/15/datatype-json.html
- Salesforce: Custom objects, fields, validation rules, Process Builder
- ServiceNow: Business rules, UI policies, workflow activities
- Transaction codes and authorization checks
- Transport requests for moving configurations
- Visual workflow designer
- WCAG 2.1 AA: https://www.w3.org/WAI/WCAG21/quickref/
- YAML 1.2 Specification: https://yaml.org/spec/1.2/spec.html
