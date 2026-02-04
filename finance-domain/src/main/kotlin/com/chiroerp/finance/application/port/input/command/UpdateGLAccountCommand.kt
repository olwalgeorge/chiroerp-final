package com.chiroerp.finance.application.port.input.command

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Command to update an existing GL Account.
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS), ADR-009 (Financial Domain)
 */
data class UpdateGLAccountCommand(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: UUID,

    @field:NotBlank(message = "Account number is required")
    val accountNumber: String,

    @field:NotBlank(message = "Account name is required")
    @field:Size(max = 100, message = "Account name must not exceed 100 characters")
    val accountName: String,

    val isActive: Boolean,
    val isPostingAllowed: Boolean,
    val costCenter: String? = null,
    val profitCenter: String? = null,
    val description: String? = null,

    @field:NotNull(message = "User ID is required")
    val userId: UUID
)
