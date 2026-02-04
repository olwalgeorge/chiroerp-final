package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.query.GetGLAccountQuery
import com.chiroerp.finance.application.port.output.GLAccountPort
import com.chiroerp.finance.domain.GLAccount
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.validation.Valid

/**
 * Use case for getting a specific GL account.
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS - Use Cases)
 */
@ApplicationScoped
class GetGLAccountUseCase {

    @Inject
    lateinit var glAccountPort: GLAccountPort

    fun execute(@Valid query: GetGLAccountQuery): GLAccount? {
        return glAccountPort.findByTenantAndAccountNumber(query.tenantId, query.accountNumber)
    }
}
