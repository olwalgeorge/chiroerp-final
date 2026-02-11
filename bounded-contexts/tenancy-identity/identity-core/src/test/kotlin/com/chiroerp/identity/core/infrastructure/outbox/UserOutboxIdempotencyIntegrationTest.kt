package com.chiroerp.identity.core.infrastructure.outbox

import com.chiroerp.identity.core.domain.event.UserCreatedEvent
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.tenancy.shared.TenantId
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@QuarkusTest
class UserOutboxIdempotencyIntegrationTest {

    @Inject
    lateinit var userOutboxStore: UserOutboxStore

    @Inject
    lateinit var userOutboxEventPublisher: UserOutboxEventPublisher

    @Inject
    lateinit var userOutboxRelayService: UserOutboxRelayService

    @Inject
    lateinit var entityManager: EntityManager

    @BeforeEach
    @Transactional
    fun cleanupOutbox() {
        entityManager.createNativeQuery("DELETE FROM identity_outbox").executeUpdate()
    }

    @Test
    @Transactional
    fun `duplicate domain events result in single outbox entry`() {
        val eventId = UUID.fromString("9f7ad308-7f79-4e58-ae2d-4f40f39e0726")
        val event = UserCreatedEvent(
            eventId = eventId,
            userId = UserId(UUID.fromString("7fa3d7f3-8deb-4fcd-8f95-12f9fb655246")),
            tenantId = TenantId(UUID.fromString("9ed5ee22-fbe8-4f44-8a8a-4e0d5a6d3c59")),
            occurredAt = Instant.parse("2026-02-11T12:00:00Z"),
            email = "auditor@acme.example",
            status = UserStatus.ACTIVE,
            roles = setOf("FINANCE_ADMIN"),
        )

        // Publish the same event twice
        userOutboxEventPublisher.publish(listOf(event))
        userOutboxEventPublisher.publish(listOf(event))

        // Should result in only one outbox entry
        val pending = userOutboxStore.fetchPending(limit = 2000, now = Instant.now())
        assertThat(pending.count { it.eventId == eventId }).isEqualTo(1)
    }

    @Test
    @Transactional
    fun `relay processes entry once even with duplicate publications`() {
        val eventId = UUID.fromString("9f7ad308-7f79-4e58-ae2d-4f40f39e0726")
        val event = UserCreatedEvent(
            eventId = eventId,
            userId = UserId(UUID.fromString("7fa3d7f3-8deb-4fcd-8f95-12f9fb655246")),
            tenantId = TenantId(UUID.fromString("9ed5ee22-fbe8-4f44-8a8a-4e0d5a6d3c59")),
            occurredAt = Instant.parse("2026-02-11T12:00:00Z"),
            email = "auditor@acme.example",
            status = UserStatus.ACTIVE,
            roles = setOf("FINANCE_ADMIN"),
        )

        // Publish the same event multiple times
        userOutboxEventPublisher.publish(listOf(event))
        userOutboxEventPublisher.publish(listOf(event))
        userOutboxEventPublisher.publish(listOf(event))

        // Should result in only one outbox entry
        val pending = userOutboxStore.fetchPending(limit = 2000, now = Instant.now())
        assertThat(pending.count { it.eventId == eventId }).isEqualTo(1)

        // Relay should process the single entry
        val processed = userOutboxRelayService.relayBatch()
        assertThat(processed).isEqualTo(1)

        // After processing, no pending entries should remain
        val remaining = userOutboxStore.fetchPending(limit = 2000, now = Instant.now())
        assertThat(remaining).isEmpty()
    }

    @Test
    @Transactional
    fun `relay handles concurrent duplicate processing gracefully`() {
        val eventId = UUID.fromString("9f7ad308-7f79-4e58-ae2d-4f40f39e0726")
        val event = UserCreatedEvent(
            eventId = eventId,
            userId = UserId(UUID.fromString("7fa3d7f3-8deb-4fcd-8f95-12f9fb655246")),
            tenantId = TenantId(UUID.fromString("9ed5ee22-fbe8-4f44-8a8a-4e0d5a6d3c59")),
            occurredAt = Instant.parse("2026-02-11T12:00:00Z"),
            email = "auditor@acme.example",
            status = UserStatus.ACTIVE,
            roles = setOf("FINANCE_ADMIN"),
        )

        // Publish the same event multiple times to simulate concurrent processing
        userOutboxEventPublisher.publish(listOf(event))
        userOutboxEventPublisher.publish(listOf(event))

        // Both relay calls should succeed but only one should process the entry
        val processed1 = userOutboxRelayService.relayBatch()
        val processed2 = userOutboxRelayService.relayBatch()

        // One should process 1, the other should process 0
        assertThat(processed1 + processed2).isEqualTo(1)

        // After processing, no pending entries should remain
        val remaining = userOutboxStore.fetchPending(limit = 2000, now = Instant.now())
        assertThat(remaining).isEmpty()
    }
}