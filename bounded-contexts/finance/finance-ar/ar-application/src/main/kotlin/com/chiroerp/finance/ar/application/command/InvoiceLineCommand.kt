package com.chiroerp.finance.ar.application.command

import com.chiroerp.finance.shared.valueobjects.Money
import java.math.BigDecimal

data class InvoiceLineCommand(
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: Money,
)
