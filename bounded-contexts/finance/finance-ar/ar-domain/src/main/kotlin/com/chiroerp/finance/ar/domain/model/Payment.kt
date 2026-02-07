package com.chiroerp.finance.ar.domain.model

import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Money
import java.time.LocalDate
import java.util.UUID

data class Payment(
    val id: PaymentId,
    val tenantId: UUID,
    val customerId: UUID,
    val amount: Money,
    val paidAt: LocalDate,
    val reference: String,
) {
    init {
        require(reference.isNotBlank()) { "Payment reference cannot be blank" }
        require(amount.isPositive()) { "Payment amount must be positive" }
    }
}
