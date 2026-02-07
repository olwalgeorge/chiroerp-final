package com.chiroerp.finance.ap.application.handler

import com.chiroerp.finance.ap.application.command.ApplyPaymentCommand
import com.chiroerp.finance.ap.application.command.RecordPaymentCommand
import com.chiroerp.finance.ap.application.service.APEventPublisher
import com.chiroerp.finance.ap.application.service.BillRepository
import com.chiroerp.finance.ap.application.service.PaymentRepository
import com.chiroerp.finance.ap.domain.events.PaymentRecordedEvent
import com.chiroerp.finance.ap.domain.model.Payment
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.shared.types.results.DomainError
import com.chiroerp.shared.types.results.Result

class PaymentCommandHandler(
    private val paymentRepository: PaymentRepository,
    private val billRepository: BillRepository,
    private val eventPublisher: APEventPublisher,
) {
    fun handle(command: RecordPaymentCommand): Result<PaymentId> {
        return try {
            val payment = Payment.create(
                id = PaymentId.random(),
                tenantId = command.tenantId,
                vendorId = command.vendorId,
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
                        vendorId = payment.vendorId,
                        amount = payment.amount,
                    )
                )
            )
            Result.success(payment.id)
        } catch (ex: RuntimeException) {
            Result.failure(SimpleDomainError("RECORD_PAYMENT_FAILED", ex.message ?: "Unknown error"))
        }
    }

    fun handle(command: ApplyPaymentCommand): Result<Unit> {
        val bill = billRepository.findById(command.tenantId, command.billId)
            ?: return Result.failure(SimpleDomainError("BILL_NOT_FOUND", "Bill not found for tenant"))
        val payment = paymentRepository.findById(command.tenantId, command.paymentId)
            ?: return Result.failure(SimpleDomainError("PAYMENT_NOT_FOUND", "Payment not found for tenant"))
        if (payment.vendorId != bill.vendorId) {
            return Result.failure(
                SimpleDomainError(
                    "VENDOR_MISMATCH",
                    "Payment vendor does not match bill vendor",
                )
            )
        }
        return try {
            if (command.amount.currency != payment.amount.currency) {
                return Result.failure(
                    SimpleDomainError(
                        "PAYMENT_CURRENCY_MISMATCH",
                        "Applied amount currency must match recorded payment currency",
                    ),
                )
            }
            if (command.amount.amount > payment.remainingAmount.amount) {
                return Result.failure(
                    SimpleDomainError(
                        "PAYMENT_OVER_ALLOCATED",
                        "Applied amount exceeds remaining payment balance",
                    ),
                )
            }

            bill.applyPayment(paymentId = payment.id, amount = command.amount)
            payment.allocateToBill(billId = bill.id, allocationAmount = command.amount)

            billRepository.save(bill)
            paymentRepository.save(payment)
            eventPublisher.publish(bill.pullDomainEvents())
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
