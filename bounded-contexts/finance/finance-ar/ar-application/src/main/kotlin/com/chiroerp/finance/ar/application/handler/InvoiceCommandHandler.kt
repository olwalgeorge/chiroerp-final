package com.chiroerp.finance.ar.application.handler

import com.chiroerp.finance.ar.application.command.CreateInvoiceCommand
import com.chiroerp.finance.ar.application.command.PostInvoiceCommand
import com.chiroerp.finance.ar.application.service.AREventPublisher
import com.chiroerp.finance.ar.application.service.InvoiceRepository
import com.chiroerp.finance.ar.domain.model.Invoice
import com.chiroerp.finance.ar.domain.model.InvoiceLine
import com.chiroerp.finance.shared.identifiers.InvoiceId
import com.chiroerp.shared.types.results.DomainError
import com.chiroerp.shared.types.results.Result

class InvoiceCommandHandler(
    private val invoiceRepository: InvoiceRepository,
    private val eventPublisher: AREventPublisher,
) {
    fun handle(command: CreateInvoiceCommand): Result<InvoiceId> {
        if (command.lines.isEmpty()) {
            return Result.failure(SimpleDomainError("EMPTY_INVOICE", "Invoice must contain at least one line"))
        }

        return try {
            val invoice = Invoice.create(
                tenantId = command.tenantId,
                customerId = command.customerId,
                invoiceNumber = command.invoiceNumber,
                currency = command.currency,
                issueDate = command.issueDate,
                dueDate = command.dueDate,
                lines = command.lines.map {
                    InvoiceLine(
                        description = it.description,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                    )
                },
            )

            val saved = invoiceRepository.save(invoice)
            eventPublisher.publish(saved.pullDomainEvents())
            Result.success(saved.id)
        } catch (ex: RuntimeException) {
            Result.failure(SimpleDomainError("CREATE_INVOICE_FAILED", ex.message ?: "Unknown error"))
        }
    }

    fun handle(command: PostInvoiceCommand): Result<Unit> {
        val invoice = invoiceRepository.findById(command.tenantId, command.invoiceId)
            ?: return Result.failure(SimpleDomainError("INVOICE_NOT_FOUND", "Invoice not found for tenant"))

        return try {
            invoice.post()
            invoiceRepository.save(invoice)
            eventPublisher.publish(invoice.pullDomainEvents())
            Result.success(Unit)
        } catch (ex: RuntimeException) {
            Result.failure(SimpleDomainError("POST_INVOICE_FAILED", ex.message ?: "Unknown error"))
        }
    }

    private data class SimpleDomainError(
        override val code: String,
        override val message: String,
    ) : DomainError
}
