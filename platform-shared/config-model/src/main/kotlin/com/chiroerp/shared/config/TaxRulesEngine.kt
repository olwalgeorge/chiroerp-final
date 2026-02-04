package com.chiroerp.shared.config

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Interface for tax calculation and compliance.
 *
 * Phase 0: Hardcoded tax rates (simple percentage-based)
 * Phase 1: Configuration-driven (jurisdiction mapping, Avalara/Vertex integration)
 * Phase 2: AI-powered compliance checks with regulatory updates
 *
 * Related ADRs: ADR-006, ADR-030, ADR-047
 */
interface TaxRulesEngine {
    /**
     * Calculate tax for a transaction.
     *
     * @param context Tax calculation context
     * @return Tax calculation result with jurisdiction breakdown
     */
    suspend fun calculateTax(context: TaxContext): Result<TaxCalculation>

    /**
     * Determine tax jurisdiction for a transaction.
     *
     * @param context Tax context
     * @return Jurisdiction(s) with applicable tax codes
     */
    suspend fun determineJurisdiction(context: TaxContext): Result<List<TaxJurisdiction>>

    /**
     * Validate tax exemption certificate.
     *
     * @param certificateNumber Certificate ID
     * @param context Transaction context
     * @return Validation result
     */
    suspend fun validateExemption(certificateNumber: String, context: TaxContext): Result<TaxExemption>
}

/**
 * Context for tax calculation.
 */
data class TaxContext(
    val transactionType: String,         // e.g., "SALE", "PURCHASE", "SERVICE"
    val fromAddress: Address,            // Ship-from/Bill-from
    val toAddress: Address,              // Ship-to/Bill-to
    val amount: BigDecimal,              // Taxable amount
    val currency: String,
    val transactionDate: LocalDate,
    val productTaxCode: String?,         // Product tax classification
    val exemptionCertificate: String?,   // Tax exemption ID
    val attributes: Map<String, Any>
)

/**
 * Address for tax jurisdiction determination.
 */
data class Address(
    val line1: String,
    val line2: String? = null,
    val city: String,
    val stateProvince: String,
    val postalCode: String,
    val country: String
)

/**
 * Tax calculation result.
 */
data class TaxCalculation(
    val taxableAmount: BigDecimal,
    val exemptAmount: BigDecimal,
    val jurisdictions: List<TaxJurisdictionAmount>,
    val totalTax: BigDecimal,
    val effectiveTaxRate: BigDecimal,    // Percentage (e.g., 8.25 for 8.25%)
    val explanation: String
)

/**
 * Tax jurisdiction with applicable rates.
 */
data class TaxJurisdiction(
    val jurisdictionCode: String,        // e.g., "US-CA-SF"
    val jurisdictionName: String,        // e.g., "San Francisco, CA"
    val jurisdictionType: JurisdictionType,
    val taxType: String,                 // e.g., "SALES_TAX", "VAT", "GST"
    val taxRate: BigDecimal              // Percentage
)

/**
 * Tax amount per jurisdiction (for breakdown).
 */
data class TaxJurisdictionAmount(
    val jurisdiction: TaxJurisdiction,
    val taxableAmount: BigDecimal,
    val taxAmount: BigDecimal
)

enum class JurisdictionType {
    COUNTRY,
    STATE,
    COUNTY,
    CITY,
    DISTRICT
}

/**
 * Tax exemption certificate validation result.
 */
data class TaxExemption(
    val certificateNumber: String,
    val isValid: Boolean,
    val exemptionType: String,           // e.g., "RESALE", "NONPROFIT", "GOVERNMENT"
    val expirationDate: LocalDate?,
    val jurisdictions: List<String>,     // Where exemption applies
    val reason: String?                  // If invalid, why?
)
