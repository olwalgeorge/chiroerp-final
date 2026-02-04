package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.command.PostJournalEntryCommand
import com.chiroerp.finance.application.port.output.JournalEntryPort
import com.chiroerp.finance.domain.JournalEntry
import com.chiroerp.finance.domain.JournalEntryStatus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import java.time.Instant

/**
 * Use case for posting a journal entry to the general ledger.
 *
 * Business Rules:
 * - Only DRAFT entries can be posted
 * - Entry must be balanced
 * - All account references must be valid
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS - Use Cases), ADR-009 (Financial Domain)
 */
@ApplicationScoped
class PostJournalEntryUseCase {

    @Inject
    lateinit var journalEntryPort: JournalEntryPort

    @Transactional
    fun execute(@Valid command: PostJournalEntryCommand): JournalEntry {
        // Retrieve existing entry
        val entry = journalEntryPort.findByTenantAndEntryNumber(command.tenantId, command.entryNumber)
            ?: throw IllegalArgumentException("Journal entry ${command.entryNumber} not found")

        // Business Rule: Can only post DRAFT entries
        if (entry.status != JournalEntryStatus.DRAFT) {
            throw IllegalStateException("Can only post DRAFT journal entries. Current status: ${entry.status}")
        }

        // Validate balance before posting
        entry.validateBalance().getOrThrow()

        // TODO: validate accounting period is open (AccountingPeriod aggregate not yet implemented)
        // TODO: validate all referenced accounts are posting-allowed (needs GL account lookup)

        // Update status to POSTED
        val posted = entry.copy(
            status = JournalEntryStatus.POSTED,
            postedBy = command.userId.toString(),
            postedAt = Instant.now()
        )

        // Persist via output port
        return journalEntryPort.update(posted, command.tenantId)
    }
}
