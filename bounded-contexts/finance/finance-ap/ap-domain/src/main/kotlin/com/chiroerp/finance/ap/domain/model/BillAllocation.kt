package com.chiroerp.finance.ap.domain.model

import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.finance.shared.valueobjects.Money
import java.time.LocalDate

data class BillAllocation(
    val billId: BillId,
    val amount: Money,
    val appliedAt: LocalDate,
)
