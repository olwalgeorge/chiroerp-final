package com.chiroerp.identity.core.infrastructure.observability

import com.chiroerp.identity.core.infrastructure.outbox.UserOutboxStore
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Readiness

@Readiness
@ApplicationScoped
class UserOutboxHealthCheck(
    private val userOutboxStore: UserOutboxStore,
    @param:ConfigProperty(name = "chiroerp.messaging.identity-events.outbox.pending-threshold", defaultValue = "500")
    private val pendingThreshold: Long,
    @param:ConfigProperty(name = "chiroerp.messaging.identity-events.outbox.dead-threshold", defaultValue = "0")
    private val deadThreshold: Long,
    @param:ConfigProperty(name = "chiroerp.messaging.identity-events.outbox.enabled", defaultValue = "true")
    private val enabled: Boolean,
) : HealthCheck {
    @Transactional(Transactional.TxType.SUPPORTS)
    override fun call(): HealthCheckResponse {
        if (!enabled) {
            return HealthCheckResponse.named("identity-outbox")
                .status(true)
                .withData("enabled", false)
                .build()
        }

        val pending = userOutboxStore.countPending()
        val dead = userOutboxStore.countDead()
        val healthy = pending <= pendingThreshold && dead <= deadThreshold

        return HealthCheckResponse.named("identity-outbox")
            .status(healthy)
            .withData("pendingCount", pending)
            .withData("pendingThreshold", pendingThreshold)
            .withData("deadCount", dead)
            .withData("deadThreshold", deadThreshold)
            .build()
    }
}
