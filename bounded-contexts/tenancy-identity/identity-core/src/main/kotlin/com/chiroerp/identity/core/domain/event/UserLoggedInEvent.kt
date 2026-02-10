package com.chiroerp.identity.core.domain.event

import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

data class UserLoggedInEvent(
    override val userId: UserId,
    override val tenantId: TenantId,
    val sessionId: UUID,
    val ipAddress: String?,
    val userAgent: String?,
    val mfaVerified: Boolean,
    override val occurredAt: Instant,
    override val eventId: UUID = UUID.randomUUID(),
) : UserDomainEvent {
    override val eventType: String = "UserLoggedIn"
}
