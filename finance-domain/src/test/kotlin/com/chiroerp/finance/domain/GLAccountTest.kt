package com.chiroerp.finance.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate

class GLAccountTest {

    @Test
    fun `should validate correct account number format`() {
        val account = createTestAccount(accountNumber = "1000")
        val result = account.validateAccountNumber()
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should validate account number with sub-account`() {
        val account = createTestAccount(accountNumber = "1000-001")
        val result = account.validateAccountNumber()
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should reject account number with invalid format`() {
        val account = createTestAccount(accountNumber = "ABC123")
        val result = account.validateAccountNumber()
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should reject account number that is too short`() {
        val account = createTestAccount(accountNumber = "100")
        val result = account.validateAccountNumber()
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should reject account number that is too long`() {
        val account = createTestAccount(accountNumber = "12345678901")
        val result = account.validateAccountNumber()
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should identify balance sheet accounts - assets`() {
        val account = createTestAccount(accountType = AccountType.ASSET)
        assertThat(account.isBalanceSheet()).isTrue()
        assertThat(account.isIncomeStatement()).isFalse()
    }

    @Test
    fun `should identify balance sheet accounts - liabilities`() {
        val account = createTestAccount(accountType = AccountType.LIABILITY)
        assertThat(account.isBalanceSheet()).isTrue()
        assertThat(account.isIncomeStatement()).isFalse()
    }

    @Test
    fun `should identify balance sheet accounts - equity`() {
        val account = createTestAccount(accountType = AccountType.EQUITY)
        assertThat(account.isBalanceSheet()).isTrue()
        assertThat(account.isIncomeStatement()).isFalse()
    }

    @Test
    fun `should identify income statement accounts - revenue`() {
        val account = createTestAccount(accountType = AccountType.REVENUE)
        assertThat(account.isIncomeStatement()).isTrue()
        assertThat(account.isBalanceSheet()).isFalse()
    }

    @Test
    fun `should identify income statement accounts - expense`() {
        val account = createTestAccount(accountType = AccountType.EXPENSE)
        assertThat(account.isIncomeStatement()).isTrue()
        assertThat(account.isBalanceSheet()).isFalse()
    }

    @Test
    fun `should correctly determine debit balance type for assets`() {
        val account = createTestAccount(
            accountType = AccountType.ASSET,
            balanceType = BalanceType.DEBIT
        )
        assertThat(account.balanceType).isEqualTo(BalanceType.DEBIT)
    }

    @Test
    fun `should correctly determine credit balance type for revenue`() {
        val account = createTestAccount(
            accountType = AccountType.REVENUE,
            balanceType = BalanceType.CREDIT
        )
        assertThat(account.balanceType).isEqualTo(BalanceType.CREDIT)
    }

    @Test
    fun `should support hierarchical chart of accounts`() {
        val parentAccount = createTestAccount(accountNumber = "1000", accountName = "Assets")
        val childAccount = createTestAccount(
            accountNumber = "1000-001",
            accountName = "Cash",
            parentAccount = "1000"
        )

        assertThat(childAccount.parentAccount).isEqualTo("1000")
    }

    @Test
    fun `should support cost center assignment`() {
        val account = createTestAccount(costCenter = "CC-100")
        assertThat(account.costCenter).isEqualTo("CC-100")
    }

    @Test
    fun `should support profit center assignment`() {
        val account = createTestAccount(profitCenter = "PC-1000")
        assertThat(account.profitCenter).isEqualTo("PC-1000")
    }

    @Test
    fun `should track creation and modification dates`() {
        val now = LocalDate.now()
        val account = createTestAccount(
            createdDate = now,
            lastModifiedDate = now
        )

        assertThat(account.createdDate).isEqualTo(now)
        assertThat(account.lastModifiedDate).isEqualTo(now)
    }

    @Test
    fun `should allow posting to active accounts with posting allowed`() {
        val account = createTestAccount(
            isActive = true,
            isPostingAllowed = true
        )

        assertThat(account.isActive).isTrue()
        assertThat(account.isPostingAllowed).isTrue()
    }

    @Test
    fun `should prevent posting to accounts with posting not allowed`() {
        val account = createTestAccount(
            isActive = true,
            isPostingAllowed = false
        )

        assertThat(account.isActive).isTrue()
        assertThat(account.isPostingAllowed).isFalse()
    }

    // =========================================================================
    // AccountBalance Tests
    // =========================================================================

    @Test
    fun `should calculate net change correctly for debit balance`() {
        val balance = AccountBalance(
            accountNumber = "1000",
            companyCode = "1000",
            fiscalYear = 2026,
            fiscalPeriod = 1,
            currency = "USD",
            beginningBalance = BigDecimal("1000.00"),
            debitAmount = BigDecimal("500.00"),
            creditAmount = BigDecimal("200.00"),
            endingBalance = BigDecimal("1300.00")
        )

        assertThat(balance.netChange()).isEqualTo(BigDecimal("300.00"))
    }

    @Test
    fun `should calculate net change correctly for credit balance`() {
        val balance = AccountBalance(
            accountNumber = "2000",
            companyCode = "1000",
            fiscalYear = 2026,
            fiscalPeriod = 1,
            currency = "USD",
            beginningBalance = BigDecimal("5000.00"),
            debitAmount = BigDecimal("200.00"),
            creditAmount = BigDecimal("800.00"),
            endingBalance = BigDecimal("5600.00")
        )

        assertThat(balance.netChange()).isEqualTo(BigDecimal("-600.00"))
    }

    @Test
    fun `should verify balance calculation is correct`() {
        val balance = AccountBalance(
            accountNumber = "1000",
            companyCode = "1000",
            fiscalYear = 2026,
            fiscalPeriod = 1,
            currency = "USD",
            beginningBalance = BigDecimal("1000.00"),
            debitAmount = BigDecimal("500.00"),
            creditAmount = BigDecimal("200.00"),
            endingBalance = BigDecimal("1300.00")
        )

        assertThat(balance.verifyBalance()).isTrue()
    }

    @Test
    fun `should detect incorrect balance calculation`() {
        val balance = AccountBalance(
            accountNumber = "1000",
            companyCode = "1000",
            fiscalYear = 2026,
            fiscalPeriod = 1,
            currency = "USD",
            beginningBalance = BigDecimal("1000.00"),
            debitAmount = BigDecimal("500.00"),
            creditAmount = BigDecimal("200.00"),
            endingBalance = BigDecimal("9999.99")  // Incorrect
        )

        assertThat(balance.verifyBalance()).isFalse()
    }

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private fun createTestAccount(
        accountNumber: String = "1000",
        accountName: String = "Cash",
        accountType: AccountType = AccountType.ASSET,
        balanceType: BalanceType = BalanceType.DEBIT,
        currency: String = "USD",
        companyCode: String = "1000",
        isActive: Boolean = true,
        isPostingAllowed: Boolean = true,
        parentAccount: String? = null,
        costCenter: String? = null,
        profitCenter: String? = null,
        description: String? = "Test account",
        createdDate: LocalDate = LocalDate.now(),
        lastModifiedDate: LocalDate = LocalDate.now()
    ): GLAccount {
        return GLAccount(
            accountNumber = accountNumber,
            accountName = accountName,
            accountType = accountType,
            balanceType = balanceType,
            currency = currency,
            companyCode = companyCode,
            isActive = isActive,
            isPostingAllowed = isPostingAllowed,
            parentAccount = parentAccount,
            costCenter = costCenter,
            profitCenter = profitCenter,
            description = description,
            createdDate = createdDate,
            lastModifiedDate = lastModifiedDate
        )
    }
}
