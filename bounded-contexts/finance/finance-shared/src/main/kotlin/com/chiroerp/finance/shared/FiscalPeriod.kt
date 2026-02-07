package com.chiroerp.finance.shared

import java.time.LocalDate

data class FiscalPeriod(
    val year: Int,
    val period: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    init {
        require(period in 1..16) { "Period must be between 1 and 16" }
        require(!endDate.isBefore(startDate)) { "End date cannot be before start date" }
    }
}
