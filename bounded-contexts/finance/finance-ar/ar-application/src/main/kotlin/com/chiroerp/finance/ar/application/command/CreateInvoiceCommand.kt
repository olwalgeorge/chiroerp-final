package com.chiroerp.finance.ar.application.command

import com.chiroerp.shared.types.cqrs.Command
import com.chiroerp.finance.shared.valueobjects.Currency
import java.time.LocalDate
import java.util.UUID

data class CreateInvoiceCommand(
    val tenantId: UUID,
    val customerId: UUID,
    val invoiceNumber: String,
    val currency: Currency,
    val issueDate: LocalDate,
    val dueDate: LocalDate,
    val lines: List<InvoiceLineCommand>,
) : Command
