package com.chiroerp.tenancy.core.infrastructure.observability

import com.chiroerp.tenancy.core.application.service.ProvisioningStepStatus
import com.chiroerp.tenancy.core.application.service.ProvisioningTelemetry
import com.chiroerp.tenancy.shared.IsolationLevel
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class TenantProvisioningMetrics(
    private val meterRegistry: MeterRegistry,
    @ConfigProperty(name = "chiroerp.tenancy.provisioning.metrics.enabled", defaultValue = "true")
    private val enabled: Boolean,
) : ProvisioningTelemetry {
    override fun recordStep(stepName: String, status: ProvisioningStepStatus) {
        if (!enabled) return
        Counter.builder("chiroerp.tenancy.provisioning.step")
            .tag("step", stepName)
            .tag("status", status.name.lowercase())
            .description("Counts provisioning steps by outcome")
            .register(meterRegistry)
            .increment()
    }

    override fun recordOutcome(isolationLevel: IsolationLevel, readyForActivation: Boolean) {
        if (!enabled) return
        Counter.builder("chiroerp.tenancy.provisioning.outcome")
            .tag("isolation", isolationLevel.name.lowercase())
            .tag("ready", readyForActivation.toString())
            .description("Counts provisioning results by isolation level and readiness")
            .register(meterRegistry)
            .increment()
    }
}
