package com.chiroerp.tenancy.core.domain.event

import com.chiroerp.shared.types.events.DomainEvent
import com.chiroerp.shared.types.events.EventVersion
import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

data class TenantCreatedEvent(
    val tenantId: TenantId,
    val name: String,
    val domain: String,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: EventVersion = EventVersion(1),
) : DomainEvent {
    override val eventType: String = "tenancy.tenant.created"
}
