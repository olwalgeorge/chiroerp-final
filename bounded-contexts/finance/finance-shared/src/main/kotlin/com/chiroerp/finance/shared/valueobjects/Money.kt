package com.chiroerp.finance.shared.valueobjects

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Finance-specific monetary type with strict scale handling.
 */
data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    companion object {
        fun of(amount: BigDecimal, currency: Currency): Money {
            val normalized = amount.setScale(currency.fractionDigits(), RoundingMode.HALF_UP)
            return Money(normalized, currency)
        }

        fun zero(currency: Currency): Money = of(BigDecimal.ZERO, currency)
    }

    operator fun plus(other: Money): Money {
        ensureSameCurrency(other)
        return of(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        ensureSameCurrency(other)
        return of(amount - other.amount, currency)
    }

    fun isNegative(): Boolean = amount < BigDecimal.ZERO
    fun isPositive(): Boolean = amount > BigDecimal.ZERO

    private fun ensureSameCurrency(other: Money) {
        require(currency == other.currency) { "Currency mismatch: $currency != ${other.currency}" }
    }
}
