package com.chiroerp.finance.application.port.output

import com.chiroerp.finance.domain.GLAccount
import java.util.UUID

/**
 * Output port for GL Account persistence.
 *
 * This port defines the contract for persisting and retrieving GL accounts.
 * Implementations will handle the actual persistence mechanism.
 *
 * Related ADRs: ADR-001 (Hexagonal Architecture - Ports & Adapters)
 */
interface GLAccountPort {
    fun findActiveByTenant(tenantId: UUID): List<GLAccount>
    fun findByTenantAndAccountNumber(tenantId: UUID, accountNumber: String): GLAccount?
    fun existsByTenantAndAccountNumber(tenantId: UUID, accountNumber: String): Boolean
    fun save(account: GLAccount, tenantId: UUID, userId: UUID): GLAccount
    fun update(account: GLAccount, tenantId: UUID, userId: UUID): GLAccount
}
