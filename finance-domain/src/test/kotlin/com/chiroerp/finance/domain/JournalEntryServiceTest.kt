package com.chiroerp.finance.domain

import com.chiroerp.finance.domain.HardcodedPostingRules
import com.chiroerp.shared.org.AuthorizationContext
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class JournalEntryServiceTest {

    private lateinit var service: JournalEntryService

    @BeforeEach
    fun setup() {
        service = JournalEntryService(HardcodedPostingRules())
    }

    // =========================================================================
    // Create Journal Entry Tests
    // =========================================================================

    @Test
    fun `should create valid journal entry`(): Unit = runBlocking {
        val entry = createBalancedDraftEntry()

        val result = service.createJournalEntry(entry)

        assertThat(result.isSuccess).isTrue()
        val createdEntry = result.getOrNull()
        assertThat(createdEntry).isNotNull
        assertThat(createdEntry?.status).isEqualTo(JournalEntryStatus.DRAFT)
    }

    @Test
    fun `should reject unbalanced journal entry`(): Unit = runBlocking {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("1000.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("500.00"))
            )
        )

        val result = service.createJournalEntry(entry)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should reject entry with invalid lines`(): Unit = runBlocking {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("100.00"))
            )
        )

        val result = service.createJournalEntry(entry)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should reject entry with line having both debit and credit`(): Unit = runBlocking {
        val invalidLine = JournalEntryLine(
            lineNumber = 1,
            accountNumber = "1000",
            debitAmount = BigDecimal("100.00"),
            creditAmount = BigDecimal("100.00"),
            costCenter = null,
            profitCenter = null,
            businessArea = null,
            text = "Invalid",
            assignment = null,
            taxCode = null,
            quantity = null,
            uom = null
        )

        val entry = createTestEntry(
            lines = listOf(
                invalidLine,
                createCreditLine(accountNumber = "4000", amount = BigDecimal("100.00"))
            )
        )

        val result = service.createJournalEntry(entry)

        assertThat(result.isFailure).isTrue()
    }

    // =========================================================================
    // Post Journal Entry Tests
    // =========================================================================

    @Test
    fun `should post valid draft entry`(): Unit = runBlocking {
        val entry = createBalancedDraftEntry()

        val result = service.postJournalEntry(entry, "user123")

        assertThat(result.isSuccess).isTrue()
        val postedEntry = result.getOrNull()
        assertThat(postedEntry).isNotNull
        assertThat(postedEntry?.status).isEqualTo(JournalEntryStatus.POSTED)
        assertThat(postedEntry?.postedBy).isEqualTo("user123")
        assertThat(postedEntry?.postedAt).isNotNull()
    }

    @Test
    fun `should reject posting already posted entry`(): Unit = runBlocking {
        val entry = createBalancedDraftEntry().copy(
            status = JournalEntryStatus.POSTED,
            postedBy = "user999"
        )

        val result = service.postJournalEntry(entry, "user123")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("cannot be posted")
    }

    @Test
    fun `should reject posting unbalanced entry`(): Unit = runBlocking {
        val entry = createTestEntry(
            status = JournalEntryStatus.DRAFT,
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("1000.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("500.00"))
            )
        )

        val result = service.postJournalEntry(entry, "user123")

        assertThat(result.isFailure).isTrue()
    }

    // =========================================================================
    // Reverse Journal Entry Tests
    // =========================================================================

    @Test
    fun `should reverse posted entry with swapped debits and credits`(): Unit = runBlocking {
        val originalEntry = createBalancedDraftEntry().copy(
            status = JournalEntryStatus.POSTED,
            postedBy = "user123"
        )

        val result = service.reverseJournalEntry(
            originalEntry = originalEntry,
            userId = "user456",
            reversalDate = LocalDate.of(2026, 2, 15)
        )

        assertThat(result.isSuccess).isTrue()
        val reversalEntry = result.getOrNull()
        assertThat(reversalEntry).isNotNull
        assertThat(reversalEntry?.entryType).isEqualTo(JournalEntryType.REVERSAL)
        assertThat(reversalEntry?.entryNumber).isEqualTo("${originalEntry.entryNumber}-REV")

        // Verify amounts are swapped
        val originalDebitLine = originalEntry.lines.first { it.isDebit() }
        val reversalDebitLine = reversalEntry?.lines?.first { it.accountNumber == originalDebitLine.accountNumber }

        assertThat(reversalDebitLine?.creditAmount).isEqualTo(originalDebitLine.debitAmount)
        assertThat(reversalDebitLine?.debitAmount).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `should reject reversing draft entry`(): Unit = runBlocking {
        val draftEntry = createBalancedDraftEntry()

        val result = service.reverseJournalEntry(
            originalEntry = draftEntry,
            userId = "user456",
            reversalDate = LocalDate.now()
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("cannot be reversed")
    }

    @Test
    fun `should reject reversing already reversed entry`(): Unit = runBlocking {
        val reversedEntry = createBalancedDraftEntry().copy(
            status = JournalEntryStatus.POSTED,
            postedBy = "user123",
            reversedBy = "user999"
        )

        val result = service.reverseJournalEntry(
            originalEntry = reversedEntry,
            userId = "user456",
            reversalDate = LocalDate.now()
        )

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should create reversal with correct posting date`(): Unit = runBlocking {
        val originalEntry = createBalancedDraftEntry().copy(
            status = JournalEntryStatus.POSTED,
            postedBy = "user123"
        )

        val reversalDate = LocalDate.of(2026, 3, 1)
        val result = service.reverseJournalEntry(
            originalEntry = originalEntry,
            userId = "user456",
            reversalDate = reversalDate
        )

        assertThat(result.isSuccess).isTrue()
        val reversalEntry = result.getOrNull()
        assertThat(reversalEntry?.postingDate).isEqualTo(reversalDate)
        assertThat(reversalEntry?.documentDate).isEqualTo(reversalDate)
    }

    // =========================================================================
    // Validate Entry Tests
    // =========================================================================

    @Test
    fun `should validate correct entry`() {
        val entry = createBalancedDraftEntry()

        val result = service.validateEntry(entry)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should reject entry with future posting date`() {
        val entry = createBalancedDraftEntry().copy(
            postingDate = LocalDate.now().plusDays(10)
        )

        val result = service.validateEntry(entry)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("future")
    }

    @Test
    fun `should reject unbalanced entry on validation`() {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("1000.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("500.00"))
            )
        )

        val result = service.validateEntry(entry)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should reject entry with insufficient lines on validation`() {
        val entry = createTestEntry(
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("100.00"))
            )
        )

        val result = service.validateEntry(entry)

        assertThat(result.isFailure).isTrue()
    }

    // =========================================================================
    // Authorization Context Tests
    // =========================================================================

    @Test
    fun `should use SINGLE_TENANT_DEV context by default`(): Unit = runBlocking {
        val entry = createBalancedDraftEntry()

        // Verify default context works
        val result = service.createJournalEntry(entry)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should accept custom authorization context`(): Unit = runBlocking {
        val entry = createBalancedDraftEntry()
        val customContext = AuthorizationContext(
            userId = "custom-user",
            tenantId = UUID.randomUUID().toString(),
            orgUnitId = "2000",
            accessibleOrgUnits = setOf("2000"),
            roles = setOf("USER"),
            permissions = setOf("finance:write")
        )

        val result = service.createJournalEntry(entry, customContext)

        assertThat(result.isSuccess).isTrue()
    }

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private fun createBalancedDraftEntry(): JournalEntry {
        return createTestEntry(
            status = JournalEntryStatus.DRAFT,
            lines = listOf(
                createDebitLine(accountNumber = "1000", amount = BigDecimal("1000.00")),
                createCreditLine(accountNumber = "4000", amount = BigDecimal("1000.00"))
            )
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
        description: String = "Test journal entry",
        lines: List<JournalEntryLine>,
        status: JournalEntryStatus = JournalEntryStatus.DRAFT
    ): JournalEntry {
        return JournalEntry(
            entryNumber = entryNumber,
            companyCode = companyCode,
            entryType = entryType,
            documentDate = documentDate,
            postingDate = postingDate,
            fiscalYear = fiscalYear,
            fiscalPeriod = fiscalPeriod,
            currency = "USD",
            exchangeRate = BigDecimal.ONE,
            description = description,
            reference = null,
            lines = lines,
            status = status,
            createdBy = "test-user",
            postedBy = null,
            postedAt = null,
            reversedBy = null,
            reversalEntryId = null
        )
    }

    private fun createDebitLine(
        lineNumber: Int = 1,
        accountNumber: String,
        amount: BigDecimal
    ): JournalEntryLine {
        return JournalEntryLine(
            lineNumber = lineNumber,
            accountNumber = accountNumber,
            debitAmount = amount,
            creditAmount = BigDecimal.ZERO,
            costCenter = null,
            profitCenter = null,
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
        amount: BigDecimal
    ): JournalEntryLine {
        return JournalEntryLine(
            lineNumber = lineNumber,
            accountNumber = accountNumber,
            debitAmount = BigDecimal.ZERO,
            creditAmount = amount,
            costCenter = null,
            profitCenter = null,
            businessArea = null,
            text = "Credit line",
            assignment = null,
            taxCode = null,
            quantity = null,
            uom = null
        )
    }
}
