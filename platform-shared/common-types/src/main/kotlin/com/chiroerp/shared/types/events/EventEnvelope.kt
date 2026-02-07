package com.chiroerp.shared.types.events

data class EventEnvelope<T : DomainEvent>(
    val event: T,
    val metadata: EventMetadata = EventMetadata(occurredAt = event.occurredAt),
)
