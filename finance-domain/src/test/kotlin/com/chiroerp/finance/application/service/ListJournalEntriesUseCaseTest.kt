package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.query.ListJournalEntriesQuery
import com.chiroerp.finance.application.port.output.JournalEntryPort
import com.chiroerp.finance.domain.JournalEntry
import com.chiroerp.finance.domain.JournalEntryLine
import com.chiroerp.finance.domain.JournalEntryStatus
import com.chiroerp.finance.domain.JournalEntryType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class ListJournalEntriesUseCaseTest {

    private val tenantId = UUID.randomUUID()
    private val otherTenant = UUID.randomUUID()
    private lateinit var port: InMemoryJournalEntryPort
    private lateinit var useCase: ListJournalEntriesUseCase

    @BeforeEach
    fun setUp() {
        port = InMemoryJournalEntryPort()
        useCase = ListJournalEntriesUseCase().apply { journalEntryPort = port }
    }

    @Test
    fun `should list entries for tenant only`() {
        port.save(sampleEntry("JE-1"), tenantId)
        port.save(sampleEntry("JE-2"), tenantId)
        port.save(sampleEntry("JE-X-OTHER"), otherTenant)

        val result = useCase.execute(ListJournalEntriesQuery(tenantId))

        assertThat(result).hasSize(2)
        assertThat(result.map { it.entryNumber }).containsExactlyInAnyOrder("JE-1", "JE-2")
    }

    @Test
    fun `should return empty list when tenant has no entries`() {
        val result = useCase.execute(ListJournalEntriesQuery(tenantId))

        assertThat(result).isEmpty()
    }

    private fun sampleEntry(entryNumber: String): JournalEntry {
        val debit = JournalEntryLine(lineNumber = 1, accountNumber = "1000", debitAmount = BigDecimal("25"))
        val credit = JournalEntryLine(lineNumber = 2, accountNumber = "2000", creditAmount = BigDecimal("25"))
        return JournalEntry(
            entryNumber = entryNumber,
            companyCode = "1000",
            entryType = JournalEntryType.STANDARD,
            documentDate = LocalDate.now(),
            postingDate = LocalDate.now(),
            fiscalYear = 2026,
            fiscalPeriod = 1,
            currency = "USD",
            description = "List test",
            lines = listOf(debit, credit),
            status = JournalEntryStatus.DRAFT,
            createdBy = UUID.randomUUID().toString()
        )
    }

    private class InMemoryJournalEntryPort : JournalEntryPort {
        private val entries: MutableMap<Pair<UUID, String>, JournalEntry> = mutableMapOf()

        override fun findByTenant(tenantId: UUID): List<JournalEntry> =
            entries.filterKeys { it.first == tenantId }.values.toList()

        override fun findByTenantAndEntryNumber(tenantId: UUID, entryNumber: String): JournalEntry? =
            entries[tenantId to entryNumber]

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
