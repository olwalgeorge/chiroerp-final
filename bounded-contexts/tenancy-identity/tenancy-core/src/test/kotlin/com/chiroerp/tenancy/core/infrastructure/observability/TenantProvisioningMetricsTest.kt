package com.chiroerp.tenancy.core.infrastructure.observability

import com.chiroerp.tenancy.core.application.service.ProvisioningStepStatus
import com.chiroerp.tenancy.shared.IsolationLevel
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TenantProvisioningMetricsTest {
    private val registry = SimpleMeterRegistry()
    private val metrics = TenantProvisioningMetrics(registry, enabled = true)

    @Test
    fun `records step metrics with tags`() {
        metrics.recordStep("create-tenant-schema", ProvisioningStepStatus.SUCCEEDED)

        val counter = registry.get("chiroerp.tenancy.provisioning.step")
            .tag("step", "create-tenant-schema")
            .tag("status", "succeeded")
            .counter()

        assertThat(counter.count()).isEqualTo(1.0)
    }

    @Test
    fun `records outcome metrics`() {
        metrics.recordOutcome(IsolationLevel.SCHEMA, true)

        val counter = registry.get("chiroerp.tenancy.provisioning.outcome")
            .tag("isolation", "schema")
            .tag("ready", "true")
            .counter()

        assertThat(counter.count()).isEqualTo(1.0)
    }
}
