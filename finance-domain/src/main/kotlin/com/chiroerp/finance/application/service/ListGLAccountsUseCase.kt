package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.query.ListGLAccountsQuery
import com.chiroerp.finance.application.port.output.GLAccountPort
import com.chiroerp.finance.domain.GLAccount
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.validation.Valid

/**
 * Use case for listing all active GL accounts.
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS - Use Cases)
 */
@ApplicationScoped
class ListGLAccountsUseCase {

    @Inject
    lateinit var glAccountPort: GLAccountPort

    fun execute(@Valid query: ListGLAccountsQuery): List<GLAccount> {
        return glAccountPort.findActiveByTenant(query.tenantId)
    }
}
