package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.query.ListJournalEntriesQuery
import com.chiroerp.finance.application.port.output.JournalEntryPort
import com.chiroerp.finance.domain.JournalEntry
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.validation.Valid

@ApplicationScoped
class ListJournalEntriesUseCase {

    @Inject
    lateinit var journalEntryPort: JournalEntryPort

    fun execute(@Valid query: ListJournalEntriesQuery): List<JournalEntry> {
        return journalEntryPort.findByTenant(query.tenantId)
    }
}
