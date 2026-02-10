package com.chiroerp.tenancy.core.infrastructure.observability

import com.chiroerp.tenancy.core.infrastructure.outbox.TenantOutboxEntry
import com.chiroerp.tenancy.core.infrastructure.outbox.TenantOutboxStore
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TenantOutboxMetricsCollectorTest {
    @Test
    fun `refresh updates gauges with store counts`() {
        val store = MetricsOnlyOutboxStore(pending = 12, dead = 2)
        val registry = SimpleMeterRegistry()
        val collector = TenantOutboxMetricsCollector(
            tenantOutboxStore = store,
            meterRegistry = registry,
            enabled = true,
        )

        collector.refreshGauges()

        assertThat(registry.find("chiroerp.tenancy.outbox.pending").gauge()?.value()).isEqualTo(12.0)
        assertThat(registry.find("chiroerp.tenancy.outbox.dead").gauge()?.value()).isEqualTo(2.0)
    }

    @Test
    fun `refresh resets gauges when disabled`() {
        val store = MetricsOnlyOutboxStore(pending = 99, dead = 4)
        val registry = SimpleMeterRegistry()
        val collector = TenantOutboxMetricsCollector(
            tenantOutboxStore = store,
            meterRegistry = registry,
            enabled = false,
        )

        collector.refreshGauges()

        assertThat(registry.find("chiroerp.tenancy.outbox.pending").gauge()?.value()).isEqualTo(0.0)
        assertThat(registry.find("chiroerp.tenancy.outbox.dead").gauge()?.value()).isEqualTo(0.0)
    }

    private class MetricsOnlyOutboxStore(
        private val pending: Long,
        private val dead: Long,
    ) : TenantOutboxStore {
        override fun save(entries: List<TenantOutboxEntry>) = Unit
        override fun fetchPending(limit: Int, now: Instant): List<TenantOutboxEntry> = emptyList()
        override fun claimPending(limit: Int, now: Instant): List<TenantOutboxEntry> = emptyList()
        override fun markPublished(eventId: UUID, publishedAt: Instant) = Unit
        override fun markFailed(eventId: UUID, attempts: Int, nextAttemptAt: Instant, lastError: String?) = Unit
        override fun markDead(eventId: UUID, attempts: Int, lastError: String?) = Unit
        override fun countPending(): Long = pending
        override fun countDead(): Long = dead
    }
}
