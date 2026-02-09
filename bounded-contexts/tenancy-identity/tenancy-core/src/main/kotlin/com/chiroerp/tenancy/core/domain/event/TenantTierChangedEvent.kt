package com.chiroerp.tenancy.core.domain.event

import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantTier
import java.time.Instant
import java.util.UUID

data class TenantTierChangedEvent(
    override val tenantId: TenantId,
    val previousTier: TenantTier,
    val newTier: TenantTier,
    override val occurredAt: Instant,
    override val eventId: UUID = UUID.randomUUID(),
) : TenantDomainEvent {
    override val eventType: String = "TenantTierChanged"
}
