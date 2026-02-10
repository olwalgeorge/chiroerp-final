package com.chiroerp.identity.core.domain.event

import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

data class UserActivatedEvent(
    override val userId: UserId,
    override val tenantId: TenantId,
    override val occurredAt: Instant,
    override val eventId: UUID = UUID.randomUUID(),
) : UserDomainEvent {
    override val eventType: String = "UserActivated"
}
