package com.chiroerp.tenancy.core.testsupport

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class PostgresTestResource : QuarkusTestResourceLifecycleManager {
    private val container = PostgreSQLContainer(DockerImageName.parse("postgres:16.4"))

    override fun start(): MutableMap<String, String> {
        container.start()
        container.createConnection("").use { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute("CREATE SCHEMA IF NOT EXISTS tenancy_core")
            }
        }
        val jdbcUrlWithSchema = buildJdbcUrlWithSchema(container.jdbcUrl)
        return mutableMapOf(
            "quarkus.datasource.jdbc.url" to jdbcUrlWithSchema,
            "quarkus.datasource.username" to container.username,
            "quarkus.datasource.password" to container.password,
            "chiroerp.tenancy.provisioning.database.admin-url" to container.jdbcUrl,
            "chiroerp.tenancy.provisioning.database.admin-username" to container.username,
            "chiroerp.tenancy.provisioning.database.admin-password" to container.password,
            "chiroerp.tenancy.provisioning.database.owner-role" to container.username
        )
    }

    override fun stop() {
        container.stop()
    }

    private fun buildJdbcUrlWithSchema(jdbcUrl: String): String {
        return if (jdbcUrl.contains('?')) {
            "$jdbcUrl&currentSchema=tenancy_core"
        } else {
            "$jdbcUrl?currentSchema=tenancy_core"
        }
    }
}
