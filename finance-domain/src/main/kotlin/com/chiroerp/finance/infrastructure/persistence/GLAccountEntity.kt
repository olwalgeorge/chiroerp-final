package com.chiroerp.finance.infrastructure.persistence

import com.chiroerp.finance.domain.AccountType
import com.chiroerp.finance.domain.BalanceType
import com.chiroerp.finance.domain.GLAccount
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * JPA Entity for GL Account persistence
 * Maps domain model to database table finance.gl_accounts
 */
@Entity
@Table(
    name = "gl_accounts",
    schema = "finance",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_gl_account_number", columnNames = ["tenant_id", "account_number"])
    ],
    indexes = [
        Index(name = "idx_gl_accounts_tenant", columnList = "tenant_id"),
        Index(name = "idx_gl_accounts_type", columnList = "account_type"),
        Index(name = "idx_gl_accounts_parent", columnList = "parent_account_id"),
        Index(name = "idx_gl_accounts_number", columnList = "tenant_id, account_number")
    ]
)
class GLAccountEntity : PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID()

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID = UUID.randomUUID()

    @Column(name = "account_number", nullable = false, length = 50)
    var accountNumber: String = ""

    @Column(name = "account_name", nullable = false, length = 255)
    var accountName: String = ""

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 50)
    var accountType: AccountType = AccountType.ASSET

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_type", nullable = false, length = 10)
    var balanceType: BalanceType = BalanceType.DEBIT

    @Column(name = "parent_account_id")
    var parentAccountId: String? = null

    @Column(name = "currency_code", nullable = false, length = 3)
    var currencyCode: String = "USD"

    @Column(name = "is_control_account", nullable = false)
    var isControlAccount: Boolean = false

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "is_posting_allowed", nullable = false)
    var isPostingAllowed: Boolean = true

    @Column(name = "cost_center_id")
    var costCenter: String? = null

    @Column(name = "profit_center_id")
    var profitCenter: String? = null
    
    @Column(name = "company_code", nullable = false, length = 20)
    var companyCode: String = "1000"
    
    @Column(name = "description")
    var description: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "created_by", nullable = false)
    var createdBy: UUID = UUID.randomUUID()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

    @Column(name = "updated_by", nullable = false)
    var updatedBy: UUID = UUID.randomUUID()

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0

    companion object {
        /**
         * Convert domain model to JPA entity
         */
        fun fromDomain(account: GLAccount, tenantId: UUID, userId: UUID): GLAccountEntity {
            return GLAccountEntity().apply {
                id = UUID.randomUUID()
                this.tenantId = tenantId
                accountNumber = account.accountNumber
                accountName = account.accountName
                accountType = account.accountType
                balanceType = account.balanceType
                parentAccountId = account.parentAccount
                currencyCode = account.currency
                isControlAccount = false
                isActive = account.isActive
                isPostingAllowed = account.isPostingAllowed
                costCenter = account.costCenter
                profitCenter = account.profitCenter
                companyCode = account.companyCode
                description = account.description
                createdAt = account.createdDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                createdBy = userId
                updatedAt = account.lastModifiedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                updatedBy = userId
            }
        }
    }

    /**
     * Convert JPA entity to domain model
     */
    fun toDomain(): GLAccount {
        return GLAccount(
            accountNumber = accountNumber,
            accountName = accountName,
            accountType = accountType,
            balanceType = balanceType,
            currency = currencyCode,
            companyCode = companyCode,
            isActive = isActive,
            isPostingAllowed = isPostingAllowed,
            parentAccount = parentAccountId,
            costCenter = costCenter,
            profitCenter = profitCenter,
            description = description,
            createdDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDate(),
            lastModifiedDate = updatedAt.atZone(ZoneId.systemDefault()).toLocalDate()
        )
    }

    /**
     * Update entity from domain model (for updates)
     */
    fun updateFromDomain(account: GLAccount, userId: UUID) {
        accountName = account.accountName
        isActive = account.isActive
        isPostingAllowed = account.isPostingAllowed
        costCenter = account.costCenter
        profitCenter = account.profitCenter
        description = account.description
        updatedAt = Instant.now()
        updatedBy = userId
        // version is managed by JPA @Version
    }
}
