package com.chiroerp.finance.ar.domain.services

import com.chiroerp.finance.ar.domain.model.AgingReport
import com.chiroerp.finance.ar.domain.model.Invoice
import com.chiroerp.finance.ar.domain.model.InvoiceStatus
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AgingCalculationService {
    fun calculate(
        invoices: List<Invoice>,
        asOfDate: LocalDate,
        currency: Currency,
    ): AgingReport {
        val openInvoices = invoices.filter {
            it.status == InvoiceStatus.POSTED || it.status == InvoiceStatus.PARTIALLY_PAID
        }

        var current = Money.zero(currency)
        var days1To30 = Money.zero(currency)
        var days31To60 = Money.zero(currency)
        var days61To90 = Money.zero(currency)
        var over90 = Money.zero(currency)

        openInvoices.forEach { invoice ->
            if (invoice.outstandingAmount.currency != currency) {
                return@forEach
            }

            val daysPastDue = ChronoUnit.DAYS.between(invoice.dueDate, asOfDate)
            when {
                daysPastDue <= 0 -> current += invoice.outstandingAmount
                daysPastDue <= 30 -> days1To30 += invoice.outstandingAmount
                daysPastDue <= 60 -> days31To60 += invoice.outstandingAmount
                daysPastDue <= 90 -> days61To90 += invoice.outstandingAmount
                else -> over90 += invoice.outstandingAmount
            }
        }

        return AgingReport(
            asOfDate = asOfDate,
            currencyCode = currency.code,
            current = current,
            days1To30 = days1To30,
            days31To60 = days31To60,
            days61To90 = days61To90,
            over90 = over90,
        )
    }
}
