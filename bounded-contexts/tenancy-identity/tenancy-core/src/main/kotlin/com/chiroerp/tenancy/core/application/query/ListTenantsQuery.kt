package com.chiroerp.tenancy.core.application.query

import com.chiroerp.tenancy.shared.TenantStatus

data class ListTenantsQuery(
    val offset: Int = 0,
    val limit: Int = 50,
    val status: TenantStatus? = null,
)
