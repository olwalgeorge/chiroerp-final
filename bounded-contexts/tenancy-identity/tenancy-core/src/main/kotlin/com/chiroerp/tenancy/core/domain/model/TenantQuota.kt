package com.chiroerp.tenancy.core.domain.model

data class TenantQuota(
    val maxUsers: Int = 100,
    val maxStorageGb: Int = 100,
)
