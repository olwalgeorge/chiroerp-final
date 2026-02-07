package com.chiroerp.finance.ar.application.command

import com.chiroerp.finance.shared.identifiers.InvoiceId
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.cqrs.Command
import java.util.UUID

data class ApplyPaymentCommand(
    val tenantId: UUID,
    val invoiceId: InvoiceId,
    val paymentId: PaymentId,
    val amount: Money,
) : Command
