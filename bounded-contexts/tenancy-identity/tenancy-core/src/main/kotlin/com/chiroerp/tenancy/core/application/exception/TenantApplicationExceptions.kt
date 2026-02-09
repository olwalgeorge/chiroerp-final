package com.chiroerp.tenancy.core.application.exception

import com.chiroerp.tenancy.shared.TenantId

class TenantAlreadyExistsException(domain: String) :
    RuntimeException("Tenant with domain '$domain' already exists")

class TenantNotFoundException(tenantId: TenantId) :
    RuntimeException("Tenant '${tenantId.value}' was not found")

class TenantLifecycleTransitionException(
    tenantId: TenantId,
    reason: String,
) : RuntimeException("Tenant '${tenantId.value}' lifecycle transition rejected: $reason")

class TenantProvisioningException(
    val tenantId: TenantId,
    reason: String,
) : RuntimeException("Tenant '${tenantId.value}' provisioning failed: $reason")
