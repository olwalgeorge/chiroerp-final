package com.chiroerp.tenancy.shared

data class TenantContext(
    val tenantId: TenantId,
    val tier: TenantTier,
    val isolationLevel: IsolationLevel,
    val domain: String? = null,
)
