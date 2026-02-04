package com.chiroerp.finance.domain

import com.chiroerp.shared.org.AuthorizationContext
import com.chiroerp.finance.domain.HardcodedPostingRules
import java.time.Instant

/**
 * Domain Service for Journal Entry operations.
 *
 * Encapsulates business logic for creating, posting, and reversing journal entries.
 * Uses platform-shared interfaces for posting rules and event publishing.
 *
 * Phase 0: In-memory only (no persistence)
 * Phase 1: Add repository for database persistence
 */
class JournalEntryService(
    private val postingRules: HardcodedPostingRules = HardcodedPostingRules()
) {
    /**
     * Create a new journal entry (draft status).
     *
     * @param entry Draft journal entry
     * @param context Authorization context
     * @return Result with created entry or error
     */
    suspend fun createJournalEntry(
        entry: JournalEntry,
        context: AuthorizationContext = AuthorizationContext.SINGLE_TENANT_DEV
    ): Result<JournalEntry> {
        // Validate entry
        entry.validateBalance().onFailure { return Result.failure(it) }
        entry.validateLines().onFailure { return Result.failure(it) }

        // Validate all lines
        entry.lines.forEach { line ->
            line.validate().onFailure {
                return Result.failure(IllegalArgumentException("Line ${line.lineNumber}: ${it.message}"))
            }
        }

        // Phase 0: Just return the entry (no persistence yet)
        // Phase 1: Save to repository
        return Result.success(entry)
    }

    /**
     * Post a journal entry to the general ledger.
     *
     * Changes status from DRAFT to POSTED and records posting timestamp.
     * Posted entries are immutable.
     *
     * @param entry Journal entry to post
     * @param userId User performing the posting
     * @param context Authorization context
     * @return Result with posted entry or error
     */
    suspend fun postJournalEntry(
        entry: JournalEntry,
        userId: String,
        context: AuthorizationContext = AuthorizationContext.SINGLE_TENANT_DEV
    ): Result<JournalEntry> {
        if (!entry.canPost()) {
            return Result.failure(IllegalStateException("Journal entry cannot be posted (status=${entry.status})"))
        }

        // Verify balance one more time before posting
        entry.validateBalance().onFailure { return Result.failure(it) }

        // Create posted entry
        val postedEntry = entry.copy(
            status = JournalEntryStatus.POSTED,
            postedBy = userId,
            postedAt = Instant.now()
        )

        // Phase 0: Just return the posted entry
        // Phase 1: Update balances in database
        // Phase 2: Publish JournalEntryPosted event

        return Result.success(postedEntry)
    }

    /**
     * Reverse a posted journal entry.
     *
     * Creates a new journal entry with opposite debits/credits.
     * Original entry is marked as reversed.
     *
     * @param originalEntry Entry to reverse (must be POSTED)
     * @param userId User performing the reversal
     * @param reversalDate Posting date for reversal entry
     * @param context Authorization context
     * @return Result with reversal entry or error
     */
    suspend fun reverseJournalEntry(
        originalEntry: JournalEntry,
        userId: String,
        reversalDate: java.time.LocalDate,
        context: AuthorizationContext = AuthorizationContext.SINGLE_TENANT_DEV
    ): Result<JournalEntry> {
        if (!originalEntry.canReverse()) {
            return Result.failure(IllegalStateException("Journal entry cannot be reversed (status=${originalEntry.status})"))
        }

        // Create reversal lines (swap debit/credit)
        val reversalLines = originalEntry.lines.map { line ->
            line.copy(
                lineId = java.util.UUID.randomUUID().toString(),
                debitAmount = line.creditAmount,  // Swap
                creditAmount = line.debitAmount   // Swap
            )
        }

        // Create reversal entry
        val reversalEntry = JournalEntry(
            entryNumber = "${originalEntry.entryNumber}-REV",
            companyCode = originalEntry.companyCode,
            entryType = JournalEntryType.REVERSAL,
            documentDate = reversalDate,
            postingDate = reversalDate,
            fiscalYear = reversalDate.year,
            fiscalPeriod = reversalDate.monthValue,
            currency = originalEntry.currency,
            exchangeRate = originalEntry.exchangeRate,
            description = "Reversal of ${originalEntry.entryNumber}: ${originalEntry.description}",
            reference = originalEntry.entryId,
            lines = reversalLines,
            status = JournalEntryStatus.DRAFT,
            createdBy = userId
        )

        // Phase 0: Just return the reversal entry
        // Phase 1: Auto-post reversal and update original entry's reversedBy field
        // Phase 2: Publish JournalEntryReversed event

        return Result.success(reversalEntry)
    }

    /**
     * Validate journal entry before saving.
     *
     * @param entry Entry to validate
     * @return Result with validation errors or success
     */
    fun validateEntry(entry: JournalEntry): Result<Unit> {
        // Balance validation
        entry.validateBalance().onFailure { return Result.failure(it) }

        // Lines validation
        entry.validateLines().onFailure { return Result.failure(it) }

        // Individual line validation
        entry.lines.forEach { line ->
            line.validate().onFailure {
                return Result.failure(IllegalArgumentException("Line ${line.lineNumber}: ${it.message}"))
            }
        }

        // Business rule: Posting date should not be in the future
        if (entry.postingDate.isAfter(java.time.LocalDate.now())) {
            return Result.failure(IllegalArgumentException("Posting date cannot be in the future"))
        }

        return Result.success(Unit)
    }
}
