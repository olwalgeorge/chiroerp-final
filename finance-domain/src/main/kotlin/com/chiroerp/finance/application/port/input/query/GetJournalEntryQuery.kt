package com.chiroerp.finance.application.port.input.query

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

/**
 * Query object for fetching a single journal entry by entry number.
 */
data class GetJournalEntryQuery(
    @field:NotNull val tenantId: UUID,
    @field:NotBlank val entryNumber: String
)
