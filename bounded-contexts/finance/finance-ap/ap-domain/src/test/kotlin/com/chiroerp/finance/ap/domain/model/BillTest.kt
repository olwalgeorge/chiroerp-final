package com.chiroerp.finance.ap.domain.model

import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class BillTest {
    @Test
    fun `should post bill and mark as paid after full allocation`() {
        val currency = Currency.of("USD")
        val bill = Bill.create(
            tenantId = UUID.randomUUID(),
            vendorId = UUID.randomUUID(),
            billNumber = "BILL-1001",
            currency = currency,
            issueDate = LocalDate.parse("2026-02-01"),
            dueDate = LocalDate.parse("2026-02-28"),
            lines = listOf(
                BillLine(
                    description = "Consulting",
                    quantity = BigDecimal("2"),
                    unitPrice = Money.of(BigDecimal("150.00"), currency),
                ),
            ),
        )
        bill.post()
        bill.applyPayment(
            paymentId = PaymentId.random(),
            amount = Money.of(BigDecimal("300.00"), currency),
            appliedAt = LocalDate.parse("2026-02-15"),
        )
        assertEquals(BillStatus.PAID, bill.status)
        assertEquals(BigDecimal("0.00"), bill.outstandingAmount.amount)
    }
}