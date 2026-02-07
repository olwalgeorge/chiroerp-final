package com.chiroerp.finance.gl.domain.events

import com.chiroerp.finance.shared.identifiers.JournalEntryId
import com.chiroerp.finance.shared.identifiers.LedgerId
import com.chiroerp.shared.types.events.DomainEvent
import com.chiroerp.shared.types.events.EventVersion
import java.time.Instant
import java.util.UUID

data class JournalEntryPostedEvent(
    val tenantId: UUID,
    val journalEntryId: JournalEntryId,
    val ledgerId: LedgerId,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: EventVersion = EventVersion(1),
) : DomainEvent {
    override val eventType: String = "finance.gl.journal-entry.posted"
}
