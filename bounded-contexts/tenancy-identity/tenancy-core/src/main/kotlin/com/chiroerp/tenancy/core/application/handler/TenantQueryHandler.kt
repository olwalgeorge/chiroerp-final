package com.chiroerp.tenancy.core.application.handler

import com.chiroerp.tenancy.core.application.query.GetTenantByDomainQuery
import com.chiroerp.tenancy.core.application.query.GetTenantQuery
import com.chiroerp.tenancy.core.application.query.ListTenantsQuery
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.port.TenantRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TenantQueryHandler(
    private val tenantRepository: TenantRepository,
) {
    fun handle(query: GetTenantQuery): Tenant? = tenantRepository.findById(query.tenantId)

    fun handle(query: GetTenantByDomainQuery): Tenant? =
        tenantRepository.findByDomain(query.normalizedDomain)

    fun handle(query: ListTenantsQuery): List<Tenant> {
        val limit = query.limit.coerceIn(1, 200)
        val offset = query.offset.coerceAtLeast(0)
        return tenantRepository.findAll(limit = limit, offset = offset, status = query.status)
    }
}
