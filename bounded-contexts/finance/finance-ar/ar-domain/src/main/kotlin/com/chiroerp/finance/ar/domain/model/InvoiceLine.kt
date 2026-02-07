package com.chiroerp.finance.ar.domain.model

import com.chiroerp.finance.shared.valueobjects.Money
import java.math.BigDecimal

data class InvoiceLine(
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: Money,
) {
    init {
        require(description.isNotBlank()) { "Invoice line description cannot be blank" }
        require(quantity > BigDecimal.ZERO) { "Invoice line quantity must be greater than zero" }
    }

    val lineAmount: Money = Money.of(unitPrice.amount.multiply(quantity), unitPrice.currency)
}
