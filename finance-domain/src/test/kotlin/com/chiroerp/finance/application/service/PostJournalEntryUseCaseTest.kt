package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.command.PostJournalEntryCommand
import com.chiroerp.finance.application.port.output.JournalEntryPort
import com.chiroerp.finance.domain.JournalEntry
import com.chiroerp.finance.domain.JournalEntryLine
import com.chiroerp.finance.domain.JournalEntryStatus
import com.chiroerp.finance.domain.JournalEntryType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class PostJournalEntryUseCaseTest {

    private val tenantId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private lateinit var port: InMemoryJournalEntryPort
    private lateinit var useCase: PostJournalEntryUseCase

    @BeforeEach
    fun setUp() {
        port = InMemoryJournalEntryPort()
        useCase = PostJournalEntryUseCase().apply { journalEntryPort = port }
    }

    @Test
    fun `should post draft balanced entry`() {
        val draft = balancedEntry(status = JournalEntryStatus.DRAFT)
        port.save(draft, tenantId)

        val result = useCase.execute(PostJournalEntryCommand(tenantId, draft.entryNumber, userId))

        assertThat(result.status).isEqualTo(JournalEntryStatus.POSTED)
        assertThat(result.postedBy).isEqualTo(userId.toString())
        assertThat(result.postedAt).isNotNull()
        assertThat(port.findByTenantAndEntryNumber(tenantId, draft.entryNumber)?.status)
            .isEqualTo(JournalEntryStatus.POSTED)
    }

    @Test
    fun `should reject posting non-draft entry`() {
        val posted = balancedEntry(status = JournalEntryStatus.POSTED)
        port.save(posted, tenantId)

        assertThatThrownBy {
            useCase.execute(PostJournalEntryCommand(tenantId, posted.entryNumber, userId))
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("DRAFT")
    }

    @Test
    fun `should reject unbalanced entry`() {
        val unbalanced = balancedEntry(status = JournalEntryStatus.DRAFT).copy(
            lines = listOf(
                JournalEntryLine(lineNumber = 1, accountNumber = "1000", debitAmount = BigDecimal("100")),
                JournalEntryLine(lineNumber = 2, accountNumber = "2000", creditAmount = BigDecimal("50"))
            )
        )
        port.save(unbalanced, tenantId)

        assertThatThrownBy {
            useCase.execute(PostJournalEntryCommand(tenantId, unbalanced.entryNumber, userId))
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("out of balance")
    }

    @Test
    fun `should reject posting non-existent entry`() {
        assertThatThrownBy {
            useCase.execute(PostJournalEntryCommand(tenantId, "MISSING-123", userId))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("not found")
    }

    @Test
    fun `should reject posting entry from another tenant`() {
        val draft = balancedEntry(status = JournalEntryStatus.DRAFT)
        port.save(draft, tenantId)
        val otherTenant = UUID.randomUUID()

        assertThatThrownBy {
            useCase.execute(PostJournalEntryCommand(otherTenant, draft.entryNumber, userId))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("not found")
    }

    private fun balancedEntry(status: JournalEntryStatus = JournalEntryStatus.DRAFT): JournalEntry {
        val debit = JournalEntryLine(lineNumber = 1, accountNumber = "1000", debitAmount = BigDecimal("100"))
        val credit = JournalEntryLine(lineNumber = 2, accountNumber = "2000", creditAmount = BigDecimal("100"))
        return JournalEntry(
            entryNumber = "JE-1",
            companyCode = "1000",
            entryType = JournalEntryType.STANDARD,
            documentDate = LocalDate.now(),
            postingDate = LocalDate.now(),
            fiscalYear = 2026,
            fiscalPeriod = 1,
            currency = "USD",
            description = "Test",
            lines = listOf(debit, credit),
            status = status,
            createdBy = userId.toString()
        )
    }

    private class InMemoryJournalEntryPort : JournalEntryPort {
        private val entries: MutableMap<Pair<UUID, String>, JournalEntry> = mutableMapOf()

        override fun findByTenant(tenantId: UUID): List<JournalEntry> {
            return entries.filterKeys { it.first == tenantId }.values.toList()
        }

        override fun findByTenantAndEntryNumber(tenantId: UUID, entryNumber: String): JournalEntry? {
            return entries[tenantId to entryNumber]
        }

        override fun save(entry: JournalEntry, tenantId: UUID): JournalEntry {
            entries[tenantId to entry.entryNumber] = entry
            return entry
        }

        override fun update(entry: JournalEntry, tenantId: UUID): JournalEntry {
            entries[tenantId to entry.entryNumber] = entry
            return entry
        }
    }
}
