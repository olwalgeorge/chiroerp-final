package com.chiroerp.finance.ap.application.command

import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.cqrs.Command
import java.util.UUID

data class ApplyPaymentCommand(
    val tenantId: UUID,
    val billId: BillId,
    val paymentId: PaymentId,
    val amount: Money,
) : Command