package com.chiroerp.finance.ap.infrastructure.persistence

import com.chiroerp.finance.ap.domain.model.BillAllocation
import com.chiroerp.finance.ap.domain.model.PaymentStatus
import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class PaymentEntityTest {
    @Test
    fun `should reconstruct payment status and remaining amount from allocations`() {
        val tenantId = UUID.randomUUID()
        val paymentEntity = PaymentEntity().apply {
            id = UUID.randomUUID()
            this.tenantId = tenantId
            vendorId = UUID.randomUUID()
            amount = BigDecimal("100.00")
            currency = "USD"
            paidAt = LocalDate.parse("2026-02-10")
            reference = "PAY-500"
            status = PaymentStatus.UNALLOCATED
            allocatedAmount = BigDecimal.ZERO
        }

        val allocation = BillAllocation(
            billId = BillId.random(),
            amount = Money.of(BigDecimal("40.00"), Currency.of("USD")),
            appliedAt = LocalDate.parse("2026-02-11"),
        )

        val payment = paymentEntity.toDomain(mutableListOf(allocation))

        assertEquals(PaymentStatus.PARTIALLY_ALLOCATED, payment.status)
        assertEquals(BigDecimal("60.00"), payment.remainingAmount.amount)
    }
}
