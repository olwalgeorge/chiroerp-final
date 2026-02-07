package com.chiroerp.tenancy.core.application.query

import com.chiroerp.shared.types.cqrs.Query
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.TenantId

data class GetTenantQuery(val tenantId: TenantId) : Query<Tenant?>
