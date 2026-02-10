package com.chiroerp.identity.core.domain.event

import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

data class UserLoggedOutEvent(
    override val userId: UserId,
    override val tenantId: TenantId,
    val sessionId: UUID,
    val reason: String?,
    override val occurredAt: Instant,
    override val eventId: UUID = UUID.randomUUID(),
) : UserDomainEvent {
    override val eventType: String = "UserLoggedOut"
}
