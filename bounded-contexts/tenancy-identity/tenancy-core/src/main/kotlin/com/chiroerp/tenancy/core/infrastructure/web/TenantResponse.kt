package com.chiroerp.tenancy.core.infrastructure.api

import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.model.TenantSettings
import com.chiroerp.tenancy.shared.TenantStatus
import com.chiroerp.tenancy.shared.TenantTier
import java.time.Instant
import java.util.UUID

data class TenantResponse(
    val id: UUID,
    val name: String,
    val domain: String,
    val tier: TenantTier,
    val status: TenantStatus,
    val dataResidencyCountry: String,
    val settings: TenantSettingsResponse,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(tenant: Tenant): TenantResponse = TenantResponse(
            id = tenant.id.value,
            name = tenant.name,
            domain = tenant.domain,
            tier = tenant.tier,
            status = tenant.status,
            dataResidencyCountry = tenant.dataResidency.countryCode,
            settings = TenantSettingsResponse.from(tenant.settings),
            createdAt = tenant.createdAt,
            updatedAt = tenant.updatedAt,
        )
    }
}

data class TenantSettingsResponse(
    val locale: String,
    val timezone: String,
    val currency: String,
    val featureFlags: Map<String, Boolean>,
    val customConfiguration: Map<String, String>,
) {
    companion object {
        fun from(settings: TenantSettings): TenantSettingsResponse = TenantSettingsResponse(
            locale = settings.locale,
            timezone = settings.timezone,
            currency = settings.currency,
            featureFlags = settings.featureFlags,
            customConfiguration = settings.customConfiguration,
        )
    }
}

data class TenantListResponse(
    val items: List<TenantResponse>,
    val offset: Int,
    val limit: Int,
    val count: Int,
)
