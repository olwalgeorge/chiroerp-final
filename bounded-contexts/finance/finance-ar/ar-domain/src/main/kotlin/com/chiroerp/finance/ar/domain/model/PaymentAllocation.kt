package com.chiroerp.finance.ar.domain.model

import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Money
import java.time.LocalDate

data class PaymentAllocation(
    val paymentId: PaymentId,
    val amount: Money,
    val appliedAt: LocalDate,
)
