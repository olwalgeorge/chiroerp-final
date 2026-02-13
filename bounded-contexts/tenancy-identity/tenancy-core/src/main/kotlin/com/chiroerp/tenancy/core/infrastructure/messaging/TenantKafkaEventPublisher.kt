package com.chiroerp.tenancy.core.infrastructure.messaging

import com.chiroerp.tenancy.core.infrastructure.outbox.TenantOutboxDispatcher
import com.chiroerp.tenancy.core.infrastructure.outbox.TenantOutboxEntry
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.Properties

@ApplicationScoped
class TenantKafkaEventPublisher(
    @ConfigProperty(name = "kafka.bootstrap.servers")
    private val bootstrapServers: String,
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.topic", defaultValue = "chiroerp.tenancy.events")
    private val topic: String,
) : TenantOutboxDispatcher {
    private val logger = Logger.getLogger(TenantKafkaEventPublisher::class.java)

    private val producer: KafkaProducer<String, String> by lazy {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.CLIENT_ID_CONFIG, "tenancy-core-publisher")
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
        }
        KafkaProducer(props)
    }

    override fun dispatch(entry: TenantOutboxEntry) {
        val key = entry.tenantId.toString()
        val metadata = producer.send(ProducerRecord(topic, key, entry.payload))
            .get(5, TimeUnit.SECONDS)
        logger.debugf(
            "Published outbox event %s to %s[%d]@%d",
            entry.eventType,
            metadata.topic(),
            metadata.partition(),
            metadata.offset(),
        )
    }

    @PreDestroy
    fun closeProducer() {
        runCatching { producer.close(Duration.ofSeconds(2)) }
            .onFailure { logger.warn("Kafka producer close failed: ${it.message}") }
    }
}
