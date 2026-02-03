package com.chiroerp.finance.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant
import java.util.UUID

/**
 * Journal Entry
 * 
 * Represents a financial transaction posting to the general ledger.
 * Follows double-entry bookkeeping: debits must equal credits.
 * 
 * Related ADRs: ADR-009 (Financial Accounting Domain)
 */
data class JournalEntry(
    val entryId: String = UUID.randomUUID().toString(),
    val entryNumber: String,             // e.g., "JE-2026-001"
    val companyCode: String,
    val entryType: JournalEntryType,
    val documentDate: LocalDate,         // Transaction date
    val postingDate: LocalDate,          // GL posting date
    val fiscalYear: Int,
    val fiscalPeriod: Int,
    val currency: String,
    val exchangeRate: BigDecimal = BigDecimal.ONE,
    val description: String,
    val reference: String? = null,       // External reference (invoice #, PO #, etc.)
    val lines: List<JournalEntryLine>,
    val status: JournalEntryStatus = JournalEntryStatus.DRAFT,
    val createdBy: String,
    val createdAt: Instant = Instant.now(),
    val postedBy: String? = null,
    val postedAt: Instant? = null,
    val reversedBy: String? = null,
    val reversalEntryId: String? = null  // If this entry was reversed
) {
    /**
     * Validate double-entry bookkeeping: debits = credits.
     */
    fun validateBalance(): Result<Unit> {
        val totalDebit = lines.sumOf { it.debitAmount }
        val totalCredit = lines.sumOf { it.creditAmount }
        
        if (totalDebit != totalCredit) {
            return Result.failure(IllegalStateException(
                "Journal entry out of balance: Debit=$totalDebit, Credit=$totalCredit"
            ))
        }
        
        if (totalDebit == BigDecimal.ZERO) {
            return Result.failure(IllegalStateException(
                "Journal entry has no amounts"
            ))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Validate all lines have valid accounts.
     */
    fun validateLines(): Result<Unit> {
        if (lines.isEmpty()) {
            return Result.failure(IllegalStateException("Journal entry must have at least 2 lines"))
        }
        
        if (lines.size < 2) {
            return Result.failure(IllegalStateException("Journal entry must have at least 2 lines (debit + credit)"))
        }
        
        // Check for duplicate line numbers
        val lineNumbers = lines.map { it.lineNumber }
        if (lineNumbers.size != lineNumbers.distinct().size) {
            return Result.failure(IllegalStateException("Duplicate line numbers found"))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Check if entry can be posted.
     */
    fun canPost(): Boolean {
        return status == JournalEntryStatus.DRAFT &&
               validateBalance().isSuccess &&
               validateLines().isSuccess
    }
    
    /**
     * Check if entry can be reversed.
     */
    fun canReverse(): Boolean {
        return status == JournalEntryStatus.POSTED &&
               reversedBy == null
    }
    
    /**
     * Get total debit amount.
     */
    fun totalDebit(): BigDecimal = lines.sumOf { it.debitAmount }
    
    /**
     * Get total credit amount.
     */
    fun totalCredit(): BigDecimal = lines.sumOf { it.creditAmount }
}

/**
 * Journal Entry Line Item
 * 
 * Individual line in a journal entry (debit or credit to one account).
 */
data class JournalEntryLine(
    val lineId: String = UUID.randomUUID().toString(),
    val lineNumber: Int,                 // 1, 2, 3, etc.
    val accountNumber: String,
    val debitAmount: BigDecimal = BigDecimal.ZERO,
    val creditAmount: BigDecimal = BigDecimal.ZERO,
    val costCenter: String? = null,
    val profitCenter: String? = null,
    val businessArea: String? = null,
    val text: String? = null,            // Line-level description
    val assignment: String? = null,      // Free-text assignment field
    val taxCode: String? = null,
    val quantity: BigDecimal? = null,
    val uom: String? = null              // Unit of measure
) {
    /**
     * Validate that line has either debit OR credit, not both.
     */
    fun validate(): Result<Unit> {
        if (debitAmount < BigDecimal.ZERO || creditAmount < BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("Amounts cannot be negative"))
        }
        
        if (debitAmount > BigDecimal.ZERO && creditAmount > BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("Line cannot have both debit and credit"))
        }
        
        if (debitAmount == BigDecimal.ZERO && creditAmount == BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("Line must have either debit or credit amount"))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Check if this is a debit line.
     */
    fun isDebit(): Boolean = debitAmount > BigDecimal.ZERO
    
    /**
     * Check if this is a credit line.
     */
    fun isCredit(): Boolean = creditAmount > BigDecimal.ZERO
    
    /**
     * Get the absolute amount (debit or credit).
     */
    fun amount(): BigDecimal = if (isDebit()) debitAmount else creditAmount
}

/**
 * Journal Entry Type
 */
enum class JournalEntryType {
    /**
     * Standard journal entry (manual posting)
     */
    STANDARD,
    
    /**
     * Automatic posting from sub-ledger (AP, AR, etc.)
     */
    AUTOMATIC,
    
    /**
     * Adjusting entry (month-end, year-end)
     */
    ADJUSTING,
    
    /**
     * Closing entry (period close)
     */
    CLOSING,
    
    /**
     * Reversal of a previous entry
     */
    REVERSAL,
    
    /**
     * Accrual entry
     */
    ACCRUAL,
    
    /**
     * Reclassification entry
     */
    RECLASSIFICATION
}

/**
 * Journal Entry Status
 */
enum class JournalEntryStatus {
    /**
     * Draft - can be edited
     */
    DRAFT,
    
    /**
     * Pending approval
     */
    PENDING_APPROVAL,
    
    /**
     * Posted to GL - immutable
     */
    POSTED,
    
    /**
     * Reversed (original entry remains, reversal created)
     */
    REVERSED,
    
    /**
     * Cancelled (before posting)
     */
    CANCELLED
}
