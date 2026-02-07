package com.chiroerp.finance.ar.domain.events

import com.chiroerp.finance.shared.identifiers.InvoiceId
import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.events.DomainEvent
import com.chiroerp.shared.types.events.EventVersion
import java.time.Instant
import java.util.UUID

data class InvoiceCreatedEvent(
    val tenantId: UUID,
    val invoiceId: InvoiceId,
    val customerId: UUID,
    val invoiceNumber: String,
    val totalAmount: Money,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: EventVersion = EventVersion(1),
) : DomainEvent {
    override val eventType: String = "finance.ar.invoice.created"
}
