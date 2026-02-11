package com.chiroerp.identity.core.infrastructure.observability

import com.chiroerp.identity.core.infrastructure.outbox.UserOutboxEntry
import com.chiroerp.identity.core.infrastructure.outbox.UserOutboxStore
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.microprofile.health.HealthCheckResponse
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class UserOutboxHealthCheckTest {
    @Test
    fun `health is up when counts under thresholds`() {
        val store = ThresholdAwareStore(pending = 10, dead = 0)
        val health = UserOutboxHealthCheck(
            userOutboxStore = store,
            pendingThreshold = 100,
            deadThreshold = 0,
            enabled = true,
        )

        val response = health.call()

        assertThat(response.status).isEqualTo(HealthCheckResponse.Status.UP)
    }

    @Test
    fun `health is down when dead entries exceed threshold`() {
        val store = ThresholdAwareStore(pending = 1, dead = 2)
        val health = UserOutboxHealthCheck(
            userOutboxStore = store,
            pendingThreshold = 100,
            deadThreshold = 0,
            enabled = true,
        )

        val response = health.call()

        assertThat(response.status).isEqualTo(HealthCheckResponse.Status.DOWN)
    }

    @Test
    fun `health is up when outbox disabled`() {
        val store = ThresholdAwareStore(pending = 999, dead = 20)
        val health = UserOutboxHealthCheck(
            userOutboxStore = store,
            pendingThreshold = 10,
            deadThreshold = 0,
            enabled = false,
        )

        val response = health.call()

        assertThat(response.status).isEqualTo(HealthCheckResponse.Status.UP)
    }

    private class ThresholdAwareStore(
        private val pending: Long,
        private val dead: Long,
    ) : UserOutboxStore {
        override fun save(entries: List<UserOutboxEntry>) = Unit
        override fun fetchPending(limit: Int, now: Instant): List<UserOutboxEntry> = emptyList()
        override fun claimPending(limit: Int, now: Instant): List<UserOutboxEntry> = emptyList()
        override fun markPublished(eventId: UUID, publishedAt: Instant) = Unit
        override fun markFailed(eventId: UUID, attempts: Int, nextAttemptAt: Instant, lastError: String?) = Unit
        override fun markDead(eventId: UUID, attempts: Int, lastError: String?) = Unit
        override fun countPending(): Long = pending
        override fun countDead(): Long = dead
    }
}
