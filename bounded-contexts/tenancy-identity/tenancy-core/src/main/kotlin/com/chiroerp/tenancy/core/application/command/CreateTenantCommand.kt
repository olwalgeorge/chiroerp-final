package com.chiroerp.tenancy.core.application.command

import com.chiroerp.tenancy.shared.TenantTier

data class CreateTenantCommand(
    val name: String,
    val domain: String,
    val tier: TenantTier = TenantTier.STANDARD,
    val dataResidencyCountry: String = "US",
    val locale: String = "en_US",
    val timezone: String = "UTC",
    val currency: String = "USD",
    val featureFlags: Map<String, Boolean> = emptyMap(),
    val customConfiguration: Map<String, String> = emptyMap(),
)
