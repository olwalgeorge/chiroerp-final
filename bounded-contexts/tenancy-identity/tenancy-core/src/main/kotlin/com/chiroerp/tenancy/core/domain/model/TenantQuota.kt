package com.chiroerp.tenancy.core.domain.model

data class TenantQuota(
    val maxStorageMb: Long,
    val maxApiRequestsPerMinute: Int,
) {
    init {
        require(maxStorageMb > 0) { "maxStorageMb must be positive" }
        require(maxApiRequestsPerMinute > 0) { "maxApiRequestsPerMinute must be positive" }
    }
}
