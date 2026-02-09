package com.chiroerp.tenancy.core.domain.port

import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantStatus

interface TenantRepository {
    fun save(tenant: Tenant): Tenant

    fun findById(id: TenantId): Tenant?

    fun findByDomain(domain: String): Tenant?

    fun findAll(limit: Int, offset: Int, status: TenantStatus? = null): List<Tenant>
}
