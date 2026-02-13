package com.chiroerp.identity.core.infrastructure.messaging

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class TenantEventConsumptionJpaStore(
    private val entityManager: EntityManager,
) : TenantEventConsumptionStore {
    override fun isProcessed(eventId: UUID): Boolean {
        @Suppress("UNCHECKED_CAST")
        val result = entityManager.createNativeQuery(
            """
            SELECT 1
            FROM identity_tenant_event_consumption
            WHERE event_id = :eventId
            """.trimIndent(),
        )
            .setParameter("eventId", eventId)
            .setMaxResults(1)
            .resultList as List<Any>

        return result.isNotEmpty()
    }

    override fun markProcessed(
        eventId: UUID,
        tenantId: UUID,
        eventType: String,
        occurredAt: Instant?,
    ): Boolean {
        val updated = entityManager.createNativeQuery(
            """
            INSERT INTO identity_tenant_event_consumption (
                event_id,
                tenant_id,
                event_type,
                occurred_at,
                processed_at
            ) VALUES (
                :eventId,
                :tenantId,
                :eventType,
                :occurredAt,
                :processedAt
            )
            ON CONFLICT (event_id) DO NOTHING
            """.trimIndent(),
        )
            .setParameter("eventId", eventId)
            .setParameter("tenantId", tenantId)
            .setParameter("eventType", eventType)
            .setParameter("occurredAt", occurredAt?.let(Timestamp::from))
            .setParameter("processedAt", Timestamp.from(Instant.now()))
            .executeUpdate()

        return updated > 0
    }
}

