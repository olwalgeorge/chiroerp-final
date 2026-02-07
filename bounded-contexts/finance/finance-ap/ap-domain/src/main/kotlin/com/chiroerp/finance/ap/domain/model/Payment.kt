package com.chiroerp.finance.ap.domain.model

import com.chiroerp.finance.ap.domain.exceptions.OverpaymentException
import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Money
import java.time.LocalDate
import java.util.UUID

class Payment private constructor(
    val id: PaymentId,
    val tenantId: UUID,
    val vendorId: UUID,
    val amount: Money,
    val paidAt: LocalDate,
    val reference: String,
    val allocations: MutableList<BillAllocation>,
    var status: PaymentStatus,
) {
    init {
        require(reference.isNotBlank()) { "Payment reference cannot be blank" }
        require(amount.isPositive()) { "Payment amount must be positive" }
    }

    val allocatedAmount: Money
        get() = allocations
            .map { it.amount }
            .fold(Money.zero(amount.currency)) { acc, allocated -> acc + allocated }

    val remainingAmount: Money
        get() = amount - allocatedAmount

    fun allocateToBill(
        billId: BillId,
        allocationAmount: Money,
        appliedAt: LocalDate = LocalDate.now(),
    ) {
        require(allocationAmount.currency == amount.currency) {
            "Allocation currency must match payment currency"
        }
        require(allocationAmount.isPositive()) { "Allocation amount must be positive" }

        if (allocationAmount.amount > remainingAmount.amount) {
            throw OverpaymentException("Allocation amount cannot exceed remaining payment balance")
        }

        allocations += BillAllocation(
            billId = billId,
            amount = allocationAmount,
            appliedAt = appliedAt,
        )

        status = if (remainingAmount.amount.signum() == 0) {
            PaymentStatus.FULLY_ALLOCATED
        } else {
            PaymentStatus.PARTIALLY_ALLOCATED
        }
    }

    companion object {
        fun create(
            id: PaymentId,
            tenantId: UUID,
            vendorId: UUID,
            amount: Money,
            paidAt: LocalDate,
            reference: String,
        ): Payment {
            return Payment(
                id = id,
                tenantId = tenantId,
                vendorId = vendorId,
                amount = amount,
                paidAt = paidAt,
                reference = reference,
                allocations = mutableListOf(),
                status = PaymentStatus.UNALLOCATED,
            )
        }

        fun reconstruct(
            id: PaymentId,
            tenantId: UUID,
            vendorId: UUID,
            amount: Money,
            paidAt: LocalDate,
            reference: String,
            allocations: MutableList<BillAllocation>,
            status: PaymentStatus,
        ): Payment {
            return Payment(
                id = id,
                tenantId = tenantId,
                vendorId = vendorId,
                amount = amount,
                paidAt = paidAt,
                reference = reference,
                allocations = allocations,
                status = status,
            )
        }
    }
}
