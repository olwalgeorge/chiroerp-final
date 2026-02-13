package com.chiroerp.identity.core.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TenantLifecycleEventProcessorTest {
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val meterRegistry = SimpleMeterRegistry()

    @Test
    fun `process handles supported tenancy event`() {
        val handler = RecordingHandler()
        val store = InMemoryConsumptionStore()
        val processor = TenantLifecycleEventProcessor(
            objectMapper = objectMapper,
            eventHandler = handler,
            eventConsumptionStore = store,
            meterRegistry = meterRegistry,
        )
        val payload = """
            {
              "eventId": "8f355a98-5716-4eb9-a1ac-6f9bcb0867bf",
              "tenantId": "f183819d-c06c-4334-b0ae-f6cc5ffae80a",
              "eventType": "TenantCreated",
              "occurredAt": "2026-02-13T10:00:00Z",
              "tenantName": "Acme Chiro"
            }
        """.trimIndent()

        val result = processor.process(
            rawPayload = payload,
            topic = "chiroerp.tenancy.events",
            partition = 0,
            offset = 42L,
        )

        assertThat(result).isEqualTo(TenantEventProcessingResult.SUCCESS)
        assertThat(handler.events).hasSize(1)
        assertThat(handler.events.single().eventType).isEqualTo("TenantCreated")
        assertThat(handler.events.single().tenantId)
            .isEqualTo(UUID.fromString("f183819d-c06c-4334-b0ae-f6cc5ffae80a"))
        assertThat(store.processed)
            .contains(UUID.fromString("8f355a98-5716-4eb9-a1ac-6f9bcb0867bf"))
        assertThat(meterRegistry.counter("chiroerp.identity.tenancy-events.consumer.processed").count())
            .isEqualTo(1.0)
    }

    @Test
    fun `process skips malformed payload`() {
        val handler = RecordingHandler()
        val store = InMemoryConsumptionStore()
        val processor = TenantLifecycleEventProcessor(
            objectMapper = objectMapper,
            eventHandler = handler,
            eventConsumptionStore = store,
            meterRegistry = meterRegistry,
        )

        val result = processor.process(
            rawPayload = "{not-json",
            topic = "chiroerp.tenancy.events",
            partition = 0,
            offset = 7L,
        )

        assertThat(result).isEqualTo(TenantEventProcessingResult.SKIPPED)
        assertThat(handler.events).isEmpty()
        assertThat(meterRegistry.counter("chiroerp.identity.tenancy-events.consumer.skipped").count())
            .isGreaterThanOrEqualTo(1.0)
    }

    @Test
    fun `process returns retry when handler throws`() {
        val handler = ThrowingHandler()
        val store = InMemoryConsumptionStore()
        val processor = TenantLifecycleEventProcessor(
            objectMapper = objectMapper,
            eventHandler = handler,
            eventConsumptionStore = store,
            meterRegistry = meterRegistry,
        )
        val payload = """
            {
              "eventId": "1f0d3a34-e520-4680-87fc-5647a93031b7",
              "tenantId": "e7ec25a1-7c46-486f-9d9b-be0f8dc7f915",
              "eventType": "TenantSuspended",
              "occurredAt": "2026-02-13T10:10:00Z",
              "reason": "billing-overdue"
            }
        """.trimIndent()

        val result = processor.process(
            rawPayload = payload,
            topic = "chiroerp.tenancy.events",
            partition = 2,
            offset = 11L,
        )

        assertThat(result).isEqualTo(TenantEventProcessingResult.RETRY)
        assertThat(meterRegistry.counter("chiroerp.identity.tenancy-events.consumer.failed").count())
            .isEqualTo(1.0)
    }

    @Test
    fun `process skips already processed event id`() {
        val handler = RecordingHandler()
        val store = InMemoryConsumptionStore(
            mutableSetOf(UUID.fromString("5d9da64f-2fd6-40bb-a489-bbb5d1a0e655")),
        )
        val processor = TenantLifecycleEventProcessor(
            objectMapper = objectMapper,
            eventHandler = handler,
            eventConsumptionStore = store,
            meterRegistry = meterRegistry,
        )
        val payload = """
            {
              "eventId": "5d9da64f-2fd6-40bb-a489-bbb5d1a0e655",
              "tenantId": "f183819d-c06c-4334-b0ae-f6cc5ffae80a",
              "eventType": "TenantCreated",
              "occurredAt": "2026-02-13T10:00:00Z"
            }
        """.trimIndent()

        val result = processor.process(
            rawPayload = payload,
            topic = "chiroerp.tenancy.events",
            partition = 0,
            offset = 88L,
        )

        assertThat(result).isEqualTo(TenantEventProcessingResult.SKIPPED)
        assertThat(handler.events).isEmpty()
        assertThat(meterRegistry.counter("chiroerp.identity.tenancy-events.consumer.deduplicated").count())
            .isGreaterThanOrEqualTo(1.0)
    }

    private class RecordingHandler : TenantLifecycleEventHandler {
        val events = mutableListOf<TenantLifecycleEvent>()

        override fun handle(event: TenantLifecycleEvent) {
            events += event
        }
    }

    private class ThrowingHandler : TenantLifecycleEventHandler {
        override fun handle(event: TenantLifecycleEvent) {
            error("simulated handler failure")
        }
    }

    private class InMemoryConsumptionStore(
        val processed: MutableSet<UUID> = mutableSetOf(),
    ) : TenantEventConsumptionStore {
        override fun isProcessed(eventId: UUID): Boolean = processed.contains(eventId)

        override fun markProcessed(
            eventId: UUID,
            tenantId: UUID,
            eventType: String,
            occurredAt: Instant?,
        ): Boolean = processed.add(eventId)
    }
}
