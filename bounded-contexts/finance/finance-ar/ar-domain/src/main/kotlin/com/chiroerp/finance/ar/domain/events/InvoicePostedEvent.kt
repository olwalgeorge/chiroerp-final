package com.chiroerp.finance.ar.domain.events

import com.chiroerp.finance.shared.identifiers.InvoiceId
import com.chiroerp.shared.types.events.DomainEvent
import com.chiroerp.shared.types.events.EventVersion
import java.time.Instant
import java.util.UUID

data class InvoicePostedEvent(
    val tenantId: UUID,
    val invoiceId: InvoiceId,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: EventVersion = EventVersion(1),
) : DomainEvent {
    override val eventType: String = "finance.ar.invoice.posted"
}
