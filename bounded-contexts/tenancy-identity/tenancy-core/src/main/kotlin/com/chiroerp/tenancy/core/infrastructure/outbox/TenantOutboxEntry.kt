package com.chiroerp.tenancy.core.infrastructure.outbox

import java.time.Instant
import java.util.UUID

data class TenantOutboxEntry(
    val eventId: UUID,
    val tenantId: UUID,
    val aggregateType: String,
    val aggregateId: UUID,
    val eventType: String,
    val payload: String,
    val occurredAt: Instant,
    val createdAt: Instant,
    val publishedAt: Instant? = null,
    val publishAttempts: Int = 0,
    val nextAttemptAt: Instant = createdAt,
    val lastError: String? = null,
)
