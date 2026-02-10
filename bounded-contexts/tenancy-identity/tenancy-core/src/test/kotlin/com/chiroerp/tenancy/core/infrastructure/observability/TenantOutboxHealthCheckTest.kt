package com.chiroerp.tenancy.core.infrastructure.observability

import com.chiroerp.tenancy.core.infrastructure.outbox.TenantOutboxEntry
import com.chiroerp.tenancy.core.infrastructure.outbox.TenantOutboxStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TenantOutboxHealthCheckTest {
    @Test
    fun `health is up when counts under thresholds`() {
        val store = ThresholdAwareStore(pending = 10, dead = 0)
        val health = TenantOutboxHealthCheck(
            tenantOutboxStore = store,
            pendingThreshold = 100,
            deadThreshold = 0,
            enabled = true,
        )

        val response = health.call()

        assertThat(response.status).isEqualTo(org.eclipse.microprofile.health.HealthCheckResponse.Status.UP)
    }

    @Test
    fun `health is down when dead entries exceed threshold`() {
        val store = ThresholdAwareStore(pending = 1, dead = 2)
        val health = TenantOutboxHealthCheck(
            tenantOutboxStore = store,
            pendingThreshold = 100,
            deadThreshold = 0,
            enabled = true,
        )

        val response = health.call()

        assertThat(response.status).isEqualTo(org.eclipse.microprofile.health.HealthCheckResponse.Status.DOWN)
    }

    @Test
    fun `health is up when outbox disabled`() {
        val store = ThresholdAwareStore(pending = 999, dead = 20)
        val health = TenantOutboxHealthCheck(
            tenantOutboxStore = store,
            pendingThreshold = 10,
            deadThreshold = 0,
            enabled = false,
        )

        val response = health.call()

        assertThat(response.status).isEqualTo(org.eclipse.microprofile.health.HealthCheckResponse.Status.UP)
    }

    private class ThresholdAwareStore(
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
