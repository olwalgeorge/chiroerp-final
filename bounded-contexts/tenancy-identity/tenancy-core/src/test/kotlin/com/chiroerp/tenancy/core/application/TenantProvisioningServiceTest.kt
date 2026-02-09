package com.chiroerp.tenancy.core.application

import com.chiroerp.tenancy.core.application.service.ProvisioningStepStatus
import com.chiroerp.tenancy.core.application.service.TenantIsolationService
import com.chiroerp.tenancy.core.application.service.TenantProvisioningService
import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.IsolationLevel
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantTier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TenantProvisioningServiceTest {
    @Test
    fun `auto strategy maps standard tier to discriminator isolation`() {
        val service = TenantProvisioningService(TenantIsolationService("AUTO"))
        val tenant = tenant(TenantTier.STANDARD, "standard.example")

        val result = service.provision(tenant)

        assertThat(result.isolationPlan.isolationLevel).isEqualTo(IsolationLevel.DISCRIMINATOR)
        assertThat(result.isolationPlan.requiresTenantDiscriminator).isTrue()
        assertThat(result.bootstrapSteps).contains("enable-row-level-security-policy")
        assertThat(result.readyForActivation).isTrue()
    }

    @Test
    fun `auto strategy maps enterprise tier to dedicated database isolation`() {
        val service = TenantProvisioningService(TenantIsolationService("AUTO"))
        val tenant = tenant(TenantTier.ENTERPRISE, "enterprise.example")

        val result = service.provision(tenant)

        assertThat(result.isolationPlan.isolationLevel).isEqualTo(IsolationLevel.DATABASE)
        // Database name includes hash suffix for collision resistance
        assertThat(result.isolationPlan.databaseName).startsWith("tenant_enterprise_example_")
        assertThat(result.bootstrapSteps).contains("create-tenant-database")
    }

    @Test
    fun `configured strategy overrides tier defaults`() {
        val service = TenantProvisioningService(TenantIsolationService("SCHEMA"))
        val tenant = tenant(TenantTier.ENTERPRISE, "override.example")

        val result = service.provision(tenant)

        assertThat(result.isolationPlan.isolationLevel).isEqualTo(IsolationLevel.SCHEMA)
        // Schema name includes hash suffix for collision resistance
        assertThat(result.isolationPlan.schemaName).startsWith("tenant_override_example_")
    }

    @Test
    fun `provisioning result includes executed steps with PLANNED status`() {
        val service = TenantProvisioningService(TenantIsolationService("AUTO"))
        val tenant = tenant(TenantTier.PREMIUM, "premium.example")

        val result = service.provision(tenant)

        assertThat(result.executedSteps).isNotEmpty()
        assertThat(result.executedSteps).allMatch { it.status == ProvisioningStepStatus.PLANNED }
        assertThat(result.executedSteps.map { it.stepName }).containsAll(result.bootstrapSteps)
    }

    @Test
    fun `schema names are within postgres identifier limits`() {
        val service = TenantProvisioningService(TenantIsolationService("SCHEMA"))
        // Create tenant with very long domain name (150 chars allowed by validation)
        val longDomain = "a".repeat(150) + ".example"
        val tenant = tenant(TenantTier.PREMIUM, longDomain)

        val result = service.provision(tenant)

        // Postgres identifiers max 63 bytes; tenant_ prefix + slug + hash must fit
        val schemaName = result.isolationPlan.schemaName!!
        assertThat(schemaName.length).isLessThanOrEqualTo(63)
        assertThat(schemaName).startsWith("tenant_")
    }

    private fun tenant(tier: TenantTier, domain: String): Tenant = Tenant.create(
        id = TenantId(UUID.randomUUID()),
        name = "Tenant-${tier.name}",
        domain = domain,
        tier = tier,
        dataResidency = DataResidency("US"),
        now = Instant.parse("2026-02-09T10:00:00Z"),
    )
}
