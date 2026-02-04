package com.chiroerp.finance.infrastructure

import com.chiroerp.finance.domain.AccountType
import com.chiroerp.finance.domain.BalanceType
import com.chiroerp.finance.domain.JournalEntryStatus
import com.chiroerp.finance.domain.JournalEntryType
import com.chiroerp.finance.infrastructure.persistence.GLAccountEntity
import com.chiroerp.finance.infrastructure.persistence.JournalEntryEntity
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * CRITICAL SECURITY TESTS: Tenant Isolation
 *
 * Validates that tenant A cannot access tenant B's data.
 * These tests must NEVER fail - they prevent cross-tenant data leaks.
 *
 * Related ADRs: ADR-005 (Multi-Tenancy Isolation)
 */
@QuarkusTest
class TenantIsolationTest {

    @Inject
    lateinit var entityManager: EntityManager

    private val tenantA = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val tenantB = UUID.fromString("00000000-0000-0000-0000-000000000002")

    @BeforeEach
    @Transactional
    fun setup() {
        // Clean up existing test data for our test tenants using native queries
        entityManager.createNativeQuery("DELETE FROM finance.journal_entry_lines WHERE journal_entry_id IN (SELECT id FROM finance.journal_entries WHERE tenant_id IN (:tenantA, :tenantB))")
            .setParameter("tenantA", tenantA)
            .setParameter("tenantB", tenantB)
            .executeUpdate()

        entityManager.createNativeQuery("DELETE FROM finance.journal_entries WHERE tenant_id IN (:tenantA, :tenantB)")
            .setParameter("tenantA", tenantA)
            .setParameter("tenantB", tenantB)
            .executeUpdate()

        entityManager.createNativeQuery("DELETE FROM finance.gl_accounts WHERE tenant_id IN (:tenantA, :tenantB)")
            .setParameter("tenantA", tenantA)
            .setParameter("tenantB", tenantB)
            .executeUpdate()
    }

    @Test
    @Transactional
    fun `tenant A cannot see tenant B GL accounts`() {
        // Given: GL accounts for two different tenants
        val accountA = createGLAccount(tenantA, "1000", "Cash - Tenant A")
        val accountB = createGLAccount(tenantB, "1000", "Cash - Tenant B")

        entityManager.persist(accountA)
        entityManager.persist(accountB)
        entityManager.flush()

        // When: Tenant A queries for account 1000
        val result = entityManager.createQuery(
            "SELECT a FROM GLAccountEntity a WHERE a.tenantId = :tenantId AND a.accountNumber = :accountNumber AND a.isActive = true",
            GLAccountEntity::class.java
        )
            .setParameter("tenantId", tenantA)
            .setParameter("accountNumber", "1000")
            .resultList.firstOrNull()

        // Then: Should only see their own account
        assertThat(result).isNotNull
        assertThat(result!!.accountName).isEqualTo("Cash - Tenant A")
        assertThat(result.tenantId).isEqualTo(tenantA)

        // And: Tenant B should see their own account
        val resultB = entityManager.createQuery(
            "SELECT a FROM GLAccountEntity a WHERE a.tenantId = :tenantId AND a.accountNumber = :accountNumber AND a.isActive = true",
            GLAccountEntity::class.java
        )
            .setParameter("tenantId", tenantB)
            .setParameter("accountNumber", "1000")
            .resultList.firstOrNull()

        assertThat(resultB).isNotNull
        assertThat(resultB!!.accountName).isEqualTo("Cash - Tenant B")
        assertThat(resultB.tenantId).isEqualTo(tenantB)
    }

    @Test
    @Transactional
    fun `listActiveByTenant only returns tenant-specific accounts`() {
        // Given: Multiple accounts for different tenants
        val accountsA = listOf(
            createGLAccount(tenantA, "1000", "Cash"),
            createGLAccount(tenantA, "1100", "AR"),
            createGLAccount(tenantA, "2000", "AP")
        )
        val accountsB = listOf(
            createGLAccount(tenantB, "1000", "Cash"),
            createGLAccount(tenantB, "1100", "AR")
        )

        accountsA.forEach { entityManager.persist(it) }
        accountsB.forEach { entityManager.persist(it) }
        entityManager.flush()

        // When: Tenant A lists their accounts
        val resultA = entityManager.createQuery(
            "SELECT a FROM GLAccountEntity a WHERE a.tenantId = :tenantId AND a.isActive = true",
            GLAccountEntity::class.java
        )
            .setParameter("tenantId", tenantA)
            .resultList

        // Then: Should only see 3 accounts (tenant A)
        assertThat(resultA).hasSize(3)
        assertThat(resultA.map { it.accountNumber }).containsExactlyInAnyOrder("1000", "1100", "2000")
        assertThat(resultA).allMatch { it.tenantId == tenantA }

        // When: Tenant B lists their accounts
        val resultB = entityManager.createQuery(
            "SELECT a FROM GLAccountEntity a WHERE a.tenantId = :tenantId AND a.isActive = true",
            GLAccountEntity::class.java
        )
            .setParameter("tenantId", tenantB)
            .resultList

        // Then: Should only see 2 accounts (tenant B)
        assertThat(resultB).hasSize(2)
        assertThat(resultB.map { it.accountNumber }).containsExactlyInAnyOrder("1000", "1100")
        assertThat(resultB).allMatch { it.tenantId == tenantB }
    }

    @Test
    @Transactional
    fun `tenant A cannot see tenant B journal entries`() {
        // Given: Journal entries for two different tenants
        val entryA = createJournalEntry(tenantA, "JE-2026-001", "Entry for Tenant A")
        val entryB = createJournalEntry(tenantB, "JE-2026-001", "Entry for Tenant B")

        entityManager.persist(entryA)
        entityManager.persist(entryB)
        entityManager.flush()

        // When: Tenant A queries for JE-2026-001
        val result = entityManager.createQuery(
            "SELECT j FROM JournalEntryEntity j WHERE j.tenantId = :tenantId AND j.entryNumber = :entryNumber",
            JournalEntryEntity::class.java
        )
            .setParameter("tenantId", tenantA)
            .setParameter("entryNumber", "JE-2026-001")
            .resultList.firstOrNull()

        // Then: Should only see their own entry
        assertThat(result).isNotNull
        assertThat(result!!.description).isEqualTo("Entry for Tenant A")
        assertThat(result.tenantId).isEqualTo(tenantA)

        // And: Tenant B should see their own entry
        val resultB = entityManager.createQuery(
            "SELECT j FROM JournalEntryEntity j WHERE j.tenantId = :tenantId AND j.entryNumber = :entryNumber",
            JournalEntryEntity::class.java
        )
            .setParameter("tenantId", tenantB)
            .setParameter("entryNumber", "JE-2026-001")
            .resultList.firstOrNull()

        assertThat(resultB).isNotNull
        assertThat(resultB!!.description).isEqualTo("Entry for Tenant B")
        assertThat(resultB.tenantId).isEqualTo(tenantB)
    }

    @Test
    @Transactional
    fun `findByTenant only returns tenant-specific journal entries`() {
        // Given: Multiple journal entries for different tenants
        val entriesA = listOf(
            createJournalEntry(tenantA, "JE-2026-001", "Entry 1"),
            createJournalEntry(tenantA, "JE-2026-002", "Entry 2"),
            createJournalEntry(tenantA, "JE-2026-003", "Entry 3")
        )
        val entriesB = listOf(
            createJournalEntry(tenantB, "JE-2026-001", "Entry 1"),
            createJournalEntry(tenantB, "JE-2026-002", "Entry 2")
        )

        entriesA.forEach { entityManager.persist(it) }
        entriesB.forEach { entityManager.persist(it) }
        entityManager.flush()

        // When: Tenant A lists their entries
        val resultA = entityManager.createQuery(
            "SELECT j FROM JournalEntryEntity j WHERE j.tenantId = :tenantId",
            JournalEntryEntity::class.java
        )
            .setParameter("tenantId", tenantA)
            .resultList

        // Then: Should only see 3 entries (tenant A)
        assertThat(resultA).hasSize(3)
        assertThat(resultA.map { it.entryNumber }).containsExactlyInAnyOrder(
            "JE-2026-001", "JE-2026-002", "JE-2026-003"
        )
        assertThat(resultA).allMatch { it.tenantId == tenantA }

        // When: Tenant B lists their entries
        val resultB = entityManager.createQuery(
            "SELECT j FROM JournalEntryEntity j WHERE j.tenantId = :tenantId",
            JournalEntryEntity::class.java
        )
            .setParameter("tenantId", tenantB)
            .resultList

        // Then: Should only see 2 entries (tenant B)
        assertThat(resultB).hasSize(2)
        assertThat(resultB.map { it.entryNumber }).containsExactlyInAnyOrder("JE-2026-001", "JE-2026-002")
        assertThat(resultB).allMatch { it.tenantId == tenantB }
    }

    @Test
    @Transactional
    fun `findByTenantAndStatus filters by both tenant and status`() {
        // Given: Mix of DRAFT and POSTED entries for different tenants
        val draftA = createJournalEntry(tenantA, "JE-DRAFT-A", "Draft A", JournalEntryStatus.DRAFT)
        val postedA = createJournalEntry(tenantA, "JE-POST-A", "Posted A", JournalEntryStatus.POSTED)
        val draftB = createJournalEntry(tenantB, "JE-DRAFT-B", "Draft B", JournalEntryStatus.DRAFT)

        entityManager.persist(draftA)
        entityManager.persist(postedA)
        entityManager.persist(draftB)
        entityManager.flush()

        // When: Tenant A queries for DRAFT entries
        val result = entityManager.createQuery(
            "SELECT j FROM JournalEntryEntity j WHERE j.tenantId = :tenantId AND j.status = :status",
            JournalEntryEntity::class.java
        )
            .setParameter("tenantId", tenantA)
            .setParameter("status", JournalEntryStatus.DRAFT)
            .resultList

        // Then: Should only see tenant A's DRAFT entry
        assertThat(result).hasSize(1)
        assertThat(result[0].entryNumber).isEqualTo("JE-DRAFT-A")
        assertThat(result[0].status).isEqualTo(JournalEntryStatus.DRAFT)
        assertThat(result[0].tenantId).isEqualTo(tenantA)
    }

    @Test
    @Transactional
    fun `findByTenantAndCompanyCode filters by tenant`() {
        // Given: Entries for same company code but different tenants
        val entryA = createJournalEntry(tenantA, "JE-001", "Entry A").apply { companyCode = "1000" }
        val entryB = createJournalEntry(tenantB, "JE-002", "Entry B").apply { companyCode = "1000" }

        entityManager.persist(entryA)
        entityManager.persist(entryB)
        entityManager.flush()

        // When: Tenant A queries for company 1000
        val result = entityManager.createQuery(
            "SELECT j FROM JournalEntryEntity j WHERE j.tenantId = :tenantId AND j.companyCode = :companyCode",
            JournalEntryEntity::class.java
        )
            .setParameter("tenantId", tenantA)
            .setParameter("companyCode", "1000")
            .resultList

        // Then: Should only see tenant A's entry
        assertThat(result).hasSize(1)
        assertThat(result[0].entryNumber).isEqualTo("JE-001")
        assertThat(result[0].tenantId).isEqualTo(tenantA)
    }

    @Test
    @Transactional
    fun `existsByTenantAndAccountNumber respects tenant boundary`() {
        // Given: Same account number for different tenants
        val accountA = createGLAccount(tenantA, "1000", "Cash A")
        entityManager.persist(accountA)
        entityManager.flush()

        // When: Check existence for both tenants
        val existsForA = entityManager.createQuery(
            "SELECT COUNT(a) FROM GLAccountEntity a WHERE a.tenantId = :tenantId AND a.accountNumber = :accountNumber",
            Long::class.java
        )
            .setParameter("tenantId", tenantA)
            .setParameter("accountNumber", "1000")
            .singleResult > 0

        val existsForB = entityManager.createQuery(
            "SELECT COUNT(a) FROM GLAccountEntity a WHERE a.tenantId = :tenantId AND a.accountNumber = :accountNumber",
            Long::class.java
        )
            .setParameter("tenantId", tenantB)
            .setParameter("accountNumber", "1000")
            .singleResult > 0

        // Then: Should exist for tenant A only
        assertThat(existsForA).isTrue()
        assertThat(existsForB).isFalse()
    }

    // =========================================================================
    // Helper Methods - Test Data Creation
    // =========================================================================

    private fun createGLAccount(
        tenantId: UUID,
        accountNumber: String,
        accountName: String
    ): GLAccountEntity {
        return GLAccountEntity().apply {
            id = UUID.randomUUID()
            this.tenantId = tenantId
            this.accountNumber = accountNumber
            this.accountName = accountName
            this.accountType = AccountType.ASSET
            this.balanceType = BalanceType.DEBIT
            this.currencyCode = "USD"
            this.companyCode = "1000"
            this.isActive = true
            this.isPostingAllowed = true
            this.description = "Test account"
            this.createdBy = UUID.randomUUID()
            this.createdAt = Instant.now()
            this.updatedBy = UUID.randomUUID()
            this.updatedAt = Instant.now()
            this.version = 0L
        }
    }

    private fun createJournalEntry(
        tenantId: UUID,
        entryNumber: String,
        description: String,
        status: JournalEntryStatus = JournalEntryStatus.DRAFT
    ): JournalEntryEntity {
        return JournalEntryEntity().apply {
            id = UUID.randomUUID()
            this.tenantId = tenantId
            this.entryNumber = entryNumber
            this.companyCode = "1000"
            this.entryType = JournalEntryType.STANDARD
            this.documentDate = LocalDate.now()
            this.postingDate = LocalDate.now()
            this.fiscalYear = 2026
            this.fiscalPeriod = 2
            this.description = description
            this.status = status
            this.currencyCode = "USD"
            this.createdAt = Instant.now()
            this.createdBy = "test-user"
            this.updatedAt = Instant.now()
            this.updatedBy = "test-user"
            this.version = 0L
        }
    }
}
