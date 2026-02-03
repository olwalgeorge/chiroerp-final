package com.chiroerp.finance.infrastructure.persistence

import com.chiroerp.finance.domain.AccountType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

/**
 * Repository for GL Account persistence operations.
 * 
 * Provides tenant-aware queries for GL accounts with support for:
 * - Account lookup by number
 * - Hierarchical parent-child relationships
 * - Active account filtering
 * - Cost center and profit center queries
 * 
 * All queries enforce tenant isolation.
 */
@ApplicationScoped
class GLAccountRepository : PanacheRepositoryBase<GLAccountEntity, UUID> {
    
    /**
     * Find an active GL account by tenant and account number.
     */
    fun findByTenantAndAccountNumber(tenantId: UUID, accountNumber: String): GLAccountEntity? {
        return find("tenantId = ?1 and accountNumber = ?2 and isActive = true", tenantId, accountNumber)
            .firstResult()
    }
    
    /**
     * Find all active GL accounts for a tenant.
     */
    fun findActiveByTenant(tenantId: UUID): List<GLAccountEntity> {
        return find("tenantId = ?1 and isActive = true", tenantId)
            .list()
    }
    
    /**
     * Find active accounts by type and tenant.
     */
    fun findByTenantAndType(
        tenantId: UUID,
        accountType: AccountType
    ): List<GLAccountEntity> {
        return find("tenantId = ?1 and accountType = ?2 and isActive = true", tenantId, accountType)
            .list()
    }
    
    /**
     * Find child accounts of a parent account.
     */
    fun findChildAccounts(tenantId: UUID, parentAccountId: String): List<GLAccountEntity> {
        return find("tenantId = ?1 and parentAccountId = ?2 and isActive = true", tenantId, parentAccountId)
            .list()
    }
    
    /**
     * Find accounts assigned to a cost center.
     */
    fun findByCostCenter(tenantId: UUID, costCenter: String): List<GLAccountEntity> {
        return find("tenantId = ?1 and costCenter = ?2 and isActive = true", tenantId, costCenter)
            .list()
    }
    
    /**
     * Find accounts assigned to a profit center.
     */
    fun findByProfitCenter(tenantId: UUID, profitCenter: String): List<GLAccountEntity> {
        return find("tenantId = ?1 and profitCenter = ?2 and isActive = true", tenantId, profitCenter)
            .list()
    }
    
    /**
     * Check if an account number already exists for a tenant.
     */
    fun existsByTenantAndAccountNumber(tenantId: UUID, accountNumber: String): Boolean {
        return count("tenantId = ?1 and accountNumber = ?2", tenantId, accountNumber) > 0
    }
    
    /**
     * Find all posting-allowed accounts for a tenant.
     */
    fun findPostingAccountsByTenant(tenantId: UUID): List<GLAccountEntity> {
        return find(
            "tenantId = ?1 and isPostingAllowed = true and isActive = true",
            tenantId
        ).list()
    }
    
    /**
     * Find control accounts (summary accounts) for a tenant.
     */
    fun findControlAccountsByTenant(tenantId: UUID): List<GLAccountEntity> {
        return find(
            "tenantId = ?1 and isControlAccount = true and isActive = true",
            tenantId
        ).list()
    }
}
