package com.chiroerp.finance.ar.application

import com.chiroerp.finance.ar.application.command.ApplyPaymentCommand
import com.chiroerp.finance.ar.application.command.InvoiceLineCommand
import com.chiroerp.finance.ar.application.command.RecordPaymentCommand
import com.chiroerp.finance.ar.application.handler.InvoiceCommandHandler
import com.chiroerp.finance.ar.application.handler.PaymentCommandHandler
import com.chiroerp.finance.ar.application.service.AREventPublisher
import com.chiroerp.finance.ar.application.service.InvoiceRepository
import com.chiroerp.finance.ar.application.service.PaymentRepository
import com.chiroerp.finance.ar.domain.model.Invoice
import com.chiroerp.finance.ar.domain.model.Payment
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.events.DomainEvent
import com.chiroerp.shared.types.results.Result
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class PaymentCommandHandlerTest {
    @Test
    fun `should apply payment to invoice within same tenant`() {
        val tenantId = UUID.randomUUID()
        val customerId = UUID.randomUUID()
        val currency = Currency.of("USD")

        val invoices = InMemoryInvoiceRepository()
        val payments = InMemoryPaymentRepository()
        val publisher = NoopPublisher()

        val invoiceCommandHandler = InvoiceCommandHandler(invoices, publisher)
        val paymentCommandHandler = PaymentCommandHandler(payments, invoices, publisher)

        val createInvoice = com.chiroerp.finance.ar.application.command.CreateInvoiceCommand(
            tenantId = tenantId,
            customerId = customerId,
            invoiceNumber = "INV-1002",
            currency = currency,
            issueDate = LocalDate.parse("2026-02-01"),
            dueDate = LocalDate.parse("2026-02-15"),
            lines = listOf(
                InvoiceLineCommand(
                    description = "Service",
                    quantity = BigDecimal.ONE,
                    unitPrice = Money.of(BigDecimal("100.00"), currency),
                ),
            ),
        )

        val invoiceId = (invoiceCommandHandler.handle(createInvoice) as Result.Success).value
        invoiceCommandHandler.handle(
            com.chiroerp.finance.ar.application.command.PostInvoiceCommand(
                tenantId = tenantId,
                invoiceId = invoiceId,
            ),
        )

        val paymentId = (paymentCommandHandler.handle(
            RecordPaymentCommand(
                tenantId = tenantId,
                customerId = customerId,
                amount = Money.of(BigDecimal("100.00"), currency),
                paidAt = LocalDate.parse("2026-02-10"),
                reference = "PAY-1",
            ),
        ) as Result.Success).value

        val result = paymentCommandHandler.handle(
            ApplyPaymentCommand(
                tenantId = tenantId,
                invoiceId = invoiceId,
                paymentId = paymentId,
                amount = Money.of(BigDecimal("100.00"), currency),
            ),
        )

        assertTrue(result is Result.Success)
    }

    private class InMemoryInvoiceRepository : InvoiceRepository {
        private val data = mutableMapOf<Pair<UUID, String>, Invoice>()

        override fun save(invoice: Invoice): Invoice {
            data[invoice.tenantId to invoice.id.value.toString()] = invoice
            return invoice
        }

        override fun findById(tenantId: UUID, invoiceId: com.chiroerp.finance.shared.identifiers.InvoiceId): Invoice? {
            return data[tenantId to invoiceId.value.toString()]
        }

        override fun listOpenInvoices(tenantId: UUID): List<Invoice> = data.values.filter { it.tenantId == tenantId }

        override fun listOpenInvoicesByCustomer(tenantId: UUID, customerId: UUID): List<Invoice> =
            data.values.filter { it.tenantId == tenantId && it.customerId == customerId }
    }

    private class InMemoryPaymentRepository : PaymentRepository {
        private val data = mutableMapOf<Pair<UUID, String>, Payment>()

        override fun save(payment: Payment): Payment {
            data[payment.tenantId to payment.id.value.toString()] = payment
            return payment
        }

        override fun findById(tenantId: UUID, paymentId: com.chiroerp.finance.shared.identifiers.PaymentId): Payment? {
            return data[tenantId to paymentId.value.toString()]
        }
    }

    private class NoopPublisher : AREventPublisher {
        override fun publish(events: List<DomainEvent>) = Unit
    }
}
