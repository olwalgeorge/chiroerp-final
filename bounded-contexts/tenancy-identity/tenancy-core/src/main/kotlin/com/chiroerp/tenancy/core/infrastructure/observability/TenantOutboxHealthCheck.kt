package com.chiroerp.tenancy.core.infrastructure.observability

import com.chiroerp.tenancy.core.infrastructure.outbox.TenantOutboxStore
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Readiness

@Readiness
@ApplicationScoped
class TenantOutboxHealthCheck(
    private val tenantOutboxStore: TenantOutboxStore,
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.outbox.pending-threshold", defaultValue = "500")
    private val pendingThreshold: Long,
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.outbox.dead-threshold", defaultValue = "0")
    private val deadThreshold: Long,
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.outbox.enabled", defaultValue = "true")
    private val enabled: Boolean,
) : HealthCheck {
    @Transactional(Transactional.TxType.SUPPORTS)
    override fun call(): HealthCheckResponse {
        if (!enabled) {
            return HealthCheckResponse.named("tenant-outbox")
                .status(true)
                .withData("enabled", false)
                .build()
        }

        val pending = tenantOutboxStore.countPending()
        val dead = tenantOutboxStore.countDead()
        val healthy = pending <= pendingThreshold && dead <= deadThreshold

        return HealthCheckResponse.named("tenant-outbox")
            .status(healthy)
            .withData("pendingCount", pending)
            .withData("pendingThreshold", pendingThreshold)
            .withData("deadCount", dead)
            .withData("deadThreshold", deadThreshold)
            .build()
    }
}
