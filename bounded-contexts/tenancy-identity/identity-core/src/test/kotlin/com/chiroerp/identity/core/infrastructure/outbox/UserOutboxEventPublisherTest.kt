package com.chiroerp.identity.core.infrastructure.outbox

import com.chiroerp.identity.core.domain.event.UserCreatedEvent
import com.chiroerp.identity.core.domain.model.UserId
import com.chiroerp.identity.core.domain.model.UserStatus
import com.chiroerp.tenancy.shared.TenantId
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class UserOutboxEventPublisherTest {
    @Test
    fun `publish writes domain events to outbox store`() {
        val store = InMemoryOutboxStore()
        val publisher = UserOutboxEventPublisher(
            objectMapper = objectMapper(),
            userOutboxStore = store,
        )
        val event = UserCreatedEvent(
            eventId = UUID.fromString("9f7ad308-7f79-4e58-ae2d-4f40f39e0726"),
            userId = UserId(UUID.fromString("7fa3d7f3-8deb-4fcd-8f95-12f9fb655246")),
            tenantId = TenantId(UUID.fromString("9ed5ee22-fbe8-4f44-8a8a-4e0d5a6d3c59")),
            occurredAt = Instant.parse("2026-02-11T12:00:00Z"),
            email = "auditor@acme.example",
            status = UserStatus.ACTIVE,
            roles = setOf("FINANCE_ADMIN"),
        )

        publisher.publish(listOf(event))

        assertThat(store.saved).hasSize(1)
        val entry = store.saved.single()
        assertThat(entry.eventId).isEqualTo(event.eventId)
        assertThat(entry.tenantId).isEqualTo(event.tenantId.value)
        assertThat(entry.aggregateType).isEqualTo("User")
        assertThat(entry.aggregateId).isEqualTo(event.userId.value)
        assertThat(entry.eventType).isEqualTo(event.eventType)
        assertThat(entry.occurredAt).isEqualTo(event.occurredAt)
        assertThat(entry.status).isEqualTo(UserOutboxStatus.PENDING)
        assertThat(entry.payload).contains("UserCreated")
        assertThat(entry.payload).contains("auditor@acme.example")
    }

    @Test
    fun `publish skips store write when no events provided`() {
        val store = InMemoryOutboxStore()
        val publisher = UserOutboxEventPublisher(
            objectMapper = objectMapper(),
            userOutboxStore = store,
        )

        publisher.publish(emptyList())

        assertThat(store.saved).isEmpty()
    }

    private class InMemoryOutboxStore : UserOutboxStore {
        val saved = mutableListOf<UserOutboxEntry>()

        override fun save(entries: List<UserOutboxEntry>) {
            saved += entries
        }

        override fun fetchPending(limit: Int, now: Instant): List<UserOutboxEntry> = emptyList()

        override fun claimPending(limit: Int, now: Instant): List<UserOutboxEntry> = emptyList()

        override fun markPublished(eventId: UUID, publishedAt: Instant) = Unit

        override fun markFailed(eventId: UUID, attempts: Int, nextAttemptAt: Instant, lastError: String?) = Unit

        override fun markDead(eventId: UUID, attempts: Int, lastError: String?) = Unit

        override fun countPending(): Long = 0

        override fun countDead(): Long = 0
    }

    private fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
}
