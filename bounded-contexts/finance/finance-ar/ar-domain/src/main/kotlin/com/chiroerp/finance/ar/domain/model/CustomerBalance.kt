package com.chiroerp.finance.ar.domain.model

import com.chiroerp.finance.shared.valueobjects.Money
import java.util.UUID

data class CustomerBalance(
    val tenantId: UUID,
    val customerId: UUID,
    val balance: Money,
)
