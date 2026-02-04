package com.chiroerp.finance.application.port.input.command

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Command to create a new GL Account.
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS), ADR-009 (Financial Domain)
 */
data class CreateGLAccountCommand(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: UUID,

    @field:NotBlank(message = "Account number is required")
    @field:Size(max = 20, message = "Account number must not exceed 20 characters")
    @field:Pattern(regexp = "^[0-9A-Z-]+$", message = "Account number must contain only digits, uppercase letters, and hyphens")
    val accountNumber: String,

    @field:NotBlank(message = "Account name is required")
    @field:Size(max = 100, message = "Account name must not exceed 100 characters")
    val accountName: String,

    @field:NotBlank(message = "Account type is required")
    val accountType: String,

    @field:NotBlank(message = "Balance type is required")
    val balanceType: String,

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    val currency: String,

    @field:NotBlank(message = "Company code is required")
    @field:Size(max = 10, message = "Company code must not exceed 10 characters")
    val companyCode: String,

    val isActive: Boolean = true,
    val isPostingAllowed: Boolean = true,
    val parentAccount: String? = null,
    val costCenter: String? = null,
    val profitCenter: String? = null,
    val description: String? = null,

    @field:NotNull(message = "User ID is required")
    val userId: UUID
)
