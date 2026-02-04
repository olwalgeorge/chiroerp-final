package com.chiroerp.finance.domain

import com.chiroerp.shared.config.PostingRulesEngine
import com.chiroerp.shared.config.PostingContext
import com.chiroerp.shared.config.AccountMapping

/**
 * Phase 0: Hardcoded Posting Rules
 *
 * Simple hardcoded implementation of PostingRulesEngine for Phase 0 development.
 * Maps common transaction types to GL accounts using if/when logic.
 *
 * Phase 1: Replace with Drools-based configuration engine
 * Phase 2: Replace with AI-powered suggestion engine
 *
 * Related ADRs: ADR-006 (Platform-Shared Governance), ADR-009 (Financial Accounting)
 */
class HardcodedPostingRules : PostingRulesEngine {

    override suspend fun determineAccounts(context: PostingContext): Result<AccountMapping> {
        return try {
            val mapping = when (context.transactionType.uppercase()) {
                "INVOICE" -> handleInvoice(context)
                "PAYMENT" -> handlePayment(context)
                "EXPENSE" -> handleExpense(context)
                "REVENUE" -> handleRevenue(context)
                "ASSET_PURCHASE" -> handleAssetPurchase(context)
                "DEPRECIATION" -> handleDepreciation(context)
                else -> return Result.failure(
                    IllegalArgumentException("Unknown transaction type: ${context.transactionType}")
                )
            }
            Result.success(mapping)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validatePosting(context: PostingContext): Result<Unit> {
        // Basic validation
        if (context.amount.signum() <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be positive"))
        }

        if (context.companyCode.isBlank()) {
            return Result.failure(IllegalArgumentException("Company code is required"))
        }

        if (context.currency.isBlank()) {
            return Result.failure(IllegalArgumentException("Currency is required"))
        }

        return Result.success(Unit)
    }

    // =========================================================================
    // Transaction-Specific Handlers
    // =========================================================================

    /**
     * Invoice posting (Accounts Receivable)
     * Debit: AR (1200), Credit: Revenue (4000)
     */
    private fun handleInvoice(context: PostingContext): AccountMapping {
        val customer = context.attributes["customerId"] as? String

        return AccountMapping(
            debitAccount = "1200",      // Accounts Receivable
            creditAccount = "4000",     // Revenue
            costCenter = context.costCenter,
            profitCenter = deriveProfitCenter(context),
            explanation = "Customer Invoice - AR debit, Revenue credit (Customer: $customer)"
        )
    }

    /**
     * Payment posting (Cash Receipt)
     * Debit: Cash (1000), Credit: AR (1200)
     */
    private fun handlePayment(context: PostingContext): AccountMapping {
        val paymentMethod = context.attributes["paymentMethod"] as? String ?: "CASH"

        val cashAccount = when (paymentMethod.uppercase()) {
            "CASH" -> "1000"           // Cash
            "CHECK" -> "1010"          // Cash - Checks
            "WIRE" -> "1020"           // Cash - Wire Transfers
            "ACH" -> "1020"            // Cash - ACH
            else -> "1000"             // Default to cash
        }

        return AccountMapping(
            debitAccount = cashAccount,
            creditAccount = "1200",    // Accounts Receivable
            costCenter = context.costCenter,
            profitCenter = null,
            explanation = "Payment Received - Cash debit, AR credit (Method: $paymentMethod)"
        )
    }

    /**
     * Expense posting
     * Debit: Expense (5000-9999), Credit: AP or Cash
     */
    private fun handleExpense(context: PostingContext): AccountMapping {
        val expenseCategory = context.attributes["expenseCategory"] as? String ?: "GENERAL"
        val isPaid = context.attributes["isPaid"] as? Boolean ?: false

        val expenseAccount = when (expenseCategory.uppercase()) {
            "SALARY" -> "5100"         // Salaries Expense
            "RENT" -> "5200"           // Rent Expense
            "UTILITIES" -> "5300"      // Utilities Expense
            "MARKETING" -> "5400"      // Marketing Expense
            "TRAVEL" -> "5500"         // Travel Expense
            "SUPPLIES" -> "5600"       // Office Supplies Expense
            else -> "5000"             // General Expense
        }

        val creditAccount = if (isPaid) "1000" else "2000"  // Cash or AP

        return AccountMapping(
            debitAccount = expenseAccount,
            creditAccount = creditAccount,
            costCenter = context.costCenter ?: "CC-100",  // Default cost center
            profitCenter = deriveProfitCenter(context),
            explanation = "Expense Posting - ${expenseCategory} expense debit, ${if (isPaid) "Cash" else "AP"} credit"
        )
    }

    /**
     * Revenue posting (Direct revenue recognition)
     * Debit: Cash (1000), Credit: Revenue (4000)
     */
    private fun handleRevenue(context: PostingContext): AccountMapping {
        val revenueType = context.attributes["revenueType"] as? String ?: "PRODUCT"

        val revenueAccount = when (revenueType.uppercase()) {
            "PRODUCT" -> "4000"        // Product Sales Revenue
            "SERVICE" -> "4100"        // Service Revenue
            "INTEREST" -> "4200"       // Interest Income
            "OTHER" -> "4900"          // Other Revenue
            else -> "4000"
        }

        return AccountMapping(
            debitAccount = "1000",     // Cash
            creditAccount = revenueAccount,
            costCenter = context.costCenter,
            profitCenter = deriveProfitCenter(context),
            explanation = "Revenue Recognition - Cash debit, ${revenueType} revenue credit"
        )
    }

    /**
     * Asset purchase posting
     * Debit: Fixed Asset (1500), Credit: AP or Cash
     */
    private fun handleAssetPurchase(context: PostingContext): AccountMapping {
        val assetType = context.attributes["assetType"] as? String ?: "EQUIPMENT"
        val isPaid = context.attributes["isPaid"] as? Boolean ?: false

        val assetAccount = when (assetType.uppercase()) {
            "BUILDING" -> "1510"       // Buildings
            "EQUIPMENT" -> "1520"      // Equipment
            "VEHICLE" -> "1530"        // Vehicles
            "FURNITURE" -> "1540"      // Furniture & Fixtures
            else -> "1500"             // Fixed Assets
        }

        val creditAccount = if (isPaid) "1000" else "2000"

        return AccountMapping(
            debitAccount = assetAccount,
            creditAccount = creditAccount,
            costCenter = context.costCenter,
            profitCenter = null,  // Assets don't typically have profit centers
            explanation = "Asset Purchase - ${assetType} asset debit, ${if (isPaid) "Cash" else "AP"} credit"
        )
    }

    /**
     * Depreciation posting
     * Debit: Depreciation Expense (5700), Credit: Accumulated Depreciation (1590)
     */
    private fun handleDepreciation(context: PostingContext): AccountMapping {
        return AccountMapping(
            debitAccount = "5700",     // Depreciation Expense
            creditAccount = "1590",    // Accumulated Depreciation
            costCenter = context.costCenter ?: "CC-100",
            profitCenter = null,
            explanation = "Depreciation - Expense debit, Accumulated Depreciation credit"
        )
    }

    // =========================================================================
    // Helper Functions
    // =========================================================================

    /**
     * Derive profit center from cost center (simple mapping).
     * Phase 1: Replace with org-model hierarchy lookup.
     */
    private fun deriveProfitCenter(context: PostingContext): String? {
        return context.costCenter?.let { cc ->
            when {
                cc.startsWith("CC-1") -> "PC-1000"  // Sales profit center
                cc.startsWith("CC-2") -> "PC-2000"  // Operations profit center
                cc.startsWith("CC-3") -> "PC-3000"  // Support profit center
                else -> null
            }
        }
    }
}
