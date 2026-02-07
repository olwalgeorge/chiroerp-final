package com.chiroerp.finance.ar.application.handler

import com.chiroerp.finance.ar.application.query.GetInvoiceByIdQuery
import com.chiroerp.finance.ar.application.service.InvoiceRepository
import com.chiroerp.finance.ar.domain.model.Invoice
import com.chiroerp.shared.types.results.Result

class InvoiceQueryHandler(
    private val invoiceRepository: InvoiceRepository,
) {
    fun handle(query: GetInvoiceByIdQuery): Result<Invoice?> = Result.success(
        invoiceRepository.findById(query.tenantId, query.invoiceId),
    )
}
