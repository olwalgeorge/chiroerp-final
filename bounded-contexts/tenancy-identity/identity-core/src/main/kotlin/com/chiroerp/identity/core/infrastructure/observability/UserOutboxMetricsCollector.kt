package com.chiroerp.identity.core.infrastructure.observability

import com.chiroerp.identity.core.infrastructure.outbox.UserOutboxStore
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.concurrent.atomic.AtomicLong

@ApplicationScoped
class UserOutboxMetricsCollector(
    private val userOutboxStore: UserOutboxStore,
    meterRegistry: MeterRegistry,
    @param:ConfigProperty(name = "chiroerp.messaging.identity-events.outbox.enabled", defaultValue = "true")
    private val enabled: Boolean,
) {
    private val pendingGaugeValue = AtomicLong(0)
    private val deadGaugeValue = AtomicLong(0)

    init {
        Gauge.builder("chiroerp.identity.outbox.pending") { pendingGaugeValue.get().toDouble() }
            .description("Identity outbox pending entries awaiting dispatch")
            .register(meterRegistry)
        Gauge.builder("chiroerp.identity.outbox.dead") { deadGaugeValue.get().toDouble() }
            .description("Identity outbox entries marked as dead/poison")
            .register(meterRegistry)
    }

    @Scheduled(
        every = "{chiroerp.messaging.identity-events.outbox.metrics-refresh-interval:30s}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @Transactional(Transactional.TxType.SUPPORTS)
    internal fun refreshGauges() {
        if (!enabled) {
            pendingGaugeValue.set(0)
            deadGaugeValue.set(0)
            return
        }

        pendingGaugeValue.set(userOutboxStore.countPending())
        deadGaugeValue.set(userOutboxStore.countDead())
    }
}
