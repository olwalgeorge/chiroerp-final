package com.chiroerp.tenancy.core.infrastructure.outbox

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.Instant

@ApplicationScoped
class TenantOutboxRelayService(
    private val tenantOutboxStore: TenantOutboxStore,
    private val tenantOutboxDispatcher: TenantOutboxDispatcher,
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.outbox.batch-size", defaultValue = "100")
    private val batchSize: Int,
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.outbox.max-backoff-seconds", defaultValue = "60")
    private val maxBackoffSeconds: Long,
) {
    private val logger = Logger.getLogger(TenantOutboxRelayService::class.java)

    @Transactional
    fun relayBatch(now: Instant = Instant.now()): Int {
        val entries = tenantOutboxStore.fetchPending(limit = batchSize, now = now)
        if (entries.isEmpty()) {
            return 0
        }

        entries.forEach { entry ->
            try {
                tenantOutboxDispatcher.dispatch(entry)
                tenantOutboxStore.markPublished(entry.eventId, now)
            } catch (ex: Exception) {
                val attempts = entry.publishAttempts + 1
                val nextAttemptAt = now.plusSeconds(computeBackoffSeconds(attempts))
                val error = ex.message?.take(1000) ?: "Unknown dispatch error"
                tenantOutboxStore.markFailed(
                    eventId = entry.eventId,
                    attempts = attempts,
                    nextAttemptAt = nextAttemptAt,
                    lastError = error,
                )
                logger.warnf(
                    ex,
                    "Outbox dispatch failed for event %s (attempt %d); retry at %s",
                    entry.eventId,
                    attempts,
                    nextAttemptAt,
                )
            }
        }

        return entries.size
    }

    internal fun computeBackoffSeconds(attempts: Int): Long {
        val boundedShift = (attempts - 1).coerceAtLeast(0).coerceAtMost(30)
        val exponential = 1L shl boundedShift
        return exponential.coerceAtMost(maxBackoffSeconds)
    }
}
