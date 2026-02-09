package com.chiroerp.tenancy.core.domain.model

import com.chiroerp.tenancy.core.domain.event.TenantActivatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantCreatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantDomainEvent
import com.chiroerp.tenancy.core.domain.event.TenantSettingsUpdatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantSuspendedEvent
import com.chiroerp.tenancy.core.domain.event.TenantTerminatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantTierChangedEvent
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantStatus
import com.chiroerp.tenancy.shared.TenantTier
import java.time.Instant

class Tenant private constructor(
    val id: TenantId,
    var name: String,
    var domain: String,
    var tier: TenantTier,
    var status: TenantStatus,
    val dataResidency: DataResidency,
    var settings: TenantSettings,
    val createdAt: Instant,
    var updatedAt: Instant,
) {
    private val pendingEvents = mutableListOf<TenantDomainEvent>()

    fun updateSettings(newSettings: TenantSettings, occurredAt: Instant = Instant.now()) {
        settings = newSettings
        updatedAt = occurredAt
        record(
            TenantSettingsUpdatedEvent(
                tenantId = id,
                settings = newSettings,
                occurredAt = occurredAt,
            ),
        )
    }

    fun activate(occurredAt: Instant = Instant.now()) {
        if (status == TenantStatus.TERMINATED) {
            throw IllegalStateException("Terminated tenant cannot be activated")
        }
        if (status == TenantStatus.ACTIVE) {
            return
        }
        if (status != TenantStatus.PENDING && status != TenantStatus.SUSPENDED) {
            throw IllegalStateException("Tenant can only be activated from PENDING or SUSPENDED status, current: $status")
        }
        status = TenantStatus.ACTIVE
        updatedAt = occurredAt
        record(TenantActivatedEvent(id, occurredAt))
    }

    fun suspend(reason: String, occurredAt: Instant = Instant.now()) {
        require(reason.isNotBlank()) { "Suspension reason is required" }
        if (status == TenantStatus.TERMINATED) {
            throw IllegalStateException("Terminated tenant cannot be suspended")
        }
        if (status == TenantStatus.PENDING) {
            throw IllegalStateException("Pending tenant cannot be suspended - activate first")
        }
        if (status == TenantStatus.SUSPENDED) {
            return
        }
        status = TenantStatus.SUSPENDED
        updatedAt = occurredAt
        record(TenantSuspendedEvent(id, reason, occurredAt))
    }

    fun terminate(reason: String, occurredAt: Instant = Instant.now()) {
        require(reason.isNotBlank()) { "Termination reason is required" }
        if (status == TenantStatus.TERMINATED) {
            return
        }
        status = TenantStatus.TERMINATED
        updatedAt = occurredAt
        record(TenantTerminatedEvent(id, reason, occurredAt))
    }

    fun changeTier(newTier: TenantTier, occurredAt: Instant = Instant.now()) {
        if (tier == newTier) {
            return
        }
        val previous = tier
        tier = newTier
        updatedAt = occurredAt
        record(TenantTierChangedEvent(id, previous, newTier, occurredAt))
    }

    fun pullDomainEvents(): List<TenantDomainEvent> {
        val events = pendingEvents.toList()
        pendingEvents.clear()
        return events
    }

    private fun record(event: TenantDomainEvent) {
        pendingEvents += event
    }

    companion object {
        fun create(
            name: String,
            domain: String,
            tier: TenantTier,
            dataResidency: DataResidency,
            settings: TenantSettings = TenantSettings(),
            now: Instant = Instant.now(),
            id: TenantId = TenantId.random(),
        ): Tenant {
            require(name.isNotBlank()) { "Tenant name is required" }
            val normalizedDomain = normalizeDomain(domain)

            val tenant = Tenant(
                id = id,
                name = name.trim(),
                domain = normalizedDomain,
                tier = tier,
                status = TenantStatus.PENDING,
                dataResidency = dataResidency,
                settings = settings,
                createdAt = now,
                updatedAt = now,
            )

            tenant.record(
                TenantCreatedEvent(
                    tenantId = tenant.id,
                    tenantName = tenant.name,
                    domain = tenant.domain,
                    tier = tenant.tier,
                    occurredAt = now,
                ),
            )
            return tenant
        }

        fun rehydrate(
            id: TenantId,
            name: String,
            domain: String,
            tier: TenantTier,
            status: TenantStatus,
            dataResidency: DataResidency,
            settings: TenantSettings,
            createdAt: Instant,
            updatedAt: Instant,
        ): Tenant = Tenant(
            id = id,
            name = name,
            domain = domain,
            tier = tier,
            status = status,
            dataResidency = dataResidency,
            settings = settings,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        private fun normalizeDomain(domain: String): String {
            val normalized = domain.trim().lowercase()
            require(DOMAIN_REGEX.matches(normalized)) {
                "Domain must contain only lowercase letters, digits, dots, and hyphens"
            }
            return normalized
        }

        private val DOMAIN_REGEX = Regex("^[a-z0-9.-]+$")
    }
}
