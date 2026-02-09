package com.chiroerp.tenancy.core.application.command

import com.chiroerp.tenancy.shared.TenantId

data class UpdateTenantSettingsCommand(
    val tenantId: TenantId,
    val locale: String? = null,
    val timezone: String? = null,
    val currency: String? = null,
    val featureFlags: Map<String, Boolean>? = null,
    val customConfiguration: Map<String, String>? = null,
)
