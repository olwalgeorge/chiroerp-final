package com.chiroerp.finance.application.port.input.query

import jakarta.validation.constraints.NotNull
import java.util.UUID

/**
 * Query object for listing journal entries for a tenant.
 */
data class ListJournalEntriesQuery(
    @field:NotNull val tenantId: UUID
)
