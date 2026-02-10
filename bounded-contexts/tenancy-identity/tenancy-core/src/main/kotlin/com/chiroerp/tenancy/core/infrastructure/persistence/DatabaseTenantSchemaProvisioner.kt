package com.chiroerp.tenancy.core.infrastructure.persistence

import com.chiroerp.tenancy.core.application.service.TenantSchemaProvisioner
import com.chiroerp.tenancy.shared.TenantId
import io.agroal.api.AgroalDataSource
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.sql.Connection
import java.sql.DriverManager
import java.util.Optional

@ApplicationScoped
class DatabaseTenantSchemaProvisioner(
    private val dataSource: AgroalDataSource,
    @ConfigProperty(name = "chiroerp.tenancy.provisioning.schema-grant-role")
    private val grantRole: Optional<String>,
    @ConfigProperty(name = "chiroerp.tenancy.provisioning.database.admin-url")
    private val adminJdbcUrl: String,
    @ConfigProperty(name = "chiroerp.tenancy.provisioning.database.admin-username")
    private val adminUsername: String,
    @ConfigProperty(name = "chiroerp.tenancy.provisioning.database.admin-password")
    private val adminPassword: String,
    @ConfigProperty(name = "chiroerp.tenancy.provisioning.database.owner-role")
    private val databaseOwnerRole: String,
    @ConfigProperty(name = "chiroerp.tenancy.provisioning.database.template")
    private val templateDatabase: Optional<String>,
) : TenantSchemaProvisioner {

    private val logger = Logger.getLogger(DatabaseTenantSchemaProvisioner::class.java)

    override fun verifySharedSchema() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { stmt ->
                stmt.executeQuery("select current_schema()").use { rs ->
                    if (rs.next()) {
                        logger.debugf("Shared schema resolved to %s", rs.getString(1))
                    }
                }
            }
        }
    }

    override fun createSchema(schemaName: String) {
        executeStatement("CREATE SCHEMA IF NOT EXISTS $schemaName")
    }

    override fun grantSchemaUsage(schemaName: String) {
        executeStatement("GRANT USAGE ON SCHEMA $schemaName TO CURRENT_USER")
        val role = grantRole.orElse("").trim()
        if (role.isNotEmpty()) {
            executeStatement("GRANT USAGE ON SCHEMA $schemaName TO $role")
        }
    }

    override fun createBootstrapObjects(schemaName: String) {
        val sql = """
            CREATE TABLE IF NOT EXISTS $schemaName.provisioning_audit (
                tenant_id UUID PRIMARY KEY,
                provisioned_at TIMESTAMPTZ NOT NULL,
                status VARCHAR(32) NOT NULL,
                message TEXT
            )
        """.trimIndent()
        executeStatement(sql)
    }

    override fun seedSchemaReferenceData(schemaName: String, tenantId: TenantId) {
        val sql = """
            INSERT INTO $schemaName.provisioning_audit (tenant_id, provisioned_at, status, message)
            VALUES (?, now(), 'SUCCEEDED', 'Schema bootstrap complete')
            ON CONFLICT (tenant_id) DO UPDATE
            SET provisioned_at = excluded.provisioned_at,
                status = excluded.status,
                message = excluded.message
        """.trimIndent()
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { ps ->
                ps.setObject(1, tenantId.value)
                ps.executeUpdate()
            }
        }
    }

    override fun dropSchema(schemaName: String) {
        executeStatement("DROP SCHEMA IF EXISTS $schemaName CASCADE")
    }

    override fun createDatabase(databaseName: String) {
        withAdminConnection { connection ->
            val templateClause = templateDatabase.orElse("").trim()
                .takeIf { it.isNotBlank() }
                ?.let { " TEMPLATE ${quoteIdentifier(it)}" }
                ?: ""
            connection.createStatement().use { stmt ->
                stmt.execute("CREATE DATABASE ${quoteIdentifier(databaseName)}$templateClause")
            }
            logger.infof("Created tenant database %s", databaseName)
        }
    }

    override fun grantDatabaseAccess(databaseName: String) {
        val ownerRole = databaseOwnerRole.trim()
        if (ownerRole.isEmpty()) {
            logger.warn("No database owner role configured; skipping GRANT for $databaseName")
            return
        }

        withAdminConnection { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute("GRANT ALL PRIVILEGES ON DATABASE ${quoteIdentifier(databaseName)} TO ${quoteIdentifier(ownerRole)}")
            }
        }
    }

    override fun runDatabaseMigrations(databaseName: String, tenantId: TenantId) {
        withDatabaseConnection(databaseName) { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS provisioning_audit (
                        tenant_id UUID PRIMARY KEY,
                        provisioned_at TIMESTAMPTZ NOT NULL,
                        status VARCHAR(32) NOT NULL,
                        message TEXT
                    )
                    """.trimIndent(),
                )
            }

            connection.prepareStatement(
                """
                INSERT INTO provisioning_audit (tenant_id, provisioned_at, status, message)
                VALUES (?, now(), 'SUCCEEDED', 'Database bootstrap complete')
                ON CONFLICT (tenant_id) DO UPDATE
                SET provisioned_at = excluded.provisioned_at,
                    status = excluded.status,
                    message = excluded.message
                """.trimIndent(),
            ).use { ps ->
                ps.setObject(1, tenantId.value)
                ps.executeUpdate()
            }
        }
    }

    override fun dropDatabase(databaseName: String) {
        withAdminConnection { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute(
                    """
                    SELECT pg_terminate_backend(pid)
                    FROM pg_stat_activity
                    WHERE datname = ${quoteLiteral(databaseName)}
                        AND pid <> pg_backend_pid()
                    """.trimIndent(),
                )
                stmt.execute("DROP DATABASE IF EXISTS ${quoteIdentifier(databaseName)}")
            }
            logger.infof("Dropped tenant database %s", databaseName)
        }
    }

    private fun executeStatement(sql: String) {
        dataSource.connection.use { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        }
    }

    private fun withAdminConnection(block: (Connection) -> Unit) {
        DriverManager.getConnection(adminJdbcUrl, adminUsername, adminPassword).use { connection ->
            connection.autoCommit = true
            block(connection)
        }
    }

    private fun withDatabaseConnection(databaseName: String, block: (Connection) -> Unit) {
        DriverManager.getConnection(jdbcUrlForDatabase(databaseName), adminUsername, adminPassword).use { connection ->
            connection.autoCommit = true
            block(connection)
        }
    }

    private fun jdbcUrlForDatabase(databaseName: String): String {
        val prefix = adminJdbcUrl.substringBeforeLast("/")
        val remainder = adminJdbcUrl.substringAfterLast("/")
        val dbAndParams = remainder.split("?", limit = 2)
        val params = if (dbAndParams.size == 2) dbAndParams[1] else ""
        return buildString {
            append(prefix)
            append("/")
            append(databaseName)
            if (params.isNotEmpty()) {
                append("?")
                append(params)
            }
        }
    }

    private fun quoteIdentifier(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    private fun quoteLiteral(value: String): String = "'${value.replace("'", "''")}'"
}
