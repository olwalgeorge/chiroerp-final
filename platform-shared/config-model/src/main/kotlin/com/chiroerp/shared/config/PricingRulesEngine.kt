package com.chiroerp.shared.config

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Interface for pricing rule evaluation.
 * 
 * Phase 0: Hardcoded implementations (catalog price + manual discounts)
 * Phase 1: Configuration-driven (discount matrices, volume tiers, promotions)
 * Phase 2: AI-powered dynamic pricing with market optimization
 * 
 * Related ADRs: ADR-006, ADR-025, ADR-044
 */
interface PricingRulesEngine {
    /**
     * Calculate price for a product/service in context.
     * 
     * @param context Pricing context (product, customer, quantity, date)
     * @return Price calculation result with breakdown
     */
    suspend fun calculatePrice(context: PricingContext): Result<PriceCalculation>
    
    /**
     * Determine applicable discounts for a transaction.
     * 
     * @param context Pricing context
     * @return List of applicable discounts with amounts
     */
    suspend fun determineDiscounts(context: PricingContext): Result<List<DiscountRule>>
}

/**
 * Context for pricing rule evaluation.
 */
data class PricingContext(
    val productId: String,
    val customerId: String?,
    val customerGroup: String?,          // e.g., "WHOLESALE", "RETAIL"
    val quantity: BigDecimal,
    val uom: String,                     // Unit of measure (e.g., "EA", "KG")
    val pricingDate: LocalDate,
    val currency: String,
    val salesOrg: String,                // Sales organization
    val distributionChannel: String,      // e.g., "DIRECT", "PARTNER"
    val attributes: Map<String, Any>     // Flexible attributes
)

/**
 * Result of price calculation.
 */
data class PriceCalculation(
    val basePrice: BigDecimal,           // Catalog price per unit
    val quantity: BigDecimal,
    val subtotal: BigDecimal,            // basePrice * quantity
    val discounts: List<DiscountRule>,   // Applied discounts
    val totalDiscount: BigDecimal,       // Sum of all discounts
    val netPrice: BigDecimal,            // subtotal - totalDiscount
    val tax: BigDecimal,                 // Tax amount (if calculated)
    val total: BigDecimal,               // netPrice + tax
    val currency: String,
    val explanation: String              // Human-readable breakdown
)

/**
 * Discount rule application result.
 */
data class DiscountRule(
    val ruleId: String,
    val description: String,             // e.g., "10% volume discount"
    val discountType: DiscountType,
    val discountValue: BigDecimal,       // Percentage or amount
    val discountAmount: BigDecimal,      // Calculated discount in currency
    val priority: Int                    // Lower = higher priority
)

enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT,
    TIERED
}
