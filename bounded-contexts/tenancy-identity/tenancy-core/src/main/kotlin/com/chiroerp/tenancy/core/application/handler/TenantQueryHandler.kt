package com.chiroerp.tenancy.core.application.handler

import com.chiroerp.shared.types.results.Result
import com.chiroerp.tenancy.core.application.query.GetTenantByDomainQuery
import com.chiroerp.tenancy.core.application.query.GetTenantQuery
import com.chiroerp.tenancy.core.application.query.ListTenantsQuery
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.port.TenantRepository

class TenantQueryHandler(
    private val tenantRepository: TenantRepository,
) {
    fun handle(query: GetTenantQuery): Result<Tenant?> = Result.success(
        tenantRepository.findById(query.tenantId),
    )

    fun handle(query: GetTenantByDomainQuery): Result<Tenant?> = Result.success(
        tenantRepository.findByDomain(query.domain),
    )

    fun handle(query: ListTenantsQuery): Result<List<Tenant>> = Result.success(
        tenantRepository.listByStatus(query.status),
    )
}
