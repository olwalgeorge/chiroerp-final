package com.chiroerp.tenancy.core.infrastructure.outbox

import com.chiroerp.tenancy.core.domain.event.TenantDomainEvent
import com.chiroerp.tenancy.core.domain.port.TenantEventPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class TenantOutboxEventPublisher(
    private val objectMapper: ObjectMapper,
    private val tenantOutboxStore: TenantOutboxStore,
) : TenantEventPublisher {
    override fun publish(events: List<TenantDomainEvent>) {
        if (events.isEmpty()) {
            return
        }

        val now = Instant.now()
        val entries = events.map { event ->
            TenantOutboxEntry(
                eventId = event.eventId,
                tenantId = event.tenantId.value,
                aggregateType = "Tenant",
                aggregateId = event.tenantId.value,
                eventType = event.eventType,
                payload = objectMapper.writeValueAsString(event),
                occurredAt = event.occurredAt,
                createdAt = now,
                nextAttemptAt = now,
            )
        }

        tenantOutboxStore.save(entries)
    }
}
