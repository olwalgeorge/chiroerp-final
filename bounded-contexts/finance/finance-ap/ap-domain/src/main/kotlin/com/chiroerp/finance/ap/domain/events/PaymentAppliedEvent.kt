package com.chiroerp.finance.ap.domain.events

import com.chiroerp.finance.shared.identifiers.BillId
import com.chiroerp.finance.shared.identifiers.PaymentId
import com.chiroerp.finance.shared.valueobjects.Money
import com.chiroerp.shared.types.events.DomainEvent
import com.chiroerp.shared.types.events.EventVersion
import java.time.Instant
import java.util.UUID

data class PaymentAppliedEvent(
    val tenantId: UUID,
    val billId: BillId,
    val paymentId: PaymentId,
    val amount: Money,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: EventVersion = EventVersion(1),
) : DomainEvent {
    override val eventType: String = "finance.ap.payment.applied"
}