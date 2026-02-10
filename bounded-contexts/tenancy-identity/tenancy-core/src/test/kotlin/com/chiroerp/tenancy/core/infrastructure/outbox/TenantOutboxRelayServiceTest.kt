package com.chiroerp.tenancy.core.infrastructure.outbox

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TenantOutboxRelayServiceTest {
    @Test
    fun `relay marks event published on successful dispatch`() {
        val store = InMemoryOutboxStore()
        val dispatcher = RecordingDispatcher()
        val meterRegistry = SimpleMeterRegistry()
        val service = TenantOutboxRelayService(
            tenantOutboxStore = store,
            tenantOutboxDispatcher = dispatcher,
            meterRegistry = meterRegistry,
            batchSize = 100,
            maxBackoffSeconds = 60,
            maxAttempts = 10,
        )

        val now = Instant.parse("2026-02-09T12:00:00Z")
        val entry = sampleEntry(createdAt = now.minusSeconds(10), nextAttemptAt = now.minusSeconds(1))
        store.save(listOf(entry))

        val processed = service.relayBatch(now)

        assertThat(processed).isEqualTo(1)
        assertThat(dispatcher.dispatched).extracting<UUID> { it.eventId }.contains(entry.eventId)
        assertThat(store.entries[entry.eventId]!!.status).isEqualTo(OutboxStatus.PUBLISHED)
        assertThat(store.entries[entry.eventId]!!.publishedAt).isEqualTo(now)
        assertThat(store.entries[entry.eventId]!!.publishAttempts).isEqualTo(0)
        assertThat(meterRegistry.counter("chiroerp.tenancy.outbox.relay.dispatched").count()).isEqualTo(1.0)
        assertThat(meterRegistry.counter("chiroerp.tenancy.outbox.relay.failed").count()).isEqualTo(0.0)
    }

    @Test
    fun `relay records failed attempt and retries with exponential backoff`() {
        val store = InMemoryOutboxStore()
        val dispatcher = FailingOnceDispatcher()
        val meterRegistry = SimpleMeterRegistry()
        val service = TenantOutboxRelayService(
            tenantOutboxStore = store,
            tenantOutboxDispatcher = dispatcher,
            meterRegistry = meterRegistry,
            batchSize = 100,
            maxBackoffSeconds = 60,
            maxAttempts = 10,
        )

        val firstAttempt = Instant.parse("2026-02-09T12:00:00Z")
        val entry = sampleEntry(createdAt = firstAttempt.minusSeconds(10), nextAttemptAt = firstAttempt.minusSeconds(1))
        store.save(listOf(entry))

        val firstProcessed = service.relayBatch(firstAttempt)
        val afterFailure = store.entries[entry.eventId]!!

        assertThat(firstProcessed).isEqualTo(1)
        assertThat(afterFailure.status).isEqualTo(OutboxStatus.PENDING)
        assertThat(afterFailure.publishedAt).isNull()
        assertThat(afterFailure.publishAttempts).isEqualTo(1)
        assertThat(afterFailure.nextAttemptAt).isEqualTo(firstAttempt.plusSeconds(1))

        val secondAttempt = firstAttempt.plusSeconds(1)
        val secondProcessed = service.relayBatch(secondAttempt)
        val afterSuccess = store.entries[entry.eventId]!!

        assertThat(secondProcessed).isEqualTo(1)
        assertThat(afterSuccess.status).isEqualTo(OutboxStatus.PUBLISHED)
        assertThat(afterSuccess.publishedAt).isEqualTo(secondAttempt)
        assertThat(dispatcher.calls).isEqualTo(2)
        assertThat(meterRegistry.counter("chiroerp.tenancy.outbox.relay.failed").count()).isEqualTo(1.0)
        assertThat(meterRegistry.counter("chiroerp.tenancy.outbox.relay.dispatched").count()).isEqualTo(1.0)
    }

    @Test
    fun `relay marks event dead after max attempts exceeded`() {
        val store = InMemoryOutboxStore()
        val dispatcher = AlwaysFailingDispatcher()
        val maxAttempts = 3
        val meterRegistry = SimpleMeterRegistry()
        val service = TenantOutboxRelayService(
            tenantOutboxStore = store,
            tenantOutboxDispatcher = dispatcher,
            meterRegistry = meterRegistry,
            batchSize = 100,
            maxBackoffSeconds = 60,
            maxAttempts = maxAttempts,
        )

        val now = Instant.parse("2026-02-09T12:00:00Z")
        val entry = sampleEntry(createdAt = now.minusSeconds(10), nextAttemptAt = now.minusSeconds(1))
        store.save(listOf(entry))

        // Run relay until max attempts
        repeat(maxAttempts) { attempt ->
            val attemptTime = now.plusSeconds((attempt * 10).toLong())
            // Update nextAttemptAt to allow retry
            if (attempt > 0) {
                store.entries[entry.eventId] = store.entries[entry.eventId]!!.copy(
                    nextAttemptAt = attemptTime.minusSeconds(1),
                )
            }
            service.relayBatch(attemptTime)
        }

        val afterMaxAttempts = store.entries[entry.eventId]!!

        assertThat(afterMaxAttempts.status).isEqualTo(OutboxStatus.DEAD)
        assertThat(afterMaxAttempts.lastError).isEqualTo("Intentional failure")
        assertThat(dispatcher.calls).isEqualTo(maxAttempts)
        assertThat(meterRegistry.counter("chiroerp.tenancy.outbox.relay.dead").count()).isEqualTo(1.0)
        assertThat(meterRegistry.counter("chiroerp.tenancy.outbox.relay.failed").count()).isEqualTo(maxAttempts.toDouble())
    }

    @Test
    fun `dead entries are not fetched by claimPending`() {
        val store = InMemoryOutboxStore()
        val now = Instant.parse("2026-02-09T12:00:00Z")

        val deadEntry = sampleEntry(
            createdAt = now.minusSeconds(10),
            nextAttemptAt = now.minusSeconds(1),
        ).copy(status = OutboxStatus.DEAD)
        store.entries[deadEntry.eventId] = deadEntry

        val pending = store.claimPending(limit = 100, now = now)

        assertThat(pending).isEmpty()
    }

    private fun sampleEntry(
        createdAt: Instant,
        nextAttemptAt: Instant,
        eventId: UUID = UUID.randomUUID(),
    ): TenantOutboxEntry = TenantOutboxEntry(
        eventId = eventId,
        tenantId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
        aggregateType = "Tenant",
        aggregateId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
        eventType = "TenantCreated",
        payload = """{"eventType":"TenantCreated"}""",
        occurredAt = createdAt,
        createdAt = createdAt,
        status = OutboxStatus.PENDING,
        nextAttemptAt = nextAttemptAt,
    )

    private class InMemoryOutboxStore : TenantOutboxStore {
        val entries = linkedMapOf<UUID, TenantOutboxEntry>()

        override fun save(entries: List<TenantOutboxEntry>) {
            entries.forEach { entry ->
                this.entries.putIfAbsent(entry.eventId, entry)
            }
        }

        override fun fetchPending(limit: Int, now: Instant): List<TenantOutboxEntry> =
            claimPending(limit, now)

        override fun claimPending(limit: Int, now: Instant): List<TenantOutboxEntry> = entries.values
            .filter { it.status == OutboxStatus.PENDING && !it.nextAttemptAt.isAfter(now) }
            .sortedBy { it.createdAt }
            .take(limit)

        override fun markPublished(eventId: UUID, publishedAt: Instant) {
            val existing = entries[eventId] ?: return
            entries[eventId] = existing.copy(
                status = OutboxStatus.PUBLISHED,
                publishedAt = publishedAt,
                lastError = null,
            )
        }

        override fun markFailed(eventId: UUID, attempts: Int, nextAttemptAt: Instant, lastError: String?) {
            val existing = entries[eventId] ?: return
            entries[eventId] = existing.copy(
                publishAttempts = attempts,
                nextAttemptAt = nextAttemptAt,
                lastError = lastError,
            )
        }

        override fun markDead(eventId: UUID, attempts: Int, lastError: String?) {
            val existing = entries[eventId] ?: return
            entries[eventId] = existing.copy(
                status = OutboxStatus.DEAD,
                publishAttempts = attempts,
                lastError = lastError,
            )
        }

        override fun countPending(): Long = entries.values.count { it.status == OutboxStatus.PENDING }.toLong()

        override fun countDead(): Long = entries.values.count { it.status == OutboxStatus.DEAD }.toLong()
    }

    private class RecordingDispatcher : TenantOutboxDispatcher {
        val dispatched = mutableListOf<TenantOutboxEntry>()

        override fun dispatch(entry: TenantOutboxEntry) {
            dispatched += entry
        }
    }

    private class FailingOnceDispatcher : TenantOutboxDispatcher {
        var calls: Int = 0

        override fun dispatch(entry: TenantOutboxEntry) {
            calls += 1
            if (calls == 1) {
                throw IllegalStateException("broker unavailable")
            }
        }
    }

    private class AlwaysFailingDispatcher : TenantOutboxDispatcher {
        var calls: Int = 0

        override fun dispatch(entry: TenantOutboxEntry) {
            calls += 1
            throw IllegalStateException("Intentional failure")
        }
    }
}
