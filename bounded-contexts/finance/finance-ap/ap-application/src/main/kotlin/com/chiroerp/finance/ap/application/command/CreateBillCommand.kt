package com.chiroerp.finance.ap.application.command

import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.shared.types.cqrs.Command
import java.time.LocalDate
import java.util.UUID

data class CreateBillCommand(
    val tenantId: UUID,
    val vendorId: UUID,
    val billNumber: String,
    val currency: Currency,
    val issueDate: LocalDate,
    val dueDate: LocalDate,
    val lines: List<BillLineCommand>,
) : Command