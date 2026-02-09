package com.chiroerp.tenancy.core.infrastructure.messaging

import com.chiroerp.tenancy.core.domain.event.TenantDomainEvent
import com.chiroerp.tenancy.core.domain.port.TenantEventPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.Duration
import java.util.Properties

@ApplicationScoped
class TenantKafkaEventPublisher(
    private val objectMapper: ObjectMapper,
    @ConfigProperty(name = "kafka.bootstrap.servers")
    private val bootstrapServers: String,
    @ConfigProperty(name = "chiroerp.messaging.tenant-events.topic", defaultValue = "tenancy.tenant.lifecycle")
    private val topic: String,
) : TenantEventPublisher {
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

    override fun publish(events: List<TenantDomainEvent>) {
        events.forEach { event ->
            val key = event.tenantId.value.toString()
            val payload = objectMapper.writeValueAsString(event)

            producer.send(ProducerRecord(topic, key, payload)) { metadata, error ->
                if (error != null) {
                    logger.errorf(
                        error,
                        "Failed to publish tenancy event %s for tenant %s",
                        event.eventType,
                        key,
                    )
                } else {
                    logger.debugf(
                        "Published tenancy event %s to %s[%d]@%d",
                        event.eventType,
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                    )
                }
            }
        }
        producer.flush()
    }

    @PreDestroy
    fun closeProducer() {
        runCatching { producer.close(Duration.ofSeconds(2)) }
            .onFailure { logger.warn("Kafka producer close failed: ${it.message}") }
    }
}
