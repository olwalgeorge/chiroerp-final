package com.chiroerp.finance.application.port.input.query

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

/**
 * Query to get a specific GL account by account number.
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS)
 */
data class GetGLAccountQuery(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: UUID,

    @field:NotBlank(message = "Account number is required")
    val accountNumber: String
)
