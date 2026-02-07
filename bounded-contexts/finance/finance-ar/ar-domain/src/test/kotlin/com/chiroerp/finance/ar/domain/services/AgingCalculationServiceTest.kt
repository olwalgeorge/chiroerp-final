package com.chiroerp.finance.ar.domain.services

import com.chiroerp.finance.ar.domain.model.Invoice
import com.chiroerp.finance.ar.domain.model.InvoiceLine
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class AgingCalculationServiceTest {
    @Test
    fun `should classify open invoices into aging buckets`() {
        val currency = Currency.of("USD")
        val service = AgingCalculationService()
        val asOf = LocalDate.parse("2026-03-01")

        val currentInvoice = Invoice.create(
            tenantId = UUID.randomUUID(),
            customerId = UUID.randomUUID(),
            invoiceNumber = "INV-CURRENT",
            currency = currency,
            issueDate = LocalDate.parse("2026-02-15"),
            dueDate = LocalDate.parse("2026-03-15"),
            lines = listOf(line(currency, "100.00")),
        ).also { it.post() }

        val overdueInvoice = Invoice.create(
            tenantId = UUID.randomUUID(),
            customerId = UUID.randomUUID(),
            invoiceNumber = "INV-OVERDUE",
            currency = currency,
            issueDate = LocalDate.parse("2026-01-01"),
            dueDate = LocalDate.parse("2026-01-31"),
            lines = listOf(line(currency, "200.00")),
        ).also { it.post() }

        val report = service.calculate(
            invoices = listOf(currentInvoice, overdueInvoice),
            asOfDate = asOf,
            currency = currency,
        )

        assertEquals(BigDecimal("100.00"), report.current.amount)
        assertEquals(BigDecimal("200.00"), report.days1To30.amount)
    }

    private fun line(currency: Currency, amount: String): InvoiceLine = InvoiceLine(
        description = "Line",
        quantity = BigDecimal.ONE,
        unitPrice = Money.of(BigDecimal(amount), currency),
    )
}
