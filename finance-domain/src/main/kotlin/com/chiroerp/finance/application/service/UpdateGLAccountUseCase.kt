package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.command.UpdateGLAccountCommand
import com.chiroerp.finance.application.port.output.GLAccountPort
import com.chiroerp.finance.domain.GLAccount
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import java.time.LocalDate

/**
 * Use case for updating an existing GL Account.
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS - Use Cases)
 */
@ApplicationScoped
class UpdateGLAccountUseCase {

    @Inject
    lateinit var glAccountPort: GLAccountPort

    @Transactional
    fun execute(@Valid command: UpdateGLAccountCommand): GLAccount {
        // Retrieve existing account
        val existing = glAccountPort.findByTenantAndAccountNumber(command.tenantId, command.accountNumber)
            ?: throw IllegalArgumentException("Account ${command.accountNumber} not found")

        // Update mutable fields
        val updated = existing.copy(
            accountName = command.accountName,
            isActive = command.isActive,
            isPostingAllowed = command.isPostingAllowed,
            costCenter = command.costCenter,
            profitCenter = command.profitCenter,
            description = command.description,
            lastModifiedDate = LocalDate.now()
        )

        // Persist via output port
        return glAccountPort.update(updated, command.tenantId, command.userId)
    }
}
