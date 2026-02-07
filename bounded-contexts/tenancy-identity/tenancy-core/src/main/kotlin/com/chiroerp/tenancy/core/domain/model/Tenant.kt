package com.chiroerp.tenancy.core.domain.model

import com.chiroerp.shared.types.aggregate.AggregateRoot
import com.chiroerp.tenancy.core.domain.event.TenantActivatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantCreatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantSettingsUpdatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantSuspendedEvent
import com.chiroerp.tenancy.core.domain.event.TenantTerminatedEvent
import com.chiroerp.tenancy.shared.IsolationLevel
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantStatus
import com.chiroerp.tenancy.shared.TenantTier

class Tenant private constructor(
    override val id: TenantId,
    val name: String,
    val domain: String,
    var status: TenantStatus,
    var tier: TenantTier,
    var isolationLevel: IsolationLevel,
    var settings: TenantSettings,
    var subscription: TenantSubscription,
    var quota: TenantQuota,
    var dataResidency: DataResidency,
) : AggregateRoot<TenantId>(id) {

    init {
        require(name.isNotBlank()) { "Tenant name cannot be blank" }
        require(domain.isNotBlank()) { "Tenant domain cannot be blank" }
    }

    fun activate() {
        if (status == TenantStatus.TERMINATED) {
            error("Cannot activate a terminated tenant")
        }
        status = TenantStatus.ACTIVE
        registerEvent(TenantActivatedEvent(tenantId = id))
    }

    fun suspend(reason: String) {
        require(reason.isNotBlank()) { "Suspension reason is required" }
        if (status == TenantStatus.TERMINATED) {
            error("Cannot suspend a terminated tenant")
        }
        status = TenantStatus.SUSPENDED
        registerEvent(TenantSuspendedEvent(tenantId = id, reason = reason))
    }

    fun terminate(reason: String) {
        require(reason.isNotBlank()) { "Termination reason is required" }
        status = TenantStatus.TERMINATED
        registerEvent(TenantTerminatedEvent(tenantId = id, reason = reason))
    }

    fun updateSettings(newSettings: TenantSettings) {
        settings = newSettings
        registerEvent(TenantSettingsUpdatedEvent(tenantId = id))
    }

    companion object {
        fun create(
            id: TenantId,
            name: String,
            domain: String,
            tier: TenantTier,
            isolationLevel: IsolationLevel,
            settings: TenantSettings,
            subscription: TenantSubscription,
            quota: TenantQuota,
            dataResidency: DataResidency,
        ): Tenant {
            val tenant = Tenant(
                id = id,
                name = name,
                domain = domain,
                status = TenantStatus.PROVISIONING,
                tier = tier,
                isolationLevel = isolationLevel,
                settings = settings,
                subscription = subscription,
                quota = quota,
                dataResidency = dataResidency,
            )
            tenant.registerEvent(TenantCreatedEvent(tenantId = id, name = name, domain = domain))
            return tenant
        }
    }
}
