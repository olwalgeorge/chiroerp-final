package com.chiroerp.identity.core.infrastructure.messaging

import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.Duration
import java.util.Properties

@ApplicationScoped
class TenantKafkaEventConsumer(
    private val tenantLifecycleEventProcessor: TenantLifecycleEventProcessor,
    @param:ConfigProperty(name = "kafka.bootstrap.servers")
    private val bootstrapServers: String,
    @param:ConfigProperty(name = "chiroerp.messaging.tenancy-events.topic", defaultValue = "chiroerp.tenancy.events")
    private val topic: String,
    @param:ConfigProperty(name = "chiroerp.messaging.tenancy-events.consumer.group-id", defaultValue = "identity-core-tenancy-events")
    private val groupId: String,
    @param:ConfigProperty(name = "chiroerp.messaging.tenancy-events.consumer.enabled", defaultValue = "true")
    private val enabled: Boolean,
    @param:ConfigProperty(name = "chiroerp.messaging.tenancy-events.consumer.poll-timeout-ms", defaultValue = "750")
    private val pollTimeoutMs: Long,
    @param:ConfigProperty(name = "chiroerp.messaging.tenancy-events.consumer.max-records", defaultValue = "100")
    private val maxRecords: Int,
    @param:ConfigProperty(name = "chiroerp.messaging.tenancy-events.consumer.auto-offset-reset", defaultValue = "latest")
    private val autoOffsetReset: String,
) {
    private val logger = Logger.getLogger(TenantKafkaEventConsumer::class.java)
    private var consumerRef: KafkaConsumer<String, String>? = null

    @Scheduled(
        every = "{chiroerp.messaging.tenancy-events.consumer.poll-interval:2s}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    fun poll() {
        if (!enabled) {
            return
        }

        val consumer = getOrCreateConsumer()
        val records = runCatching {
            consumer.poll(Duration.ofMillis(pollTimeoutMs.coerceAtLeast(1L)))
        }.getOrElse { ex ->
            logger.warnf(ex, "Tenant event poll failed for topic %s", topic)
            return
        }

        if (records.isEmpty) {
            return
        }

        val offsetsToCommit = linkedMapOf<TopicPartition, OffsetAndMetadata>()
        for (record in records) {
            val result = tenantLifecycleEventProcessor.process(
                rawPayload = record.value(),
                topic = record.topic(),
                partition = record.partition(),
                offset = record.offset(),
            )

            if (result == TenantEventProcessingResult.RETRY) {
                logger.warnf(
                    "Tenant event processing requested retry at %s[%d] offset=%d; leaving offset uncommitted",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                )
                break
            }

            offsetsToCommit[TopicPartition(record.topic(), record.partition())] =
                OffsetAndMetadata(record.offset() + 1)
        }

        if (offsetsToCommit.isNotEmpty()) {
            runCatching { consumer.commitSync(offsetsToCommit) }
                .onFailure { ex ->
                    logger.warnf(ex, "Tenant event commit failed for %d partition(s)", offsetsToCommit.size)
                }
        }
    }

    private fun getOrCreateConsumer(): KafkaConsumer<String, String> {
        val existing = consumerRef
        if (existing != null) {
            return existing
        }

        val created = KafkaConsumer<String, String>(consumerProperties()).also {
            it.subscribe(listOf(topic))
            logger.infof("Tenant event consumer subscribed to %s with group %s", topic, groupId)
        }
        consumerRef = created
        return created
    }

    private fun consumerProperties(): Properties = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        put(ConsumerConfig.CLIENT_ID_CONFIG, "identity-core-tenant-events-consumer")
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset)
        put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxRecords.coerceAtLeast(1))
    }

    @PreDestroy
    fun closeConsumer() {
        runCatching { consumerRef?.close(Duration.ofSeconds(2)) }
            .onFailure { logger.warn("Kafka consumer close failed: ${it.message}") }
        consumerRef = null
    }
}
