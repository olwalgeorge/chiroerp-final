package com.chiroerp.finance.ap.application.command

import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.cqrs.Command
import java.time.LocalDate
import java.util.UUID

data class RecordPaymentCommand(
    val tenantId: UUID,
    val vendorId: UUID,
    val amount: Money,
    val paidAt: LocalDate,
    val reference: String,
) : Command