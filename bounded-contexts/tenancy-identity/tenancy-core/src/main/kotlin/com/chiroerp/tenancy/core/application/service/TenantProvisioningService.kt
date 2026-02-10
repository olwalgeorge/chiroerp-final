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
    private val provisioningTelemetry: ProvisioningTelemetry,
) {
    fun provision(tenant: Tenant): TenantProvisioningResult {
        val isolationPlan = tenantIsolationService.buildProvisioningPlan(tenant)
        val bootstrapSteps = bootstrapStepsFor(isolationPlan.isolationLevel)
        val executedSteps = mutableListOf<ProvisioningStepResult>()
        var createdSchema: String? = null
        var createdDatabase: String? = null

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
                        IsolationLevel.DATABASE -> runStep(step, executedSteps, tenant) {
                            "Enterprise database contains dedicated reference data"
                        }
                        else -> runStep(step, executedSteps, tenant) {
                            "Shared reference data already available"
                        }
                    }
                    "create-tenant-database" -> {
                        val databaseName = isolationPlan.databaseName
                            ?: throw TenantProvisioningException(tenant.id, "Isolation plan missing database name")
                        runStep(step, executedSteps, tenant) {
                            tenantSchemaProvisioner.createDatabase(databaseName)
                            tenantSchemaProvisioner.grantDatabaseAccess(databaseName)
                            createdDatabase = databaseName
                            "Database $databaseName created"
                        }
                    }
                    "apply-database-migrations" -> {
                        val databaseName = isolationPlan.databaseName
                            ?: throw TenantProvisioningException(tenant.id, "Isolation plan missing database name")
                        runStep(step, executedSteps, tenant) {
                            tenantSchemaProvisioner.runDatabaseMigrations(databaseName, tenant.id)
                            "Database migrations executed for $databaseName"
                        }
                    }
                    else -> skipStep(step, executedSteps, "No action mapped for step")
                }
            }
        } catch (ex: TenantProvisioningException) {
            rollbackSchema(createdSchema)
            rollbackDatabase(createdDatabase)
            throw ex
        } catch (ex: Exception) {
            rollbackSchema(createdSchema)
            rollbackDatabase(createdDatabase)
            throw TenantProvisioningException(
                tenantId = tenant.id,
                reason = ex.message ?: "Provisioning failed unexpectedly",
                cause = ex,
            )
        }

        val ready = executedSteps.none { it.status == ProvisioningStepStatus.FAILED }
        provisioningTelemetry.recordOutcome(isolationPlan.isolationLevel, ready)

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
            provisioningTelemetry.recordStep(stepName, ProvisioningStepStatus.SUCCEEDED)
        } catch (ex: Exception) {
            executedSteps += ProvisioningStepResult(
                stepName = stepName,
                status = ProvisioningStepStatus.FAILED,
                message = ex.message,
            )
            provisioningTelemetry.recordStep(stepName, ProvisioningStepStatus.FAILED)
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
        provisioningTelemetry.recordStep(stepName, ProvisioningStepStatus.SKIPPED)
    }

    private fun rollbackSchema(schemaName: String?) {
        if (schemaName == null) return
        runCatching { tenantSchemaProvisioner.dropSchema(schemaName) }
            .onFailure { Log.warnf(it, "Failed to rollback schema %s after provisioning error", schemaName) }
    }

    private fun rollbackDatabase(databaseName: String?) {
        if (databaseName == null) return
        runCatching { tenantSchemaProvisioner.dropDatabase(databaseName) }
            .onFailure { Log.warnf(it, "Failed to rollback database %s after provisioning error", databaseName) }
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
