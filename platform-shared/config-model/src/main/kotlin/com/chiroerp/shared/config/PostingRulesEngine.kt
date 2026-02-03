package com.chiroerp.shared.config

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Interface for posting rule evaluation.
 * 
 * Phase 0: Hardcoded implementations in domains (e.g., HardcodedPostingRules)
 * Phase 1: Configuration-driven (Drools-based rules engine)
 * Phase 2: AI-powered suggestions with approval workflow
 * 
 * Related ADRs: ADR-006, ADR-009, ADR-044
 */
interface PostingRulesEngine {
    /**
     * Determine GL account mapping for a business transaction.
     * 
     * @param context Business context (transaction type, org unit, attributes)
     * @return Account mapping result with debit/credit accounts
     */
    suspend fun determineAccounts(context: PostingContext): Result<AccountMapping>
    
    /**
     * Validate posting rules for a given context.
     * 
     * @param context Business context to validate
     * @return Validation result with errors if any
     */
    suspend fun validatePosting(context: PostingContext): Result<Unit>
}

/**
 * Context for posting rule evaluation.
 * Contains all information needed to determine account mappings.
 */
data class PostingContext(
    val transactionType: String,        // e.g., "INVOICE", "PAYMENT", "EXPENSE"
    val companyCode: String,             // e.g., "1000"
    val costCenter: String?,             // e.g., "CC-100"
    val amount: BigDecimal,
    val currency: String,                // e.g., "USD"
    val documentDate: LocalDate,
    val attributes: Map<String, Any>     // Flexible attributes (vendor, customer, etc.)
)

/**
 * Result of account determination.
 * Contains debit/credit account assignments.
 */
data class AccountMapping(
    val debitAccount: String,            // GL account number for debit
    val creditAccount: String,           // GL account number for credit
    val costCenter: String?,             // Derived cost center (may differ from input)
    val profitCenter: String?,           // Derived profit center
    val explanation: String              // Human-readable explanation of rule applied
)
