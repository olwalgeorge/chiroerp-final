package com.chiroerp.finance.gl.application.service.command

import com.chiroerp.finance.gl.application.port.input.command.PostJournalEntryCommand
import com.chiroerp.finance.gl.application.port.output.EventPublisherPort
import com.chiroerp.finance.gl.application.port.output.JournalEntryRepository
import com.chiroerp.finance.gl.domain.model.JournalEntry
import com.chiroerp.finance.gl.domain.model.JournalLine
import com.chiroerp.finance.gl.domain.services.JournalValidationService
import com.chiroerp.finance.shared.identifiers.JournalEntryId
import com.chiroerp.shared.types.cqrs.CommandHandler
import com.chiroerp.shared.types.results.DomainError
import com.chiroerp.shared.types.results.Result

class JournalEntryCommandHandler(
    private val repository: JournalEntryRepository,
    private val eventPublisher: EventPublisherPort,
    private val validationService: JournalValidationService = JournalValidationService(),
) : CommandHandler<PostJournalEntryCommand, JournalEntryId> {

    override fun handle(command: PostJournalEntryCommand): Result<JournalEntryId> {
        if (command.lines.isEmpty()) {
            return Result.failure(SimpleDomainError("EMPTY_LINES", "Journal entry must contain lines"))
        }

        val entry = JournalEntry(
            id = JournalEntryId.random(),
            tenantId = command.tenantId,
            ledgerId = command.ledgerId,
            type = command.type,
            reference = command.reference,
            lines = command.lines.map { line ->
                JournalLine(
                    accountId = line.accountId,
                    amount = line.amount,
                    indicator = line.indicator,
                    description = line.description,
                )
            },
        )

        return try {
            validationService.validate(entry)
            entry.post()
            val saved = repository.save(entry)
            eventPublisher.publish(saved.pullDomainEvents())
            Result.success(saved.id)
        } catch (ex: RuntimeException) {
            Result.failure(SimpleDomainError("JOURNAL_POST_FAILED", ex.message ?: "Unknown journal posting failure"))
        }
    }

    private data class SimpleDomainError(
        override val code: String,
        override val message: String,
    ) : DomainError
}
