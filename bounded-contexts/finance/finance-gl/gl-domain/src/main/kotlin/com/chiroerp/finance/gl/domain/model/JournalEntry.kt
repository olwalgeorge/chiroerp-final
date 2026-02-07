package com.chiroerp.finance.gl.domain.model

import com.chiroerp.finance.gl.domain.events.JournalEntryPostedEvent
import com.chiroerp.finance.gl.domain.exceptions.UnbalancedJournalException
import com.chiroerp.finance.shared.JournalEntryType
import com.chiroerp.finance.shared.PostingStatus
import com.chiroerp.finance.shared.identifiers.JournalEntryId
import com.chiroerp.finance.shared.identifiers.LedgerId
import com.chiroerp.shared.types.aggregate.AggregateRoot
import java.util.UUID

class JournalEntry(
    override val id: JournalEntryId,
    val tenantId: UUID,
    val ledgerId: LedgerId,
    val type: JournalEntryType,
    val lines: List<JournalLine>,
    val reference: String,
    var status: PostingStatus = PostingStatus.DRAFT,
) : AggregateRoot<JournalEntryId>(id) {

    init {
        require(lines.isNotEmpty()) { "Journal entry must contain at least one line" }
        require(reference.isNotBlank()) { "Reference cannot be blank" }
    }

    fun post() {
        ensureBalanced()
        status = PostingStatus.POSTED
        registerEvent(
            JournalEntryPostedEvent(
                tenantId = tenantId,
                journalEntryId = id,
                ledgerId = ledgerId,
            ),
        )
    }

    fun ensureBalanced() {
        val currencies = lines.map { it.amount.currency }.distinct()
        require(currencies.size == 1) { "Journal entry lines must use a single currency" }
        val currency = currencies.first()

        val credits = lines
            .filter { it.indicator == DebitCreditIndicator.CREDIT }
            .map { it.amount.amount }
            .reduceOrNull { a, b -> a + b }
            ?: java.math.BigDecimal.ZERO

        val debits = lines
            .filter { it.indicator == DebitCreditIndicator.DEBIT }
            .map { it.amount.amount }
            .reduceOrNull { a, b -> a + b }
            ?: java.math.BigDecimal.ZERO

        if (debits.compareTo(credits) != 0) {
            throw UnbalancedJournalException(
                "Journal entry $id is not balanced: debit=$debits credit=$credits currency=$currency",
            )
        }
    }
}
