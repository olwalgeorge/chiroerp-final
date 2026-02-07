package com.chiroerp.tenancy.core.domain.model

import com.chiroerp.tenancy.shared.TenantTier

data class TenantSubscription(
    val tier: TenantTier,
    val maxUsers: Int,
    val maxCompanyCodes: Int,
) {
    init {
        require(maxUsers > 0) { "maxUsers must be positive" }
        require(maxCompanyCodes > 0) { "maxCompanyCodes must be positive" }
    }
}
