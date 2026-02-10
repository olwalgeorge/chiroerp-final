package com.chiroerp.tenancy.core.infrastructure.provisioning

import com.chiroerp.tenancy.core.application.service.TenantIsolationService
import com.chiroerp.tenancy.core.application.service.TenantProvisioningService
import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantTier
import io.agroal.api.AgroalDataSource
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@QuarkusTest
class TenantProvisioningSchemaIntegrationTest {

    @Inject
    lateinit var tenantProvisioningService: TenantProvisioningService

    @Inject
    lateinit var tenantIsolationService: TenantIsolationService

    @Inject
    lateinit var dataSource: AgroalDataSource

    @Test
    fun `schema provisioning creates schema artifacts`() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
            canCreateSchemas(),
            "Current database user lacks CREATE privilege; skipping schema provisioning integration test",
        )

        val tenant = Tenant.create(
            id = TenantId(UUID.randomUUID()),
            name = "Integration Premium",
            domain = "integration-premium.example",
            tier = TenantTier.PREMIUM,
            dataResidency = DataResidency("US"),
            now = Instant.parse("2026-02-10T00:00:00Z"),
        )
        val plan = tenantIsolationService.buildProvisioningPlan(tenant)
        val schemaName = plan.schemaName ?: error("Schema name expected for premium tier")

        try {
            val result = tenantProvisioningService.provision(tenant)
            assertThat(result.readyForActivation).isTrue()

            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "select schema_name from information_schema.schemata where schema_name = ?",
                ).use { stmt ->
                    stmt.setString(1, schemaName)
                    stmt.executeQuery().use { rs ->
                        assertThat(rs.next()).isTrue()
                    }
                }
            }

            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "select count(*) from $schemaName.provisioning_audit where tenant_id = ?",
                ).use { stmt ->
                    stmt.setObject(1, tenant.id.value)
                    stmt.executeQuery().use { rs ->
                        assertThat(rs.next()).isTrue()
                        assertThat(rs.getLong(1)).isEqualTo(1)
                    }
                }
            }
        } finally {
            dataSource.connection.use { connection ->
                connection.createStatement().use { stmt ->
                    stmt.execute("DROP SCHEMA IF EXISTS $schemaName CASCADE")
                }
            }
        }
    }

    private fun canCreateSchemas(): Boolean =
        dataSource.connection.use { connection ->
            connection.prepareStatement("select has_database_privilege(current_user, current_database(), 'CREATE')").use { stmt ->
                stmt.executeQuery().use { rs ->
                    rs.next() && rs.getBoolean(1)
                }
            }
        }
}
