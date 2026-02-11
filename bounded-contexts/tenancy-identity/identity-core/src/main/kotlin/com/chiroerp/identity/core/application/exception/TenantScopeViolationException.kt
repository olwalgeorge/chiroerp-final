package com.chiroerp.identity.core.application.exception

import com.chiroerp.tenancy.shared.TenantId

class TenantScopeViolationException(
    val tenantId: TenantId,
) : RuntimeException("Operation not allowed for tenant ${tenantId.value}")
