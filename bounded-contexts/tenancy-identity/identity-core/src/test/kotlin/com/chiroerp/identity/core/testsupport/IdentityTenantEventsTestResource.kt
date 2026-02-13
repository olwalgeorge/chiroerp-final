package com.chiroerp.identity.core.testsupport

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

class IdentityTenantEventsTestResource : QuarkusTestResourceLifecycleManager {
    private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16.4"))
    private val kafka = KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"))

    override fun start(): MutableMap<String, String> {
        postgres.start()
        postgres.createConnection("").use { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute("CREATE SCHEMA IF NOT EXISTS identity_core")
            }
        }

        kafka.start()

        return mutableMapOf(
            "quarkus.datasource.jdbc.url" to buildJdbcUrlWithSchema(postgres.jdbcUrl),
            "quarkus.datasource.username" to postgres.username,
            "quarkus.datasource.password" to postgres.password,
            "kafka.bootstrap.servers" to kafka.bootstrapServers,
            "chiroerp.messaging.tenancy-events.consumer.enabled" to "true",
            "chiroerp.messaging.tenancy-events.consumer.poll-interval" to "1s",
            "chiroerp.messaging.tenancy-events.consumer.poll-timeout-ms" to "300",
            "chiroerp.messaging.tenancy-events.consumer.auto-offset-reset" to "earliest",
            "chiroerp.messaging.tenancy-events.consumer.group-id" to "identity-core-tenant-events-e2e",
            "chiroerp.messaging.tenancy-events.topic" to "chiroerp.tenancy.events",
            "chiroerp.identity.tests.in-memory-repositories.enabled" to "false",
        )
    }

    override fun stop() {
        kafka.stop()
        postgres.stop()
    }

    private fun buildJdbcUrlWithSchema(jdbcUrl: String): String {
        return if (jdbcUrl.contains('?')) {
            "$jdbcUrl&currentSchema=identity_core"
        } else {
            "$jdbcUrl?currentSchema=identity_core"
        }
    }
}
