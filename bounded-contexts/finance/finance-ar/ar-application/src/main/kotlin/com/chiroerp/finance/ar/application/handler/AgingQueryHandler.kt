package com.chiroerp.finance.ar.application.handler

import com.chiroerp.finance.ar.application.query.GetAgingReportQuery
import com.chiroerp.finance.ar.application.query.GetCustomerBalanceQuery
import com.chiroerp.finance.ar.application.service.InvoiceRepository
import com.chiroerp.finance.ar.domain.model.CustomerBalance
import com.chiroerp.finance.ar.domain.services.AgingCalculationService
import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.results.Result

class AgingQueryHandler(
    private val invoiceRepository: InvoiceRepository,
    private val agingCalculationService: AgingCalculationService = AgingCalculationService(),
) {
    fun handle(query: GetAgingReportQuery) = Result.success(
        agingCalculationService.calculate(
            invoices = invoiceRepository.listOpenInvoices(query.tenantId),
            asOfDate = query.asOfDate,
            currency = query.currency,
        ),
    )

    fun handle(query: GetCustomerBalanceQuery): Result<CustomerBalance> {
        val invoices = invoiceRepository.listOpenInvoicesByCustomer(query.tenantId, query.customerId)
        val balance = invoices
            .map { it.outstandingAmount }
            .filter { it.currency == query.currency }
            .fold(Money.zero(query.currency)) { acc, amount -> acc + amount }

        return Result.success(
            CustomerBalance(
                tenantId = query.tenantId,
                customerId = query.customerId,
                balance = balance,
            ),
        )
    }
}
