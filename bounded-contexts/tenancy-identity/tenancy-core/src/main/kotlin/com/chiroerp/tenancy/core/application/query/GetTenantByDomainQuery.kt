package com.chiroerp.tenancy.core.application.query

import com.chiroerp.shared.types.cqrs.Query
import com.chiroerp.tenancy.core.domain.model.Tenant

data class GetTenantByDomainQuery(val domain: String) : Query<Tenant?>
