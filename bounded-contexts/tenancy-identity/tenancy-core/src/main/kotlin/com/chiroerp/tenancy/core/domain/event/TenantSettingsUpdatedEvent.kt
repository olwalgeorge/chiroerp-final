package com.chiroerp.tenancy.core.domain.event

import com.chiroerp.tenancy.core.domain.model.TenantSettings
import com.chiroerp.tenancy.shared.TenantId
import java.time.Instant
import java.util.UUID

data class TenantSettingsUpdatedEvent(
    override val tenantId: TenantId,
    val settings: TenantSettings,
    override val occurredAt: Instant,
    override val eventId: UUID = UUID.randomUUID(),
) : TenantDomainEvent {
    override val eventType: String = "TenantSettingsUpdated"
}
