package com.chiroerp.finance.domain

import java.math.BigDecimal
import java.time.LocalDate

/**
 * General Ledger Account
 * 
 * Represents a GL account in the chart of accounts.
 * Tracks account metadata, balances, and posting rules.
 * 
 * Related ADRs: ADR-009 (Financial Accounting Domain)
 */
data class GLAccount(
    val accountNumber: String,           // e.g., "1000", "4000-001"
    val accountName: String,             // e.g., "Cash", "Revenue - Product Sales"
    val accountType: AccountType,
    val balanceType: BalanceType,
    val currency: String,                // e.g., "USD", "EUR"
    val companyCode: String,             // e.g., "1000"
    val isActive: Boolean = true,
    val isPostingAllowed: Boolean = true, // Some accounts are totals-only
    val parentAccount: String? = null,   // For hierarchical chart of accounts
    val costCenter: String? = null,      // Default cost center
    val profitCenter: String? = null,    // Default profit center
    val description: String? = null,
    val createdDate: LocalDate = LocalDate.now(),
    val lastModifiedDate: LocalDate = LocalDate.now()
) {
    /**
     * Check if this is a balance sheet account.
     */
    fun isBalanceSheet(): Boolean {
        return accountType in setOf(
            AccountType.ASSET,
            AccountType.LIABILITY,
            AccountType.EQUITY
        )
    }
    
    /**
     * Check if this is an income statement account.
     */
    fun isIncomeStatement(): Boolean {
        return accountType in setOf(
            AccountType.REVENUE,
            AccountType.EXPENSE
        )
    }
    
    /**
     * Validate account number format.
     */
    fun validateAccountNumber(): Result<Unit> {
        if (accountNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Account number cannot be blank"))
        }
        if (!accountNumber.matches(Regex("^[0-9]{4,10}(-[0-9]{3})?$"))) {
            return Result.failure(IllegalArgumentException("Invalid account number format: $accountNumber"))
        }
        return Result.success(Unit)
    }
}

/**
 * Account type in the chart of accounts.
 */
enum class AccountType {
    /**
     * Assets (1000-1999): Cash, Accounts Receivable, Inventory, Fixed Assets
     */
    ASSET,
    
    /**
     * Liabilities (2000-2999): Accounts Payable, Loans, Accrued Expenses
     */
    LIABILITY,
    
    /**
     * Equity (3000-3999): Common Stock, Retained Earnings
     */
    EQUITY,
    
    /**
     * Revenue (4000-4999): Sales, Service Revenue, Interest Income
     */
    REVENUE,
    
    /**
     * Expenses (5000-9999): COGS, Salaries, Rent, Utilities
     */
    EXPENSE
}

/**
 * Normal balance direction for an account.
 */
enum class BalanceType {
    /**
     * Debit balance (Assets, Expenses)
     */
    DEBIT,
    
    /**
     * Credit balance (Liabilities, Equity, Revenue)
     */
    CREDIT
}

/**
 * Account balance for a specific period.
 */
data class AccountBalance(
    val accountNumber: String,
    val companyCode: String,
    val fiscalYear: Int,
    val fiscalPeriod: Int,               // 1-12 for monthly, 1-4 for quarterly
    val currency: String,
    val beginningBalance: BigDecimal,
    val debitAmount: BigDecimal,         // Sum of debits in period
    val creditAmount: BigDecimal,        // Sum of credits in period
    val endingBalance: BigDecimal,
    val lastUpdated: LocalDate = LocalDate.now()
) {
    /**
     * Calculate net change for the period.
     */
    fun netChange(): BigDecimal {
        return debitAmount - creditAmount
    }
    
    /**
     * Verify balance calculation is correct.
     */
    fun verifyBalance(): Boolean {
        return endingBalance == beginningBalance + netChange()
    }
}
