# ADR-061: Enterprise Data Migration Toolkit

**Status**: Accepted (Implementation Starting)  
**Date**: 2026-02-06  
**Deciders**: Architecture Team, Implementation Team, Customer Success Team  
**Priority**: P0 (Critical - Blocking Enterprise Sales)  
**Tier**: Platform Core  
**Tags**: data-migration, etl, onboarding, customer-success, integration, import

---

## Context

**Problem**: Enterprise customers **will not adopt ChiroERP** without a **proven, automated data migration path** from their existing ERP systems. The current manual migration process is:
- **Time-consuming**: 4-12 weeks per customer (80% data mapping + validation)
- **Error-prone**: 30-40% of migrations have data quality issues requiring rework
- **Expensive**: $50K-150K professional services cost per migration
- **Risky**: No automated validation, silent data corruption possible
- **Not scalable**: Blocks sales (cannot onboard 10+ enterprises simultaneously)

**Current State**:
- **Manual SQL scripts** (custom per customer, not reusable)
- **No validation framework** (data quality issues discovered post-migration)
- **No rollback capability** (failed migrations require manual cleanup)
- **No self-service** (100% dependency on professional services)
- **Competitive disadvantage**: SAP, Oracle, D365 offer automated migration tools

**Market Reality**:
- **80% of enterprise ERP deals** involve migration from legacy systems
- **Average migration cost** (SAP → ChiroERP): $50K-150K (professional services)
- **Average migration duration**: 8-12 weeks (blocks revenue recognition)
- **Customer churn risk**: 25% abandon project during migration phase
- **Lost deals**: 15-20% of pipeline lost due to migration complexity

**Competitive Position**:
- **SAP**: Migration Cockpit, Pre-Delivered Content (150+ source systems)
- **Oracle**: Data Migration Cloud Service (automated, AI-assisted)
- **Dynamics 365**: Data Import Wizard, Templates for 50+ systems
- **NetSuite**: SuiteSuccess Methodology, Pre-Built Connectors
- **ChiroERP**: ❌ No automated migration toolkit (manual only)

**Customer Quote** (Fortune 500 CFO):
> "We have 15 years of financial data in our legacy system. Show us a proven migration path with data validation, or we can't consider ChiroERP."

---

## Decision

Build an **Enterprise Data Migration Toolkit** providing:
1. **Pre-Built Connectors** for top 10 source systems (QuickBooks, Xero, Sage, SAP, Oracle, D365, NetSuite, Odoo, Acumatica, custom ERP)
2. **ETL Pipeline Framework** (Extract, Transform, Load with validation)
3. **Data Mapping Templates** (field mappings, business rules, default values)
4. **Automated Validation** (referential integrity, business rules, data quality)
5. **Migration Dashboard** (progress tracking, error reporting, rollback)
6. **Self-Service Wizard** (guided migration for SMB/mid-market)
7. **Professional Services API** (for complex migrations)

**Target**: Q4 2026 (production-ready with 5+ connectors)

---

## Architecture

### 1. Migration Service (Core Orchestration)

**New Platform Service**: `platform-operations/data-migration-service`

```kotlin
/**
 * Data Migration Service
 * Orchestrates end-to-end data migration from legacy ERP systems
 */
@Service
class MigrationOrchestrator(
    private val connectorRegistry: ConnectorRegistry,
    private val mappingEngine: MappingEngine,
    private val validationEngine: ValidationEngine,
    private val loadEngine: LoadEngine,
    private val rollbackManager: RollbackManager
) {
    
    /**
     * Execute migration workflow:
     * 1. Extract: Pull data from source system
     * 2. Transform: Map to ChiroERP schema + apply business rules
     * 3. Validate: Check data quality, referential integrity
     * 4. Load: Insert into ChiroERP (staged, transactional)
     * 5. Verify: Post-load validation, reconciliation
     */
    suspend fun executeMigration(request: MigrationRequest): MigrationResult {
        val executionId = UUID.randomUUID()
        val startTime = clock.now()
        
        logger.info("Starting migration $executionId: ${request.sourceSystem} → ChiroERP")
        
        try {
            // Stage 1: Extract data from source system
            val extraction = extractData(
                connector = request.connector,
                entities = request.entities,
                executionId = executionId
            )
            
            if (extraction.status == ExtractionStatus.FAILED) {
                return MigrationResult.Failed(
                    stage = MigrationStage.EXTRACT,
                    reason = extraction.error!!
                )
            }
            
            // Stage 2: Transform data (mapping + business rules)
            val transformation = transformData(
                extractedData = extraction.data,
                mappingTemplate = request.mappingTemplate,
                executionId = executionId
            )
            
            if (transformation.status == TransformationStatus.FAILED) {
                return MigrationResult.Failed(
                    stage = MigrationStage.TRANSFORM,
                    reason = transformation.error!!
                )
            }
            
            // Stage 3: Validate transformed data
            val validation = validateData(
                transformedData = transformation.data,
                rules = request.validationRules,
                executionId = executionId
            )
            
            if (!validation.isValid) {
                // Return validation errors for review
                return MigrationResult.ValidationFailed(
                    errors = validation.errors,
                    warnings = validation.warnings,
                    canProceed = validation.warnings.isNotEmpty() && validation.errors.isEmpty()
                )
            }
            
            // Stage 4: Load data into ChiroERP (transactional)
            val load = loadData(
                validatedData = validation.data,
                tenantId = request.tenantId,
                executionId = executionId,
                dryRun = request.dryRun
            )
            
            if (load.status == LoadStatus.FAILED) {
                // Automatic rollback on load failure
                rollbackManager.rollback(executionId)
                return MigrationResult.Failed(
                    stage = MigrationStage.LOAD,
                    reason = load.error!!
                )
            }
            
            // Stage 5: Post-load verification & reconciliation
            val verification = verifyMigration(
                executionId = executionId,
                tenantId = request.tenantId,
                expectedCounts = extraction.counts
            )
            
            if (!verification.isValid) {
                // Rollback if reconciliation fails
                rollbackManager.rollback(executionId)
                return MigrationResult.Failed(
                    stage = MigrationStage.VERIFY,
                    reason = "Reconciliation failed: ${verification.discrepancies}"
                )
            }
            
            val duration = clock.now() - startTime
            logger.info("Migration $executionId completed successfully in $duration")
            
            return MigrationResult.Success(
                executionId = executionId,
                duration = duration,
                recordsMigrated = load.counts,
                warnings = validation.warnings
            )
            
        } catch (e: Exception) {
            logger.error("Migration $executionId failed with exception", e)
            rollbackManager.rollback(executionId)
            return MigrationResult.Failed(
                stage = MigrationStage.UNKNOWN,
                reason = "Unexpected error: ${e.message}"
            )
        }
    }
}

data class MigrationRequest(
    val tenantId: UUID,
    val sourceSystem: SourceSystem,
    val connector: ConnectorConfig,
    val entities: List<EntityType>, // e.g., Customers, Invoices, Products
    val mappingTemplate: MappingTemplateId,
    val validationRules: List<ValidationRule>,
    val dryRun: Boolean = true // Default: test mode (no data written)
)

enum class SourceSystem {
    QUICKBOOKS_ONLINE,
    QUICKBOOKS_DESKTOP,
    XERO,
    SAGE_50,
    SAGE_INTACCT,
    SAP_ERP,
    SAP_BUSINESS_ONE,
    ORACLE_ERP_CLOUD,
    ORACLE_NETSUITE,
    DYNAMICS_365_BC, // Business Central
    DYNAMICS_365_FO, // Finance & Operations
    ODOO,
    ACUMATICA,
    CUSTOM_ERP,      // Generic CSV/Excel import
    LEGACY_DATABASE  // Direct database connection (MySQL, PostgreSQL, SQL Server)
}

enum class MigrationStage {
    EXTRACT,
    TRANSFORM,
    VALIDATE,
    LOAD,
    VERIFY,
    UNKNOWN
}

sealed class MigrationResult {
    data class Success(
        val executionId: UUID,
        val duration: Duration,
        val recordsMigrated: Map<EntityType, Int>,
        val warnings: List<ValidationWarning>
    ) : MigrationResult()
    
    data class ValidationFailed(
        val errors: List<ValidationError>,
        val warnings: List<ValidationWarning>,
        val canProceed: Boolean // True if only warnings (user can override)
    ) : MigrationResult()
    
    data class Failed(
        val stage: MigrationStage,
        val reason: String
    ) : MigrationResult()
}
```

---

### 2. Pre-Built Connectors (Source System Integration)

**Connector Interface**:

```kotlin
/**
 * Source System Connector
 * Extracts data from legacy ERP system
 */
interface SourceConnector {
    
    /**
     * Source system identifier
     */
    val sourceSystem: SourceSystem
    
    /**
     * Supported entity types (e.g., Customers, Invoices, Products)
     */
    val supportedEntities: Set<EntityType>
    
    /**
     * Test connection to source system
     */
    suspend fun testConnection(config: ConnectorConfig): ConnectionTestResult
    
    /**
     * Extract data from source system
     */
    suspend fun extract(
        config: ConnectorConfig,
        entities: List<EntityType>,
        filters: ExtractionFilters? = null
    ): ExtractionResult
    
    /**
     * Get record count (for reconciliation)
     */
    suspend fun getRecordCounts(
        config: ConnectorConfig,
        entities: List<EntityType>
    ): Map<EntityType, Int>
}

/**
 * QuickBooks Online Connector
 * Extracts data from QuickBooks Online via OAuth 2.0 API
 */
@Component
class QuickBooksOnlineConnector(
    private val httpClient: HttpClient,
    private val oauthService: OAuthService
) : SourceConnector {
    
    override val sourceSystem = SourceSystem.QUICKBOOKS_ONLINE
    
    override val supportedEntities = setOf(
        EntityType.CUSTOMER,
        EntityType.VENDOR,
        EntityType.INVOICE,
        EntityType.BILL,
        EntityType.PAYMENT,
        EntityType.PRODUCT,
        EntityType.ACCOUNT,
        EntityType.JOURNAL_ENTRY,
        EntityType.EMPLOYEE,
        EntityType.PURCHASE_ORDER
    )
    
    override suspend fun testConnection(config: ConnectorConfig): ConnectionTestResult {
        try {
            val token = oauthService.getAccessToken(config.credentials)
            val response = httpClient.get("${config.baseUrl}/v3/company/${config.companyId}/companyinfo/${config.companyId}") {
                header("Authorization", "Bearer $token")
                header("Accept", "application/json")
            }
            
            return if (response.status == HttpStatusCode.OK) {
                ConnectionTestResult.Success
            } else {
                ConnectionTestResult.Failed("HTTP ${response.status}")
            }
        } catch (e: Exception) {
            return ConnectionTestResult.Failed(e.message ?: "Unknown error")
        }
    }
    
    override suspend fun extract(
        config: ConnectorConfig,
        entities: List<EntityType>,
        filters: ExtractionFilters?
    ): ExtractionResult {
        val extractedData = mutableMapOf<EntityType, List<SourceRecord>>()
        val errors = mutableListOf<ExtractionError>()
        
        for (entity in entities) {
            try {
                val records = when (entity) {
                    EntityType.CUSTOMER -> extractCustomers(config, filters)
                    EntityType.INVOICE -> extractInvoices(config, filters)
                    EntityType.PRODUCT -> extractProducts(config, filters)
                    // ... other entities
                    else -> throw UnsupportedOperationException("Entity $entity not supported")
                }
                extractedData[entity] = records
            } catch (e: Exception) {
                errors.add(ExtractionError(entity, e.message ?: "Unknown error"))
            }
        }
        
        return ExtractionResult(
            status = if (errors.isEmpty()) ExtractionStatus.SUCCESS else ExtractionStatus.PARTIAL,
            data = extractedData,
            errors = errors,
            counts = extractedData.mapValues { it.value.size }
        )
    }
    
    private suspend fun extractCustomers(
        config: ConnectorConfig,
        filters: ExtractionFilters?
    ): List<SourceRecord> {
        val token = oauthService.getAccessToken(config.credentials)
        val customers = mutableListOf<SourceRecord>()
        
        // QuickBooks uses pagination (maxResults=1000 per page)
        var startPosition = 1
        var hasMore = true
        
        while (hasMore) {
            val query = buildQuery(
                entity = "Customer",
                filters = filters,
                startPosition = startPosition,
                maxResults = 1000
            )
            
            val response = httpClient.get("${config.baseUrl}/v3/company/${config.companyId}/query?query=$query") {
                header("Authorization", "Bearer $token")
                header("Accept", "application/json")
            }
            
            val page: QuickBooksCustomerPage = response.body()
            customers.addAll(page.Customer.map { it.toSourceRecord() })
            
            hasMore = page.Customer.size == 1000
            startPosition += 1000
        }
        
        return customers
    }
    
    // Similar methods for extractInvoices, extractProducts, etc.
}

/**
 * SAP ERP Connector
 * Extracts data from SAP ECC/S4HANA via RFC or OData
 */
@Component
class SapErpConnector(
    private val sapClient: SapJCoClient, // JCo for RFC
    private val odataClient: ODataClient  // OData for S/4HANA
) : SourceConnector {
    
    override val sourceSystem = SourceSystem.SAP_ERP
    
    override val supportedEntities = setOf(
        EntityType.CUSTOMER,      // KNA1, KNVV
        EntityType.VENDOR,        // LFA1
        EntityType.MATERIAL,      // MARA, MARC
        EntityType.SALES_ORDER,   // VBAK, VBAP
        EntityType.INVOICE,       // VBRK, VBRP
        EntityType.PURCHASE_ORDER,// EKKO, EKPO
        EntityType.GL_ACCOUNT,    // SKA1
        EntityType.COST_CENTER,   // CSKS
        EntityType.FI_DOCUMENT    // BKPF, BSEG
    )
    
    override suspend fun extract(
        config: ConnectorConfig,
        entities: List<EntityType>,
        filters: ExtractionFilters?
    ): ExtractionResult {
        // Use RFC or OData based on SAP version
        val useOData = config.metadata["version"] == "S/4HANA"
        
        return if (useOData) {
            extractViaOData(config, entities, filters)
        } else {
            extractViaRFC(config, entities, filters)
        }
    }
    
    private suspend fun extractViaRFC(
        config: ConnectorConfig,
        entities: List<EntityType>,
        filters: ExtractionFilters?
    ): ExtractionResult {
        // Call SAP RFC function modules (e.g., BAPI_CUSTOMER_GETLIST)
        // Convert ABAP structures to SourceRecord
        // ...
    }
}

/**
 * Generic CSV/Excel Connector
 * For custom ERPs or manual exports
 */
@Component
class CsvConnector : SourceConnector {
    override val sourceSystem = SourceSystem.CUSTOM_ERP
    
    override val supportedEntities = EntityType.values().toSet()
    
    override suspend fun extract(
        config: ConnectorConfig,
        entities: List<EntityType>,
        filters: ExtractionFilters?
    ): ExtractionResult {
        val extractedData = mutableMapOf<EntityType, List<SourceRecord>>()
        
        for (entity in entities) {
            val filePath = config.metadata["${entity.name.lowercase()}_file"]
                ?: continue
            
            val records = parseCsvFile(
                filePath = filePath,
                entity = entity,
                delimiter = config.metadata["delimiter"] ?: ",",
                hasHeader = config.metadata["has_header"]?.toBoolean() ?: true
            )
            
            extractedData[entity] = records
        }
        
        return ExtractionResult(
            status = ExtractionStatus.SUCCESS,
            data = extractedData,
            counts = extractedData.mapValues { it.value.size }
        )
    }
}
```

**Connector Registry** (10 Pre-Built Connectors):

| Source System | Priority | Complexity | Market Share | Status |
|---------------|----------|------------|--------------|--------|
| **QuickBooks Online** | P0 | Low | 40% SMB | ✅ Included |
| **QuickBooks Desktop** | P0 | Medium | 25% SMB | ✅ Included |
| **Xero** | P0 | Low | 15% SMB | ✅ Included |
| **Sage 50** | P1 | Medium | 10% SMB | ✅ Included |
| **Sage Intacct** | P1 | Medium | 5% Mid-Market | ✅ Included |
| **SAP ERP** (ECC/S/4HANA) | P0 | High | 30% Enterprise | ⏳ Phase 2 |
| **Oracle ERP Cloud** | P1 | High | 15% Enterprise | ⏳ Phase 2 |
| **Dynamics 365 BC** | P1 | Medium | 10% Mid-Market | ⏳ Phase 2 |
| **NetSuite** | P1 | Medium | 8% Mid-Market | ⏳ Phase 2 |
| **CSV/Excel** (Custom) | P0 | Low | 100% (fallback) | ✅ Included |

---

### 3. Data Mapping Engine

**Mapping Templates** (Pre-Built + Customizable):

```kotlin
/**
 * Mapping Engine
 * Transforms source data to ChiroERP schema
 */
@Service
class MappingEngine(
    private val templateRepository: MappingTemplateRepository,
    private val expressionEvaluator: ExpressionEvaluator
) {
    
    fun transform(
        sourceRecords: List<SourceRecord>,
        template: MappingTemplate
    ): List<TargetRecord> {
        return sourceRecords.map { sourceRecord ->
            val targetRecord = TargetRecord(
                entity = template.targetEntity,
                fields = mutableMapOf()
            )
            
            for (fieldMapping in template.fieldMappings) {
                val value = when (fieldMapping.mappingType) {
                    MappingType.DIRECT -> {
                        // Direct field mapping: source.field → target.field
                        sourceRecord.get(fieldMapping.sourceField)
                    }
                    
                    MappingType.EXPRESSION -> {
                        // Expression mapping: e.g., "UPPER(source.name)"
                        expressionEvaluator.evaluate(
                            expression = fieldMapping.expression!!,
                            context = sourceRecord
                        )
                    }
                    
                    MappingType.LOOKUP -> {
                        // Lookup mapping: foreign key resolution
                        // e.g., QuickBooks CustomerId → ChiroERP Customer UUID
                        lookupForeignKey(
                            sourceValue = sourceRecord.get(fieldMapping.sourceField),
                            lookupTable = fieldMapping.lookupTable!!
                        )
                    }
                    
                    MappingType.DEFAULT -> {
                        // Default value: e.g., currency = "USD"
                        fieldMapping.defaultValue!!
                    }
                    
                    MappingType.CALCULATED -> {
                        // Calculated field: e.g., totalAmount = unitPrice * quantity
                        expressionEvaluator.evaluate(
                            expression = fieldMapping.calculationFormula!!,
                            context = sourceRecord
                        )
                    }
                }
                
                targetRecord.fields[fieldMapping.targetField] = value
            }
            
            targetRecord
        }
    }
}

/**
 * Mapping Template
 * Defines how to transform source data to ChiroERP schema
 */
data class MappingTemplate(
    val id: UUID,
    val name: String,
    val sourceSystem: SourceSystem,
    val sourceEntity: String,
    val targetEntity: EntityType,
    val fieldMappings: List<FieldMapping>,
    val businessRules: List<BusinessRule>,
    val isBuiltIn: Boolean = false, // Pre-built templates (read-only)
    val version: Int = 1
)

data class FieldMapping(
    val sourceField: String,
    val targetField: String,
    val mappingType: MappingType,
    val expression: String? = null,        // For EXPRESSION type
    val lookupTable: String? = null,       // For LOOKUP type
    val defaultValue: Any? = null,         // For DEFAULT type
    val calculationFormula: String? = null,// For CALCULATED type
    val isRequired: Boolean = false,
    val transformations: List<Transformation> = emptyList()
)

enum class MappingType {
    DIRECT,      // source.field → target.field
    EXPRESSION,  // Custom expression (e.g., UPPER(name))
    LOOKUP,      // Foreign key resolution
    DEFAULT,     // Fixed default value
    CALCULATED   // Formula-based (e.g., price * quantity)
}

data class Transformation(
    val type: TransformationType,
    val params: Map<String, Any> = emptyMap()
)

enum class TransformationType {
    UPPERCASE,
    LOWERCASE,
    TRIM,
    REGEX_REPLACE,
    DATE_FORMAT,
    NUMBER_FORMAT,
    CURRENCY_CONVERSION,
    CONCATENATE,
    SPLIT,
    SUBSTRING
}
```

**Pre-Built Mapping Templates** (Examples):

```yaml
# QuickBooks Online → ChiroERP Customer Mapping
template:
  name: "QuickBooks Online Customer to ChiroERP Customer"
  sourceSystem: QUICKBOOKS_ONLINE
  sourceEntity: "Customer"
  targetEntity: CUSTOMER
  
  fieldMappings:
    - sourceField: "Id"
      targetField: "externalId"
      mappingType: DIRECT
      
    - sourceField: "DisplayName"
      targetField: "name"
      mappingType: DIRECT
      transformations:
        - type: TRIM
      
    - sourceField: "PrimaryEmailAddr.Address"
      targetField: "email"
      mappingType: DIRECT
      
    - sourceField: "BillAddr"
      targetField: "billingAddress"
      mappingType: EXPRESSION
      expression: |
        {
          "line1": source.BillAddr.Line1,
          "city": source.BillAddr.City,
          "state": source.BillAddr.CountrySubDivisionCode,
          "postalCode": source.BillAddr.PostalCode,
          "country": source.BillAddr.Country
        }
    
    - sourceField: "Balance"
      targetField: "accountBalance"
      mappingType: DIRECT
      
    - sourceField: "CurrencyRef.value"
      targetField: "currency"
      mappingType: LOOKUP
      lookupTable: "currency_mapping"
      defaultValue: "USD"
  
  businessRules:
    - type: VALIDATION
      rule: "email must be valid email format"
      errorLevel: ERROR
      
    - type: VALIDATION
      rule: "accountBalance must be >= 0"
      errorLevel: WARNING
      
    - type: ENRICHMENT
      rule: "Set customerType = BUSINESS if CompanyName is not empty, else INDIVIDUAL"
```

---

### 4. Validation Engine

**Multi-Layer Validation**:

```kotlin
/**
 * Validation Engine
 * Validates data quality before loading into ChiroERP
 */
@Service
class ValidationEngine(
    private val referentialIntegrityChecker: ReferentialIntegrityChecker,
    private val businessRuleEngine: BusinessRuleEngine
) {
    
    fun validate(
        records: List<TargetRecord>,
        rules: List<ValidationRule>
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()
        
        for (record in records) {
            // Layer 1: Schema validation (required fields, data types)
            val schemaErrors = validateSchema(record)
            errors.addAll(schemaErrors)
            
            // Layer 2: Business rules (custom validation logic)
            val ruleViolations = validateBusinessRules(record, rules)
            errors.addAll(ruleViolations.errors)
            warnings.addAll(ruleViolations.warnings)
            
            // Layer 3: Referential integrity (foreign key constraints)
            val integrityErrors = validateReferentialIntegrity(record)
            errors.addAll(integrityErrors)
            
            // Layer 4: Data quality checks (duplicates, orphans)
            val qualityWarnings = checkDataQuality(record)
            warnings.addAll(qualityWarnings)
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            data = if (errors.isEmpty()) records else emptyList()
        )
    }
    
    private fun validateSchema(record: TargetRecord): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val schema = getSchema(record.entity)
        
        for (field in schema.fields) {
            val value = record.fields[field.name]
            
            // Check required fields
            if (field.isRequired && value == null) {
                errors.add(
                    ValidationError(
                        recordId = record.externalId,
                        field = field.name,
                        message = "Required field is missing",
                        severity = ErrorSeverity.ERROR
                    )
                )
                continue
            }
            
            // Check data type
            if (value != null && !field.dataType.isValidValue(value)) {
                errors.add(
                    ValidationError(
                        recordId = record.externalId,
                        field = field.name,
                        message = "Invalid data type: expected ${field.dataType}, got ${value::class.simpleName}",
                        severity = ErrorSeverity.ERROR
                    )
                )
            }
            
            // Check constraints (length, range, format)
            val constraintViolations = field.constraints.mapNotNull { constraint ->
                constraint.validate(value)
            }
            errors.addAll(constraintViolations.map { violation ->
                ValidationError(
                    recordId = record.externalId,
                    field = field.name,
                    message = violation,
                    severity = ErrorSeverity.ERROR
                )
            })
        }
        
        return errors
    }
    
    private fun validateReferentialIntegrity(record: TargetRecord): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val schema = getSchema(record.entity)
        
        for (field in schema.fields.filter { it.isForeignKey }) {
            val foreignKeyValue = record.fields[field.name]
            
            if (foreignKeyValue != null) {
                val exists = referentialIntegrityChecker.exists(
                    table = field.foreignKeyTable!!,
                    id = foreignKeyValue
                )
                
                if (!exists) {
                    errors.add(
                        ValidationError(
                            recordId = record.externalId,
                            field = field.name,
                            message = "Foreign key violation: ${field.foreignKeyTable} record not found",
                            severity = ErrorSeverity.ERROR
                        )
                    )
                }
            }
        }
        
        return errors
    }
    
    private fun checkDataQuality(record: TargetRecord): List<ValidationWarning> {
        val warnings = mutableListOf<ValidationWarning>()
        
        // Check for duplicates (fuzzy matching on key fields)
        val duplicates = findPotentialDuplicates(record)
        if (duplicates.isNotEmpty()) {
            warnings.add(
                ValidationWarning(
                    recordId = record.externalId,
                    message = "Potential duplicate found: ${duplicates.size} similar records exist",
                    suggestion = "Review and merge if appropriate"
                )
            )
        }
        
        // Check for orphaned records (e.g., Invoice without Customer)
        val orphans = findOrphanedReferences(record)
        if (orphans.isNotEmpty()) {
            warnings.add(
                ValidationWarning(
                    recordId = record.externalId,
                    message = "Orphaned reference: ${orphans.joinToString()}",
                    suggestion = "Ensure referenced records are migrated first"
                )
            )
        }
        
        return warnings
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError>,
    val warnings: List<ValidationWarning>,
    val data: List<TargetRecord>
)

data class ValidationError(
    val recordId: String?,
    val field: String?,
    val message: String,
    val severity: ErrorSeverity
)

data class ValidationWarning(
    val recordId: String?,
    val message: String,
    val suggestion: String? = null
)

enum class ErrorSeverity {
    ERROR,   // Blocks migration
    WARNING  // Allows migration with user confirmation
}
```

**Validation Rules** (Examples):

```yaml
# Customer Validation Rules
rules:
  - id: "customer-email-format"
    field: "email"
    type: REGEX
    pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    errorMessage: "Invalid email format"
    severity: ERROR
  
  - id: "customer-name-length"
    field: "name"
    type: LENGTH
    min: 1
    max: 255
    errorMessage: "Customer name must be 1-255 characters"
    severity: ERROR
  
  - id: "customer-balance-non-negative"
    field: "accountBalance"
    type: RANGE
    min: 0
    errorMessage: "Account balance cannot be negative"
    severity: WARNING # Allow with confirmation
  
  - id: "customer-duplicate-check"
    type: CUSTOM
    expression: |
      NOT EXISTS (
        SELECT 1 FROM customers 
        WHERE name = record.name 
        AND email = record.email
      )
    errorMessage: "Duplicate customer detected"
    severity: WARNING

# Invoice Validation Rules
rules:
  - id: "invoice-customer-exists"
    field: "customerId"
    type: FOREIGN_KEY
    referenceTable: "customers"
    errorMessage: "Customer not found"
    severity: ERROR
  
  - id: "invoice-date-not-future"
    field: "invoiceDate"
    type: DATE_RANGE
    max: "TODAY"
    errorMessage: "Invoice date cannot be in the future"
    severity: ERROR
  
  - id: "invoice-total-matches-line-items"
    type: CUSTOM
    expression: |
      record.totalAmount == SUM(record.lineItems.map { it.amount })
    errorMessage: "Invoice total does not match line item sum"
    severity: ERROR
```

---

### 5. Migration Dashboard (Self-Service UI)

**Migration Wizard** (Multi-Step Flow):

```typescript
/**
 * Migration Wizard (React + TypeScript)
 * Self-service migration for SMB/mid-market customers
 */

// Step 1: Source System Selection
interface Step1_SourceSystem {
  sourceSystem: SourceSystem;
  connectionDetails: {
    quickbooksOnline?: {
      oauthToken: string;
      realmId: string;
    };
    csv?: {
      fileUploads: Map<EntityType, File>;
      delimiter: string;
      hasHeader: boolean;
    };
    database?: {
      host: string;
      port: number;
      database: string;
      username: string;
      password: string;
      dbType: 'mysql' | 'postgresql' | 'sqlserver';
    };
  };
}

// Step 2: Entity Selection
interface Step2_EntitySelection {
  entities: {
    entityType: EntityType;
    enabled: boolean;
    recordCount: number; // Preview from source system
  }[];
  estimatedDuration: Duration; // Based on record counts
}

// Step 3: Mapping Configuration
interface Step3_MappingConfig {
  template: MappingTemplate;
  customMappings: FieldMapping[]; // Override default mappings
  previewData: {
    sourceRecords: SourceRecord[];
    transformedRecords: TargetRecord[];
  };
}

// Step 4: Validation & Review
interface Step4_Validation {
  validationResult: ValidationResult;
  errors: ValidationError[];
  warnings: ValidationWarning[];
  canProceed: boolean;
  errorResolution: {
    error: ValidationError;
    resolutionAction: 'skip' | 'fix' | 'abort';
    fixedValue?: any;
  }[];
}

// Step 5: Execution & Monitoring
interface Step5_Execution {
  executionId: UUID;
  status: MigrationStatus;
  progress: {
    stage: MigrationStage;
    percentComplete: number;
    currentEntity: EntityType;
    recordsProcessed: number;
    recordsTotal: number;
    estimatedTimeRemaining: Duration;
  };
  realTimeLog: LogEntry[];
}

// Step 6: Completion & Verification
interface Step6_Completion {
  result: MigrationResult;
  summary: {
    recordsMigrated: Map<EntityType, number>;
    duration: Duration;
    warnings: ValidationWarning[];
  };
  reconciliationReport: {
    entity: EntityType;
    sourceCount: number;
    targetCount: number;
    discrepancy: number;
  }[];
  nextSteps: string[];
}

enum MigrationStatus {
  PENDING,
  EXTRACTING,
  TRANSFORMING,
  VALIDATING,
  LOADING,
  VERIFYING,
  COMPLETED,
  FAILED,
  ROLLED_BACK
}
```

**Dashboard Features**:

```yaml
Migration Dashboard:
  Current Migrations:
    - Status (in progress, completed, failed)
    - Progress bar (real-time updates via WebSocket)
    - ETA (estimated time remaining)
    - Actions (pause, resume, cancel, rollback)
  
  Past Migrations:
    - History log (all migrations)
    - Downloadable reports (PDF/Excel)
    - Re-run capability (for failed migrations)
  
  Templates:
    - Pre-built templates (10+ source systems)
    - Custom templates (create, edit, delete)
    - Template marketplace (community-contributed)
  
  Connectors:
    - Installed connectors (QuickBooks, Xero, SAP, etc.)
    - Connector configuration (OAuth, credentials)
    - Test connection (validation)
  
  Validation Rules:
    - Default rules (built-in)
    - Custom rules (per tenant)
    - Rule testing (dry-run validation)
```

---

### 6. Rollback & Recovery

**Automatic Rollback**:

```kotlin
/**
 * Rollback Manager
 * Reverts failed migrations to pre-migration state
 */
@Service
class RollbackManager(
    private val database: Database,
    private val auditLog: AuditLog
) {
    
    suspend fun rollback(executionId: UUID): RollbackResult {
        logger.warn("Initiating rollback for migration $executionId")
        
        // Step 1: Get all inserted records (from staging table)
        val insertedRecords = database.query(
            """
            SELECT entity_type, record_id 
            FROM migration_staging 
            WHERE execution_id = :executionId
            """,
            mapOf("executionId" to executionId)
        )
        
        // Step 2: Delete inserted records (in reverse dependency order)
        val entityOrder = getEntityDeletionOrder() // e.g., Invoices before Customers
        
        for (entity in entityOrder) {
            val recordIds = insertedRecords
                .filter { it["entity_type"] == entity.name }
                .map { it["record_id"] as UUID }
            
            if (recordIds.isNotEmpty()) {
                database.execute(
                    "DELETE FROM ${entity.tableName} WHERE id = ANY(:ids)",
                    mapOf("ids" to recordIds.toTypedArray())
                )
                
                logger.info("Rolled back ${recordIds.size} ${entity.name} records")
            }
        }
        
        // Step 3: Clean up staging table
        database.execute(
            "DELETE FROM migration_staging WHERE execution_id = :executionId",
            mapOf("executionId" to executionId)
        )
        
        // Step 4: Update audit log
        auditLog.record(
            MigrationEvent.ROLLED_BACK,
            executionId = executionId,
            reason = "Automatic rollback due to failure"
        )
        
        logger.info("Rollback completed for migration $executionId")
        
        return RollbackResult.Success
    }
}

/**
 * Transactional Loading
 * Load data in transactions (all-or-nothing)
 */
suspend fun loadData(
    validatedData: List<TargetRecord>,
    tenantId: UUID,
    executionId: UUID,
    dryRun: Boolean
): LoadResult {
    if (dryRun) {
        // Dry run: Validate only, no data written
        return LoadResult(
            status = LoadStatus.DRY_RUN_SUCCESS,
            counts = validatedData.groupingBy { it.entity }.eachCount()
        )
    }
    
    return database.transaction {
        val counts = mutableMapOf<EntityType, Int>()
        
        for (record in validatedData) {
            // Insert into staging table first (for rollback capability)
            database.execute(
                """
                INSERT INTO migration_staging (execution_id, entity_type, record_id, data)
                VALUES (:executionId, :entityType, :recordId, :data)
                """,
                mapOf(
                    "executionId" to executionId,
                    "entityType" to record.entity.name,
                    "recordId" to record.id,
                    "data" to record.toJson()
                )
            )
            
            // Insert into actual table
            database.insert(
                table = record.entity.tableName,
                data = record.fields + mapOf("tenant_id" to tenantId)
            )
            
            counts[record.entity] = counts.getOrDefault(record.entity, 0) + 1
        }
        
        LoadResult(
            status = LoadStatus.SUCCESS,
            counts = counts
        )
    }
}
```

---

## Professional Services Integration

### API for Migration Consultants

```kotlin
/**
 * Professional Services API
 * For complex migrations requiring human expertise
 */
@RestController
@RequestMapping("/api/v1/migrations")
class MigrationController(
    private val migrationOrchestrator: MigrationOrchestrator,
    private val templateService: MappingTemplateService
) {
    
    /**
     * Create custom mapping template (for complex migrations)
     */
    @PostMapping("/templates")
    suspend fun createTemplate(
        @RequestBody template: MappingTemplate
    ): MappingTemplate {
        return templateService.create(template)
    }
    
    /**
     * Execute migration with custom template
     */
    @PostMapping("/execute")
    suspend fun executeMigration(
        @RequestBody request: MigrationRequest
    ): MigrationResult {
        return migrationOrchestrator.executeMigration(request)
    }
    
    /**
     * Get migration status (long-running operation)
     */
    @GetMapping("/{executionId}/status")
    suspend fun getStatus(
        @PathVariable executionId: UUID
    ): MigrationStatus {
        return migrationOrchestrator.getStatus(executionId)
    }
    
    /**
     * Download migration report (post-migration)
     */
    @GetMapping("/{executionId}/report")
    suspend fun downloadReport(
        @PathVariable executionId: UUID
    ): ResponseEntity<ByteArray> {
        val report = migrationOrchestrator.generateReport(executionId)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .body(report.toPdf())
    }
}
```

### Migration Services Offering

```yaml
ChiroERP Migration Services:
  Tiers:
    Self-Service (Free):
      - Wizard-driven migration
      - Pre-built connectors (QuickBooks, Xero, CSV)
      - Standard validation rules
      - Email support (72-hour SLA)
      - Suitable for: <10K records, standard data model
    
    Assisted Migration ($5K-15K):
      - Dedicated migration consultant
      - Custom field mappings
      - Data cleansing & validation
      - 1-on-1 training session
      - Suitable for: 10K-100K records, moderate complexity
    
    Concierge Migration ($15K-50K):
      - Full-service migration (consultant does everything)
      - Custom connectors (if needed)
      - Data transformation & enrichment
      - Multi-system consolidation
      - Post-migration support (30 days)
      - Suitable for: 100K+ records, complex data model, legacy systems
    
    Enterprise Migration ($50K-150K):
      - Multi-entity/multi-subsidiary
      - Historical data (5+ years)
      - Custom integrations
      - Phased cutover plan
      - 90-day post-migration support
      - Suitable for: Fortune 500, complex ERP landscapes

  SLA Commitments:
    - Self-Service: No SLA (best-effort)
    - Assisted: 4-week completion
    - Concierge: 6-8 week completion
    - Enterprise: 12-16 week completion
```

---

## Implementation Roadmap

### Phase 1: Foundation (Q2 2026)

**Deliverables**:
- [ ] Migration Service (core orchestration)
- [ ] QuickBooks Online connector
- [ ] CSV/Excel connector (generic)
- [ ] Mapping engine (with 2 pre-built templates)
- [ ] Basic validation engine

**Timeline**: 8 weeks (April-May 2026)
**Resources**: 2 backend engineers, 1 integration specialist

### Phase 2: SMB Connectors (Q3 2026)

**Deliverables**:
- [ ] QuickBooks Desktop connector
- [ ] Xero connector
- [ ] Sage 50 connector
- [ ] Migration wizard (self-service UI)
- [ ] 10+ pre-built mapping templates

**Timeline**: 8 weeks (July-August 2026)
**Resources**: 2 backend engineers, 1 frontend engineer, 1 QA engineer

### Phase 3: Validation & Dashboard (Q4 2026)

**Deliverables**:
- [ ] Advanced validation engine (referential integrity, data quality)
- [ ] Migration dashboard (progress tracking, reporting)
- [ ] Rollback system (automatic + manual)
- [ ] Professional Services API

**Timeline**: 6 weeks (October-November 2026)
**Resources**: 2 backend engineers, 1 frontend engineer

### Phase 4: Enterprise Connectors (Q1 2027)

**Deliverables**:
- [ ] SAP ERP connector (RFC + OData)
- [ ] Oracle ERP Cloud connector
- [ ] Dynamics 365 BC connector
- [ ] Legacy database connector (MySQL, PostgreSQL, SQL Server)

**Timeline**: 12 weeks (January-March 2027)
**Resources**: 2 integration specialists, 1 backend engineer

---

## Cost Estimate

### Development Costs

| Item | Cost | Timeline |
|------|------|----------|
| **Backend Engineers** (2 FTE x 9 months) | $360K-450K | Q2 2026 - Q1 2027 |
| **Integration Specialists** (2 FTE x 3 months) | $120K-150K | Q1 2027 |
| **Frontend Engineer** (1 FTE x 3 months) | $60K-75K | Q3 2026 |
| **QA Engineer** (1 FTE x 3 months) | $45K-60K | Q3 2026 |
| **Total Development** | **$585K-735K** | |

### Infrastructure & Tools

| Item | Cost/Year | Notes |
|------|-----------|-------|
| **Connector Licensing** (SAP JCo, Oracle OCI) | $20K-30K | One-time + annual maintenance |
| **Sandbox Environments** (testing) | $12K-18K | Per-connector testing infra |
| **Data Storage** (staging, audit logs) | $6K-12K | S3, database storage |
| **Total Infrastructure** | **$38K-60K** | |

### Total Investment: **$623K-795K** (first year)

---

## Success Metrics

### Technical KPIs
- ✅ **Migration success rate**: >95% (first-time success)
- ✅ **Migration duration**: <4 hours (median, 10K records)
- ✅ **Data accuracy**: >99.9% (post-migration validation)
- ✅ **Rollback capability**: 100% (all failed migrations)

### Business KPIs
- ✅ **Self-service adoption**: >60% (SMB customers)
- ✅ **Professional services revenue**: $500K-1M (first year)
- ✅ **Customer satisfaction**: >4.5/5 (post-migration survey)
- ✅ **Deal velocity**: 4-week reduction (migration no longer bottleneck)

### Operational KPIs
- ✅ **Support ticket reduction**: 70% (migration-related)
- ✅ **Implementation team efficiency**: 3x (from 8 weeks to <3 weeks avg)
- ✅ **Concurrent migrations**: 20+ (vs 3-5 currently)

---

## Alternatives Considered

### 1. Third-Party ETL Tools (Informatica, Talend)
**Rejected**: Generic ETL tools lack ERP-specific business logic, require heavy customization, high licensing cost ($50K-100K/year).

### 2. Manual SQL Scripts (Status Quo)
**Rejected**: Not scalable, error-prone, blocks enterprise sales.

### 3. Partner Integration (Hire Migration Consultants Only)
**Rejected**: High cost per migration ($50K-150K), not repeatable, slow turnaround.

### 4. Customer-Led Migration (Provide Documentation Only)
**Rejected**: 80% failure rate, high support burden, poor customer experience.

---

## Consequences

### Positive ✅
- **Accelerated onboarding**: 4-12 weeks → <1 week (self-service SMB)
- **Unlocks enterprise pipeline**: SAP/Oracle migration path proven
- **New revenue stream**: Professional services ($500K-1M/year)
- **Reduced support burden**: 70% fewer migration-related tickets
- **Competitive advantage**: 5 pre-built connectors (vs competitors' 50+, but growing)
- **Customer confidence**: Automated validation, rollback safety

### Negative ⚠️
- **Development investment**: $623K-795K (first year)
- **Ongoing connector maintenance**: Updates for API changes (QuickBooks, Xero, etc.)
- **Support complexity**: New support category (migration troubleshooting)
- **Liability risk**: Data loss/corruption (mitigated by rollback + insurance)

### Risks 🚨
- **Connector API changes**: QuickBooks/Xero/SAP break connectors
  - Mitigation: API versioning, automated testing, monitoring for breaking changes
- **Data quality issues**: Garbage in, garbage out
  - Mitigation: Comprehensive validation, dry-run mode, pre-migration assessment
- **Regulatory compliance**: GDPR, data sovereignty (cross-border migrations)
  - Mitigation: Data residency controls, customer consent workflows
- **Performance**: Large migrations (1M+ records) timeout
  - Mitigation: Batch processing, resume capability, async execution

---

## Compliance & Security

### Integration with Other ADRs

- **ADR-058**: SOC 2 Compliance (audit trails, access controls)
- **ADR-059**: ISO 27001 ISMS (data protection, operational procedures)
- **ADR-015**: Data Lifecycle Management (data retention, deletion)
- **ADR-064**: GDPR Compliance (data subject rights, cross-border transfers)

### Data Protection During Migration

```yaml
Security Controls:
  Encryption:
    - In-transit: TLS 1.3 (all API calls)
    - At-rest: AES-256 (staging tables, audit logs)
  
  Access Control:
    - Migration service: Dedicated service account (least privilege)
    - Connector credentials: Encrypted (Vault), rotated (90 days)
    - Customer data: Tenant-isolated (no cross-tenant access)
  
  Audit Trail:
    - All migration attempts (success + failure)
    - Data access logs (who accessed what, when)
    - Configuration changes (mapping templates, connectors)
    - Retention: 1 year (compliance requirement)
  
  Data Residency:
    - EU customers: Data stays in EU (GDPR)
    - US customers: Data stays in US
    - No cross-border transfers (without customer consent)
```

---

## References

### Industry Best Practices
- Gartner: "Magic Quadrant for Data Integration Tools"
- ETL Best Practices (Kimball Methodology)
- AWS Database Migration Service (DMS) Architecture

### Related ADRs
- ADR-058: SOC 2 Type II Compliance
- ADR-059: ISO 27001 ISMS
- ADR-015: Data Lifecycle Management
- ADR-064: GDPR Compliance

### Tools & Technologies
- Apache NiFi: https://nifi.apache.org/
- Airbyte: https://airbyte.com/ (open-source ELT)
- QuickBooks API: https://developer.intuit.com/
- SAP JCo: https://support.sap.com/en/product/connectors/jco.html

---

*Document Owner*: Implementation Team Lead  
*Review Frequency*: Quarterly  
*Next Review*: May 2027  
*Status*: **APPROVED - IMPLEMENTATION STARTING Q2 2026**
