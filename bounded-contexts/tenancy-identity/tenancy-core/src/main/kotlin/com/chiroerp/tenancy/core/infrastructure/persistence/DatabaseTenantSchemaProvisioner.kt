package com.chiroerp.tenancy.core.infrastructure.persistence

import com.chiroerp.tenancy.core.application.service.TenantSchemaProvisioner
import com.chiroerp.tenancy.shared.TenantId
import io.agroal.api.AgroalDataSource
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.Optional

@ApplicationScoped
class DatabaseTenantSchemaProvisioner(
    private val dataSource: AgroalDataSource,
    @ConfigProperty(name = "chiroerp.tenancy.provisioning.schema-grant-role")
    private val grantRole: Optional<String>,
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

    private fun executeStatement(sql: String) {
        dataSource.connection.use { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        }
    }
}
