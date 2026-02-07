package com.chiroerp.tenancy.core.application.query

import com.chiroerp.shared.types.cqrs.Query
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.TenantStatus

data class ListTenantsQuery(val status: TenantStatus? = null) : Query<List<Tenant>>
