package com.chiroerp.finance.infrastructure.persistence

import com.chiroerp.finance.domain.JournalEntryStatus
import com.chiroerp.finance.domain.JournalEntryType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate
import java.util.UUID

/**
 * Repository for Journal Entry persistence using Panache.
 * 
 * Provides tenant-aware query methods for journal entries.
 * All queries filter by tenant ID to enforce multi-tenancy isolation.
 * 
 * Related ADRs:
 * - ADR-009: Financial Accounting Domain
 * - ADR-005: Multi-Tenancy Isolation
 */
@ApplicationScoped
class JournalEntryRepository : PanacheRepositoryBase<JournalEntryEntity, String> {
    
    /**
     * Find journal entry by entry number within tenant.
     */
    fun findByTenantAndEntryNumber(
        tenantId: UUID,
        entryNumber: String
    ): JournalEntryEntity? {
        return find("tenantId = ?1 and entryNumber = ?2", tenantId, entryNumber)
            .firstResult()
    }
    
    /**
     * Find all journal entries for a tenant.
     */
    fun findByTenant(tenantId: UUID): List<JournalEntryEntity> {
        return find("tenantId", tenantId)
            .list()
    }
    
    /**
     * Find journal entries by status within tenant.
     */
    fun findByTenantAndStatus(
        tenantId: UUID,
        status: JournalEntryStatus
    ): List<JournalEntryEntity> {
        return find("tenantId = ?1 and status = ?2", tenantId, status)
            .list()
    }
    
    /**
     * Find journal entries by type within tenant.
     */
    fun findByTenantAndType(
        tenantId: UUID,
        entryType: JournalEntryType
    ): List<JournalEntryEntity> {
        return find("tenantId = ?1 and entryType = ?2", tenantId, entryType)
            .list()
    }
    
    /**
     * Find journal entries within date range for a tenant.
     */
    fun findByTenantAndDateRange(
        tenantId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<JournalEntryEntity> {
        return find(
            "tenantId = ?1 and postingDate >= ?2 and postingDate <= ?3",
            tenantId,
            startDate,
            endDate
        ).list()
    }
    
    /**
     * Find journal entries by company code within tenant.
     */
    fun findByTenantAndCompanyCode(
        tenantId: UUID,
        companyCode: String
    ): List<JournalEntryEntity> {
        return find("tenantId = ?1 and companyCode = ?2", tenantId, companyCode)
            .list()
    }
    
    /**
     * Find journal entries by fiscal period within tenant.
     */
    fun findByTenantAndFiscalPeriod(
        tenantId: UUID,
        fiscalYear: Int,
        fiscalPeriod: Int
    ): List<JournalEntryEntity> {
        return find(
            "tenantId = ?1 and fiscalYear = ?2 and fiscalPeriod = ?3",
            tenantId,
            fiscalYear,
            fiscalPeriod
        ).list()
    }
    
    /**
     * Check if entry number exists within tenant.
     */
    fun existsByTenantAndEntryNumber(
        tenantId: UUID,
        entryNumber: String
    ): Boolean {
        return count("tenantId = ?1 and entryNumber = ?2", tenantId, entryNumber) > 0
    }
    
    /**
     * Find draft entries for a tenant.
     */
    fun findDraftsByTenant(tenantId: UUID): List<JournalEntryEntity> {
        return find(
            "tenantId = ?1 and status = ?2",
            tenantId,
            JournalEntryStatus.DRAFT
        ).list()
    }
    
    /**
     * Find posted entries for a tenant.
     */
    fun findPostedByTenant(tenantId: UUID): List<JournalEntryEntity> {
        return find(
            "tenantId = ?1 and status = ?2",
            tenantId,
            JournalEntryStatus.POSTED
        ).list()
    }
    
    /**
     * Find entries created by a specific user within tenant.
     */
    fun findByTenantAndCreatedBy(
        tenantId: UUID,
        createdBy: String
    ): List<JournalEntryEntity> {
        return find("tenantId = ?1 and createdBy = ?2", tenantId, createdBy)
            .list()
    }
    
    /**
     * Find entries with a specific reference within tenant.
     */
    fun findByTenantAndReference(
        tenantId: UUID,
        reference: String
    ): List<JournalEntryEntity> {
        return find("tenantId = ?1 and reference = ?2", tenantId, reference)
            .list()
    }
}
