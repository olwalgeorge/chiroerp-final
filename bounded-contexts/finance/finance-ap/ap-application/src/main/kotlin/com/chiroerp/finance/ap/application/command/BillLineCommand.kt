package com.chiroerp.finance.ap.application.command

import com.chiroerp.finance.shared.valueobjects.Money
import java.math.BigDecimal

data class BillLineCommand(
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: Money,
)
