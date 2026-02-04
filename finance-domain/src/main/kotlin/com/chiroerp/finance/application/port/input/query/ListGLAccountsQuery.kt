package com.chiroerp.finance.application.port.input.query

import jakarta.validation.constraints.NotNull
import java.util.UUID

/**
 * Query to list all active GL accounts for a tenant.
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS)
 */
data class ListGLAccountsQuery(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: UUID
)
