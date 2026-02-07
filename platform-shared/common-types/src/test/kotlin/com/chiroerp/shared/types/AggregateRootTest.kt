package com.chiroerp.shared.types

import com.chiroerp.shared.types.aggregate.AggregateRoot
import com.chiroerp.shared.types.events.DomainEvent
import com.chiroerp.shared.types.events.EventVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AggregateRootTest {
    @Test
    fun `should register and clear domain events`() {
        val aggregate = TestAggregate("agg-1")

        aggregate.emit("created")

        val firstPull = aggregate.pullDomainEvents()
        val secondPull = aggregate.pullDomainEvents()

        assertEquals(1, firstPull.size)
        assertEquals(0, secondPull.size)
    }

    private class TestAggregate(id: String) : AggregateRoot<String>(id) {
        fun emit(type: String) {
            registerEvent(TestEvent(type))
        }
    }

    private data class TestEvent(
        override val eventType: String,
        override val eventId: UUID = UUID.randomUUID(),
        override val occurredAt: Instant = Instant.now(),
        override val version: EventVersion = EventVersion(1),
    ) : DomainEvent
}
