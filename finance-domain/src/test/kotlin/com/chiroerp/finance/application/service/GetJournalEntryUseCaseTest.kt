package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.query.GetJournalEntryQuery
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

class GetJournalEntryUseCaseTest {

    private val tenantId = UUID.randomUUID()
    private val otherTenant = UUID.randomUUID()
    private lateinit var port: InMemoryJournalEntryPort
    private lateinit var useCase: GetJournalEntryUseCase

    @BeforeEach
    fun setUp() {
        port = InMemoryJournalEntryPort()
        useCase = GetJournalEntryUseCase().apply { journalEntryPort = port }
    }

    @Test
    fun `should return entry for tenant`() {
        val entry = sampleEntry(entryNumber = "JE-1")
        port.save(entry, tenantId)

        val result = useCase.execute(GetJournalEntryQuery(tenantId, "JE-1"))

        assertThat(result).isNotNull
        assertThat(result?.entryNumber).isEqualTo("JE-1")
    }

    @Test
    fun `should return null when entry not found`() {
        val result = useCase.execute(GetJournalEntryQuery(tenantId, "MISSING"))

        assertThat(result).isNull()
    }

    @Test
    fun `should not return entry from another tenant`() {
        val entry = sampleEntry(entryNumber = "JE-2")
        port.save(entry, otherTenant)

        val result = useCase.execute(GetJournalEntryQuery(tenantId, "JE-2"))

        assertThat(result).isNull()
    }

    private fun sampleEntry(entryNumber: String): JournalEntry {
        val debit = JournalEntryLine(lineNumber = 1, accountNumber = "1000", debitAmount = BigDecimal("50"))
        val credit = JournalEntryLine(lineNumber = 2, accountNumber = "2000", creditAmount = BigDecimal("50"))
        return JournalEntry(
            entryNumber = entryNumber,
            companyCode = "1000",
            entryType = JournalEntryType.STANDARD,
            documentDate = LocalDate.now(),
            postingDate = LocalDate.now(),
            fiscalYear = 2026,
            fiscalPeriod = 1,
            currency = "USD",
            description = "Test",
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
