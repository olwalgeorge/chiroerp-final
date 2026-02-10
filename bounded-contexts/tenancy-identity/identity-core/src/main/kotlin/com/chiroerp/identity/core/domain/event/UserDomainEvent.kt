package com.chiroerp.identity.core.domain.event

import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

sealed interface UserDomainEvent {
    val eventId: UUID
    val userId: UserId
    val tenantId: TenantId
    val occurredAt: Instant
    val eventType: String
}
