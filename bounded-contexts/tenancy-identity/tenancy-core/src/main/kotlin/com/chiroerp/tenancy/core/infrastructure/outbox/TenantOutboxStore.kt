package com.chiroerp.tenancy.core.infrastructure.outbox

import java.time.Instant
import java.util.UUID

interface TenantOutboxStore {
    fun save(entries: List<TenantOutboxEntry>)

    fun fetchPending(limit: Int, now: Instant = Instant.now()): List<TenantOutboxEntry>

    fun markPublished(eventId: UUID, publishedAt: Instant)

    fun markFailed(
        eventId: UUID,
        attempts: Int,
        nextAttemptAt: Instant,
        lastError: String?,
    )
}
