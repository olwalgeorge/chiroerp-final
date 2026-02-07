package com.chiroerp.finance.ar.application.handler

import com.chiroerp.finance.ar.application.command.ApplyPaymentCommand
import com.chiroerp.finance.ar.application.command.RecordPaymentCommand
import com.chiroerp.finance.ar.application.service.AREventPublisher
import com.chiroerp.finance.ar.application.service.InvoiceRepository
import com.chiroerp.finance.ar.application.service.PaymentRepository
import com.chiroerp.finance.ar.domain.events.PaymentRecordedEvent
import com.chiroerp.finance.ar.domain.model.Payment
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.shared.types.results.DomainError
import com.chiroerp.shared.types.results.Result

class PaymentCommandHandler(
    private val paymentRepository: PaymentRepository,
    private val invoiceRepository: InvoiceRepository,
    private val eventPublisher: AREventPublisher,
) {
    fun handle(command: RecordPaymentCommand): Result<PaymentId> {
        return try {
            val payment = Payment(
                id = PaymentId.random(),
                tenantId = command.tenantId,
                customerId = command.customerId,
                amount = command.amount,
                paidAt = command.paidAt,
                reference = command.reference,
            )

            paymentRepository.save(payment)
            eventPublisher.publish(
                listOf(
                    PaymentRecordedEvent(
                        tenantId = payment.tenantId,
                        paymentId = payment.id,
                        customerId = payment.customerId,
                        amount = payment.amount,
                    ),
                ),
            )
            Result.success(payment.id)
        } catch (ex: RuntimeException) {
            Result.failure(SimpleDomainError("RECORD_PAYMENT_FAILED", ex.message ?: "Unknown error"))
        }
    }

    fun handle(command: ApplyPaymentCommand): Result<Unit> {
        val invoice = invoiceRepository.findById(command.tenantId, command.invoiceId)
            ?: return Result.failure(SimpleDomainError("INVOICE_NOT_FOUND", "Invoice not found for tenant"))

        val payment = paymentRepository.findById(command.tenantId, command.paymentId)
            ?: return Result.failure(SimpleDomainError("PAYMENT_NOT_FOUND", "Payment not found for tenant"))

        if (payment.customerId != invoice.customerId) {
            return Result.failure(
                SimpleDomainError(
                    "CUSTOMER_MISMATCH",
                    "Payment customer does not match invoice customer",
                ),
            )
        }

        return try {
            invoice.applyPayment(paymentId = payment.id, amount = command.amount)
            invoiceRepository.save(invoice)
            eventPublisher.publish(invoice.pullDomainEvents())
            Result.success(Unit)
        } catch (ex: RuntimeException) {
            Result.failure(SimpleDomainError("APPLY_PAYMENT_FAILED", ex.message ?: "Unknown error"))
        }
    }

    private data class SimpleDomainError(
        override val code: String,
        override val message: String,
    ) : DomainError
}
