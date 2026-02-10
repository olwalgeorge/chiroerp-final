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
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.outbox.max-attempts", defaultValue = "10")
    private val maxAttempts: Int,
) {
    private val logger = Logger.getLogger(TenantOutboxRelayService::class.java)

    @Transactional
    fun relayBatch(now: Instant = Instant.now()): Int {
        val entries = tenantOutboxStore.claimPending(limit = batchSize, now = now)
        if (entries.isEmpty()) {
            return 0
        }

        entries.forEach { entry ->
            try {
                tenantOutboxDispatcher.dispatch(entry)
                tenantOutboxStore.markPublished(entry.eventId, now)
                logger.debugf("Outbox event %s published successfully", entry.eventId)
            } catch (ex: Exception) {
                handleDispatchFailure(entry, ex, now)
            }
        }

        return entries.size
    }

    private fun handleDispatchFailure(entry: TenantOutboxEntry, ex: Exception, now: Instant) {
        val attempts = entry.publishAttempts + 1
        val error = ex.message?.take(1000) ?: "Unknown dispatch error"

        if (attempts >= maxAttempts) {
            tenantOutboxStore.markDead(
                eventId = entry.eventId,
                attempts = attempts,
                lastError = error,
            )
            logger.errorf(
                ex,
                "Outbox event %s marked DEAD after %d attempts; requires manual intervention",
                entry.eventId,
                attempts,
            )
        } else {
            val nextAttemptAt = now.plusSeconds(computeBackoffSeconds(attempts))
            tenantOutboxStore.markFailed(
                eventId = entry.eventId,
                attempts = attempts,
                nextAttemptAt = nextAttemptAt,
                lastError = error,
            )
            logger.warnf(
                ex,
                "Outbox dispatch failed for event %s (attempt %d/%d); retry at %s",
                entry.eventId,
                attempts,
                maxAttempts,
                nextAttemptAt,
            )
        }
    }

    internal fun computeBackoffSeconds(attempts: Int): Long {
        val boundedShift = (attempts - 1).coerceAtLeast(0).coerceAtMost(30)
        val exponential = 1L shl boundedShift
        return exponential.coerceAtMost(maxBackoffSeconds)
    }
}
