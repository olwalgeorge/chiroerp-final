package com.chiroerp.finance.infrastructure.adapter.output.persistence

import com.chiroerp.finance.application.port.output.GLAccountPort
import com.chiroerp.finance.domain.GLAccount
import com.chiroerp.finance.infrastructure.persistence.GLAccountEntity
import com.chiroerp.finance.infrastructure.persistence.GLAccountRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

/**
 * Persistence adapter implementing GLAccountPort using Panache repository.
 *
 * Related ADRs: ADR-001 (Hexagonal Ports & Adapters)
 */
@ApplicationScoped
class GLAccountPersistenceAdapter : GLAccountPort {

    @Inject
    lateinit var repository: GLAccountRepository

    override fun findActiveByTenant(tenantId: UUID): List<GLAccount> =
        repository.findActiveByTenant(tenantId).map { it.toDomain() }

    override fun findByTenantAndAccountNumber(tenantId: UUID, accountNumber: String): GLAccount? =
        repository.findByTenantAndAccountNumber(tenantId, accountNumber)?.toDomain()

    override fun existsByTenantAndAccountNumber(tenantId: UUID, accountNumber: String): Boolean =
        repository.existsByTenantAndAccountNumber(tenantId, accountNumber)

    override fun save(account: GLAccount, tenantId: UUID, userId: UUID): GLAccount {
        val entity = GLAccountEntity.fromDomain(account, tenantId, userId)
        repository.persist(entity)
        return entity.toDomain()
    }

    override fun update(account: GLAccount, tenantId: UUID, userId: UUID): GLAccount {
        val entity = repository.findByTenantAndAccountNumber(tenantId, account.accountNumber)
            ?: throw IllegalArgumentException("Account ${account.accountNumber} not found for tenant $tenantId")
        entity.updateFromDomain(account, userId)
        return entity.toDomain()
    }
}
