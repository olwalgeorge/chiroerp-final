package com.chiroerp.finance.ap.domain.model

import com.chiroerp.finance.shared.valueobjects.Money
import java.math.BigDecimal

data class BillLine(
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: Money,
) {
    init {
        require(description.isNotBlank()) { "Bill line description cannot be blank" }
        require(quantity > BigDecimal.ZERO) { "Bill line quantity must be greater than zero" }
    }
    val lineAmount: Money = Money.of(unitPrice.amount.multiply(quantity), unitPrice.currency)
}