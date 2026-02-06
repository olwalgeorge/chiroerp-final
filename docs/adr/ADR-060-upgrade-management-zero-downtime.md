# ADR-060: Upgrade Management System & Zero-Downtime Upgrades

**Status**: Accepted (Implementation Starting)  
**Date**: 2026-02-06  
**Deciders**: Architecture Team, Platform Team, Operations Team  
**Priority**: P0 (Critical - Enterprise Expectation)  
**Tier**: Platform Core  
**Tags**: upgrade-management, zero-downtime, version-compatibility, rollback, enterprise-operations

---

## Context

**Problem**: Enterprise customers **demand zero-downtime upgrades** and **seamless version transitions** without business interruption. ChiroERP currently lacks:
- **Automated upgrade orchestration** (manual, error-prone upgrades)
- **Version compatibility checking** (breaking changes detected too late)
- **Automated rollback** (incidents require manual intervention)
- **Upgrade scheduling & coordination** (no tenant-specific maintenance windows)
- **Data migration automation** (schema changes require manual SQL scripts)

**Current State Pain Points**:
- Upgrades require **scheduled downtime** (2-4 hours per major release)
- **Manual coordination** across 92 microservices (version drift, failures)
- **No automated rollback** (incidents extend downtime)
- **Customer disruption** (batch job interruptions, transaction failures)
- **Competitive disadvantage**: SAP, Oracle, Dynamics 365 all offer zero-downtime upgrades

**Market Reality**:
- **100% of Fortune 500** expect zero-downtime for mission-critical ERP
- **Average enterprise cost of downtime**: $5,600/minute ($336K/hour)
- **Customer churn risk**: 40% of customers consider switching after 2+ unplanned outages
- **SLA penalties**: $10K-100K per SLA breach (depending on contract)

**Competitive Position**:
- **SAP S/4HANA Cloud**: Zero-downtime maintenance (ZDM), automated upgrades (quarterly)
- **Oracle ERP Cloud**: Zero-downtime patching, rolling upgrades, automated rollback
- **Dynamics 365**: Service updates (monthly), zero-downtime, tenant-level scheduling
- **NetSuite**: Twice-yearly releases, zero-downtime, automated testing
- **ChiroERP**: ❌ Scheduled downtime required (2-4 hours per major release)

**Customer Quote** (Fortune 500 prospect):
> "We run 24/7 operations across 3 continents. Any downtime costs us $400K/hour in lost revenue. Zero-downtime upgrades are non-negotiable."

---

## Decision

Implement a **comprehensive Upgrade Management System** enabling:
1. **Zero-downtime upgrades** (blue-green deployments, rolling updates)
2. **Automated upgrade orchestration** (cross-service coordination, dependency management)
3. **Version compatibility checking** (breaking change detection, contract testing)
4. **Automated rollback** (health check failures, error rate thresholds)
5. **Tenant-specific scheduling** (maintenance windows, phased rollouts)
6. **Data migration automation** (schema evolution, backward compatibility)

**Target**: Q4 2026 (production-ready), phased rollout Q1 2027.

---

## Architecture

### 1. Upgrade Management Service

**New Platform Service**: `platform-operations/upgrade-management-service`

```kotlin
/**
 * Upgrade Management Service
 * Orchestrates zero-downtime upgrades across all ChiroERP microservices
 */
@Service
class UpgradeOrchestrator(
    private val versionRegistry: VersionRegistry,
    private val compatibilityChecker: CompatibilityChecker,
    private val deploymentOrchestrator: DeploymentOrchestrator,
    private val rollbackManager: RollbackManager,
    private val tenantScheduler: TenantScheduler,
    private val migrationExecutor: MigrationExecutor
) {
    
    /**
     * Initiate zero-downtime upgrade for specific tenant or all tenants
     */
    suspend fun initiateUpgrade(request: UpgradeRequest): UpgradeResult {
        // 1. Validate upgrade path
        val validation = compatibilityChecker.validateUpgradePath(
            from = request.currentVersion,
            to = request.targetVersion
        )
        if (!validation.isCompatible) {
            return UpgradeResult.Failed(
                reason = "Incompatible upgrade path: ${validation.issues}"
            )
        }
        
        // 2. Check tenant schedule (if tenant-specific)
        if (request.tenantId != null) {
            val window = tenantScheduler.getMaintenanceWindow(request.tenantId)
            if (!window.isCurrentlyInWindow()) {
                return UpgradeResult.Scheduled(
                    scheduledTime = window.nextWindowStart
                )
            }
        }
        
        // 3. Create upgrade plan (service dependency order)
        val plan = createUpgradePlan(
            targetVersion = request.targetVersion,
            services = request.services ?: allServices
        )
        
        // 4. Execute upgrade (orchestrated)
        return executeUpgrade(plan, request)
    }
    
    private suspend fun executeUpgrade(
        plan: UpgradePlan,
        request: UpgradeRequest
    ): UpgradeResult {
        val executionId = UUID.randomUUID()
        
        try {
            // Stage 1: Data migrations (if required)
            if (plan.requiresDataMigration) {
                migrationExecutor.executeMigrations(
                    migrations = plan.migrations,
                    strategy = MigrationStrategy.DUAL_WRITE // Support both old + new schema
                )
            }
            
            // Stage 2: Deploy new version (blue-green or rolling)
            val deployment = deploymentOrchestrator.deploy(
                services = plan.services,
                strategy = determineDeploymentStrategy(plan),
                healthChecks = plan.healthChecks
            )
            
            // Stage 3: Monitor health (5 min warmup + validation)
            val health = monitorDeploymentHealth(
                deploymentId = deployment.id,
                duration = 5.minutes,
                thresholds = plan.healthThresholds
            )
            
            if (!health.isHealthy) {
                // Automatic rollback on health failure
                rollbackManager.rollback(
                    executionId = executionId,
                    reason = "Health check failed: ${health.failureReason}"
                )
                return UpgradeResult.RolledBack(
                    reason = health.failureReason
                )
            }
            
            // Stage 4: Traffic migration (gradual shift)
            deploymentOrchestrator.migrateTraffic(
                from = "green", // Old version
                to = "blue",    // New version
                strategy = TrafficMigrationStrategy.CANARY(
                    steps = listOf(10, 25, 50, 100), // % traffic to new version
                    duration = 2.minutes // Per step
                )
            )
            
            // Stage 5: Finalize (cleanup old version after soak period)
            delay(plan.soakPeriod) // Default: 30 minutes
            deploymentOrchestrator.finalizeDeployment(deployment.id)
            
            // Stage 6: Cleanup dual-write (if data migration)
            if (plan.requiresDataMigration) {
                migrationExecutor.finalizeMigrations(
                    migrations = plan.migrations
                )
            }
            
            return UpgradeResult.Success(
                executionId = executionId,
                duration = clock.now() - execution.startTime,
                servicesUpgraded = plan.services.size
            )
            
        } catch (e: Exception) {
            // Automatic rollback on any failure
            rollbackManager.rollback(
                executionId = executionId,
                reason = "Upgrade failed: ${e.message}"
            )
            return UpgradeResult.RolledBack(
                reason = e.message ?: "Unknown error"
            )
        }
    }
}

data class UpgradeRequest(
    val currentVersion: SemanticVersion,
    val targetVersion: SemanticVersion,
    val tenantId: UUID? = null, // Null = all tenants (platform upgrade)
    val services: List<ServiceId>? = null, // Null = all services
    val strategy: UpgradeStrategy = UpgradeStrategy.ZERO_DOWNTIME,
    val rollbackOnFailure: Boolean = true
)

enum class UpgradeStrategy {
    ZERO_DOWNTIME,      // Blue-green or rolling (production default)
    SCHEDULED_DOWNTIME, // Maintenance window (legacy, deprecated)
    CANARY,             // Gradual rollout (10% → 50% → 100%)
    PHASED              // Region-by-region (multi-region deployments)
}

sealed class UpgradeResult {
    data class Success(
        val executionId: UUID,
        val duration: Duration,
        val servicesUpgraded: Int
    ) : UpgradeResult()
    
    data class Failed(val reason: String) : UpgradeResult()
    data class RolledBack(val reason: String) : UpgradeResult()
    data class Scheduled(val scheduledTime: Instant) : UpgradeResult()
}
```

---

### 2. Version Compatibility Checking

**Contract-Based Compatibility**:

```kotlin
/**
 * Version Compatibility Checker
 * Detects breaking changes between API versions using contract testing
 */
@Service
class CompatibilityChecker(
    private val contractRegistry: ContractRegistry,
    private val schemaRegistry: SchemaRegistry
) {
    
    fun validateUpgradePath(
        from: SemanticVersion,
        to: SemanticVersion
    ): CompatibilityResult {
        val issues = mutableListOf<CompatibilityIssue>()
        
        // Check 1: Semantic version rules
        if (to.major > from.major) {
            // Major version jump: May have breaking changes
            issues.add(
                CompatibilityIssue.Warning(
                    message = "Major version upgrade (${from.major} → ${to.major}). Review breaking changes."
                )
            )
        }
        
        // Check 2: API contract compatibility (REST + Events)
        val contractIssues = validateApiContracts(from, to)
        issues.addAll(contractIssues)
        
        // Check 3: Database schema compatibility
        val schemaIssues = validateSchemaChanges(from, to)
        issues.addAll(schemaIssues)
        
        // Check 4: Service dependency compatibility
        val dependencyIssues = validateDependencies(from, to)
        issues.addAll(dependencyIssues)
        
        val criticalIssues = issues.filterIsInstance<CompatibilityIssue.Critical>()
        
        return CompatibilityResult(
            isCompatible = criticalIssues.isEmpty(),
            issues = issues,
            upgradeStrategy = determineUpgradeStrategy(issues)
        )
    }
    
    private fun validateApiContracts(
        from: SemanticVersion,
        to: SemanticVersion
    ): List<CompatibilityIssue> {
        val oldContracts = contractRegistry.getContracts(from)
        val newContracts = contractRegistry.getContracts(to)
        
        val issues = mutableListOf<CompatibilityIssue>()
        
        // Check REST API changes
        for ((endpoint, oldContract) in oldContracts.restApis) {
            val newContract = newContracts.restApis[endpoint]
            
            if (newContract == null) {
                // API removed (breaking change)
                issues.add(
                    CompatibilityIssue.Critical(
                        message = "REST API removed: $endpoint",
                        recommendation = "Maintain backward compatibility or implement adapter"
                    )
                )
                continue
            }
            
            // Check request/response schema changes
            val schemaChanges = compareSchemas(oldContract.schema, newContract.schema)
            if (schemaChanges.hasBreakingChanges) {
                issues.add(
                    CompatibilityIssue.Critical(
                        message = "Breaking API schema change: $endpoint",
                        details = schemaChanges.breakingChanges
                    )
                )
            }
        }
        
        // Check Event schema changes (Kafka)
        for ((eventType, oldSchema) in oldContracts.eventSchemas) {
            val newSchema = newContracts.eventSchemas[eventType]
            
            if (newSchema == null) {
                issues.add(
                    CompatibilityIssue.Critical(
                        message = "Event type removed: $eventType"
                    )
                )
                continue
            }
            
            // Avro schema compatibility (backward, forward, full)
            val avroCompatibility = schemaRegistry.checkCompatibility(
                subject = eventType,
                schema = newSchema,
                compatibilityMode = CompatibilityMode.BACKWARD // Default for ChiroERP
            )
            
            if (!avroCompatibility.isCompatible) {
                issues.add(
                    CompatibilityIssue.Critical(
                        message = "Incompatible event schema: $eventType",
                        details = avroCompatibility.errors
                    )
                )
            }
        }
        
        return issues
    }
    
    private fun validateSchemaChanges(
        from: SemanticVersion,
        to: SemanticVersion
    ): List<CompatibilityIssue> {
        val issues = mutableListOf<CompatibilityIssue>()
        
        val migrations = schemaRegistry.getMigrations(from, to)
        
        for (migration in migrations) {
            // Check for destructive migrations
            if (migration.isDestructive) {
                issues.add(
                    CompatibilityIssue.Critical(
                        message = "Destructive migration: ${migration.description}",
                        recommendation = "Use dual-write strategy or data backfill"
                    )
                )
            }
            
            // Check for long-running migrations (>5 min)
            if (migration.estimatedDuration > 5.minutes) {
                issues.add(
                    CompatibilityIssue.Warning(
                        message = "Long-running migration: ${migration.description} (~${migration.estimatedDuration})",
                        recommendation = "Execute during low-traffic period"
                    )
                )
            }
        }
        
        return issues
    }
}

data class CompatibilityResult(
    val isCompatible: Boolean,
    val issues: List<CompatibilityIssue>,
    val upgradeStrategy: RecommendedStrategy
)

sealed class CompatibilityIssue {
    data class Critical(
        val message: String,
        val details: List<String> = emptyList(),
        val recommendation: String? = null
    ) : CompatibilityIssue()
    
    data class Warning(
        val message: String,
        val recommendation: String? = null
    ) : CompatibilityIssue()
    
    data class Info(val message: String) : CompatibilityIssue()
}
```

---

### 3. Automated Rollback System

**Health-Based Rollback**:

```kotlin
/**
 * Automated Rollback Manager
 * Detects deployment failures and triggers instant rollback
 */
@Service
class RollbackManager(
    private val deploymentOrchestrator: DeploymentOrchestrator,
    private val healthMonitor: HealthMonitor,
    private val alertingService: AlertingService
) {
    
    suspend fun rollback(executionId: UUID, reason: String): RollbackResult {
        logger.error("Initiating rollback for execution $executionId: $reason")
        
        // Alert operations team
        alertingService.sendAlert(
            severity = AlertSeverity.CRITICAL,
            title = "Automatic Rollback Initiated",
            message = "Execution $executionId failed: $reason",
            destination = AlertDestination.PAGERDUTY
        )
        
        val startTime = clock.now()
        
        try {
            // Step 1: Stop traffic to new version immediately
            deploymentOrchestrator.stopTrafficShift(executionId)
            
            // Step 2: Redirect 100% traffic to old version (green)
            deploymentOrchestrator.migrateTraffic(
                from = "blue",  // New (failed) version
                to = "green",   // Old (stable) version
                strategy = TrafficMigrationStrategy.IMMEDIATE // No gradual shift
            )
            
            // Step 3: Terminate new version instances
            deploymentOrchestrator.terminateDeployment(
                executionId = executionId,
                reason = "Rollback"
            )
            
            // Step 4: Rollback data migrations (if any)
            val migrations = migrationExecutor.getActiveMigrations(executionId)
            if (migrations.isNotEmpty()) {
                migrationExecutor.rollbackMigrations(migrations)
            }
            
            // Step 5: Verify system health post-rollback
            val health = healthMonitor.checkSystemHealth(
                duration = 2.minutes
            )
            
            if (!health.isHealthy) {
                // Rollback failed (critical incident)
                alertingService.sendAlert(
                    severity = AlertSeverity.CRITICAL,
                    title = "ROLLBACK FAILED",
                    message = "System health did not recover after rollback. Manual intervention required.",
                    destination = AlertDestination.PAGERDUTY
                )
                return RollbackResult.Failed(
                    reason = "Health check failed post-rollback"
                )
            }
            
            val duration = clock.now() - startTime
            logger.info("Rollback successful in $duration")
            
            return RollbackResult.Success(
                duration = duration,
                originalReason = reason
            )
            
        } catch (e: Exception) {
            logger.error("Rollback failed with exception", e)
            alertingService.sendAlert(
                severity = AlertSeverity.CRITICAL,
                title = "ROLLBACK EXCEPTION",
                message = "Rollback threw exception: ${e.message}. Manual intervention required.",
                destination = AlertDestination.PAGERDUTY
            )
            return RollbackResult.Failed(
                reason = "Exception during rollback: ${e.message}"
            )
        }
    }
    
    /**
     * Continuous health monitoring during upgrade
     * Triggers automatic rollback on threshold violations
     */
    suspend fun monitorDeploymentHealth(
        deploymentId: UUID,
        duration: Duration,
        thresholds: HealthThresholds
    ): HealthResult {
        val endTime = clock.now() + duration
        
        while (clock.now() < endTime) {
            val metrics = healthMonitor.collectMetrics(deploymentId)
            
            // Check error rate
            if (metrics.errorRate > thresholds.maxErrorRate) {
                return HealthResult.Unhealthy(
                    reason = "Error rate exceeded threshold: ${metrics.errorRate} > ${thresholds.maxErrorRate}"
                )
            }
            
            // Check latency (P95)
            if (metrics.p95Latency > thresholds.maxLatency) {
                return HealthResult.Unhealthy(
                    reason = "P95 latency exceeded threshold: ${metrics.p95Latency} > ${thresholds.maxLatency}"
                )
            }
            
            // Check critical service availability
            if (metrics.criticalServicesDown.isNotEmpty()) {
                return HealthResult.Unhealthy(
                    reason = "Critical services unavailable: ${metrics.criticalServicesDown}"
                )
            }
            
            delay(10.seconds) // Check every 10 seconds
        }
        
        return HealthResult.Healthy
    }
}

data class HealthThresholds(
    val maxErrorRate: Double = 0.01,     // 1% error rate
    val maxLatency: Duration = 2.seconds, // P95 latency
    val minSuccessRate: Double = 0.99     // 99% success rate
)

sealed class HealthResult {
    object Healthy : HealthResult()
    data class Unhealthy(val reason: String) : HealthResult()
    
    val isHealthy: Boolean get() = this is Healthy
    val failureReason: String? get() = (this as? Unhealthy)?.reason
}
```

---

### 4. Data Migration Automation

**Dual-Write Strategy for Zero-Downtime Schema Changes**:

```kotlin
/**
 * Data Migration Executor
 * Automates database schema evolution without downtime
 */
@Service
class MigrationExecutor(
    private val databaseManager: DatabaseManager,
    private val schemaRegistry: SchemaRegistry
) {
    
    /**
     * Execute migrations using dual-write strategy:
     * 1. Add new schema (additive changes only)
     * 2. Dual-write: Write to both old + new schema
     * 3. Backfill: Copy old data to new schema
     * 4. Validate: Ensure data consistency
     * 5. Cleanup: Remove old schema (after soak period)
     */
    suspend fun executeMigrations(
        migrations: List<Migration>,
        strategy: MigrationStrategy
    ): MigrationResult {
        when (strategy) {
            MigrationStrategy.DUAL_WRITE -> executeDualWriteMigrations(migrations)
            MigrationStrategy.DOWNTIME -> executeDowntimeMigrations(migrations) // Legacy
            MigrationStrategy.ONLINE_REBUILD -> executeOnlineRebuild(migrations) // For large tables
        }
    }
    
    private suspend fun executeDualWriteMigrations(
        migrations: List<Migration>
    ): MigrationResult {
        val results = mutableListOf<MigrationStepResult>()
        
        for (migration in migrations) {
            // Phase 1: Additive schema changes (non-blocking)
            val additiveChanges = migration.additiveChanges
            for (change in additiveChanges) {
                databaseManager.executeSchemaChange(change) // e.g., ADD COLUMN
                results.add(MigrationStepResult.Success(change.description))
            }
            
            // Phase 2: Enable dual-write (application-level)
            enableDualWrite(migration)
            
            // Phase 3: Backfill data (batch processing)
            if (migration.requiresBackfill) {
                val backfillResult = backfillData(
                    migration = migration,
                    batchSize = 10_000, // Process 10K rows at a time
                    parallelism = 4     // 4 concurrent workers
                )
                results.add(backfillResult)
            }
            
            // Phase 4: Validate data consistency
            val validation = validateDataConsistency(migration)
            if (!validation.isConsistent) {
                // Rollback on validation failure
                return MigrationResult.Failed(
                    reason = "Data validation failed: ${validation.issues}"
                )
            }
            
            // Phase 5: Mark migration as pending cleanup
            // (Actual cleanup happens after soak period in finalizeMigrations)
            markMigrationPendingCleanup(migration)
        }
        
        return MigrationResult.Success(
            migrationsExecuted = migrations.size,
            results = results
        )
    }
    
    private fun enableDualWrite(migration: Migration) {
        // Application-level: Write to both old and new schema
        // Implemented via feature flags (e.g., LaunchDarkly)
        featureFlagService.enable(
            flag = "dual-write-${migration.id}",
            rolloutPercentage = 100 // All writes
        )
    }
    
    private suspend fun backfillData(
        migration: Migration,
        batchSize: Int,
        parallelism: Int
    ): MigrationStepResult {
        val totalRows = databaseManager.countRows(migration.sourceTable)
        val batches = (totalRows / batchSize) + 1
        
        logger.info("Backfilling ${migration.id}: $totalRows rows in $batches batches")
        
        // Process batches in parallel
        coroutineScope {
            (0 until batches).chunked(parallelism).forEach { batchGroup ->
                batchGroup.map { batchIndex ->
                    async {
                        val offset = batchIndex * batchSize
                        databaseManager.executeBackfill(
                            migration = migration,
                            offset = offset,
                            limit = batchSize
                        )
                    }
                }.awaitAll()
            }
        }
        
        return MigrationStepResult.Success(
            description = "Backfilled $totalRows rows"
        )
    }
    
    /**
     * Finalize migrations after soak period (typically 30 min - 24 hours)
     * - Disable dual-write
     * - Remove old schema
     */
    suspend fun finalizeMigrations(migrations: List<Migration>) {
        for (migration in migrations) {
            // Disable dual-write
            featureFlagService.disable("dual-write-${migration.id}")
            
            // Execute destructive changes (DROP COLUMN, DROP TABLE)
            for (change in migration.destructiveChanges) {
                databaseManager.executeSchemaChange(change)
            }
            
            logger.info("Finalized migration: ${migration.id}")
        }
    }
}

enum class MigrationStrategy {
    DUAL_WRITE,      // Zero-downtime (default)
    DOWNTIME,        // Legacy, requires maintenance window
    ONLINE_REBUILD   // For large tables (pg_repack, pt-online-schema-change)
}

data class Migration(
    val id: String,
    val description: String,
    val sourceTable: String,
    val additiveChanges: List<SchemaChange>,      // Non-blocking (ADD COLUMN, CREATE INDEX CONCURRENTLY)
    val destructiveChanges: List<SchemaChange>,   // Blocking (DROP COLUMN, DROP TABLE)
    val requiresBackfill: Boolean,
    val backfillQuery: String? = null,
    val estimatedDuration: Duration,
    val isDestructive: Boolean
)

data class SchemaChange(
    val type: SchemaChangeType,
    val sql: String,
    val description: String
)

enum class SchemaChangeType {
    ADD_COLUMN,
    DROP_COLUMN,
    ADD_INDEX,
    DROP_INDEX,
    RENAME_COLUMN,
    ALTER_COLUMN_TYPE,
    ADD_CONSTRAINT,
    DROP_CONSTRAINT
}
```

---

### 5. Tenant-Specific Maintenance Windows

**Scheduled Upgrades Per Tenant**:

```kotlin
/**
 * Tenant Scheduler
 * Manages tenant-specific maintenance windows for upgrades
 */
@Service
class TenantScheduler(
    private val tenantRepository: TenantRepository,
    private val timeZoneService: TimeZoneService
) {
    
    fun getMaintenanceWindow(tenantId: UUID): MaintenanceWindow {
        val tenant = tenantRepository.findById(tenantId)
            ?: throw TenantNotFoundException(tenantId)
        
        val preference = tenant.maintenancePreference
            ?: MaintenancePreference.DEFAULT // Default: Sunday 2-6 AM (tenant timezone)
        
        return MaintenanceWindow(
            dayOfWeek = preference.dayOfWeek,
            startTime = preference.startTime,
            endTime = preference.endTime,
            timeZone = tenant.timeZone,
            excludedDates = preference.excludedDates // Holidays, blackout periods
        )
    }
    
    fun scheduleUpgrade(
        tenantId: UUID,
        targetVersion: SemanticVersion,
        preferredDate: LocalDate? = null
    ): ScheduledUpgrade {
        val window = getMaintenanceWindow(tenantId)
        
        val scheduledTime = if (preferredDate != null) {
            // Use specific date (must be within maintenance window)
            window.getNextOccurrence(startingFrom = preferredDate)
        } else {
            // Use next available window
            window.getNextOccurrence(startingFrom = LocalDate.now())
        }
        
        return ScheduledUpgrade(
            tenantId = tenantId,
            targetVersion = targetVersion,
            scheduledTime = scheduledTime,
            window = window
        )
    }
}

data class MaintenanceWindow(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val timeZone: ZoneId,
    val excludedDates: List<LocalDate> = emptyList()
) {
    fun isCurrentlyInWindow(): Boolean {
        val now = ZonedDateTime.now(timeZone)
        val currentDay = now.dayOfWeek
        val currentTime = now.toLocalTime()
        
        // Check day of week
        if (currentDay != dayOfWeek) return false
        
        // Check time range
        if (currentTime < startTime || currentTime > endTime) return false
        
        // Check excluded dates
        if (now.toLocalDate() in excludedDates) return false
        
        return true
    }
    
    fun getNextOccurrence(startingFrom: LocalDate): ZonedDateTime {
        var candidate = startingFrom.atTime(startTime).atZone(timeZone)
        
        // Find next matching day of week
        while (candidate.dayOfWeek != dayOfWeek || candidate.toLocalDate() in excludedDates) {
            candidate = candidate.plusDays(1)
        }
        
        return candidate
    }
}

data class MaintenancePreference(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val excludedDates: List<LocalDate> = emptyList()
) {
    companion object {
        val DEFAULT = MaintenancePreference(
            dayOfWeek = DayOfWeek.SUNDAY,
            startTime = LocalTime.of(2, 0), // 2 AM
            endTime = LocalTime.of(6, 0),   // 6 AM
            excludedDates = emptyList()
        )
    }
}
```

---

## Zero-Downtime Deployment Strategies

### Strategy 1: Blue-Green Deployment (Default)

**Use Case**: Most services (stateless, quick cutover)

```yaml
Blue-Green Process:
  1. Deploy new version (blue) alongside old version (green)
  2. Run health checks on blue (5 min validation)
  3. Switch load balancer from green to blue (instant cutover)
  4. Monitor blue for soak period (30 min)
  5. Terminate green (cleanup)

Advantages:
  - Instant rollback (switch back to green)
  - Zero downtime (traffic redirected in <1 second)
  - Simple to reason about

Disadvantages:
  - Requires 2x resources during deployment
  - Not suitable for stateful services (databases)

Implementation:
  - Kubernetes: Blue-Green service selector swap
  - AWS: ALB target group swap
  - ArgoCD: Blue-Green rollout strategy
```

### Strategy 2: Rolling Update

**Use Case**: Stateful services, resource-constrained

```yaml
Rolling Update Process:
  1. Deploy new version to 1 pod
  2. Wait for health checks to pass
  3. Terminate 1 old pod
  4. Repeat until all pods updated

Advantages:
  - Lower resource usage (no 2x resources)
  - Gradual transition (easy to detect issues)

Disadvantages:
  - Slower than blue-green (5-10 min for 6 pods)
  - Mixed versions running simultaneously

Implementation:
  - Kubernetes: RollingUpdate strategy (maxSurge=1, maxUnavailable=0)
```

### Strategy 3: Canary Deployment

**Use Case**: High-risk upgrades, major version changes

```yaml
Canary Process:
  1. Deploy new version (1 pod)
  2. Route 10% traffic to canary
  3. Monitor for 5 min (error rate, latency)
  4. If healthy: Increase to 25%, then 50%, then 100%
  5. If unhealthy: Rollback immediately

Advantages:
  - Minimize blast radius (only 10% affected initially)
  - Early detection of issues

Disadvantages:
  - Slower rollout (15-20 min total)
  - Requires sophisticated traffic management (Istio, Linkerd)

Implementation:
  - Istio VirtualService: Traffic splitting by weight
  - Flagger: Automated canary analysis and rollback
```

---

## Monitoring & Observability

### Upgrade Metrics (Real-Time Dashboard)

```yaml
Deployment Metrics:
  - Upgrade duration (target: <10 min)
  - Rollback rate (target: <2%)
  - Success rate (target: >98%)
  - Zero-downtime achievement (target: 100%)

Health Metrics (During Upgrade):
  - Error rate (target: <1%)
  - P50/P95/P99 latency (target: <500ms / <1s / <2s)
  - Request success rate (target: >99%)
  - Critical service availability (target: 100%)

Alerts:
  - P0: Upgrade failure, rollback triggered
  - P1: Health threshold exceeded (error rate >1%)
  - P2: Upgrade duration >15 min
  - P3: Rollback rate >5% (trend)
```

### Upgrade Audit Trail

```kotlin
/**
 * Upgrade Audit Log
 * Immutable record of all upgrade attempts
 */
data class UpgradeAuditLog(
    val executionId: UUID,
    val tenantId: UUID?,
    val initiatedBy: UserId,
    val initiatedAt: Instant,
    val fromVersion: SemanticVersion,
    val toVersion: SemanticVersion,
    val strategy: UpgradeStrategy,
    val servicesUpgraded: List<ServiceId>,
    val status: UpgradeStatus,
    val duration: Duration?,
    val rollbackReason: String?,
    val healthMetrics: HealthMetricsSnapshot
)

enum class UpgradeStatus {
    SCHEDULED,
    IN_PROGRESS,
    SUCCESS,
    FAILED,
    ROLLED_BACK
}
```

---

## Customer-Facing Features

### 1. Upgrade Schedule Transparency

**Self-Service Portal** (`https://app.chiroerp.com/settings/maintenance`):

```yaml
Customer View:
  - Next scheduled upgrade: Date, time, timezone
  - Affected services: List of modules being upgraded
  - Estimated duration: "Zero downtime expected"
  - Release notes: What's new, bug fixes, breaking changes
  - Maintenance window: Edit preferences (day/time)

Notifications:
  - 7 days prior: Email notification (upcoming upgrade)
  - 24 hours prior: Email + in-app banner
  - 1 hour prior: In-app notification
  - During upgrade: Status page update (in progress)
  - Post-upgrade: Email confirmation (success)
```

### 2. Status Page Integration

**Public Status Page** (`https://status.chiroerp.com`):

```yaml
Upgrade Status:
  - Current status: "Upgrade in progress" (live updates)
  - Progress: 40% complete (service-by-service)
  - Expected completion: 8:45 PM PST
  - Impact: "No expected downtime"
  - Subscribe: Email/SMS notifications

Historical Data:
  - Past upgrades: Success rate, duration, incidents
  - Uptime metrics: 99.95% (last 30 days)
```

### 3. Pre-Upgrade Testing (Enterprise Tier)

**Sandbox Environment**:

```yaml
Feature: Pre-upgrade sandbox
  - Clone production tenant to sandbox
  - Apply upcoming upgrade to sandbox
  - Run automated + manual tests
  - Report issues before production upgrade

Availability:
  - Enterprise tier: Included
  - Premium tier: Add-on ($500/month)
  - Standard tier: Not available

Timeline:
  - Sandbox available 7 days before upgrade
  - Expires 24 hours after production upgrade
```

---

## Rollback Strategy & Incident Management

### Automatic Rollback Triggers

```yaml
Rollback Conditions (Automatic):
  - Error rate >1% for >2 minutes
  - P95 latency >2 seconds for >2 minutes
  - Critical service unavailable (Auth, API Gateway)
  - Database migration failure
  - Health check failure (5+ consecutive failures)

Rollback Process:
  1. Instant traffic cutover to old version (<1 second)
  2. Terminate new version instances
  3. Rollback data migrations (if any)
  4. Alert ops team (PagerDuty)
  5. Post-incident review (required)
```

### Manual Rollback (Emergency)

```yaml
Break-Glass Procedure:
  - Access: Ops team + executives
  - Trigger: Manual button in admin dashboard
  - Confirmation: Two-person approval (dual control)
  - Audit: Full audit trail (who, when, why)
  - Communication: Automatic customer notification
```

---

## Implementation Roadmap

### Phase 1: Foundation (Q2 2026)

**Deliverables**:
- [ ] Upgrade Management Service (core orchestration logic)
- [ ] Version Registry (service versions, compatibility matrix)
- [ ] Compatibility Checker (API contract validation)
- [ ] Blue-Green deployment (Kubernetes + ArgoCD)

**Timeline**: 8 weeks (April-May 2026)
**Resources**: 2 platform engineers, 1 DevOps engineer

### Phase 2: Automation (Q3 2026)

**Deliverables**:
- [ ] Automated rollback system (health-based triggers)
- [ ] Data migration automation (dual-write strategy)
- [ ] Tenant scheduler (maintenance windows)
- [ ] Monitoring dashboard (upgrade metrics)

**Timeline**: 8 weeks (July-August 2026)
**Resources**: 2 platform engineers, 1 frontend engineer

### Phase 3: Customer Experience (Q4 2026)

**Deliverables**:
- [ ] Self-service maintenance window management
- [ ] Status page integration (live upgrade progress)
- [ ] Pre-upgrade sandbox (Enterprise tier)
- [ ] Email/SMS notifications (upgrade schedule)

**Timeline**: 6 weeks (October-November 2026)
**Resources**: 1 frontend engineer, 1 backend engineer

### Phase 4: Production Rollout (Q1 2027)

**Approach**: Phased rollout to customers
- [ ] Week 1: Internal tenants only (dogfooding)
- [ ] Week 2-3: Beta customers (10 tenants)
- [ ] Week 4-6: Standard tier (100 tenants)
- [ ] Week 7-10: Premium tier (50 tenants)
- [ ] Week 11-12: Enterprise tier (20 tenants)

**Success Criteria**:
- ✅ Zero-downtime achievement: 100% of upgrades
- ✅ Rollback rate: <2%
- ✅ Customer satisfaction: >4.5/5 (post-upgrade survey)
- ✅ Incident rate: <1% (P0/P1 incidents during upgrade)

---

## Cost Estimate

### Development Costs

| Item | Cost | Timeline |
|------|------|----------|
| **Platform Engineers** (2 FTE x 6 months) | $240K-300K | Q2-Q4 2026 |
| **DevOps Engineer** (1 FTE x 6 months) | $120K-150K | Q2-Q4 2026 |
| **Frontend Engineer** (1 FTE x 3 months) | $60K-75K | Q3-Q4 2026 |
| **Total Development** | **$420K-525K** | |

### Infrastructure Costs (Incremental)

| Item | Cost/Month | Annual |
|------|------------|--------|
| **Blue-Green Resources** (2x pods during deploy) | $2K-3K | $24K-36K |
| **Sandbox Environments** (Enterprise tier, 20 tenants) | $1K-2K | $12K-24K |
| **Monitoring/Observability** (incremental) | $500-1K | $6K-12K |
| **Total Infrastructure** | **$3.5K-6K/month** | **$42K-72K/year** |

### Total Investment: **$462K-597K** (first year)

---

## Success Metrics

### Technical KPIs
- ✅ **Zero-downtime achievement**: 100% (no customer-facing downtime)
- ✅ **Upgrade duration**: <10 minutes (median)
- ✅ **Rollback rate**: <2% (of all upgrade attempts)
- ✅ **Automation rate**: >95% (manual intervention <5%)

### Business KPIs
- ✅ **SLA compliance**: 100% (no SLA breaches due to upgrades)
- ✅ **Customer satisfaction**: >4.5/5 (post-upgrade survey)
- ✅ **Support ticket reduction**: 50% (upgrade-related tickets)
- ✅ **Enterprise sales**: Unblocked (zero-downtime requirement met)

### Operational KPIs
- ✅ **Mean time to upgrade** (MTTU): <10 minutes
- ✅ **Mean time to rollback** (MTTR): <2 minutes
- ✅ **Upgrade frequency**: 2x/month (from 1x/quarter)
- ✅ **Incident rate**: <1% (P0/P1 during upgrades)

---

## Alternatives Considered

### 1. Scheduled Downtime (Status Quo)
**Rejected**: Not acceptable for enterprise customers, competitive disadvantage.

### 2. Manual Blue-Green (No Automation)
**Rejected**: Error-prone, slow, doesn't scale to 92 microservices.

### 3. Third-Party Upgrade Tools (Harness, Spinnaker)
**Rejected**: High cost ($50K-100K/year), vendor lock-in, doesn't address ChiroERP-specific needs (multi-tenancy, data migrations).

### 4. Feature Flags Only (No Deployment Orchestration)
**Rejected**: Doesn't address infrastructure upgrades (Kubernetes, databases), incomplete solution.

---

## Consequences

### Positive ✅
- **Zero-downtime upgrades**: 100% uptime during upgrades
- **Enterprise-ready**: Meets Fortune 500 requirements
- **Faster releases**: 2x/month (from 1x/quarter)
- **Automated rollback**: <2 min recovery from failures
- **Customer satisfaction**: Improved NPS, reduced churn
- **Operational efficiency**: 95% automated (minimal manual intervention)

### Negative ⚠️
- **Development investment**: $420K-525K (first year)
- **Infrastructure cost**: $42K-72K/year (2x resources during deploy)
- **Complexity**: Increased system complexity (orchestration, monitoring)
- **Training**: Ops team training required (new tooling)

### Risks 🚨
- **Rollback failures**: Automatic rollback fails, requires manual intervention
  - Mitigation: Extensive testing, fallback to manual rollback, 24/7 on-call
- **Data migration issues**: Dual-write bugs, data inconsistency
  - Mitigation: Thorough testing in staging, data validation, small batches
- **Third-party dependencies**: Cloud provider issues (AWS, Azure)
  - Mitigation: Multi-region deployments, provider SLAs (99.95%+)

---

## Compliance & Security

### Integration with Other ADRs

- **ADR-058**: SOC 2 Compliance (change management controls, audit trails)
- **ADR-059**: ISO 27001 ISMS (operational procedures, incident management)
- **ADR-017**: Performance Standards (SLA monitoring, alerting)
- **ADR-008**: CI/CD & Network Resilience (deployment pipelines, health checks)

### Audit Trail Requirements

```yaml
Audit Logging (SOC 2 CC8.1):
  - All upgrade attempts (successful + failed)
  - Approval workflows (if manual approval required)
  - Rollback events (automatic + manual)
  - Configuration changes (version updates, feature flags)

Retention: 1 year (compliance requirement)
Access: Read-only (CISO, auditors)
```

---

## References

### Industry Best Practices
- Google SRE Book: "Release Engineering" (Chapter 8)
- AWS Well-Architected Framework: Deployment Patterns
- Kubernetes: Deployment Strategies (Blue-Green, Canary, Rolling)
- Database Reliability Engineering (O'Reilly): Schema Evolution

### Related ADRs
- ADR-017: Performance Standards & Monitoring
- ADR-008: CI/CD & Network Resilience
- ADR-058: SOC 2 Type II Compliance Framework
- ADR-059: ISO 27001 ISMS

### Tools & Technologies
- Kubernetes: https://kubernetes.io/docs/concepts/workloads/controllers/deployment/
- ArgoCD: https://argo-cd.readthedocs.io/
- Flagger (Canary): https://flagger.app/
- Istio (Traffic Management): https://istio.io/

---

*Document Owner*: Platform Team Lead  
*Review Frequency*: Quarterly (post-production rollout)  
*Next Review*: May 2027  
*Status*: **APPROVED - IMPLEMENTATION STARTING Q2 2026**
