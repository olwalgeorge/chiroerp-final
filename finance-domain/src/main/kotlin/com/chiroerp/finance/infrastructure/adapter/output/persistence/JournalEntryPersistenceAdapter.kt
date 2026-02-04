package com.chiroerp.finance.infrastructure.adapter.output.persistence

import com.chiroerp.finance.application.port.output.JournalEntryPort
import com.chiroerp.finance.domain.JournalEntry
import com.chiroerp.finance.infrastructure.persistence.JournalEntryEntity
import com.chiroerp.finance.infrastructure.persistence.JournalEntryRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

/**
 * Persistence adapter implementing JournalEntryPort using Panache repository.
 */
@ApplicationScoped
class JournalEntryPersistenceAdapter : JournalEntryPort {

    @Inject
    lateinit var repository: JournalEntryRepository

    override fun findByTenantAndEntryNumber(tenantId: UUID, entryNumber: String): JournalEntry? {
        return repository.findByTenantAndEntryNumber(tenantId, entryNumber)?.toDomain()
    }

    override fun save(entry: JournalEntry, tenantId: UUID): JournalEntry {
        val entity = JournalEntryEntity.fromDomain(entry, tenantId)
        repository.persist(entity)
        return entity.toDomain()
    }

    override fun update(entry: JournalEntry, tenantId: UUID): JournalEntry {
        val existing = repository.findByTenantAndEntryNumber(tenantId, entry.entryNumber)
            ?: throw IllegalArgumentException("Journal entry ${entry.entryNumber} not found for tenant $tenantId")

        // Update mutable fields
        existing.status = entry.status
        existing.postedBy = entry.postedBy
        existing.postedAt = entry.postedAt
        existing.updatedBy = entry.postedBy ?: existing.updatedBy
        existing.updatedAt = entry.postedAt ?: existing.updatedAt
        existing.reversedBy = entry.reversedBy
        existing.reversalEntryId = entry.reversalEntryId

        return existing.toDomain()
    }
}
