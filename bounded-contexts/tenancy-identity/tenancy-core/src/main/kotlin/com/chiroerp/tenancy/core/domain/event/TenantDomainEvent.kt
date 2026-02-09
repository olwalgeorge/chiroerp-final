package com.chiroerp.tenancy.core.domain.event

import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

sealed interface TenantDomainEvent {
    val eventId: UUID
    val tenantId: TenantId
    val occurredAt: Instant
    val eventType: String
}
