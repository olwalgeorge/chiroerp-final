package com.chiroerp.finance.ap.application.handler

import com.chiroerp.finance.ap.application.query.GetBillByIdQuery
import com.chiroerp.finance.ap.application.service.BillRepository
import com.chiroerp.finance.ap.domain.model.Bill
import com.chiroerp.shared.types.results.Result

class BillQueryHandler(
    private val billRepository: BillRepository,
) {
    fun handle(query: GetBillByIdQuery): Result<Bill?> = Result.success(
        billRepository.findById(query.tenantId, query.billId)
    )
}