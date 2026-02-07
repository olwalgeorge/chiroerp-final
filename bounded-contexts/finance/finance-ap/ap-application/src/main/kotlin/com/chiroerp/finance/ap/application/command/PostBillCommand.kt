package com.chiroerp.finance.ap.application.command

import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.shared.types.cqrs.Command
import java.util.UUID

data class PostBillCommand(
    val tenantId: UUID,
    val billId: BillId,
) : Command