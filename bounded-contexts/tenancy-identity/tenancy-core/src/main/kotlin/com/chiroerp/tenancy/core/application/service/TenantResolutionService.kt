package com.chiroerp.tenancy.core.application.service

import com.chiroerp.tenancy.core.domain.port.TenantRepository
import com.chiroerp.tenancy.shared.TenantId

class TenantResolutionService(
    private val tenantRepository: TenantRepository,
) {
    fun resolveByDomain(domain: String): TenantId? = tenantRepository
        .findByDomain(domain)
        ?.id
}
