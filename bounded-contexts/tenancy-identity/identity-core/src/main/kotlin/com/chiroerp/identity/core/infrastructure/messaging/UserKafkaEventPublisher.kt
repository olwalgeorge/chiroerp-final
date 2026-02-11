package com.chiroerp.identity.core.infrastructure.messaging

import com.chiroerp.identity.core.infrastructure.outbox.UserOutboxDispatcher
import com.chiroerp.identity.core.infrastructure.outbox.UserOutboxEntry
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
import java.util.concurrent.TimeUnit

@ApplicationScoped
class UserKafkaEventPublisher(
    @param:ConfigProperty(name = "kafka.bootstrap.servers")
    private val bootstrapServers: String,
    @param:ConfigProperty(name = "chiroerp.messaging.identity-events.topic", defaultValue = "identity.user.lifecycle")
    private val topic: String,
) : UserOutboxDispatcher {
    private val logger = Logger.getLogger(UserKafkaEventPublisher::class.java)

    private val producer: KafkaProducer<String, String> by lazy {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.CLIENT_ID_CONFIG, "identity-core-publisher")
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
        }
        KafkaProducer(props)
    }

    override fun dispatch(entry: UserOutboxEntry) {
        val key = entry.tenantId.toString()
        val metadata = producer.send(ProducerRecord(topic, key, entry.payload))
            .get(5, TimeUnit.SECONDS)
        logger.debugf(
            "Published identity outbox event %s to %s[%d]@%d",
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
