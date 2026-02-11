package com.chiroerp.identity.core.infrastructure.outbox

import io.micrometer.core.instrument.MeterRegistry
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.Instant

@ApplicationScoped
class UserOutboxRelayService(
    private val userOutboxStore: UserOutboxStore,
    private val userOutboxDispatcher: UserOutboxDispatcher,
    private val meterRegistry: MeterRegistry,
    @param:ConfigProperty(name = "chiroerp.messaging.identity-events.outbox.batch-size", defaultValue = "100")
    private val batchSize: Int,
    @param:ConfigProperty(name = "chiroerp.messaging.identity-events.outbox.max-backoff-seconds", defaultValue = "60")
    private val maxBackoffSeconds: Long,
    @param:ConfigProperty(name = "chiroerp.messaging.identity-events.outbox.max-attempts", defaultValue = "10")
    private val maxAttempts: Int,
) {
    private val logger = Logger.getLogger(UserOutboxRelayService::class.java)
    private val dispatchedCounter = meterRegistry.counter("chiroerp.identity.outbox.relay.dispatched")
    private val failedCounter = meterRegistry.counter("chiroerp.identity.outbox.relay.failed")
    private val deadCounter = meterRegistry.counter("chiroerp.identity.outbox.relay.dead")

    @Transactional
    fun relayBatch(now: Instant = Instant.now()): Int {
        val entries = userOutboxStore.claimPending(limit = batchSize, now = now)
        if (entries.isEmpty()) {
            return 0
        }

        entries.forEach { entry ->
            try {
                userOutboxDispatcher.dispatch(entry)
                userOutboxStore.markPublished(entry.eventId, now)
                dispatchedCounter.increment()
                logger.debugf("Identity outbox event %s published successfully", entry.eventId)
            } catch (ex: Exception) {
                handleDispatchFailure(entry, ex, now)
            }
        }

        return entries.size
    }

    private fun handleDispatchFailure(entry: UserOutboxEntry, ex: Exception, now: Instant) {
        val attempts = entry.publishAttempts + 1
        val error = ex.message?.take(1000) ?: "Unknown dispatch error"

        if (attempts >= maxAttempts) {
            userOutboxStore.markDead(
                eventId = entry.eventId,
                attempts = attempts,
                lastError = error,
            )
            deadCounter.increment()
            logger.errorf(
                ex,
                "Identity outbox event %s marked DEAD after %d attempts; requires manual intervention",
                entry.eventId,
                attempts,
            )
        } else {
            val nextAttemptAt = now.plusSeconds(computeBackoffSeconds(attempts))
            userOutboxStore.markFailed(
                eventId = entry.eventId,
                attempts = attempts,
                nextAttemptAt = nextAttemptAt,
                lastError = error,
            )
            logger.warnf(
                ex,
                "Identity outbox dispatch failed for event %s (attempt %d/%d); retry at %s",
                entry.eventId,
                attempts,
                maxAttempts,
                nextAttemptAt,
            )
        }
        failedCounter.increment()
    }

    internal fun computeBackoffSeconds(attempts: Int): Long {
        val boundedShift = (attempts - 1).coerceAtLeast(0).coerceAtMost(30)
        val exponential = 1L shl boundedShift
        return exponential.coerceAtMost(maxBackoffSeconds)
    }
}
