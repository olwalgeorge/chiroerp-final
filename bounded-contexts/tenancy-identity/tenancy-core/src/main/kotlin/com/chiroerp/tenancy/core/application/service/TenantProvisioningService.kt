package com.chiroerp.tenancy.core.application.service

import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.IsolationLevel
import com.chiroerp.tenancy.shared.TenantId
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class TenantProvisioningService(
    private val tenantIsolationService: TenantIsolationService,
) {
    /**
     * Provisions a tenant's infrastructure based on their isolation tier.
     * 
     * Current implementation: planning phase only (no side effects).
     * Future implementation will execute schema/database creation.
     * 
     * @return Provisioning result with readyForActivation = false if any step fails
     */
    fun provision(tenant: Tenant): TenantProvisioningResult {
        val isolationPlan = tenantIsolationService.buildProvisioningPlan(tenant)
        val bootstrapSteps = bootstrapStepsFor(isolationPlan.isolationLevel)
        
        Log.infof(
            "Provisioning tenant %s with isolation level %s, schema=%s, database=%s",
            tenant.id.value,
            isolationPlan.isolationLevel,
            isolationPlan.schemaName ?: "N/A",
            isolationPlan.databaseName ?: "N/A",
        )
        
        // TODO: TI-04 - Execute actual provisioning steps here
        // For now, we only build the plan. Real execution will:
        // 1. Create schema/database if needed
        // 2. Apply migrations
        // 3. Seed reference data
        // 4. Configure RLS policies
        // Each step should be idempotent and record success/failure
        
        val executedSteps = mutableListOf<ProvisioningStepResult>()
        var allSucceeded = true
        
        for (step in bootstrapSteps) {
            // Placeholder: mark all steps as planned but not executed
            executedSteps.add(
                ProvisioningStepResult(
                    stepName = step,
                    status = ProvisioningStepStatus.PLANNED,
                    message = "Step planned for execution",
                ),
            )
        }
        
        return TenantProvisioningResult(
            tenantId = tenant.id,
            createdAt = Instant.now(),
            isolationPlan = isolationPlan,
            bootstrapSteps = bootstrapSteps,
            executedSteps = executedSteps,
            readyForActivation = allSucceeded,
        )
    }

    private fun bootstrapStepsFor(isolationLevel: IsolationLevel): List<String> {
        val base = listOf(
            "validate-tenant-settings",
            "apply-baseline-migrations",
            "seed-default-reference-data",
        )

        return when (isolationLevel) {
            IsolationLevel.DISCRIMINATOR -> base + "enable-row-level-security-policy"
            IsolationLevel.SCHEMA -> base + listOf("create-tenant-schema", "apply-schema-grants")
            IsolationLevel.DATABASE -> base + listOf("create-tenant-database", "configure-database-routing")
        }
    }
}

data class TenantProvisioningResult(
    val tenantId: TenantId,
    val createdAt: Instant,
    val isolationPlan: TenantIsolationPlan,
    val bootstrapSteps: List<String>,
    val executedSteps: List<ProvisioningStepResult>,
    val readyForActivation: Boolean,
)

data class ProvisioningStepResult(
    val stepName: String,
    val status: ProvisioningStepStatus,
    val message: String?,
)

enum class ProvisioningStepStatus {
    PLANNED,
    EXECUTING,
    SUCCEEDED,
    FAILED,
    SKIPPED,
}
