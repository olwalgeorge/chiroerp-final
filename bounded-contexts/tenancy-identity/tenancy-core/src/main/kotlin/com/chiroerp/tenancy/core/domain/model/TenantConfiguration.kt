package com.chiroerp.tenancy.core.domain.model

data class TenantConfiguration(
    val settings: TenantSettings = TenantSettings(),
    val quota: TenantQuota = TenantQuota(),
    val subscription: TenantSubscription = TenantSubscription(),
)
