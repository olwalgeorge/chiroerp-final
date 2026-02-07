package com.chiroerp.finance.shared.valueobjects

@JvmInline
value class Currency private constructor(val code: String) {
    companion object {
        fun of(code: String): Currency {
            val normalized = code.trim().uppercase()
            val currency = java.util.Currency.getInstance(normalized)
            return Currency(currency.currencyCode)
        }
    }

    fun fractionDigits(): Int = java.util.Currency.getInstance(code).defaultFractionDigits

    override fun toString(): String = code
}
