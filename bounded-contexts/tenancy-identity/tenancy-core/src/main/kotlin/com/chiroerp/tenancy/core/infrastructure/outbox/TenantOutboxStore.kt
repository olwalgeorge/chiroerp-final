package com.chiroerp.tenancy.core.infrastructure.outbox

import java.time.Instant
import java.util.UUID

interface TenantOutboxStore {
    fun save(entries: List<TenantOutboxEntry>)

    /**
     * Fetch pending entries without claiming. Used for read-only operations or non-concurrent environments.
     */
    fun fetchPending(limit: Int, now: Instant = Instant.now()): List<TenantOutboxEntry>

    /**
     * Atomically claim pending entries for exclusive processing using FOR UPDATE SKIP LOCKED.
     * Returns only entries that were successfully locked by this transaction.
     * Multiple workers can safely call this concurrently without duplicate dispatch.
     */
    fun claimPending(limit: Int, now: Instant = Instant.now()): List<TenantOutboxEntry>

    fun markPublished(eventId: UUID, publishedAt: Instant)

    fun markFailed(
        eventId: UUID,
        attempts: Int,
        nextAttemptAt: Instant,
        lastError: String?,
    )

    /**
     * Mark an entry as dead (terminal failure) after exceeding max attempts.
     * Dead entries require manual intervention or DLQ processing.
     */
    fun markDead(
        eventId: UUID,
        attempts: Int,
        lastError: String?,
    )

    /**
     * Count pending entries (for health checks/metrics).
     */
    fun countPending(): Long

    /**
     * Count dead entries (for alerting/metrics).
     */
    fun countDead(): Long
}
