package com.chiroerp.finance.ap.domain.model

import com.chiroerp.finance.ap.domain.exceptions.OverpaymentException
import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class PaymentTest {
    @Test
    fun `should support split allocation and become fully allocated`() {
        val currency = Currency.of("USD")
        val payment = Payment.create(
            id = PaymentId.random(),
            tenantId = UUID.randomUUID(),
            vendorId = UUID.randomUUID(),
            amount = Money.of(BigDecimal("100.00"), currency),
            paidAt = LocalDate.parse("2026-02-10"),
            reference = "PAY-100",
        )

        payment.allocateToBill(BillId.random(), Money.of(BigDecimal("40.00"), currency))
        payment.allocateToBill(BillId.random(), Money.of(BigDecimal("60.00"), currency))

        assertEquals(PaymentStatus.FULLY_ALLOCATED, payment.status)
        assertEquals(BigDecimal("0.00"), payment.remainingAmount.amount)
    }

    @Test
    fun `should reject allocation that exceeds remaining amount`() {
        val currency = Currency.of("USD")
        val payment = Payment.create(
            id = PaymentId.random(),
            tenantId = UUID.randomUUID(),
            vendorId = UUID.randomUUID(),
            amount = Money.of(BigDecimal("100.00"), currency),
            paidAt = LocalDate.parse("2026-02-10"),
            reference = "PAY-200",
        )

        payment.allocateToBill(BillId.random(), Money.of(BigDecimal("80.00"), currency))

        assertThrows(OverpaymentException::class.java) {
            payment.allocateToBill(BillId.random(), Money.of(BigDecimal("30.00"), currency))
        }
    }
}
