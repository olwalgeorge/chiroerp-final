package com.chiroerp.finance.shared

import com.chiroerp.finance.shared.valueobjects.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CurrencyTest {
    @Test
    fun `should normalize to uppercase iso code`() {
        val currency = Currency.of("usd")

        assertEquals("USD", currency.code)
    }
}
