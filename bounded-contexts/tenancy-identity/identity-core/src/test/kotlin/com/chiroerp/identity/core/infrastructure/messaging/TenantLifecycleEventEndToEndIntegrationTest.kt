package com.chiroerp.identity.core.infrastructure.messaging

import com.chiroerp.identity.core.testsupport.IdentityTenantEventsTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Properties
import java.util.UUID
import java.util.concurrent.TimeUnit

@QuarkusTest
@QuarkusTestResource(IdentityTenantEventsTestResource::class)
class TenantLifecycleEventEndToEndIntegrationTest {
    @Inject
    lateinit var entityManager: EntityManager

    @ConfigProperty(name = "kafka.bootstrap.servers")
    lateinit var bootstrapServers: String

    @ConfigProperty(name = "chiroerp.messaging.tenancy-events.topic", defaultValue = "chiroerp.tenancy.events")
    lateinit var topic: String

    @BeforeEach
    @Transactional
    fun clearState() {
        entityManager.createNativeQuery(
            """
            TRUNCATE TABLE
                identity_external_identities,
                identity_permissions,
                identity_user_roles,
                identity_outbox,
                identity_tenant_event_consumption,
                identity_users
            RESTART IDENTITY CASCADE
            """.trimIndent(),
        ).executeUpdate()
    }

    @Test
    fun `publish tenant created event and consume once`() {
        val tenantId = UUID.fromString("6e59a157-2e8a-4b26-9b0d-7c9d8f120f67")
        val eventId = UUID.fromString("4ad89e96-0c67-4f55-a0be-c3ea87fb9362")
        val payload = """
            {
              "eventId": "$eventId",
              "tenantId": "$tenantId",
              "eventType": "TenantCreated",
              "occurredAt": "${Instant.now()}",
              "tenantName": "Acme Chiro",
              "domain": "acme.example"
            }
        """.trimIndent()

        publish(payload, tenantId.toString())

        eventually("bootstrap admin user created and event marked processed") {
            userCount(tenantId) == 1L && processedEventCount(eventId) == 1L
        }

        publish(payload, tenantId.toString())

        eventually("duplicate event does not create duplicate user") {
            userCount(tenantId) == 1L && processedEventCount(eventId) == 1L
        }

        assertThat(userEmail(tenantId)).isEqualTo("admin@acme.example")
    }

    @Test
    fun `publish tenant activated event activates bootstrap user without duplicate roles`() {
        val tenantId = UUID.fromString("e6536015-2394-411a-8eaf-cf09f8fe6e2f")
        val createdEventId = UUID.fromString("3de315f9-f3b2-429f-8c9a-f6bedf7db8a2")
        val activatedEventId = UUID.fromString("af08a5f6-f679-485c-9f29-5c6c830d7ab3")

        val createdPayload = """
            {
              "eventId": "$createdEventId",
              "tenantId": "$tenantId",
              "eventType": "TenantCreated",
              "occurredAt": "${Instant.now()}",
              "tenantName": "Delta Chiro",
              "domain": "delta.example"
            }
        """.trimIndent()
        val activatedPayload = """
            {
              "eventId": "$activatedEventId",
              "tenantId": "$tenantId",
              "eventType": "TenantActivated",
              "occurredAt": "${Instant.now()}"
            }
        """.trimIndent()

        publish(createdPayload, tenantId.toString())
        eventually("tenant created event is consumed") {
            processedEventCount(createdEventId) == 1L && userCount(tenantId) == 1L
        }

        publish(activatedPayload, tenantId.toString())
        eventually("tenant activated event is consumed and user is activated") {
            processedEventCount(activatedEventId) == 1L &&
                userStatus(tenantId) == "ACTIVE" &&
                userRoleCount(tenantId) == 1L
        }
    }

    private fun publish(payload: String, key: String) {
        KafkaProducer<String, String>(producerProperties()).use { producer ->
            producer.send(ProducerRecord(topic, key, payload)).get(10, TimeUnit.SECONDS)
            producer.flush()
        }
    }

    private fun producerProperties(): Properties = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
    }

    private fun userCount(tenantId: UUID): Long {
        val result = entityManager.createNativeQuery(
            "SELECT count(*) FROM identity_users WHERE tenant_id = :tenantId",
        )
            .setParameter("tenantId", tenantId)
            .singleResult as Number
        return result.toLong()
    }

    private fun processedEventCount(eventId: UUID): Long {
        val result = entityManager.createNativeQuery(
            "SELECT count(*) FROM identity_tenant_event_consumption WHERE event_id = :eventId",
        )
            .setParameter("eventId", eventId)
            .singleResult as Number
        return result.toLong()
    }

    private fun userEmail(tenantId: UUID): String? {
        @Suppress("UNCHECKED_CAST")
        val rows = entityManager.createNativeQuery(
            "SELECT email FROM identity_users WHERE tenant_id = :tenantId ORDER BY created_at ASC",
        )
            .setParameter("tenantId", tenantId)
            .setMaxResults(1)
            .resultList as List<String>

        return rows.firstOrNull()
    }

    private fun userStatus(tenantId: UUID): String? {
        @Suppress("UNCHECKED_CAST")
        val rows = entityManager.createNativeQuery(
            "SELECT status FROM identity_users WHERE tenant_id = :tenantId ORDER BY created_at ASC",
        )
            .setParameter("tenantId", tenantId)
            .setMaxResults(1)
            .resultList as List<String>

        return rows.firstOrNull()
    }

    private fun userRoleCount(tenantId: UUID): Long {
        val result = entityManager.createNativeQuery(
            """
            SELECT count(*)
            FROM identity_user_roles r
            JOIN identity_users u ON u.id = r.user_id
            WHERE u.tenant_id = :tenantId
            """.trimIndent(),
        )
            .setParameter("tenantId", tenantId)
            .singleResult as Number
        return result.toLong()
    }

    private fun eventually(
        description: String,
        timeoutMillis: Long = 20_000,
        probeEveryMillis: Long = 200,
        assertion: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (assertion()) {
                return
            }
            Thread.sleep(probeEveryMillis)
        }
        throw AssertionError("Timed out waiting for condition: $description")
    }
}
