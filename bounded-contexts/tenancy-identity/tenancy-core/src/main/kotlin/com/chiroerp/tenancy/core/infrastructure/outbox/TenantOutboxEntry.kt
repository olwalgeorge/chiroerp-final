package com.chiroerp.tenancy.core.infrastructure.outbox

import java.time.Instant
import java.util.UUID

/**
 * Status of an outbox entry.
 */
enum class OutboxStatus {
    /** Awaiting dispatch or retry. */
    PENDING,

    /** Successfully published to message broker. */
    PUBLISHED,

    /** Max attempts exceeded; requires manual intervention or DLQ processing. */
    DEAD,
}

data class TenantOutboxEntry(
    val eventId: UUID,
    val tenantId: UUID,
    val aggregateType: String,
    val aggregateId: UUID,
    val eventType: String,
    val payload: String,
    val occurredAt: Instant,
    val createdAt: Instant,
    val status: OutboxStatus = OutboxStatus.PENDING,
    val publishedAt: Instant? = null,
    val publishAttempts: Int = 0,
    val nextAttemptAt: Instant = createdAt,
    val lastError: String? = null,
)
