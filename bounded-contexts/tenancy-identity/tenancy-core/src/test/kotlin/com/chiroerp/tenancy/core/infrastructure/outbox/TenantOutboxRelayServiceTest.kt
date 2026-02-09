package com.chiroerp.tenancy.core.infrastructure.outbox

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TenantOutboxRelayServiceTest {
    @Test
    fun `relay marks event published on successful dispatch`() {
        val store = InMemoryOutboxStore()
        val dispatcher = RecordingDispatcher()
        val service = TenantOutboxRelayService(
            tenantOutboxStore = store,
            tenantOutboxDispatcher = dispatcher,
            batchSize = 100,
            maxBackoffSeconds = 60,
        )

        val now = Instant.parse("2026-02-09T12:00:00Z")
        val entry = sampleEntry(createdAt = now.minusSeconds(10), nextAttemptAt = now.minusSeconds(1))
        store.save(listOf(entry))

        val processed = service.relayBatch(now)

        assertThat(processed).isEqualTo(1)
        assertThat(dispatcher.dispatched).extracting<UUID> { it.eventId }.contains(entry.eventId)
        assertThat(store.entries[entry.eventId]!!.publishedAt).isEqualTo(now)
        assertThat(store.entries[entry.eventId]!!.publishAttempts).isEqualTo(0)
    }

    @Test
    fun `relay records failed attempt and retries with exponential backoff`() {
        val store = InMemoryOutboxStore()
        val dispatcher = FailingOnceDispatcher()
        val service = TenantOutboxRelayService(
            tenantOutboxStore = store,
            tenantOutboxDispatcher = dispatcher,
            batchSize = 100,
            maxBackoffSeconds = 60,
        )

        val firstAttempt = Instant.parse("2026-02-09T12:00:00Z")
        val entry = sampleEntry(createdAt = firstAttempt.minusSeconds(10), nextAttemptAt = firstAttempt.minusSeconds(1))
        store.save(listOf(entry))

        val firstProcessed = service.relayBatch(firstAttempt)
        val afterFailure = store.entries[entry.eventId]!!

        assertThat(firstProcessed).isEqualTo(1)
        assertThat(afterFailure.publishedAt).isNull()
        assertThat(afterFailure.publishAttempts).isEqualTo(1)
        assertThat(afterFailure.nextAttemptAt).isEqualTo(firstAttempt.plusSeconds(1))

        val secondAttempt = firstAttempt.plusSeconds(1)
        val secondProcessed = service.relayBatch(secondAttempt)
        val afterSuccess = store.entries[entry.eventId]!!

        assertThat(secondProcessed).isEqualTo(1)
        assertThat(afterSuccess.publishedAt).isEqualTo(secondAttempt)
        assertThat(dispatcher.calls).isEqualTo(2)
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
        nextAttemptAt = nextAttemptAt,
    )

    private class InMemoryOutboxStore : TenantOutboxStore {
        val entries = linkedMapOf<UUID, TenantOutboxEntry>()

        override fun save(entries: List<TenantOutboxEntry>) {
            entries.forEach { entry ->
                this.entries.putIfAbsent(entry.eventId, entry)
            }
        }

        override fun fetchPending(limit: Int, now: Instant): List<TenantOutboxEntry> = entries.values
            .filter { it.publishedAt == null && !it.nextAttemptAt.isAfter(now) }
            .sortedBy { it.createdAt }
            .take(limit)

        override fun markPublished(eventId: UUID, publishedAt: Instant) {
            val existing = entries[eventId] ?: return
            entries[eventId] = existing.copy(publishedAt = publishedAt, lastError = null)
        }

        override fun markFailed(eventId: UUID, attempts: Int, nextAttemptAt: Instant, lastError: String?) {
            val existing = entries[eventId] ?: return
            entries[eventId] = existing.copy(
                publishAttempts = attempts,
                nextAttemptAt = nextAttemptAt,
                lastError = lastError,
            )
        }
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
}
