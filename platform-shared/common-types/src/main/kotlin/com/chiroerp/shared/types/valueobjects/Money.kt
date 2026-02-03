package com.chiroerp.shared.types.valueobjects

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency as JavaCurrency

/**
 * Money Value Object
 * 
 * Represents a monetary amount with currency.
 * Immutable and thread-safe.
 * 
 * Design: DDD Value Object pattern
 * Reference: Martin Fowler - "Money Pattern"
 * 
 * @property amount The monetary amount
 * @property currency The currency code (ISO 4217)
 */
data class Money(
    val amount: BigDecimal,
    val currency: Currency
) : Comparable<Money> {
    
    init {
        require(amount.scale() <= 2) {
            "Amount must have at most 2 decimal places, got: ${amount.scale()}"
        }
    }
    
    companion object {
        fun zero(currency: Currency) = Money(BigDecimal.ZERO, currency)
        
        fun of(amount: String, currencyCode: String) = Money(
            BigDecimal(amount).setScale(2, RoundingMode.HALF_UP),
            Currency.of(currencyCode)
        )
        
        fun of(amount: Double, currencyCode: String) = Money(
            BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP),
            Currency.of(currencyCode)
        )
    }
    
    /**
     * Add two money amounts.
     * Throws exception if currencies don't match.
     */
    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amount = amount.add(other.amount))
    }
    
    /**
     * Subtract two money amounts.
     * Throws exception if currencies don't match.
     */
    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amount = amount.subtract(other.amount))
    }
    
    /**
     * Multiply money by a scalar.
     */
    operator fun times(multiplier: BigDecimal): Money {
        return copy(amount = amount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP))
    }
    
    operator fun times(multiplier: Int): Money = times(BigDecimal(multiplier))
    operator fun times(multiplier: Double): Money = times(BigDecimal.valueOf(multiplier))
    
    /**
     * Divide money by a scalar.
     */
    operator fun div(divisor: BigDecimal): Money {
        require(divisor != BigDecimal.ZERO) { "Cannot divide by zero" }
        return copy(amount = amount.divide(divisor, 2, RoundingMode.HALF_UP))
    }
    
    operator fun div(divisor: Int): Money = div(BigDecimal(divisor))
    operator fun div(divisor: Double): Money = div(BigDecimal.valueOf(divisor))
    
    /**
     * Negate the amount (useful for credit entries).
     */
    operator fun unaryMinus(): Money = copy(amount = amount.negate())
    
    /**
     * Compare two money amounts.
     * Only money in the same currency can be compared.
     */
    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return amount.compareTo(other.amount)
    }
    
    fun isPositive(): Boolean = amount > BigDecimal.ZERO
    fun isNegative(): Boolean = amount < BigDecimal.ZERO
    fun isZero(): Boolean = amount.compareTo(BigDecimal.ZERO) == 0
    
    fun abs(): Money = if (isNegative()) -this else this
    
    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) {
            "Currency mismatch: Cannot operate on ${this.currency} and ${other.currency}"
        }
    }
    
    override fun toString(): String = "${currency.code} ${amount.toPlainString()}"
}

/**
 * Currency Value Object
 * 
 * ISO 4217 currency code wrapper.
 */
data class Currency(val code: String) {
    
    init {
        require(code.length == 3) {
            "Currency code must be 3 characters (ISO 4217), got: $code"
        }
        require(code.all { it.isUpperCase() }) {
            "Currency code must be uppercase, got: $code"
        }
        
        // Validate against Java Currency
        try {
            JavaCurrency.getInstance(code)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid currency code: $code", e)
        }
    }
    
    companion object {
        fun of(code: String) = Currency(code.uppercase())
        
        // Common currencies
        val USD = Currency("USD")
        val EUR = Currency("EUR")
        val GBP = Currency("GBP")
        val KES = Currency("KES")  // Kenyan Shilling (ChiroERP's primary market)
        val JPY = Currency("JPY")
        val CNY = Currency("CNY")
    }
    
    override fun toString(): String = code
}
