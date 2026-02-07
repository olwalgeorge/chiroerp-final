package com.chiroerp.finance.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FiscalPeriodTest {
    @Test
    fun `should create fiscal period`() {
        val period = FiscalPeriod(
            year = 2026,
            period = 1,
            startDate = LocalDate.parse("2026-01-01"),
            endDate = LocalDate.parse("2026-01-31"),
        )

        assertEquals(2026, period.year)
        assertEquals(1, period.period)
    }
}
