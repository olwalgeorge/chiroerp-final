package com.chiroerp.tenancy.core.domain.model

data class TenantSettings(
    val defaultCurrency: String = "USD",
    val locale: String = "en-US",
    val timezone: String = "UTC",
    val enabledFeatures: Set<String> = emptySet(),
)
