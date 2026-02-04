package com.chiroerp.finance.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

class JournalEntryTest {

    // =========================================================================
    // Balance Validation Tests
    // =========================================================================

    @Test
    fun `should validate balanced entry`() {
        val entry = createBalancedEntry()
        val result = entry.validateBalance()

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should reject unbalanced entry with more debits`() {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("1000.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("500.00"))
            )
        )

        val result = entry.validateBalance()
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("out of balance")
    }

    @Test
    fun `should reject unbalanced entry with more credits`() {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("500.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("1000.00"))
            )
        )

        val result = entry.validateBalance()
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should reject entry with no amounts`() {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal.ZERO),
                createCreditLine(accountNumber = "4000", amount = BigDecimal.ZERO)
            )
        )

        val result = entry.validateBalance()
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should calculate total debit correctly`() {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("500.00")),
                createDebitLine(accountNumber = "1200", amount = BigDecimal("300.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("800.00"))
            )
        )

        assertThat(entry.totalDebit()).isEqualTo(BigDecimal("800.00"))
    }

    @Test
    fun `should calculate total credit correctly`() {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("1000.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("600.00")),
                createCreditLine(accountNumber = "4100", amount = BigDecimal("400.00"))
            )
        )

        assertThat(entry.totalCredit()).isEqualTo(BigDecimal("1000.00"))
    }

    // =========================================================================
    // Line Validation Tests
    // =========================================================================

    @Test
    fun `should validate entry with sufficient lines`() {
        val entry = createBalancedEntry()
        val result = entry.validateLines()

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should reject entry with fewer than 2 lines`() {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("100.00"))
            )
        )

        val result = entry.validateLines()
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should reject entry with duplicate line numbers`() {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(lineNumber = 1, accountNumber = "1000", amount = BigDecimal("500.00")),
                createCreditLine(lineNumber = 1, accountNumber = "4000", amount = BigDecimal("500.00"))
            )
        )

        val result = entry.validateLines()
        assertThat(result.isFailure).isTrue()
    }

    // =========================================================================
    // Status and State Transition Tests
    // =========================================================================

    @Test
    fun `should allow posting draft entry that is balanced`() {
        val entry = createBalancedEntry(status = JournalEntryStatus.DRAFT)
        assertThat(entry.canPost()).isTrue()
    }

    @Test
    fun `should not allow posting already posted entry`() {
        val entry = createBalancedEntry(status = JournalEntryStatus.POSTED)
        assertThat(entry.canPost()).isFalse()
    }

    @Test
    fun `should not allow posting unbalanced entry`() {
        val entry = createTestEntry(
            status = JournalEntryStatus.DRAFT,
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("1000.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("500.00"))
            )
        )

        assertThat(entry.canPost()).isFalse()
    }

    @Test
    fun `should allow reversing posted entry`() {
        val entry = createBalancedEntry(
            status = JournalEntryStatus.POSTED,
            postedBy = "user123",
            postedAt = Instant.now()
        )

        assertThat(entry.canReverse()).isTrue()
    }

    @Test
    fun `should not allow reversing draft entry`() {
        val entry = createBalancedEntry(status = JournalEntryStatus.DRAFT)
        assertThat(entry.canReverse()).isFalse()
    }

    @Test
    fun `should not allow reversing already reversed entry`() {
        val entry = createBalancedEntry(
            status = JournalEntryStatus.POSTED,
            postedBy = "user123",
            postedAt = Instant.now(),
            reversedBy = "user456"
        )

        assertThat(entry.canReverse()).isFalse()
    }

    // =========================================================================
    // JournalEntryLine Tests
    // =========================================================================

    @Test
    fun `should validate line with debit amount only`() {
        val line = createDebitLine(accountNumber = "1000", amount = BigDecimal("100.00"))
        val result = line.validate()

        assertThat(result.isSuccess).isTrue()
        assertThat(line.isDebit()).isTrue()
        assertThat(line.isCredit()).isFalse()
    }

    @Test
    fun `should validate line with credit amount only`() {
        val line = createCreditLine(accountNumber = "4000", amount = BigDecimal("100.00"))
        val result = line.validate()

        assertThat(result.isSuccess).isTrue()
        assertThat(line.isDebit()).isFalse()
        assertThat(line.isCredit()).isTrue()
    }

    @Test
    fun `should reject line with both debit and credit amounts`() {
        val line = JournalEntryLine(
            lineNumber = 1,
            accountNumber = "1000",
            debitAmount = BigDecimal("100.00"),
            creditAmount = BigDecimal("100.00"),
            costCenter = null,
            profitCenter = null,
            businessArea = null,
            text = "Test",
            assignment = null,
            taxCode = null,
            quantity = null,
            uom = null
        )

        val result = line.validate()
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should reject line with negative debit amount`() {
        val line = JournalEntryLine(
            lineNumber = 1,
            accountNumber = "1000",
            debitAmount = BigDecimal("-100.00"),
            creditAmount = BigDecimal.ZERO,
            costCenter = null,
            profitCenter = null,
            businessArea = null,
            text = "Test",
            assignment = null,
            taxCode = null,
            quantity = null,
            uom = null
        )

        val result = line.validate()
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should return correct amount for debit line`() {
        val line = createDebitLine(accountNumber = "1000", amount = BigDecimal("250.50"))
        assertThat(line.amount()).isEqualTo(BigDecimal("250.50"))
    }

    @Test
    fun `should return correct amount for credit line`() {
        val line = createCreditLine(accountNumber = "4000", amount = BigDecimal("350.75"))
        assertThat(line.amount()).isEqualTo(BigDecimal("350.75"))
    }

    @Test
    fun `should support cost center assignment on line`() {
        val line = createDebitLine(
            accountNumber = "5000",
            amount = BigDecimal("100.00"),
            costCenter = "CC-100"
        )

        assertThat(line.costCenter).isEqualTo("CC-100")
    }

    @Test
    fun `should support profit center assignment on line`() {
        val line = createCreditLine(
            accountNumber = "4000",
            amount = BigDecimal("100.00"),
            profitCenter = "PC-1000"
        )

        assertThat(line.profitCenter).isEqualTo("PC-1000")
    }

    // =========================================================================
    // Entry Type Tests
    // =========================================================================

    @Test
    fun `should support standard entry type`() {
        val entry = createBalancedEntry(entryType = JournalEntryType.STANDARD)
        assertThat(entry.entryType).isEqualTo(JournalEntryType.STANDARD)
    }

    @Test
    fun `should support automatic entry type`() {
        val entry = createBalancedEntry(entryType = JournalEntryType.AUTOMATIC)
        assertThat(entry.entryType).isEqualTo(JournalEntryType.AUTOMATIC)
    }

    @Test
    fun `should support reversal entry type`() {
        val entry = createBalancedEntry(entryType = JournalEntryType.REVERSAL)
        assertThat(entry.entryType).isEqualTo(JournalEntryType.REVERSAL)
    }

    // =========================================================================
    // Multi-Currency Tests
    // =========================================================================

    @Test
    fun `should support multi-currency entries`() {
        val entry = createBalancedEntry(
            currency = "EUR",
            exchangeRate = BigDecimal("1.10")
        )

        assertThat(entry.currency).isEqualTo("EUR")
        assertThat(entry.exchangeRate).isEqualTo(BigDecimal("1.10"))
    }

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private fun createBalancedEntry(
        entryType: JournalEntryType = JournalEntryType.STANDARD,
        status: JournalEntryStatus = JournalEntryStatus.DRAFT,
        currency: String = "USD",
        exchangeRate: BigDecimal = BigDecimal.ONE,
        postedBy: String? = null,
        postedAt: Instant? = null,
        reversedBy: String? = null
    ): JournalEntry {
        return createTestEntry(
            entryType = entryType,
            status = status,
            currency = currency,
            exchangeRate = exchangeRate,
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("1000.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("1000.00"))
            ),
            postedBy = postedBy,
            postedAt = postedAt,
            reversedBy = reversedBy
        )
    }

    private fun createTestEntry(
        entryNumber: String = "JE-2026-001",
        companyCode: String = "1000",
        entryType: JournalEntryType = JournalEntryType.STANDARD,
        documentDate: LocalDate = LocalDate.of(2026, 2, 1),
        postingDate: LocalDate = LocalDate.of(2026, 2, 1),
        fiscalYear: Int = 2026,
        fiscalPeriod: Int = 2,
        currency: String = "USD",
        exchangeRate: BigDecimal = BigDecimal.ONE,
        description: String = "Test journal entry",
        lines: List<JournalEntryLine>,
        status: JournalEntryStatus = JournalEntryStatus.DRAFT,
        createdBy: String = "test-user",
        postedBy: String? = null,
        postedAt: Instant? = null,
        reversedBy: String? = null
    ): JournalEntry {
        return JournalEntry(
            entryNumber = entryNumber,
            companyCode = companyCode,
            entryType = entryType,
            documentDate = documentDate,
            postingDate = postingDate,
            fiscalYear = fiscalYear,
            fiscalPeriod = fiscalPeriod,
            currency = currency,
            exchangeRate = exchangeRate,
            description = description,
            reference = null,
            lines = lines,
            status = status,
            createdBy = createdBy,
            postedBy = postedBy,
            postedAt = postedAt,
            reversedBy = reversedBy,
            reversalEntryId = null
        )
    }

    private fun createDebitLine(
        lineNumber: Int = 1,
        accountNumber: String,
        amount: BigDecimal,
        costCenter: String? = null,
        profitCenter: String? = null
    ): JournalEntryLine {
        return JournalEntryLine(
            lineNumber = lineNumber,
            accountNumber = accountNumber,
            debitAmount = amount,
            creditAmount = BigDecimal.ZERO,
            costCenter = costCenter,
            profitCenter = profitCenter,
            businessArea = null,
            text = "Debit line",
            assignment = null,
            taxCode = null,
            quantity = null,
            uom = null
        )
    }

    private fun createCreditLine(
        lineNumber: Int = 2,
        accountNumber: String,
        amount: BigDecimal,
        costCenter: String? = null,
        profitCenter: String? = null
    ): JournalEntryLine {
        return JournalEntryLine(
            lineNumber = lineNumber,
            accountNumber = accountNumber,
            debitAmount = BigDecimal.ZERO,
            creditAmount = amount,
            costCenter = costCenter,
            profitCenter = profitCenter,
            businessArea = null,
            text = "Credit line",
            assignment = null,
            taxCode = null,
            quantity = null,
            uom = null
        )
    }
}
