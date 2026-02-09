package com.chiroerp.tenancy.core.infrastructure.outbox

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class TenantOutboxJpaStore(
    private val entityManager: EntityManager,
) : TenantOutboxStore {
    override fun save(entries: List<TenantOutboxEntry>) {
        entries.forEach { entry ->
            entityManager.createNativeQuery(
                """
                INSERT INTO tenant_outbox(
                    event_id,
                    tenant_id,
                    aggregate_type,
                    aggregate_id,
                    event_type,
                    payload,
                    occurred_at,
                    created_at,
                    published_at,
                    publish_attempts,
                    next_attempt_at,
                    last_error
                ) VALUES (
                    :eventId,
                    :tenantId,
                    :aggregateType,
                    :aggregateId,
                    :eventType,
                    CAST(:payload AS jsonb),
                    :occurredAt,
                    :createdAt,
                    :publishedAt,
                    :publishAttempts,
                    :nextAttemptAt,
                    :lastError
                )
                ON CONFLICT (event_id) DO NOTHING
                """.trimIndent(),
            )
                .setParameter("eventId", entry.eventId)
                .setParameter("tenantId", entry.tenantId)
                .setParameter("aggregateType", entry.aggregateType)
                .setParameter("aggregateId", entry.aggregateId)
                .setParameter("eventType", entry.eventType)
                .setParameter("payload", entry.payload)
                .setParameter("occurredAt", Timestamp.from(entry.occurredAt))
                .setParameter("createdAt", Timestamp.from(entry.createdAt))
                .setParameter("publishedAt", entry.publishedAt?.let(Timestamp::from))
                .setParameter("publishAttempts", entry.publishAttempts)
                .setParameter("nextAttemptAt", Timestamp.from(entry.nextAttemptAt))
                .setParameter("lastError", entry.lastError)
                .executeUpdate()
        }
    }

    override fun fetchPending(limit: Int, now: Instant): List<TenantOutboxEntry> {
        val query = entityManager.createQuery(
            """
            SELECT o
            FROM TenantOutboxJpaEntity o
            WHERE o.publishedAt IS NULL
              AND o.nextAttemptAt <= :now
            ORDER BY o.createdAt ASC
            """.trimIndent(),
            TenantOutboxJpaEntity::class.java,
        )
            .setParameter("now", now)
            .setMaxResults(limit)

        return query.resultList.map(::toEntry)
    }

    override fun markPublished(eventId: UUID, publishedAt: Instant) {
        entityManager.createQuery(
            """
            UPDATE TenantOutboxJpaEntity o
            SET o.publishedAt = :publishedAt,
                o.lastError = NULL
            WHERE o.eventId = :eventId
              AND o.publishedAt IS NULL
            """.trimIndent(),
        )
            .setParameter("publishedAt", publishedAt)
            .setParameter("eventId", eventId)
            .executeUpdate()
    }

    override fun markFailed(
        eventId: UUID,
        attempts: Int,
        nextAttemptAt: Instant,
        lastError: String?,
    ) {
        entityManager.createQuery(
            """
            UPDATE TenantOutboxJpaEntity o
            SET o.publishAttempts = :attempts,
                o.nextAttemptAt = :nextAttemptAt,
                o.lastError = :lastError
            WHERE o.eventId = :eventId
              AND o.publishedAt IS NULL
            """.trimIndent(),
        )
            .setParameter("attempts", attempts)
            .setParameter("nextAttemptAt", nextAttemptAt)
            .setParameter("lastError", lastError)
            .setParameter("eventId", eventId)
            .executeUpdate()
    }

    private fun toEntry(entity: TenantOutboxJpaEntity): TenantOutboxEntry = TenantOutboxEntry(
        eventId = requireNotNull(entity.eventId),
        tenantId = requireNotNull(entity.tenantId),
        aggregateType = requireNotNull(entity.aggregateType),
        aggregateId = requireNotNull(entity.aggregateId),
        eventType = requireNotNull(entity.eventType),
        payload = entity.payload,
        occurredAt = requireNotNull(entity.occurredAt),
        createdAt = requireNotNull(entity.createdAt),
        publishedAt = entity.publishedAt,
        publishAttempts = entity.publishAttempts,
        nextAttemptAt = requireNotNull(entity.nextAttemptAt),
        lastError = entity.lastError,
    )
}
