package com.chiroerp.identity.core.infrastructure.outbox

import java.time.Instant
import java.util.UUID

interface UserOutboxStore {
    fun save(entries: List<UserOutboxEntry>)

    fun fetchPending(limit: Int, now: Instant = Instant.now()): List<UserOutboxEntry>

    fun claimPending(limit: Int, now: Instant = Instant.now()): List<UserOutboxEntry>

    fun markPublished(eventId: UUID, publishedAt: Instant)

    fun markFailed(
        eventId: UUID,
        attempts: Int,
        nextAttemptAt: Instant,
        lastError: String?,
    )

    fun markDead(
        eventId: UUID,
        attempts: Int,
        lastError: String?,
    )

    fun countPending(): Long

    fun countDead(): Long
}
