package com.chiroerp.identity.core.domain.event

import com.chiroerp.identity.core.domain.model.MfaMethod
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

data class MfaEnabledEvent(
    override val userId: UserId,
    override val tenantId: TenantId,
    val methods: Set<MfaMethod>,
    override val occurredAt: Instant,
    override val eventId: UUID = UUID.randomUUID(),
) : UserDomainEvent {
    override val eventType: String = "MfaEnabled"
}
