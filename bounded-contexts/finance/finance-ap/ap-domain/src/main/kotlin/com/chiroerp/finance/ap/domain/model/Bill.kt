package com.chiroerp.finance.ap.domain.model

import com.chiroerp.finance.ap.domain.events.BillCreatedEvent
import com.chiroerp.finance.ap.domain.events.BillPostedEvent
import com.chiroerp.finance.ap.domain.events.PaymentAppliedEvent
import com.chiroerp.finance.ap.domain.exceptions.InvalidBillStateException
import com.chiroerp.finance.ap.domain.exceptions.OverpaymentException
import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Currency
import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.aggregate.AggregateRoot
import java.time.LocalDate
import java.util.UUID

class Bill private constructor(
    override val id: BillId,
    val tenantId: UUID,
    val vendorId: UUID,
    val billNumber: String,
    val currency: Currency,
    val issueDate: LocalDate,
    val dueDate: LocalDate,
    val lines: List<BillLine>,
    val allocations: MutableList<PaymentAllocation>,
    var status: BillStatus,
) : AggregateRoot<BillId>(id) {

    init {
        require(billNumber.isNotBlank()) { "Bill number cannot be blank" }
        require(lines.isNotEmpty()) { "Bill must contain at least one line" }
        require(!dueDate.isBefore(issueDate)) { "Due date cannot be before issue date" }
        require(lines.all { it.unitPrice.currency == currency }) { "All bill lines must match bill currency" }
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
        if (status != BillStatus.DRAFT) {
            throw InvalidBillStateException("Only draft bills can be posted")
        }
        status = BillStatus.POSTED
        registerEvent(BillPostedEvent(tenantId = tenantId, billId = id))
    }

    fun applyPayment(paymentId: PaymentId, amount: Money, appliedAt: LocalDate = LocalDate.now()) {
        if (status != BillStatus.POSTED && status != BillStatus.PARTIALLY_PAID) {
            throw InvalidBillStateException("Payments can only be applied to posted bills")
        }
        require(amount.currency == currency) { "Payment currency must match bill currency" }
        require(amount.isPositive()) { "Applied payment amount must be positive" }
        if (amount.amount > outstandingAmount.amount) {
            throw OverpaymentException("Applied amount cannot exceed outstanding bill balance")
        }
        allocations += PaymentAllocation(paymentId = paymentId, amount = amount, appliedAt = appliedAt)
        status = if (outstandingAmount.amount.signum() == 0) {
            BillStatus.PAID
        } else {
            BillStatus.PARTIALLY_PAID
        }
        registerEvent(
            PaymentAppliedEvent(
                tenantId = tenantId,
                billId = id,
                paymentId = paymentId,
                amount = amount,
            )
        )
    }

    companion object {
        fun create(
            tenantId: UUID,
            vendorId: UUID,
            billNumber: String,
            currency: Currency,
            issueDate: LocalDate,
            dueDate: LocalDate,
            lines: List<BillLine>,
        ): Bill {
            val bill = Bill(
                id = BillId.random(),
                tenantId = tenantId,
                vendorId = vendorId,
                billNumber = billNumber,
                currency = currency,
                issueDate = issueDate,
                dueDate = dueDate,
                lines = lines,
                allocations = mutableListOf(),
                status = BillStatus.DRAFT,
            )
            bill.registerEvent(
                BillCreatedEvent(
                    tenantId = tenantId,
                    billId = bill.id,
                    vendorId = vendorId,
                    billNumber = billNumber,
                    totalAmount = bill.totalAmount,
                )
            )
            return bill
        }

        fun reconstruct(
            id: BillId,
            tenantId: UUID,
            vendorId: UUID,
            billNumber: String,
            currency: Currency,
            issueDate: LocalDate,
            dueDate: LocalDate,
            lines: List<BillLine>,
            allocations: MutableList<PaymentAllocation>,
            status: BillStatus,
        ): Bill {
            return Bill(
                id = id,
                tenantId = tenantId,
                vendorId = vendorId,
                billNumber = billNumber,
                currency = currency,
                issueDate = issueDate,
                dueDate = dueDate,
                lines = lines,
                allocations = allocations,
                status = status,
            )
        }
    }
}