package com.chiroerp.tenancy.core.infrastructure.observability

import com.chiroerp.tenancy.core.infrastructure.outbox.TenantOutboxStore
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.concurrent.atomic.AtomicLong

@ApplicationScoped
class TenantOutboxMetricsCollector(
    private val tenantOutboxStore: TenantOutboxStore,
    meterRegistry: MeterRegistry,
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.outbox.enabled", defaultValue = "true")
    private val enabled: Boolean,
) {
    private val pendingGaugeValue = AtomicLong(0)
    private val deadGaugeValue = AtomicLong(0)

    init {
        Gauge.builder("chiroerp.tenancy.outbox.pending") { pendingGaugeValue.get().toDouble() }
            .description("Tenancy outbox pending entries awaiting dispatch")
            .register(meterRegistry)
        Gauge.builder("chiroerp.tenancy.outbox.dead") { deadGaugeValue.get().toDouble() }
            .description("Tenancy outbox entries marked as dead/poison")
            .register(meterRegistry)
    }

    @Scheduled(
        every = "{chiroerp.messaging.tenant-events.outbox.metrics-refresh-interval:30s}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @Transactional(Transactional.TxType.SUPPORTS)
    internal fun refreshGauges() {
        if (!enabled) {
            pendingGaugeValue.set(0)
            deadGaugeValue.set(0)
            return
        }

        pendingGaugeValue.set(tenantOutboxStore.countPending())
        deadGaugeValue.set(tenantOutboxStore.countDead())
    }
}
