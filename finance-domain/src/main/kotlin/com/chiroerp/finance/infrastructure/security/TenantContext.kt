package com.chiroerp.finance.infrastructure.security

import jakarta.enterprise.context.RequestScoped
import java.util.UUID

/**
 * Holds the tenant identifier for the current request.
 * Must be populated by an authentication filter before any business logic executes.
 */
@RequestScoped
class TenantContext {
    private var tenantId: UUID? = null

    fun setTenantId(id: UUID) {
        this.tenantId = id
    }

    fun getTenantId(): UUID =
        tenantId ?: throw IllegalStateException("Tenant context not set for current request")
}
