package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.query.GetJournalEntryQuery
import com.chiroerp.finance.application.port.output.JournalEntryPort
import com.chiroerp.finance.domain.JournalEntry
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.validation.Valid

@ApplicationScoped
class GetJournalEntryUseCase {

    @Inject
    lateinit var journalEntryPort: JournalEntryPort

    fun execute(@Valid query: GetJournalEntryQuery): JournalEntry? {
        return journalEntryPort.findByTenantAndEntryNumber(query.tenantId, query.entryNumber)
    }
}
