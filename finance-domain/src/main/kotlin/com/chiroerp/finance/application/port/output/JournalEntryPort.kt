package com.chiroerp.finance.application.port.output

import com.chiroerp.finance.domain.JournalEntry
import java.util.UUID

/**
 * Output port for Journal Entry persistence.
 *
 * This port defines the contract for persisting and retrieving journal entries.
 * Implementations will handle the actual persistence mechanism.
 *
 * Related ADRs: ADR-001 (Hexagonal Architecture - Ports & Adapters)
 */
interface JournalEntryPort {
    fun findByTenantAndEntryNumber(tenantId: UUID, entryNumber: String): JournalEntry?
    fun save(entry: JournalEntry, tenantId: UUID): JournalEntry
    fun update(entry: JournalEntry, tenantId: UUID): JournalEntry
}
