package com.chiroerp.finance.ar.domain.model

import com.chiroerp.finance.shared.valueobjects.Money
import java.time.LocalDate

data class AgingReport(
    val asOfDate: LocalDate,
    val currencyCode: String,
    val current: Money,
    val days1To30: Money,
    val days31To60: Money,
    val days61To90: Money,
    val over90: Money,
)
