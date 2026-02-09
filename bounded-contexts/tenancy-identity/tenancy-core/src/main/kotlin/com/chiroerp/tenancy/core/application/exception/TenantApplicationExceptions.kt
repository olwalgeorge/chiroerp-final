package com.chiroerp.tenancy.core.application.exception

import com.chiroerp.tenancy.shared.TenantId

class TenantAlreadyExistsException(domain: String) :
    RuntimeException("Tenant with domain '$domain' already exists")

class TenantNotFoundException(tenantId: TenantId) :
    RuntimeException("Tenant '${tenantId.value}' was not found")
