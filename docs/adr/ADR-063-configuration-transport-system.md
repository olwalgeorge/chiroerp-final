# ADR-063: Configuration Transport System

**Status**: Accepted (Implementation Starting)  
**Date**: 2026-02-06  
**Deciders**: Architecture Team, Platform Team, DevOps Team  
**Priority**: P0 (Critical - Operational Excellence)  
**Tier**: Platform Core  
**Tags**: configuration, transport, deployment, devops, lifecycle, promotion

---

## Context

**Problem**: ChiroERP lacks a **systematic way to transport configuration** between environments (Development → Test → Production) and between tenants. This forces **manual, error-prone configuration recreation** in each environment, leading to:

- **Configuration drift**: Prod differs from Test (untested configurations go live)
- **Manual effort**: 4-8 hours per release to manually recreate configs in Prod
- **Human error**: 20% of production incidents caused by config mistakes
- **No version control**: Configuration changes not tracked in Git
- **Tenant customization bottleneck**: Cannot copy config from one tenant to another
- **Rollback complexity**: No easy way to revert configuration changes

**Current State**:
- **Manual configuration** in UI (no export/import)
- **No dependency tracking** (e.g., workflow references custom field not yet in Prod)
- **No conflict detection** (overwrite existing config silently)
- **No audit trail** (who changed what, when)
- **Database-only storage** (not version controlled)

**Configuration Types Affected**:
- **Workflows** (approval chains, state machines)
- **Business Rules** (Drools rules, validation logic)
- **Custom Fields** (per domain/entity)
- **Report Templates** (Jasper, custom reports)
- **Dashboard Layouts** (user/role-specific dashboards)
- **Integration Mappings** (EDI, API transforms)
- **Tax Configurations** (rates, exemptions, nexus)
- **Localization Settings** (date formats, currencies)

**Market Reality**:
- **SAP**: Transport Management System (TMS) with transport requests, change tracking
- **Oracle**: Configuration Migration Toolkit, snapshots
- **Dynamics 365**: Solutions (managed/unmanaged), ALM Toolkit
- **Salesforce**: Change Sets, Metadata API, DevOps Center
- **ChiroERP**: ❌ No configuration transport system

**Customer Quote** (IT Director, Enterprise):
> "We have 3 environments (Dev, QA, Prod) and 50 custom workflows. Every release, we spend 2 days manually recreating workflows in Prod. This is not sustainable and causes production issues."

---

## Decision

Build a **Configuration Transport System** providing:

1. **Configuration Export** (selective or full export to JSON/YAML)
2. **Dependency Resolution** (auto-detect dependencies, export as package)
3. **Transport Packages** (versioned, Git-compatible, immutable)
4. **Configuration Import** (with conflict detection, merge strategies)
5. **Automated Promotion** (Dev → Test → Prod pipeline)
6. **Rollback Capability** (revert to previous configuration snapshot)
7. **Audit Trail** (all configuration changes tracked)
8. **Tenant Cloning** (copy config from one tenant to another)

**Target**: Q3-Q4 2026 (production-ready)

---

## Architecture

### 1. Configuration Transport Service

**New Platform Service**: `platform-operations/config-transport-service`

```kotlin
/**
 * Configuration Transport Service
 * Exports, imports, and manages configuration across environments
 */
@Service
class ConfigurationTransportService(
    private val configRegistry: ConfigurationRegistry,
    private val dependencyResolver: DependencyResolver,
    private val conflictDetector: ConflictDetector,
    private val versionControl: ConfigVersionControl,
    private val auditLog: AuditLog
) {
    
    /**
     * Export configuration as transport package
     * 
     * Workflow:
     * 1. Select configuration objects to export
     * 2. Resolve dependencies (auto-include referenced objects)
     * 3. Serialize to JSON/YAML
     * 4. Package with metadata (version, author, timestamp)
     * 5. Sign package (integrity verification)
     */
    suspend fun exportConfiguration(request: ExportRequest): TransportPackage {
        logger.info("Exporting configuration: ${request.configurationObjects.size} objects")
        
        // Step 1: Fetch configuration objects
        val objects = request.configurationObjects.map { ref ->
            configRegistry.get(ref.type, ref.id, request.tenantId)
                ?: throw NotFoundException("Configuration object not found: ${ref.id}")
        }
        
        // Step 2: Resolve dependencies
        val dependencies = if (request.includeDependencies) {
            dependencyResolver.resolveDependencies(objects, request.tenantId)
        } else {
            emptyList()
        }
        
        val allObjects = objects + dependencies
        logger.info("Resolved ${dependencies.size} dependencies, total objects: ${allObjects.size}")
        
        // Step 3: Serialize to transport format
        val transportObjects = allObjects.map { obj ->
            TransportObject(
                id = obj.id,
                type = obj.type,
                name = obj.name,
                data = obj.toJson(),
                version = obj.version,
                dependencies = dependencyResolver.getDependencies(obj)
            )
        }
        
        // Step 4: Create package
        val packageMetadata = PackageMetadata(
            id = UUID.randomUUID(),
            name = request.packageName,
            description = request.description,
            version = request.version,
            author = request.author,
            createdAt = Instant.now(),
            sourceTenant = request.tenantId,
            sourceEnvironment = getEnvironment(),
            objectCount = transportObjects.size
        )
        
        val pkg = TransportPackage(
            metadata = packageMetadata,
            objects = transportObjects,
            signature = signPackage(packageMetadata, transportObjects)
        )
        
        // Step 5: Store package (version control)
        versionControl.store(pkg)
        
        // Step 6: Audit log
        auditLog.record(
            event = ConfigEvent.PACKAGE_EXPORTED,
            tenantId = request.tenantId,
            userId = request.author,
            details = mapOf(
                "packageId" to pkg.metadata.id,
                "objectCount" to transportObjects.size
            )
        )
        
        return pkg
    }
    
    /**
     * Import configuration from transport package
     * 
     * Workflow:
     * 1. Validate package (signature, compatibility)
     * 2. Detect conflicts with existing configuration
     * 3. Preview changes (dry-run mode)
     * 4. Apply configuration (with merge strategy)
     * 5. Update dependencies (references, IDs)
     * 6. Commit transaction (all-or-nothing)
     */
    suspend fun importConfiguration(request: ImportRequest): ImportResult {
        logger.info("Importing package ${request.packageId} to tenant ${request.targetTenant}")
        
        // Step 1: Load and validate package
        val pkg = versionControl.load(request.packageId)
            ?: throw NotFoundException("Package not found: ${request.packageId}")
        
        if (!verifySignature(pkg)) {
            throw SecurityException("Package signature verification failed")
        }
        
        // Step 2: Detect conflicts
        val conflicts = conflictDetector.detectConflicts(
            targetTenant = request.targetTenant,
            transportObjects = pkg.objects
        )
        
        if (conflicts.isNotEmpty() && request.conflictStrategy == ConflictStrategy.FAIL) {
            return ImportResult.ConflictsDetected(
                conflicts = conflicts,
                message = "Import aborted: ${conflicts.size} conflicts detected"
            )
        }
        
        // Step 3: Dry-run mode (preview changes)
        if (request.dryRun) {
            return ImportResult.DryRunSuccess(
                changes = previewChanges(request.targetTenant, pkg.objects, conflicts, request.conflictStrategy),
                conflicts = conflicts
            )
        }
        
        // Step 4: Import objects (transactional)
        return database.transaction {
            val results = mutableListOf<ObjectImportResult>()
            
            for (obj in pkg.objects) {
                val result = importObject(
                    targetTenant = request.targetTenant,
                    transportObject = obj,
                    conflicts = conflicts.filter { it.objectId == obj.id },
                    conflictStrategy = request.conflictStrategy,
                    idMappingTable = mutableMapOf() // Track ID changes for dependency updates
                )
                results.add(result)
            }
            
            // Step 5: Audit log
            auditLog.record(
                event = ConfigEvent.PACKAGE_IMPORTED,
                tenantId = request.targetTenant,
                userId = request.importedBy,
                details = mapOf(
                    "packageId" to pkg.metadata.id,
                    "objectsImported" to results.count { it.status == ImportStatus.SUCCESS },
                    "objectsSkipped" to results.count { it.status == ImportStatus.SKIPPED },
                    "objectsFailed" to results.count { it.status == ImportStatus.FAILED }
                )
            )
            
            ImportResult.Success(
                objectResults = results,
                summary = ImportSummary(
                    totalObjects = pkg.objects.size,
                    imported = results.count { it.status == ImportStatus.SUCCESS },
                    skipped = results.count { it.status == ImportStatus.SKIPPED },
                    failed = results.count { it.status == ImportStatus.FAILED }
                )
            )
        }
    }
    
    /**
     * Import single object with conflict resolution
     */
    private suspend fun importObject(
        targetTenant: UUID,
        transportObject: TransportObject,
        conflicts: List<ConfigConflict>,
        conflictStrategy: ConflictStrategy,
        idMappingTable: MutableMap<UUID, UUID>
    ): ObjectImportResult {
        try {
            // Check if object exists
            val existingObject = configRegistry.get(
                type = transportObject.type,
                id = transportObject.id,
                tenantId = targetTenant
            )
            
            if (existingObject != null && conflicts.isNotEmpty()) {
                // Conflict resolution
                return when (conflictStrategy) {
                    ConflictStrategy.OVERWRITE -> {
                        // Overwrite existing object
                        configRegistry.update(
                            tenantId = targetTenant,
                            type = transportObject.type,
                            id = transportObject.id,
                            data = transportObject.data
                        )
                        ObjectImportResult(
                            objectId = transportObject.id,
                            objectType = transportObject.type,
                            status = ImportStatus.SUCCESS,
                            action = ImportAction.UPDATED
                        )
                    }
                    
                    ConflictStrategy.SKIP -> {
                        // Skip conflicting object
                        ObjectImportResult(
                            objectId = transportObject.id,
                            objectType = transportObject.type,
                            status = ImportStatus.SKIPPED,
                            message = "Skipped due to conflict"
                        )
                    }
                    
                    ConflictStrategy.CREATE_NEW -> {
                        // Create new object with new ID
                        val newId = UUID.randomUUID()
                        configRegistry.create(
                            tenantId = targetTenant,
                            type = transportObject.type,
                            id = newId,
                            data = transportObject.data
                        )
                        idMappingTable[transportObject.id] = newId // Track ID change
                        ObjectImportResult(
                            objectId = newId,
                            objectType = transportObject.type,
                            status = ImportStatus.SUCCESS,
                            action = ImportAction.CREATED,
                            message = "Created with new ID (original: ${transportObject.id})"
                        )
                    }
                    
                    ConflictStrategy.MERGE -> {
                        // Merge changes (3-way merge if base version available)
                        val merged = mergeConfigurations(existingObject, transportObject)
                        configRegistry.update(
                            tenantId = targetTenant,
                            type = transportObject.type,
                            id = transportObject.id,
                            data = merged
                        )
                        ObjectImportResult(
                            objectId = transportObject.id,
                            objectType = transportObject.type,
                            status = ImportStatus.SUCCESS,
                            action = ImportAction.MERGED
                        )
                    }
                    
                    ConflictStrategy.FAIL -> {
                        // This shouldn't happen (conflicts detected earlier)
                        ObjectImportResult(
                            objectId = transportObject.id,
                            objectType = transportObject.type,
                            status = ImportStatus.FAILED,
                            message = "Conflict detected"
                        )
                    }
                }
            } else {
                // No conflict, create new object
                configRegistry.create(
                    tenantId = targetTenant,
                    type = transportObject.type,
                    id = transportObject.id,
                    data = transportObject.data
                )
                ObjectImportResult(
                    objectId = transportObject.id,
                    objectType = transportObject.type,
                    status = ImportStatus.SUCCESS,
                    action = ImportAction.CREATED
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to import object ${transportObject.id}", e)
            ObjectImportResult(
                objectId = transportObject.id,
                objectType = transportObject.type,
                status = ImportStatus.FAILED,
                message = e.message ?: "Unknown error"
            )
        }
    }
}

data class ExportRequest(
    val tenantId: UUID,
    val configurationObjects: List<ConfigurationReference>,
    val includeDependencies: Boolean = true,
    val packageName: String,
    val description: String,
    val version: String,
    val author: UUID
)

data class ConfigurationReference(
    val type: ConfigurationType,
    val id: UUID
)

enum class ConfigurationType {
    WORKFLOW,
    BUSINESS_RULE,
    CUSTOM_FIELD,
    REPORT_TEMPLATE,
    DASHBOARD_LAYOUT,
    INTEGRATION_MAPPING,
    TAX_CONFIGURATION,
    LOCALIZATION_SETTING,
    ROLE_DEFINITION,
    EMAIL_TEMPLATE,
    NOTIFICATION_RULE
}

data class TransportPackage(
    val metadata: PackageMetadata,
    val objects: List<TransportObject>,
    val signature: String
)

data class PackageMetadata(
    val id: UUID,
    val name: String,
    val description: String,
    val version: String,
    val author: UUID,
    val createdAt: Instant,
    val sourceTenant: UUID,
    val sourceEnvironment: String, // e.g., "development", "test", "production"
    val objectCount: Int
)

data class TransportObject(
    val id: UUID,
    val type: ConfigurationType,
    val name: String,
    val data: String, // JSON serialized
    val version: Int,
    val dependencies: List<UUID>
)

data class ImportRequest(
    val packageId: UUID,
    val targetTenant: UUID,
    val conflictStrategy: ConflictStrategy,
    val dryRun: Boolean = true, // Default: preview mode
    val importedBy: UUID
)

enum class ConflictStrategy {
    OVERWRITE,   // Replace existing object
    SKIP,        // Keep existing object
    CREATE_NEW,  // Create duplicate with new ID
    MERGE,       // Intelligent merge (if possible)
    FAIL         // Abort on conflict
}

sealed class ImportResult {
    data class Success(
        val objectResults: List<ObjectImportResult>,
        val summary: ImportSummary
    ) : ImportResult()
    
    data class ConflictsDetected(
        val conflicts: List<ConfigConflict>,
        val message: String
    ) : ImportResult()
    
    data class DryRunSuccess(
        val changes: List<PlannedChange>,
        val conflicts: List<ConfigConflict>
    ) : ImportResult()
}

data class ObjectImportResult(
    val objectId: UUID,
    val objectType: ConfigurationType,
    val status: ImportStatus,
    val action: ImportAction? = null,
    val message: String? = null
)

enum class ImportStatus {
    SUCCESS,
    SKIPPED,
    FAILED
}

enum class ImportAction {
    CREATED,
    UPDATED,
    MERGED,
    DELETED
}

data class ImportSummary(
    val totalObjects: Int,
    val imported: Int,
    val skipped: Int,
    val failed: Int
)
```

---

### 2. Dependency Resolution

**Automatic Dependency Detection**:

```kotlin
/**
 * Dependency Resolver
 * Automatically detects and resolves configuration dependencies
 */
@Service
class DependencyResolver(
    private val configRegistry: ConfigurationRegistry
) {
    
    /**
     * Resolve all dependencies for given configuration objects
     * Returns additional objects that must be included in transport package
     */
    suspend fun resolveDependencies(
        objects: List<ConfigurationObject>,
        tenantId: UUID
    ): List<ConfigurationObject> {
        val dependencies = mutableSetOf<ConfigurationObject>()
        val processed = mutableSetOf<UUID>()
        
        // Breadth-first search for dependencies
        val queue = LinkedList(objects)
        
        while (queue.isNotEmpty()) {
            val obj = queue.poll()
            
            if (processed.contains(obj.id)) {
                continue
            }
            processed.add(obj.id)
            
            // Get direct dependencies for this object
            val directDeps = getDependencies(obj)
            
            for (depId in directDeps) {
                if (!processed.contains(depId)) {
                    val depObj = configRegistry.get(
                        type = inferType(depId), // Infer type from ID or metadata
                        id = depId,
                        tenantId = tenantId
                    )
                    
                    if (depObj != null && !objects.contains(depObj)) {
                        dependencies.add(depObj)
                        queue.add(depObj)
                    }
                }
            }
        }
        
        return dependencies.toList()
    }
    
    /**
     * Get dependencies for single configuration object
     */
    fun getDependencies(obj: ConfigurationObject): List<UUID> {
        return when (obj.type) {
            ConfigurationType.WORKFLOW -> extractWorkflowDependencies(obj)
            ConfigurationType.BUSINESS_RULE -> extractBusinessRuleDependencies(obj)
            ConfigurationType.REPORT_TEMPLATE -> extractReportDependencies(obj)
            ConfigurationType.DASHBOARD_LAYOUT -> extractDashboardDependencies(obj)
            ConfigurationType.INTEGRATION_MAPPING -> extractMappingDependencies(obj)
            else -> emptyList()
        }
    }
    
    /**
     * Extract dependencies from workflow definition
     */
    private fun extractWorkflowDependencies(workflow: ConfigurationObject): List<UUID> {
        val dependencies = mutableListOf<UUID>()
        val workflowData = parseJson<WorkflowDefinition>(workflow.data)
        
        // 1. Custom fields referenced in workflow conditions
        workflowData.transitions.forEach { transition ->
            transition.conditions.forEach { condition ->
                if (condition.type == ConditionType.FIELD_VALUE) {
                    val fieldId = condition.fieldId
                    if (fieldId != null) {
                        dependencies.add(fieldId)
                    }
                }
            }
        }
        
        // 2. Business rules executed in workflow actions
        workflowData.states.forEach { state ->
            state.onEntry.forEach { action ->
                if (action.type == ActionType.EXECUTE_RULE) {
                    val ruleId = action.ruleId
                    if (ruleId != null) {
                        dependencies.add(ruleId)
                    }
                }
            }
        }
        
        // 3. Email templates used in notifications
        workflowData.states.forEach { state ->
            state.onEntry.forEach { action ->
                if (action.type == ActionType.SEND_EMAIL) {
                    val templateId = action.emailTemplateId
                    if (templateId != null) {
                        dependencies.add(templateId)
                    }
                }
            }
        }
        
        return dependencies
    }
    
    /**
     * Extract dependencies from business rule
     */
    private fun extractBusinessRuleDependencies(rule: ConfigurationObject): List<UUID> {
        val dependencies = mutableListOf<UUID>()
        val ruleData = parseJson<BusinessRuleDefinition>(rule.data)
        
        // Custom fields referenced in rule conditions
        ruleData.when_conditions.forEach { condition ->
            val fieldId = extractFieldReference(condition)
            if (fieldId != null) {
                dependencies.add(fieldId)
            }
        }
        
        return dependencies
    }
}

/**
 * Dependency Graph Visualization
 */
data class DependencyGraph(
    val nodes: List<DependencyNode>,
    val edges: List<DependencyEdge>
)

data class DependencyNode(
    val id: UUID,
    val type: ConfigurationType,
    val name: String,
    val isRoot: Boolean // True if user-selected, false if dependency
)

data class DependencyEdge(
    val from: UUID,
    val to: UUID,
    val dependencyType: String // e.g., "uses_custom_field", "executes_rule"
)
```

**Dependency Example**:

```
Workflow: "Purchase Order Approval"
├── depends on Custom Field: "PO Amount"
├── depends on Custom Field: "Department"
├── depends on Business Rule: "PO Approval Threshold"
│   └── depends on Custom Field: "Manager Approval Limit"
├── depends on Email Template: "PO Approved Notification"
└── depends on Role: "Purchasing Manager"
```

---

### 3. Conflict Detection

**Intelligent Conflict Detection**:

```kotlin
/**
 * Conflict Detector
 * Detects conflicts when importing configuration
 */
@Service
class ConflictDetector(
    private val configRegistry: ConfigurationRegistry
) {
    
    /**
     * Detect conflicts between transport package and target environment
     */
    suspend fun detectConflicts(
        targetTenant: UUID,
        transportObjects: List<TransportObject>
    ): List<ConfigConflict> {
        val conflicts = mutableListOf<ConfigConflict>()
        
        for (obj in transportObjects) {
            val existingObject = configRegistry.get(
                type = obj.type,
                id = obj.id,
                tenantId = targetTenant
            )
            
            if (existingObject != null) {
                // Object exists, check for conflicts
                val conflict = analyzeConflict(obj, existingObject)
                if (conflict != null) {
                    conflicts.add(conflict)
                }
            }
        }
        
        return conflicts
    }
    
    /**
     * Analyze conflict between transport object and existing object
     */
    private fun analyzeConflict(
        transportObject: TransportObject,
        existingObject: ConfigurationObject
    ): ConfigConflict? {
        // 1. Version conflict (existing is newer)
        if (existingObject.version > transportObject.version) {
            return ConfigConflict(
                objectId = transportObject.id,
                objectType = transportObject.type,
                objectName = transportObject.name,
                conflictType = ConflictType.VERSION_MISMATCH,
                severity = ConflictSeverity.WARNING,
                message = "Target has newer version (${existingObject.version}) than package (${transportObject.version})",
                recommendation = "Review changes before overwriting"
            )
        }
        
        // 2. Content conflict (both modified since common ancestor)
        if (hasDivergentChanges(transportObject, existingObject)) {
            return ConfigConflict(
                objectId = transportObject.id,
                objectType = transportObject.type,
                objectName = transportObject.name,
                conflictType = ConflictType.CONTENT_DIVERGENCE,
                severity = ConflictSeverity.ERROR,
                message = "Object modified in both source and target",
                recommendation = "Manual merge required"
            )
        }
        
        // 3. Dependency conflict (missing dependencies in target)
        val missingDeps = findMissingDependencies(transportObject, existingObject)
        if (missingDeps.isNotEmpty()) {
            return ConfigConflict(
                objectId = transportObject.id,
                objectType = transportObject.type,
                objectName = transportObject.name,
                conflictType = ConflictType.MISSING_DEPENDENCY,
                severity = ConflictSeverity.ERROR,
                message = "Missing dependencies: ${missingDeps.joinToString()}",
                recommendation = "Include dependencies in package"
            )
        }
        
        // 4. Name conflict (same name, different ID)
        if (transportObject.name == existingObject.name && transportObject.id != existingObject.id) {
            return ConfigConflict(
                objectId = transportObject.id,
                objectType = transportObject.type,
                objectName = transportObject.name,
                conflictType = ConflictType.NAME_COLLISION,
                severity = ConflictSeverity.WARNING,
                message = "Object with same name but different ID exists",
                recommendation = "Rename or merge"
            )
        }
        
        return null
    }
}

data class ConfigConflict(
    val objectId: UUID,
    val objectType: ConfigurationType,
    val objectName: String,
    val conflictType: ConflictType,
    val severity: ConflictSeverity,
    val message: String,
    val recommendation: String
)

enum class ConflictType {
    VERSION_MISMATCH,      // Target has different version
    CONTENT_DIVERGENCE,    // Both modified since common ancestor
    MISSING_DEPENDENCY,    // Required dependencies not in target
    NAME_COLLISION,        // Same name, different ID
    INCOMPATIBLE_VERSION   // Package requires newer platform version
}

enum class ConflictSeverity {
    INFO,     // FYI only
    WARNING,  // Can proceed with caution
    ERROR     // Blocks import (unless OVERWRITE strategy)
}
```

---

### 4. Version Control Integration

**Git Integration** (store packages as code):

```kotlin
/**
 * Configuration Version Control
 * Stores transport packages in Git repository
 */
@Service
class ConfigVersionControl(
    private val gitClient: GitClient,
    private val s3Storage: S3StorageService
) {
    
    private val repositoryPath = "config-packages"
    
    /**
     * Store transport package in version control
     */
    suspend fun store(pkg: TransportPackage): String {
        // 1. Serialize package to YAML (human-readable, Git-friendly)
        val yamlContent = pkg.toYaml()
        
        // 2. Store in Git repository
        val fileName = "${pkg.metadata.name}_${pkg.metadata.version}.yaml"
        val filePath = "$repositoryPath/${pkg.metadata.sourceTenant}/$fileName"
        
        gitClient.commitFile(
            path = filePath,
            content = yamlContent,
            commitMessage = "Export: ${pkg.metadata.name} v${pkg.metadata.version}\n\n${pkg.metadata.description}",
            author = pkg.metadata.author.toString()
        )
        
        // 3. Also store in S3 (for fast retrieval)
        s3Storage.upload(
            bucket = "chiroerp-config-packages",
            key = "${pkg.metadata.id}.yaml",
            content = yamlContent.toByteArray()
        )
        
        logger.info("Stored package ${pkg.metadata.id} in version control")
        
        return filePath
    }
    
    /**
     * Load transport package from version control
     */
    suspend fun load(packageId: UUID): TransportPackage? {
        // Try S3 first (fast)
        val s3Content = s3Storage.download(
            bucket = "chiroerp-config-packages",
            key = "$packageId.yaml"
        )
        
        if (s3Content != null) {
            return parseYaml<TransportPackage>(s3Content.toString(Charsets.UTF_8))
        }
        
        // Fallback to Git (slower)
        // Search Git history for package
        return null // TODO: Implement Git search
    }
    
    /**
     * List all packages for tenant
     */
    suspend fun listPackages(tenantId: UUID): List<PackageMetadata> {
        val packages = mutableListOf<PackageMetadata>()
        
        // List files in Git repo
        val files = gitClient.listFiles("$repositoryPath/$tenantId")
        
        for (file in files) {
            val content = gitClient.readFile(file)
            val pkg = parseYaml<TransportPackage>(content)
            packages.add(pkg.metadata)
        }
        
        return packages.sortedByDescending { it.createdAt }
    }
}
```

**Package Format** (YAML Example):

```yaml
metadata:
  id: "550e8400-e29b-41d4-a716-446655440000"
  name: "Purchase Order Approval Workflow"
  description: "3-tier approval workflow for PO amounts"
  version: "1.2.0"
  author: "john.doe@company.com"
  created_at: "2026-02-06T10:30:00Z"
  source_tenant: "acme-corp"
  source_environment: "development"
  object_count: 5

objects:
  - id: "660e8400-e29b-41d4-a716-446655440001"
    type: "WORKFLOW"
    name: "PO Approval"
    version: 3
    dependencies:
      - "770e8400-e29b-41d4-a716-446655440002" # Custom Field: PO Amount
      - "880e8400-e29b-41d4-a716-446655440003" # Business Rule: Approval Threshold
    data:
      states:
        - id: "pending"
          name: "Pending Approval"
          on_entry:
            - type: "SEND_EMAIL"
              email_template_id: "990e8400-e29b-41d4-a716-446655440004"
      transitions:
        - from: "pending"
          to: "approved"
          conditions:
            - type: "FIELD_VALUE"
              field_id: "770e8400-e29b-41d4-a716-446655440002"
              operator: "LESS_THAN"
              value: 10000

  - id: "770e8400-e29b-41d4-a716-446655440002"
    type: "CUSTOM_FIELD"
    name: "PO Amount"
    version: 1
    dependencies: []
    data:
      entity: "PurchaseOrder"
      field_name: "po_amount"
      data_type: "DECIMAL"
      is_required: true

signature: "SHA256:abc123def456..."
```

---

### 5. Automated Promotion Pipeline

**CI/CD Integration** (GitOps workflow):

```yaml
# .github/workflows/config-promotion.yml
name: Configuration Promotion

on:
  push:
    branches:
      - main
    paths:
      - 'config-packages/**'

jobs:
  promote-to-test:
    runs-on: ubuntu-latest
    environment: test
    steps:
      - name: Checkout
        uses: actions/checkout@v3
      
      - name: Detect Changed Packages
        id: changes
        run: |
          CHANGED=$(git diff --name-only HEAD~1 HEAD | grep 'config-packages/')
          echo "packages=$CHANGED" >> $GITHUB_OUTPUT
      
      - name: Import to Test Environment
        run: |
          for package in ${{ steps.changes.outputs.packages }}; do
            curl -X POST https://test.chiroerp.com/api/v1/config-transport/import \
              -H "Authorization: Bearer ${{ secrets.TEST_API_TOKEN }}" \
              -H "Content-Type: application/json" \
              -d @$package \
              -d '{"conflict_strategy": "OVERWRITE", "dry_run": false}'
          done
      
      - name: Run Integration Tests
        run: |
          npm test -- --environment=test

  promote-to-production:
    needs: promote-to-test
    runs-on: ubuntu-latest
    environment: production
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Manual Approval
        uses: trstringer/manual-approval@v1
        with:
          approvers: platform-team
          minimum-approvals: 2
      
      - name: Import to Production
        run: |
          for package in ${{ steps.changes.outputs.packages }}; do
            curl -X POST https://prod.chiroerp.com/api/v1/config-transport/import \
              -H "Authorization: Bearer ${{ secrets.PROD_API_TOKEN }}" \
              -H "Content-Type: application/json" \
              -d @$package \
              -d '{"conflict_strategy": "FAIL", "dry_run": false}'
          done
      
      - name: Create Snapshot
        run: |
          curl -X POST https://prod.chiroerp.com/api/v1/config-transport/snapshot \
            -H "Authorization: Bearer ${{ secrets.PROD_API_TOKEN }}" \
            -d '{"name": "Pre-promotion snapshot", "tenant_id": "production"}'
```

---

### 6. Configuration Snapshot & Rollback

**Point-in-Time Recovery**:

```kotlin
/**
 * Configuration Snapshot Service
 * Create snapshots for rollback capability
 */
@Service
class ConfigurationSnapshotService(
    private val configRegistry: ConfigurationRegistry,
    private val snapshotRepository: SnapshotRepository
) {
    
    /**
     * Create snapshot of all configuration for tenant
     */
    suspend fun createSnapshot(
        tenantId: UUID,
        name: String,
        description: String,
        createdBy: UUID
    ): ConfigurationSnapshot {
        logger.info("Creating configuration snapshot for tenant $tenantId")
        
        // Export all configuration objects
        val allObjects = configRegistry.getAllForTenant(tenantId)
        
        val snapshot = ConfigurationSnapshot(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            name = name,
            description = description,
            createdAt = Instant.now(),
            createdBy = createdBy,
            objectCount = allObjects.size,
            data = allObjects.toJson() // Serialize to JSON
        )
        
        snapshotRepository.save(snapshot)
        
        logger.info("Created snapshot ${snapshot.id} with ${allObjects.size} objects")
        
        return snapshot
    }
    
    /**
     * Restore configuration from snapshot
     */
    suspend fun restoreSnapshot(
        snapshotId: UUID,
        restoredBy: UUID
    ): RestoreResult {
        val snapshot = snapshotRepository.findById(snapshotId)
            ?: throw NotFoundException("Snapshot not found: $snapshotId")
        
        logger.warn("Restoring configuration from snapshot ${snapshot.id}")
        
        // 1. Create backup of current state (before restore)
        val preRestoreBackup = createSnapshot(
            tenantId = snapshot.tenantId,
            name = "Auto-backup before restore",
            description = "Automatic backup created before restoring snapshot ${snapshot.name}",
            createdBy = restoredBy
        )
        
        // 2. Restore objects from snapshot (transactional)
        return database.transaction {
            val objects = parseJson<List<ConfigurationObject>>(snapshot.data)
            
            for (obj in objects) {
                configRegistry.createOrUpdate(
                    tenantId = snapshot.tenantId,
                    type = obj.type,
                    id = obj.id,
                    data = obj.data
                )
            }
            
            RestoreResult.Success(
                snapshotId = snapshot.id,
                objectsRestored = objects.size,
                backupId = preRestoreBackup.id
            )
        }
    }
    
    /**
     * Compare two snapshots (diff)
     */
    suspend fun compareSnapshots(
        snapshot1Id: UUID,
        snapshot2Id: UUID
    ): SnapshotDiff {
        val snapshot1 = snapshotRepository.findById(snapshot1Id)
            ?: throw NotFoundException("Snapshot 1 not found")
        val snapshot2 = snapshotRepository.findById(snapshot2Id)
            ?: throw NotFoundException("Snapshot 2 not found")
        
        val objects1 = parseJson<List<ConfigurationObject>>(snapshot1.data).associateBy { it.id }
        val objects2 = parseJson<List<ConfigurationObject>>(snapshot2.data).associateBy { it.id }
        
        val added = objects2.keys - objects1.keys
        val removed = objects1.keys - objects2.keys
        val modified = objects1.keys.intersect(objects2.keys).filter { id ->
            objects1[id]!!.version != objects2[id]!!.version
        }
        
        return SnapshotDiff(
            snapshot1 = snapshot1.metadata(),
            snapshot2 = snapshot2.metadata(),
            added = added.map { objects2[it]!! },
            removed = removed.map { objects1[it]!! },
            modified = modified.map { objects2[it]!! }
        )
    }
}

data class ConfigurationSnapshot(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val description: String,
    val createdAt: Instant,
    val createdBy: UUID,
    val objectCount: Int,
    val data: String // JSON serialized
)

sealed class RestoreResult {
    data class Success(
        val snapshotId: UUID,
        val objectsRestored: Int,
        val backupId: UUID // Pre-restore backup
    ) : RestoreResult()
    
    data class Failed(
        val reason: String
    ) : RestoreResult()
}

data class SnapshotDiff(
    val snapshot1: SnapshotMetadata,
    val snapshot2: SnapshotMetadata,
    val added: List<ConfigurationObject>,
    val removed: List<ConfigurationObject>,
    val modified: List<ConfigurationObject>
)
```

---

## Configuration Transport UI

**Self-Service Portal**:

```yaml
Configuration Transport Dashboard:
  
  Export Configuration:
    Step 1 - Select Objects:
      - [ ] Workflow: PO Approval (v3)
      - [ ] Workflow: Invoice Approval (v2)
      - [x] Custom Field: PO Amount
      - [x] Business Rule: Approval Threshold
      - Auto-detect dependencies: [✓]
    
    Step 2 - Preview Package:
      Package Name: "PO Approval v1.2"
      Description: "3-tier approval workflow"
      Objects: 5 (2 selected + 3 dependencies)
      Dependency Graph: [View Visualization]
    
    Step 3 - Export:
      Format: [YAML] [JSON]
      Download: [Download Package]
      Save to Git: [✓] Commit to repository
  
  Import Configuration:
    Step 1 - Select Package:
      Available Packages:
        - PO Approval v1.2 (2026-02-06, john.doe)
        - Invoice Approval v2.1 (2026-01-15, jane.smith)
      Or Upload: [Browse Files]
    
    Step 2 - Preview Import:
      Target Tenant: [Acme Corp - Production ▼]
      Conflict Strategy: [Overwrite ▼]
      Dry Run: [✓] Preview changes
      
      Preview:
        ✓ Workflow: PO Approval (CREATE)
        ⚠ Custom Field: PO Amount (EXISTS - will overwrite)
        ✓ Business Rule: Approval Threshold (CREATE)
      
      Conflicts:
        ⚠ Custom Field "PO Amount" exists in target
        Recommendation: Review changes before overwriting
    
    Step 3 - Import:
      [Cancel] [Import Package]
  
  Snapshots:
    Active Snapshots:
      - Production Snapshot (2026-02-01, auto)
      - Pre-Release Backup (2026-01-28, john.doe)
      - Monthly Backup (2026-01-01, system)
    
    Actions:
      - Create Snapshot
      - Restore Snapshot
      - Compare Snapshots
      - Download Snapshot
  
  History:
    Recent Transports:
      - 2026-02-06 10:30: Exported "PO Approval v1.2" (john.doe)
      - 2026-02-05 15:20: Imported "Invoice Approval v2.1" to Test (jane.smith)
      - 2026-02-01 09:00: Auto-snapshot created (system)
```

---

## Implementation Roadmap

### Phase 1: Core Transport (Q3 2026)

**Deliverables**:
- [ ] Configuration Transport Service (export/import)
- [ ] Dependency resolver (auto-detect dependencies)
- [ ] Conflict detector (basic conflict detection)
- [ ] Transport package format (YAML/JSON)

**Timeline**: 8 weeks (July-August 2026)
**Resources**: 2 backend engineers, 1 DevOps engineer

### Phase 2: UI & Version Control (Q4 2026)

**Deliverables**:
- [ ] Configuration Transport UI (self-service portal)
- [ ] Git integration (store packages as code)
- [ ] Conflict resolution UI (merge strategies)
- [ ] Snapshot & rollback (point-in-time recovery)

**Timeline**: 6 weeks (October-November 2026)
**Resources**: 2 backend engineers, 1 frontend engineer

### Phase 3: Automation & CI/CD (Q1 2027)

**Deliverables**:
- [ ] Automated promotion pipeline (Dev → Test → Prod)
- [ ] GitHub Actions integration
- [ ] Scheduled snapshots (daily/weekly)
- [ ] Configuration drift detection

**Timeline**: 4 weeks (January 2027)
**Resources**: 1 DevOps engineer, 1 backend engineer

---

## Cost Estimate

### Development Costs

| Item | Cost | Timeline |
|------|------|----------|
| **Backend Engineers** (2 FTE × 4.5 months) | $180K-225K | Q3-Q4 2026 |
| **DevOps Engineers** (1 FTE × 3 months) | $60K-75K | Q3 2026 + Q1 2027 |
| **Frontend Engineer** (1 FTE × 1.5 months) | $30K-40K | Q4 2026 |
| **Total Development** | **$270K-340K** | |

### Infrastructure & Storage

| Item | Cost/Year | Notes |
|------|-----------|-------|
| **Git Repository** (GitHub Enterprise) | $2K-5K | Version control for packages |
| **S3 Storage** (package storage) | $1K-3K | Fast package retrieval |
| **Database Storage** (snapshots) | $2K-4K | 90-day retention |
| **Total Infrastructure** | **$5K-12K** | |

### Total Investment: **$275K-352K** (first year)

---

## Success Metrics

### Technical KPIs
- ✅ **Configuration export time**: <30 seconds (100 objects)
- ✅ **Configuration import time**: <2 minutes (100 objects)
- ✅ **Dependency detection accuracy**: >95%
- ✅ **Conflict detection rate**: 100% (zero false negatives)

### Business KPIs
- ✅ **Configuration deployment time**: 80% reduction (8 hours → <2 hours)
- ✅ **Production incidents (config-related)**: 70% reduction (20 → <6/year)
- ✅ **Self-service adoption**: >60% (customers manage own config transport)
- ✅ **Rollback success rate**: 100% (zero failed rollbacks)

### Operational KPIs
- ✅ **Manual configuration effort**: 90% reduction
- ✅ **Configuration drift incidents**: Zero (automated detection)
- ✅ **Audit compliance**: 100% (all changes tracked)

---

## Alternatives Considered

### 1. Manual Configuration Recreation (Status Quo)
**Rejected**: Not scalable, error-prone, 20% of production incidents.

### 2. Database-Level Export/Import (pg_dump/restore)
**Rejected**: Not selective, no dependency resolution, no conflict detection.

### 3. Third-Party Tools (Liquibase, Flyway)
**Rejected**: Schema migration tools, not designed for application configuration.

### 4. Custom Scripts Per Customer
**Rejected**: Not reusable, maintenance nightmare, no standardization.

---

## Consequences

### Positive ✅
- **Faster deployments**: 80% reduction in configuration deployment time
- **Reduced errors**: 70% fewer config-related production incidents
- **Version control**: All configuration changes tracked in Git
- **Rollback safety**: Point-in-time recovery via snapshots
- **Self-service**: Customers can manage configuration transport
- **Competitive parity**: SAP TMS, Salesforce Change Sets equivalent

### Negative ⚠️
- **Complexity**: Dependency resolution logic complex
- **Storage overhead**: Configuration stored in Git + S3 + Database
- **Learning curve**: Users must learn transport workflow
- **Merge conflicts**: Manual resolution required for complex conflicts

### Risks 🚨
- **Circular dependencies**: Infinite loop in dependency resolution
  - Mitigation: Depth limit, cycle detection algorithm
- **Large packages**: 1000+ objects timeout on import
  - Mitigation: Batch processing, async execution
- **Git repository size**: Growing over time
  - Mitigation: Git LFS for large packages, periodic cleanup
- **Snapshot bloat**: Database storage growth
  - Mitigation: 90-day retention, auto-cleanup old snapshots

---

## Integration with Other ADRs

- **ADR-058**: SOC 2 Compliance (audit trails, change management CC8.1)
- **ADR-059**: ISO 27001 ISMS (configuration management 8.9, change control)
- **ADR-060**: Upgrade Management (configuration compatibility during upgrades)
- **ADR-008**: CI/CD & Network Resilience (GitOps integration)

---

## References

### Industry Best Practices
- SAP Transport Management System (TMS)
- Salesforce Change Sets & DevOps Center
- Dynamics 365 Solution Framework

### Related ADRs
- ADR-008: CI/CD & Network Resilience
- ADR-058: SOC 2 Type II Compliance
- ADR-059: ISO 27001 ISMS
- ADR-060: Upgrade Management

---

*Document Owner*: Platform Team Lead  
*Review Frequency*: Quarterly  
*Next Review*: May 2027  
*Status*: **APPROVED - IMPLEMENTATION STARTING Q3 2026**
