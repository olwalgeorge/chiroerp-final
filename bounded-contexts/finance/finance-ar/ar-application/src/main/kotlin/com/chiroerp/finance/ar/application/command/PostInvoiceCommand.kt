package com.chiroerp.finance.ar.application.command

import com.chiroerp.finance.shared.identifiers.InvoiceId
import com.chiroerp.shared.types.cqrs.Command
import java.util.UUID

data class PostInvoiceCommand(
    val tenantId: UUID,
    val invoiceId: InvoiceId,
) : Command
