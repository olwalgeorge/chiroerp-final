package com.chiroerp.finance.ap.application.query

import com.chiroerp.finance.ap.domain.model.Bill
import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.shared.types.cqrs.Query
import java.util.UUID

data class GetBillByIdQuery(
    val tenantId: UUID,
    val billId: BillId,
) : Query<Bill?>