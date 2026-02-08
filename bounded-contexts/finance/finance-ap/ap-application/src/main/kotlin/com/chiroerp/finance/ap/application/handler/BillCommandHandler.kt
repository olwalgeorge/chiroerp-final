package com.chiroerp.finance.ap.application.handler

import com.chiroerp.finance.ap.application.command.CreateBillCommand
import com.chiroerp.finance.ap.application.command.PostBillCommand
import com.chiroerp.finance.ap.application.service.APEventPublisher
import com.chiroerp.finance.ap.application.service.BillRepository
import com.chiroerp.finance.ap.domain.model.Bill
import com.chiroerp.finance.ap.domain.model.BillLine
import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.shared.types.results.DomainError
import com.chiroerp.shared.types.results.Result
import jakarta.transaction.Transactional

class BillCommandHandler(
    private val billRepository: BillRepository,
    private val eventPublisher: APEventPublisher,
) {
    @Transactional
    fun handle(command: CreateBillCommand): Result<BillId> {
        if (command.lines.isEmpty()) {
            return Result.failure(SimpleDomainError("EMPTY_BILL", "Bill must contain at least one line"))
        }
        return try {
            val bill = Bill.create(
                tenantId = command.tenantId,
                vendorId = command.vendorId,
                billNumber = command.billNumber,
                currency = command.currency,
                issueDate = command.issueDate,
                dueDate = command.dueDate,
                lines = command.lines.map {
                    BillLine(
                        description = it.description,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                    )
                },
            )
            val saved = billRepository.save(bill)
            eventPublisher.publish(saved.pullDomainEvents())
            Result.success(saved.id)
        } catch (ex: RuntimeException) {
            Result.failure(SimpleDomainError("CREATE_BILL_FAILED", ex.message ?: "Unknown error"))
        }
    }

    @Transactional
    fun handle(command: PostBillCommand): Result<Unit> {
        val bill = billRepository.findById(command.tenantId, command.billId)
            ?: return Result.failure(SimpleDomainError("BILL_NOT_FOUND", "Bill not found for tenant"))
        return try {
            bill.post()
            billRepository.save(bill)
            eventPublisher.publish(bill.pullDomainEvents())
            Result.success(Unit)
        } catch (ex: RuntimeException) {
            Result.failure(SimpleDomainError("POST_BILL_FAILED", ex.message ?: "Unknown error"))
        }
    }

    private data class SimpleDomainError(
        override val code: String,
        override val message: String,
    ) : DomainError
}
