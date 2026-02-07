package com.chiroerp.finance.ap.application

import com.chiroerp.finance.ap.application.command.ApplyPaymentCommand
import com.chiroerp.finance.ap.application.command.BillLineCommand
import com.chiroerp.finance.ap.application.command.CreateBillCommand
import com.chiroerp.finance.ap.application.command.PostBillCommand
import com.chiroerp.finance.ap.application.command.RecordPaymentCommand
import com.chiroerp.finance.ap.application.handler.BillCommandHandler
import com.chiroerp.finance.ap.application.handler.PaymentCommandHandler
import com.chiroerp.finance.ap.application.service.APEventPublisher
import com.chiroerp.finance.ap.application.service.BillRepository
import com.chiroerp.finance.ap.application.service.PaymentRepository
import com.chiroerp.finance.ap.domain.model.Bill
import com.chiroerp.finance.ap.domain.model.Payment
import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.events.DomainEvent
import com.chiroerp.shared.types.results.Result
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class PaymentCommandHandlerTest {
    @Test
    fun `should reject over allocation across multiple bills`() {
        val tenantId = UUID.randomUUID()
        val vendorId = UUID.randomUUID()
        val currency = Currency.of("USD")

        val billRepository = InMemoryBillRepository()
        val paymentRepository = InMemoryPaymentRepository()
        val publisher = NoopEventPublisher()

        val billCommandHandler = BillCommandHandler(billRepository, publisher)
        val paymentCommandHandler = PaymentCommandHandler(paymentRepository, billRepository, publisher)

        val firstBillId = createAndPostBill(
            handler = billCommandHandler,
            tenantId = tenantId,
            vendorId = vendorId,
            billNumber = "BILL-1",
            amount = "80.00",
            currency = currency,
        )

        val secondBillId = createAndPostBill(
            handler = billCommandHandler,
            tenantId = tenantId,
            vendorId = vendorId,
            billNumber = "BILL-2",
            amount = "80.00",
            currency = currency,
        )

        val paymentId = (paymentCommandHandler.handle(
            RecordPaymentCommand(
                tenantId = tenantId,
                vendorId = vendorId,
                amount = Money.of(BigDecimal("100.00"), currency),
                paidAt = LocalDate.parse("2026-02-10"),
                reference = "PAY-300",
            ),
        ) as Result.Success).value

        val firstApplyResult = paymentCommandHandler.handle(
            ApplyPaymentCommand(
                tenantId = tenantId,
                billId = firstBillId,
                paymentId = paymentId,
                amount = Money.of(BigDecimal("80.00"), currency),
            ),
        )

        val secondApplyResult = paymentCommandHandler.handle(
            ApplyPaymentCommand(
                tenantId = tenantId,
                billId = secondBillId,
                paymentId = paymentId,
                amount = Money.of(BigDecimal("30.00"), currency),
            ),
        )

        assertTrue(firstApplyResult is Result.Success)
        assertTrue(secondApplyResult is Result.Failure)
        assertEquals(
            "PAYMENT_OVER_ALLOCATED",
            (secondApplyResult as Result.Failure).error.code,
        )
    }

    private fun createAndPostBill(
        handler: BillCommandHandler,
        tenantId: UUID,
        vendorId: UUID,
        billNumber: String,
        amount: String,
        currency: Currency,
    ): BillId {
        val created = handler.handle(
            CreateBillCommand(
                tenantId = tenantId,
                vendorId = vendorId,
                billNumber = billNumber,
                currency = currency,
                issueDate = LocalDate.parse("2026-02-01"),
                dueDate = LocalDate.parse("2026-02-28"),
                lines = listOf(
                    BillLineCommand(
                        description = "Service",
                        quantity = BigDecimal.ONE,
                        unitPrice = Money.of(BigDecimal(amount), currency),
                    ),
                ),
            ),
        ) as Result.Success

        handler.handle(PostBillCommand(tenantId = tenantId, billId = created.value))
        return created.value
    }

    private class InMemoryBillRepository : BillRepository {
        private val data = mutableMapOf<Pair<UUID, String>, Bill>()

        override fun save(bill: Bill): Bill {
            data[bill.tenantId to bill.id.value.toString()] = bill
            return bill
        }

        override fun findById(tenantId: UUID, billId: BillId): Bill? {
            return data[tenantId to billId.value.toString()]
        }

        override fun listOpenBills(tenantId: UUID): List<Bill> {
            return data.values.filter { it.tenantId == tenantId }
        }

        override fun listOpenBillsByVendor(tenantId: UUID, vendorId: UUID): List<Bill> {
            return data.values.filter { it.tenantId == tenantId && it.vendorId == vendorId }
        }
    }

    private class InMemoryPaymentRepository : PaymentRepository {
        private val data = mutableMapOf<Pair<UUID, String>, Payment>()

        override fun save(payment: Payment): Payment {
            data[payment.tenantId to payment.id.value.toString()] = payment
            return payment
        }

        override fun findById(tenantId: UUID, paymentId: PaymentId): Payment? {
            return data[tenantId to paymentId.value.toString()]
        }
    }

    private class NoopEventPublisher : APEventPublisher {
        override fun publish(events: List<DomainEvent>) = Unit
    }
}
