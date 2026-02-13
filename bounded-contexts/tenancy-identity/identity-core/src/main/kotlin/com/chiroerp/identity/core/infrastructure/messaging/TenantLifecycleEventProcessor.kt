package com.chiroerp.identity.core.infrastructure.messaging

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.jboss.logging.Logger
import java.time.Instant
import java.util.Locale
import java.util.UUID

enum class TenantEventProcessingResult {
    SUCCESS,
    SKIPPED,
    RETRY,
}

data class TenantLifecycleEvent(
    val eventType: String,
    val eventId: UUID,
    val tenantId: UUID,
    val occurredAt: Instant?,
    val payload: JsonNode,
)

fun interface TenantLifecycleEventHandler {
    fun handle(event: TenantLifecycleEvent)
}

@ApplicationScoped
class TenantLifecycleEventProcessor(
    private val objectMapper: ObjectMapper,
    private val eventHandler: TenantLifecycleEventHandler,
    private val eventConsumptionStore: TenantEventConsumptionStore,
    meterRegistry: MeterRegistry,
) {
    private val logger = Logger.getLogger(TenantLifecycleEventProcessor::class.java)
    private val processedCounter = meterRegistry.counter("chiroerp.identity.tenancy-events.consumer.processed")
    private val skippedCounter = meterRegistry.counter("chiroerp.identity.tenancy-events.consumer.skipped")
    private val failedCounter = meterRegistry.counter("chiroerp.identity.tenancy-events.consumer.failed")
    private val deduplicatedCounter = meterRegistry.counter("chiroerp.identity.tenancy-events.consumer.deduplicated")

    @Transactional
    fun process(
        rawPayload: String?,
        topic: String,
        partition: Int,
        offset: Long,
    ): TenantEventProcessingResult {
        if (rawPayload.isNullOrBlank()) {
            skippedCounter.increment()
            logger.warnf(
                "Skipping blank tenant event payload from %s[%d] offset=%d",
                topic,
                partition,
                offset,
            )
            return TenantEventProcessingResult.SKIPPED
        }

        val payload = runCatching { objectMapper.readTree(rawPayload) }
            .getOrElse { ex ->
                skippedCounter.increment()
                logger.warnf(
                    ex,
                    "Skipping malformed tenant event JSON from %s[%d] offset=%d",
                    topic,
                    partition,
                    offset,
                )
                return TenantEventProcessingResult.SKIPPED
            }

        val eventType = payload.path("eventType").asText("").trim()
        if (eventType.isEmpty()) {
            skippedCounter.increment()
            logger.warnf(
                "Skipping tenant event without eventType from %s[%d] offset=%d",
                topic,
                partition,
                offset,
            )
            return TenantEventProcessingResult.SKIPPED
        }

        if (eventType.uppercase(Locale.ROOT) !in SUPPORTED_EVENT_TYPES) {
            skippedCounter.increment()
            logger.debugf(
                "Skipping unsupported tenant event type=%s from %s[%d] offset=%d",
                eventType,
                topic,
                partition,
                offset,
            )
            return TenantEventProcessingResult.SKIPPED
        }

        val eventId = parseUuid(payload.path("eventId").asText(""))
        if (eventId == null) {
            skippedCounter.increment()
            logger.warnf(
                "Skipping tenant event with missing/invalid eventId for type=%s from %s[%d] offset=%d",
                eventType,
                topic,
                partition,
                offset,
            )
            return TenantEventProcessingResult.SKIPPED
        }

        if (eventConsumptionStore.isProcessed(eventId)) {
            deduplicatedCounter.increment()
            logger.debugf(
                "Skipping already-processed tenant event id=%s type=%s from %s[%d] offset=%d",
                eventId,
                eventType,
                topic,
                partition,
                offset,
            )
            return TenantEventProcessingResult.SKIPPED
        }

        val tenantId = parseUuid(payload.path("tenantId").asText(""))
        if (tenantId == null) {
            skippedCounter.increment()
            logger.warnf(
                "Skipping tenant event with invalid tenantId for type=%s from %s[%d] offset=%d",
                eventType,
                topic,
                partition,
                offset,
            )
            return TenantEventProcessingResult.SKIPPED
        }

        val event = TenantLifecycleEvent(
            eventType = eventType,
            eventId = eventId,
            tenantId = tenantId,
            occurredAt = parseInstant(payload.path("occurredAt").asText("")),
            payload = payload,
        )

        return runCatching {
            eventHandler.handle(event)
            val marked = eventConsumptionStore.markProcessed(
                eventId = event.eventId,
                tenantId = event.tenantId,
                eventType = event.eventType,
                occurredAt = event.occurredAt,
            )
            if (!marked) {
                deduplicatedCounter.increment()
                logger.debugf(
                    "Tenant event id=%s was processed concurrently; marker already exists",
                    event.eventId,
                )
            }
            processedCounter.increment()
            TenantEventProcessingResult.SUCCESS
        }.getOrElse { ex ->
            failedCounter.increment()
            logger.errorf(
                ex,
                "Tenant event handler failed for type=%s tenantId=%s from %s[%d] offset=%d",
                event.eventType,
                event.tenantId,
                topic,
                partition,
                offset,
            )
            TenantEventProcessingResult.RETRY
        }
    }

    private fun parseUuid(raw: String): UUID? {
        val normalized = raw.trim()
        if (normalized.isEmpty()) {
            return null
        }
        return runCatching { UUID.fromString(normalized) }.getOrNull()
    }

    private fun parseInstant(raw: String): Instant? {
        val normalized = raw.trim()
        if (normalized.isEmpty()) {
            return null
        }
        return runCatching { Instant.parse(normalized) }.getOrNull()
    }

    companion object {
        private val SUPPORTED_EVENT_TYPES = setOf(
            "TenantCreated",
            "TenantActivated",
            "TenantSuspended",
            "TenantTerminated",
            "TenantTierChanged",
            "TenantSettingsUpdated",
        ).map { it.uppercase(Locale.ROOT) }
            .toSet()
    }
}
