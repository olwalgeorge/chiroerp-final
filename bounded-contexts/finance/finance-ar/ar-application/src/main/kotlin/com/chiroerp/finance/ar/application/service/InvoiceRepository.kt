package com.chiroerp.finance.ar.application.service

import com.chiroerp.finance.ar.domain.model.Invoice
import com.chiroerp.finance.shared.identifiers.InvoiceId
import java.util.UUID

interface InvoiceRepository {
    fun save(invoice: Invoice): Invoice
    fun findById(tenantId: UUID, invoiceId: InvoiceId): Invoice?
    fun listOpenInvoices(tenantId: UUID): List<Invoice>
    fun listOpenInvoicesByCustomer(tenantId: UUID, customerId: UUID): List<Invoice>
}
