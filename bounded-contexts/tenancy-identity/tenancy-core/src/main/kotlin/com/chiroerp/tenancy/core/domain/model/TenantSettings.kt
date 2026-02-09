package com.chiroerp.tenancy.core.domain.model

data class TenantSettings(
    val locale: String = "en_US",
    val timezone: String = "UTC",
    val currency: String = "USD",
    val featureFlags: Map<String, Boolean> = emptyMap(),
    val customConfiguration: Map<String, String> = emptyMap(),
) {
    init {
        require(locale.isNotBlank()) { "Locale is required" }
        require(timezone.isNotBlank()) { "Timezone is required" }
        require(currency.isNotBlank()) { "Currency is required" }
    }
}
