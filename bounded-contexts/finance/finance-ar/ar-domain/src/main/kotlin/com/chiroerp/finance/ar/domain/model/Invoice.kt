package com.chiroerp.finance.ar.domain.model

import com.chiroerp.finance.ar.domain.events.InvoiceCreatedEvent
import com.chiroerp.finance.ar.domain.events.InvoicePostedEvent
import com.chiroerp.finance.ar.domain.events.PaymentAppliedEvent
import com.chiroerp.finance.ar.domain.exceptions.InvalidInvoiceStateException
import com.chiroerp.finance.ar.domain.exceptions.OverpaymentException
import com.chiroerp.finance.shared.identifiers.InvoiceId
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.aggregate.AggregateRoot
import java.time.LocalDate
import java.util.UUID

class Invoice private constructor(
    override val id: InvoiceId,
    val tenantId: UUID,
    val customerId: UUID,
    val invoiceNumber: String,
    val currency: Currency,
    val issueDate: LocalDate,
    val dueDate: LocalDate,
    val lines: List<InvoiceLine>,
    private val allocations: MutableList<PaymentAllocation>,
    var status: InvoiceStatus,
) : AggregateRoot<InvoiceId>(id) {

    init {
        require(invoiceNumber.isNotBlank()) { "Invoice number cannot be blank" }
        require(lines.isNotEmpty()) { "Invoice must contain at least one line" }
        require(!dueDate.isBefore(issueDate)) { "Due date cannot be before issue date" }
        require(lines.all { it.unitPrice.currency == currency }) { "All invoice lines must match invoice currency" }
    }

    val totalAmount: Money
        get() = lines
            .map { it.lineAmount }
            .fold(Money.zero(currency)) { acc, money -> acc + money }

    val paidAmount: Money
        get() = allocations
            .map { it.amount }
            .fold(Money.zero(currency)) { acc, money -> acc + money }

    val outstandingAmount: Money
        get() = totalAmount - paidAmount

    fun post() {
        if (status != InvoiceStatus.DRAFT) {
            throw InvalidInvoiceStateException("Only draft invoices can be posted")
        }

        status = InvoiceStatus.POSTED
        registerEvent(InvoicePostedEvent(tenantId = tenantId, invoiceId = id))
    }

    fun applyPayment(paymentId: PaymentId, amount: Money, appliedAt: LocalDate = LocalDate.now()) {
        if (status != InvoiceStatus.POSTED && status != InvoiceStatus.PARTIALLY_PAID) {
            throw InvalidInvoiceStateException("Payments can only be applied to posted invoices")
        }
        require(amount.currency == currency) { "Payment currency must match invoice currency" }
        require(amount.isPositive()) { "Applied payment amount must be positive" }
        if (amount.amount > outstandingAmount.amount) {
            throw OverpaymentException("Applied amount cannot exceed outstanding invoice balance")
        }

        allocations += PaymentAllocation(paymentId = paymentId, amount = amount, appliedAt = appliedAt)
        status = if (outstandingAmount.amount.signum() == 0) {
            InvoiceStatus.PAID
        } else {
            InvoiceStatus.PARTIALLY_PAID
        }

        registerEvent(
            PaymentAppliedEvent(
                tenantId = tenantId,
                invoiceId = id,
                paymentId = paymentId,
                amount = amount,
            ),
        )
    }

    companion object {
        fun create(
            tenantId: UUID,
            customerId: UUID,
            invoiceNumber: String,
            currency: Currency,
            issueDate: LocalDate,
            dueDate: LocalDate,
            lines: List<InvoiceLine>,
        ): Invoice {
            val invoice = Invoice(
                id = InvoiceId.random(),
                tenantId = tenantId,
                customerId = customerId,
                invoiceNumber = invoiceNumber,
                currency = currency,
                issueDate = issueDate,
                dueDate = dueDate,
                lines = lines,
                allocations = mutableListOf(),
                status = InvoiceStatus.DRAFT,
            )

            invoice.registerEvent(
                InvoiceCreatedEvent(
                    tenantId = tenantId,
                    invoiceId = invoice.id,
                    customerId = customerId,
                    invoiceNumber = invoiceNumber,
                    totalAmount = invoice.totalAmount,
                ),
            )

            return invoice
        }
    }
}
