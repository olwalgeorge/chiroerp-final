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
                    status,
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
                    :status,
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
                .setParameter("status", entry.status.name)
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
            WHERE o.status = :pendingStatus
              AND o.nextAttemptAt <= :now
            ORDER BY o.createdAt ASC
            """.trimIndent(),
            TenantOutboxJpaEntity::class.java,
        )
            .setParameter("pendingStatus", OutboxStatus.PENDING)
            .setParameter("now", now)
            .setMaxResults(limit)

        return query.resultList.map(::toEntry)
    }

    override fun claimPending(limit: Int, now: Instant): List<TenantOutboxEntry> {
        @Suppress("UNCHECKED_CAST")
        val eventIds = entityManager.createNativeQuery(
            """
            SELECT event_id
            FROM tenant_outbox
            WHERE status = 'PENDING'
              AND next_attempt_at <= :now
            ORDER BY created_at ASC
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """.trimIndent(),
        )
            .setParameter("now", Timestamp.from(now))
            .setParameter("limit", limit)
            .resultList as List<UUID>

        if (eventIds.isEmpty()) {
            return emptyList()
        }

        return entityManager.createQuery(
            """
            SELECT o
            FROM TenantOutboxJpaEntity o
            WHERE o.eventId IN :eventIds
            ORDER BY o.createdAt ASC
            """.trimIndent(),
            TenantOutboxJpaEntity::class.java,
        )
            .setParameter("eventIds", eventIds)
            .resultList
            .map(::toEntry)
    }

    override fun markPublished(eventId: UUID, publishedAt: Instant) {
        entityManager.createQuery(
            """
            UPDATE TenantOutboxJpaEntity o
            SET o.status = :publishedStatus,
                o.publishedAt = :publishedAt,
                o.lastError = NULL
            WHERE o.eventId = :eventId
              AND o.status = :pendingStatus
            """.trimIndent(),
        )
            .setParameter("publishedStatus", OutboxStatus.PUBLISHED)
            .setParameter("publishedAt", publishedAt)
            .setParameter("eventId", eventId)
            .setParameter("pendingStatus", OutboxStatus.PENDING)
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
              AND o.status = :pendingStatus
            """.trimIndent(),
        )
            .setParameter("attempts", attempts)
            .setParameter("nextAttemptAt", nextAttemptAt)
            .setParameter("lastError", lastError)
            .setParameter("eventId", eventId)
            .setParameter("pendingStatus", OutboxStatus.PENDING)
            .executeUpdate()
    }

    override fun markDead(eventId: UUID, attempts: Int, lastError: String?) {
        entityManager.createQuery(
            """
            UPDATE TenantOutboxJpaEntity o
            SET o.status = :deadStatus,
                o.publishAttempts = :attempts,
                o.lastError = :lastError
            WHERE o.eventId = :eventId
              AND o.status = :pendingStatus
            """.trimIndent(),
        )
            .setParameter("deadStatus", OutboxStatus.DEAD)
            .setParameter("attempts", attempts)
            .setParameter("lastError", lastError)
            .setParameter("eventId", eventId)
            .setParameter("pendingStatus", OutboxStatus.PENDING)
            .executeUpdate()
    }

    override fun countPending(): Long {
        return entityManager.createQuery(
            """
            SELECT COUNT(o)
            FROM TenantOutboxJpaEntity o
            WHERE o.status = :pendingStatus
            """.trimIndent(),
            Long::class.java,
        )
            .setParameter("pendingStatus", OutboxStatus.PENDING)
            .singleResult
    }

    override fun countDead(): Long {
        return entityManager.createQuery(
            """
            SELECT COUNT(o)
            FROM TenantOutboxJpaEntity o
            WHERE o.status = :deadStatus
            """.trimIndent(),
            Long::class.java,
        )
            .setParameter("deadStatus", OutboxStatus.DEAD)
            .singleResult
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
        status = entity.status,
        publishedAt = entity.publishedAt,
        publishAttempts = entity.publishAttempts,
        nextAttemptAt = requireNotNull(entity.nextAttemptAt),
        lastError = entity.lastError,
    )
}
