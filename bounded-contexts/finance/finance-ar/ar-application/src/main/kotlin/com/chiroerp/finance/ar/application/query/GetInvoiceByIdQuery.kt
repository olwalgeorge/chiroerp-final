package com.chiroerp.finance.ar.application.query

import com.chiroerp.finance.ar.domain.model.Invoice
import com.chiroerp.finance.shared.identifiers.InvoiceId
import com.chiroerp.shared.types.cqrs.Query
import java.util.UUID

data class GetInvoiceByIdQuery(
    val tenantId: UUID,
    val invoiceId: InvoiceId,
) : Query<Invoice?>
