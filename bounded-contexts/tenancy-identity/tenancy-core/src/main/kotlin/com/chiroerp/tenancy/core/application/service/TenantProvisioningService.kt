package com.chiroerp.tenancy.core.application.service

import com.chiroerp.tenancy.core.application.exception.TenantProvisioningException
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.IsolationLevel
import com.chiroerp.tenancy.shared.TenantId
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class TenantProvisioningService(
    private val tenantIsolationService: TenantIsolationService,
    private val tenantSchemaProvisioner: TenantSchemaProvisioner,
) {
    fun provision(tenant: Tenant): TenantProvisioningResult {
        val isolationPlan = tenantIsolationService.buildProvisioningPlan(tenant)
        val bootstrapSteps = bootstrapStepsFor(isolationPlan.isolationLevel)
        val executedSteps = mutableListOf<ProvisioningStepResult>()
        var createdSchema: String? = null

        try {
            bootstrapSteps.forEach { step ->
                when (step) {
                    "validate-tenant-settings" -> runStep(step, executedSteps, tenant) {
                        validateTenantSettings(tenant)
                        "Tenant settings validated"
                    }
                    "verify-shared-schema" -> runStep(step, executedSteps, tenant) {
                        tenantSchemaProvisioner.verifySharedSchema()
                        "Shared schema verified"
                    }
                    "create-tenant-schema" -> {
                        val schemaName = isolationPlan.schemaName
                            ?: throw TenantProvisioningException(tenant.id, "Isolation plan missing schema name")
                        runStep(step, executedSteps, tenant) {
                            tenantSchemaProvisioner.createSchema(schemaName)
                            createdSchema = schemaName
                            "Schema $schemaName created"
                        }
                    }
                    "apply-schema-grants" -> {
                        val schemaName = isolationPlan.schemaName
                            ?: throw TenantProvisioningException(tenant.id, "Isolation plan missing schema name")
                        runStep(step, executedSteps, tenant) {
                            tenantSchemaProvisioner.grantSchemaUsage(schemaName)
                            "Usage granted for schema $schemaName"
                        }
                    }
                    "apply-baseline-migrations" -> {
                        val schemaName = isolationPlan.schemaName
                            ?: throw TenantProvisioningException(tenant.id, "Isolation plan missing schema name")
                        runStep(step, executedSteps, tenant) {
                            tenantSchemaProvisioner.createBootstrapObjects(schemaName)
                            "Bootstrap objects created in schema $schemaName"
                        }
                    }
                    "seed-default-reference-data" -> when (isolationPlan.isolationLevel) {
                        IsolationLevel.SCHEMA -> {
                            val schemaName = isolationPlan.schemaName
                                ?: throw TenantProvisioningException(tenant.id, "Isolation plan missing schema name")
                            runStep(step, executedSteps, tenant) {
                                tenantSchemaProvisioner.seedSchemaReferenceData(schemaName, tenant.id)
                                "Reference data initialized in $schemaName"
                            }
                        }
                        else -> runStep(step, executedSteps, tenant) {
                            "Shared reference data already available"
                        }
                    }
                    "create-tenant-database" -> skipStep(
                        step,
                        executedSteps,
                        "Enterprise database provisioning not automated yet",
                    )
                    "apply-database-migrations" -> skipStep(
                        step,
                        executedSteps,
                        "Enterprise database migrations require manual pipeline",
                    )
                    else -> skipStep(step, executedSteps, "No action mapped for step")
                }
            }
        } catch (ex: TenantProvisioningException) {
            rollbackSchema(createdSchema)
            throw ex
        } catch (ex: Exception) {
            rollbackSchema(createdSchema)
            throw TenantProvisioningException(
                tenantId = tenant.id,
                reason = ex.message ?: "Provisioning failed unexpectedly",
                cause = ex,
            )
        }

        val ready = isolationPlan.isolationLevel != IsolationLevel.DATABASE
        if (!ready) {
            Log.warnf(
                "Tenant %s created in PENDING status - enterprise database automation pending",
                tenant.id.value,
            )
        }

        return TenantProvisioningResult(
            tenantId = tenant.id,
            createdAt = Instant.now(),
            isolationPlan = isolationPlan,
            bootstrapSteps = bootstrapSteps,
            executedSteps = executedSteps,
            readyForActivation = ready,
        )
    }

    private fun runStep(
        stepName: String,
        executedSteps: MutableList<ProvisioningStepResult>,
        tenant: Tenant,
        action: () -> String,
    ) {
        try {
            val message = action()
            executedSteps += ProvisioningStepResult(
                stepName = stepName,
                status = ProvisioningStepStatus.SUCCEEDED,
                message = message,
            )
        } catch (ex: Exception) {
            executedSteps += ProvisioningStepResult(
                stepName = stepName,
                status = ProvisioningStepStatus.FAILED,
                message = ex.message,
            )
            throw TenantProvisioningException(
                tenantId = tenant.id,
                reason = "Step '$stepName' failed: ${ex.message}",
                cause = ex,
            )
        }
    }

    private fun skipStep(
        stepName: String,
        executedSteps: MutableList<ProvisioningStepResult>,
        reason: String,
    ) {
        executedSteps += ProvisioningStepResult(
            stepName = stepName,
            status = ProvisioningStepStatus.SKIPPED,
            message = reason,
        )
    }

    private fun rollbackSchema(schemaName: String?) {
        if (schemaName == null) return
        runCatching { tenantSchemaProvisioner.dropSchema(schemaName) }
            .onFailure { Log.warnf(it, "Failed to rollback schema %s after provisioning error", schemaName) }
    }

    private fun validateTenantSettings(tenant: Tenant) {
        require(tenant.name.isNotBlank()) { "Tenant name is required" }
        require(tenant.domain.isNotBlank()) { "Tenant domain is required" }
    }

    private fun bootstrapStepsFor(isolationLevel: IsolationLevel): List<String> =
        when (isolationLevel) {
            IsolationLevel.DISCRIMINATOR -> listOf(
                "validate-tenant-settings",
                "verify-shared-schema",
                "seed-default-reference-data",
            )
            IsolationLevel.SCHEMA -> listOf(
                "validate-tenant-settings",
                "create-tenant-schema",
                "apply-schema-grants",
                "apply-baseline-migrations",
                "seed-default-reference-data",
            )
            IsolationLevel.DATABASE -> listOf(
                "validate-tenant-settings",
                "create-tenant-database",
                "apply-database-migrations",
                "seed-default-reference-data",
            )
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
