package com.chiroerp.tenancy.core.application

import com.chiroerp.tenancy.core.application.exception.TenantProvisioningException
import com.chiroerp.tenancy.core.application.service.ProvisioningStepStatus
import com.chiroerp.tenancy.core.application.service.TenantIsolationService
import com.chiroerp.tenancy.core.application.service.TenantProvisioningService
import com.chiroerp.tenancy.core.application.service.TenantSchemaProvisioner
import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.IsolationLevel
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantTier
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TenantProvisioningServiceTest {
    @Test
    fun `auto strategy maps standard tier to discriminator isolation`() {
        val service = service("AUTO")
        val tenant = tenant(TenantTier.STANDARD, "standard.example")

        val result = service.provision(tenant)

        assertThat(result.isolationPlan.isolationLevel).isEqualTo(IsolationLevel.DISCRIMINATOR)
        assertThat(result.isolationPlan.requiresTenantDiscriminator).isTrue()
        assertThat(result.bootstrapSteps).containsExactly(
            "validate-tenant-settings",
            "verify-shared-schema",
            "seed-default-reference-data",
        )
        assertThat(result.readyForActivation).isTrue()
    }

    @Test
    fun `auto strategy maps enterprise tier to dedicated database isolation`() {
        val service = service("AUTO")
        val tenant = tenant(TenantTier.ENTERPRISE, "enterprise.example")

        val result = service.provision(tenant)

        assertThat(result.isolationPlan.isolationLevel).isEqualTo(IsolationLevel.DATABASE)
        // Database name includes hash suffix for collision resistance
        assertThat(result.isolationPlan.databaseName).startsWith("tenant_enterprise_example_")
        assertThat(result.bootstrapSteps).contains("create-tenant-database")
    }

    @Test
    fun `configured strategy overrides tier defaults`() {
        val service = service("SCHEMA")
        val tenant = tenant(TenantTier.ENTERPRISE, "override.example")

        val result = service.provision(tenant)

        assertThat(result.isolationPlan.isolationLevel).isEqualTo(IsolationLevel.SCHEMA)
        // Schema name includes hash suffix for collision resistance
        assertThat(result.isolationPlan.schemaName).startsWith("tenant_override_example_")
    }

    @Test
    fun `schema names are within postgres identifier limits`() {
        val service = service("SCHEMA")
        // Create tenant with very long domain name (150 chars allowed by validation)
        val longDomain = "a".repeat(150) + ".example"
        val tenant = tenant(TenantTier.PREMIUM, longDomain)

        val result = service.provision(tenant)

        // Postgres identifiers max 63 bytes; tenant_ prefix + slug + hash must fit
        val schemaName = result.isolationPlan.schemaName!!
        assertThat(schemaName.length).isLessThanOrEqualTo(63)
        assertThat(schemaName).startsWith("tenant_")
    }

    @Test
    fun `schema isolation executes bootstrap steps`() {
        val provisioner = FakeSchemaProvisioner()
        val service = service("SCHEMA", provisioner)
        val tenant = tenant(TenantTier.PREMIUM, "premium.example")

        val result = service.provision(tenant)

        assertThat(result.readyForActivation).isTrue()
        assertThat(result.executedSteps.map { it.status })
            .containsOnly(ProvisioningStepStatus.SUCCEEDED)
        assertThat(provisioner.invocations).containsExactly(
            "create-schema:${result.isolationPlan.schemaName}",
            "grant-schema:${result.isolationPlan.schemaName}",
            "bootstrap:${result.isolationPlan.schemaName}",
            "seed:${result.isolationPlan.schemaName}",
        )
    }

    @Test
    fun `bootstrap failure rolls back schema`() {
        val provisioner = FakeSchemaProvisioner(failOnStep = "apply-baseline-migrations")
        val service = service("SCHEMA", provisioner)
        val tenant = tenant(TenantTier.PREMIUM, "fail.example")

        assertThatThrownBy { service.provision(tenant) }
            .isInstanceOf(TenantProvisioningException::class.java)
        assertThat(provisioner.droppedSchema).isNotNull()
    }

    @Test
    fun `discriminator isolation verifies shared schema`() {
        val provisioner = FakeSchemaProvisioner()
        val service = service("AUTO", provisioner)
        val tenant = tenant(TenantTier.STANDARD, "sharedschema.example")

        val result = service.provision(tenant)

        assertThat(result.executedSteps.map { it.stepName })
            .containsExactly(
                "validate-tenant-settings",
                "verify-shared-schema",
                "seed-default-reference-data",
            )
        assertThat(provisioner.invocations.first()).contains("verify-shared-schema:true")
        assertThat(result.readyForActivation).isTrue()
    }

    @Test
    fun `enterprise tenants remain pending until manual database provisioning`() {
        val provisioner = FakeSchemaProvisioner()
        val service = service("AUTO", provisioner)
        val tenant = tenant(TenantTier.ENTERPRISE, "enterprise-pending.example")

        val result = service.provision(tenant)

        assertThat(result.readyForActivation).isFalse()
        assertThat(result.executedSteps.filter { it.stepName.startsWith("create-tenant-database") })
            .allMatch { it.status == ProvisioningStepStatus.SKIPPED }
    }

    private fun service(
        strategy: String,
        provisioner: TenantSchemaProvisioner = FakeSchemaProvisioner(),
    ): TenantProvisioningService = TenantProvisioningService(
        tenantIsolationService = TenantIsolationService(strategy),
        tenantSchemaProvisioner = provisioner,
    )

    private fun tenant(tier: TenantTier, domain: String): Tenant = Tenant.create(
        id = TenantId(UUID.randomUUID()),
        name = "Tenant-${tier.name}",
        domain = domain,
        tier = tier,
        dataResidency = DataResidency("US"),
        now = Instant.parse("2026-02-09T10:00:00Z"),
    )

    private class FakeSchemaProvisioner(
        private val failOnStep: String? = null,
    ) : TenantSchemaProvisioner {
        val invocations = mutableListOf<String>()
        var droppedSchema: String? = null

        override fun verifySharedSchema() {
            invocations += "verify-shared-schema:true"
        }

        override fun createSchema(schemaName: String) {
            invocations += "create-schema:$schemaName"
            if (failOnStep == "create-tenant-schema") {
                error("schema creation failure")
            }
        }

        override fun grantSchemaUsage(schemaName: String) {
            invocations += "grant-schema:$schemaName"
        }

        override fun createBootstrapObjects(schemaName: String) {
            invocations += "bootstrap:$schemaName"
            if (failOnStep == "apply-baseline-migrations") {
                error("bootstrap failure")
            }
        }

        override fun seedSchemaReferenceData(schemaName: String, tenantId: TenantId) {
            invocations += "seed:$schemaName"
        }

        override fun dropSchema(schemaName: String) {
            droppedSchema = schemaName
        }
    }
}
