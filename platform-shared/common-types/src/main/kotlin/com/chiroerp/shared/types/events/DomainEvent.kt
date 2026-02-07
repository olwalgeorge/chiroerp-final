package com.chiroerp.shared.types.events

import java.time.Instant
import java.util.UUID

interface DomainEvent {
    val eventId: UUID
    val occurredAt: Instant
    val eventType: String
    val version: EventVersion
}
