package com.chiroerp.tenancy.core.domain.event

import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

data class TenantActivatedEvent(
    override val tenantId: TenantId,
    override val occurredAt: Instant,
    override val eventId: UUID = UUID.randomUUID(),
) : TenantDomainEvent {
    override val eventType: String = "TenantActivated"
}
