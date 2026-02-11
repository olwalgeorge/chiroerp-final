package com.chiroerp.identity.core.infrastructure.outbox

import com.chiroerp.identity.core.domain.event.UserDomainEvent
import com.chiroerp.identity.core.domain.port.UserEventPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class UserOutboxEventPublisher(
    private val objectMapper: ObjectMapper,
    private val userOutboxStore: UserOutboxStore,
) : UserEventPublisher {
    override fun publish(events: Collection<UserDomainEvent>) {
        if (events.isEmpty()) {
            return
        }

        val now = Instant.now()
        val entries = events.map { event ->
            UserOutboxEntry(
                eventId = event.eventId,
                tenantId = event.tenantId.value,
                aggregateType = "User",
                aggregateId = event.userId.value,
                eventType = event.eventType,
                payload = objectMapper.writeValueAsString(event),
                occurredAt = event.occurredAt,
                createdAt = now,
                nextAttemptAt = now,
            )
        }

        userOutboxStore.save(entries)
    }
}
