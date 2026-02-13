package com.chiroerp.identity.core.infrastructure.messaging

import java.time.Instant
import java.util.UUID

interface TenantEventConsumptionStore {
    fun isProcessed(eventId: UUID): Boolean

    fun markProcessed(
        eventId: UUID,
        tenantId: UUID,
        eventType: String,
        occurredAt: Instant?,
    ): Boolean
}

