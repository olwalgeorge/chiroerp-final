package com.chiroerp.identity.core.application.exception

import com.chiroerp.tenancy.shared.TenantId

class UserAlreadyExistsException(
    val tenantId: TenantId,
    val email: String,
) : RuntimeException("User with email $email already exists in tenant ${tenantId.value}")
