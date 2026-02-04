package com.chiroerp.finance.application.port.input.command

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Command to create a new Journal Entry.
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS), ADR-009 (Financial Domain)
 */
data class CreateJournalEntryCommand(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: UUID,

    @field:NotBlank(message = "Entry number is required")
    val entryNumber: String,

    @field:NotBlank(message = "Company code is required")
    val companyCode: String,

    @field:NotBlank(message = "Entry type is required")
    val entryType: String,

    @field:NotNull(message = "Document date is required")
    val documentDate: LocalDate,

    @field:NotNull(message = "Posting date is required")
    val postingDate: LocalDate,

    @field:NotNull(message = "Fiscal year is required")
    val fiscalYear: Int,

    @field:NotNull(message = "Fiscal period is required")
    val fiscalPeriod: Int,

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters")
    val currency: String,

    val exchangeRate: BigDecimal = BigDecimal.ONE,

    @field:NotBlank(message = "Description is required")
    val description: String,

    val reference: String? = null,

    @field:NotEmpty(message = "Journal entry must have at least one line")
    @field:Valid
    val lines: List<JournalEntryLineCommand>,

    @field:NotNull(message = "User ID is required")
    val userId: UUID
)

data class JournalEntryLineCommand(
    @field:NotNull(message = "Line number is required")
    val lineNumber: Int,

    @field:NotBlank(message = "Account number is required")
    val accountNumber: String,

    @field:NotNull(message = "Debit amount is required")
    val debitAmount: BigDecimal,

    @field:NotNull(message = "Credit amount is required")
    val creditAmount: BigDecimal,

    val costCenter: String? = null,
    val profitCenter: String? = null,
    val description: String? = null
)
