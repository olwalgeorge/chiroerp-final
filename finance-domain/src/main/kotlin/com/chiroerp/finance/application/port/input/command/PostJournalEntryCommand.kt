package com.chiroerp.finance.application.port.input.command

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

/**
 * Command to post a journal entry to the general ledger.
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS), ADR-009 (Financial Domain)
 */
data class PostJournalEntryCommand(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: UUID,

    @field:NotBlank(message = "Entry number is required")
    val entryNumber: String,

    @field:NotNull(message = "User ID is required")
    val userId: UUID
)
