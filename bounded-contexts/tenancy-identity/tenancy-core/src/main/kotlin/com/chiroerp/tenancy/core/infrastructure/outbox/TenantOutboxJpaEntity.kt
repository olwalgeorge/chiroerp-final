package com.chiroerp.tenancy.core.infrastructure.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnTransformer
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tenant_outbox")
class TenantOutboxJpaEntity {
    @Id
    @Column(name = "event_id", nullable = false)
    var eventId: UUID? = null

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID? = null

    @Column(name = "aggregate_type", nullable = false, length = 80)
    var aggregateType: String? = null

    @Column(name = "aggregate_id", nullable = false)
    var aggregateId: UUID? = null

    @Column(name = "event_type", nullable = false, length = 120)
    var eventType: String? = null

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    var payload: String = "{}"

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant? = null

    @Column(name = "published_at")
    var publishedAt: Instant? = null

    @Column(name = "publish_attempts", nullable = false)
    var publishAttempts: Int = 0

    @Column(name = "next_attempt_at", nullable = false)
    var nextAttemptAt: Instant? = null

    @Column(name = "last_error")
    var lastError: String? = null
}
