package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.command.CreateJournalEntryCommand
import com.chiroerp.finance.application.port.input.command.JournalEntryLineCommand
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

class CreateJournalEntryUseCaseTest {

    private val tenantId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private lateinit var port: InMemoryJournalEntryPort
    private lateinit var useCase: CreateJournalEntryUseCase

    @BeforeEach
    fun setUp() {
        port = InMemoryJournalEntryPort()
        useCase = CreateJournalEntryUseCase().apply { journalEntryPort = port }
    }

    @Test
    fun `should create balanced draft entry`() {
        val command = balancedCommand()

        val result = useCase.execute(command)

        assertThat(result.status).isEqualTo(JournalEntryStatus.DRAFT)
        assertThat(result.createdBy).isEqualTo(userId.toString())
        assertThat(result.lines).hasSize(2)
        assertThat(result.lines[0].text).isEqualTo(command.lines[0].description)
        assertThat(port.findByTenantAndEntryNumber(tenantId, command.entryNumber)).isNotNull
    }

    @Test
    fun `should reject unbalanced entry`() {
        val command = balancedCommand(
            debit = BigDecimal("100"),
            credit = BigDecimal("50")
        )

        assertThatThrownBy { useCase.execute(command) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("out of balance")
    }

    @Test
    fun `should reject invalid entry type`() {
        val command = balancedCommand(entryType = "UNKNOWN")

        assertThatThrownBy { useCase.execute(command) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("JournalEntryType")
    }

    private fun balancedCommand(
        debit: BigDecimal = BigDecimal("100"),
        credit: BigDecimal = debit,
        entryType: String = JournalEntryType.STANDARD.name
    ): CreateJournalEntryCommand {
        val line1 = JournalEntryLineCommand(
            lineNumber = 1,
            accountNumber = "1000",
            debitAmount = debit,
            creditAmount = BigDecimal.ZERO,
            description = "Line 1"
        )
        val line2 = JournalEntryLineCommand(
            lineNumber = 2,
            accountNumber = "2000",
            debitAmount = BigDecimal.ZERO,
            creditAmount = credit,
            description = "Line 2"
        )

        return CreateJournalEntryCommand(
            tenantId = tenantId,
            entryNumber = "JE-100",
            companyCode = "1000",
            entryType = entryType,
            documentDate = LocalDate.now(),
            postingDate = LocalDate.now(),
            fiscalYear = 2026,
            fiscalPeriod = 2,
            currency = "USD",
            exchangeRate = BigDecimal.ONE,
            description = "Test entry",
            reference = "REF-1",
            lines = listOf(line1, line2),
            userId = userId
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
