package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.command.CreateJournalEntryCommand
import com.chiroerp.finance.application.port.output.JournalEntryPort
import com.chiroerp.finance.domain.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import java.time.Instant

/**
 * Use case for creating a new Journal Entry.
 *
 * Orchestrates the business logic for journal entry creation including:
 * - Validation of double-entry balance
 * - Domain model creation
 * - Persistence
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS - Use Cases), ADR-009 (Financial Domain)
 */
@ApplicationScoped
class CreateJournalEntryUseCase {

    @Inject
    lateinit var journalEntryPort: JournalEntryPort

    @Transactional
    fun execute(@Valid command: CreateJournalEntryCommand): JournalEntry {
        // Convert command lines to domain lines
        val lines = command.lines.map { lineCmd ->
            JournalEntryLine(
                lineNumber = lineCmd.lineNumber,
                accountNumber = lineCmd.accountNumber,
                debitAmount = lineCmd.debitAmount,
                creditAmount = lineCmd.creditAmount,
                costCenter = lineCmd.costCenter,
                profitCenter = lineCmd.profitCenter,
                text = lineCmd.description
            )
        }

        // Create domain model
        val entry = JournalEntry(
            entryNumber = command.entryNumber,
            companyCode = command.companyCode,
            entryType = JournalEntryType.valueOf(command.entryType),
            documentDate = command.documentDate,
            postingDate = command.postingDate,
            fiscalYear = command.fiscalYear,
            fiscalPeriod = command.fiscalPeriod,
            currency = command.currency,
            exchangeRate = command.exchangeRate,
            description = command.description,
            reference = command.reference,
            lines = lines,
            status = JournalEntryStatus.DRAFT,
            createdBy = command.userId.toString(),
            createdAt = Instant.now()
        )

        // Validate domain rules
        entry.validateBalance().getOrThrow()
        entry.validateLines().getOrThrow()

        // Persist via output port
        return journalEntryPort.save(entry, command.tenantId)
    }
}
