package com.chiroerp.identity.core.infrastructure.outbox

import java.time.Instant
import java.util.UUID

enum class UserOutboxStatus {
    PENDING,
    PUBLISHED,
    DEAD,
}

data class UserOutboxEntry(
    val eventId: UUID,
    val tenantId: UUID,
    val aggregateType: String,
    val aggregateId: UUID,
    val eventType: String,
    val payload: String,
    val occurredAt: Instant,
    val createdAt: Instant,
    val status: UserOutboxStatus = UserOutboxStatus.PENDING,
    val publishedAt: Instant? = null,
    val publishAttempts: Int = 0,
    val nextAttemptAt: Instant = createdAt,
    val lastError: String? = null,
)
