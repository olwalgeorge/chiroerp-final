package com.chiroerp.finance.application.service

import com.chiroerp.finance.application.port.input.command.CreateGLAccountCommand
import com.chiroerp.finance.application.port.output.GLAccountPort
import com.chiroerp.finance.domain.AccountType
import com.chiroerp.finance.domain.BalanceType
import com.chiroerp.finance.domain.GLAccount
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import java.time.LocalDate

/**
 * Use case for creating a new GL Account.
 *
 * Orchestrates the business logic for account creation including:
 * - Validation of account uniqueness
 * - Domain model creation
 * - Persistence
 *
 * Related ADRs: ADR-001 (Pragmatic CQRS - Use Cases)
 */
@ApplicationScoped
class CreateGLAccountUseCase {

    @Inject
    lateinit var glAccountPort: GLAccountPort

    @Transactional
    fun execute(@Valid command: CreateGLAccountCommand): GLAccount {
        // Business Rule: Account number must be unique per tenant
        if (glAccountPort.existsByTenantAndAccountNumber(command.tenantId, command.accountNumber)) {
            throw IllegalArgumentException("Account number ${command.accountNumber} already exists for this tenant")
        }

        // Create domain model
        val account = GLAccount(
            accountNumber = command.accountNumber,
            accountName = command.accountName,
            accountType = AccountType.valueOf(command.accountType),
            balanceType = BalanceType.valueOf(command.balanceType),
            currency = command.currency,
            companyCode = command.companyCode,
            isActive = command.isActive,
            isPostingAllowed = command.isPostingAllowed,
            parentAccount = command.parentAccount,
            costCenter = command.costCenter,
            profitCenter = command.profitCenter,
            description = command.description,
            createdDate = LocalDate.now(),
            lastModifiedDate = LocalDate.now()
        )

        // Persist via output port
        return glAccountPort.save(account, command.tenantId, command.userId)
    }
}
