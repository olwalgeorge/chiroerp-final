package com.chiroerp.tenancy.core.infrastructure.outbox

import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.port.TenantRepository
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantTier
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import jakarta.transaction.UserTransaction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

/**
 * Verifies claim safety across concurrent DB transactions.
 *
 * This validates that `FOR UPDATE SKIP LOCKED` prevents overlapping claims
 * when multiple relay workers are running on different service replicas.
 */
@QuarkusTest
class TenantOutboxConcurrencyTest {
    @Inject
    lateinit var tenantOutboxStore: TenantOutboxStore

    @Inject
    lateinit var tenantRepository: TenantRepository

    @Inject
    lateinit var entityManager: EntityManager

    @Inject
    lateinit var dataSource: DataSource

    @Inject
    lateinit var userTransaction: UserTransaction

    @BeforeEach
    @Transactional
    fun cleanupOutbox() {
        entityManager.createNativeQuery("DELETE FROM tenant_outbox").executeUpdate()
    }

    @Test
    fun `skip locked prevents overlapping claims across transactions`() {
        val now = Instant.parse("2026-02-09T12:00:00Z")

        userTransaction.begin()
        try {
            val tenant = createTenant()
            val entries = (1..20).map { idx ->
                TenantOutboxEntry(
                    eventId = UUID.randomUUID(),
                    tenantId = tenant.id.value,
                    aggregateType = "Tenant",
                    aggregateId = tenant.id.value,
                    eventType = "TenantCreated",
                    payload = """{"index":$idx}""",
                    occurredAt = now.minusSeconds((20 - idx).toLong()),
                    createdAt = now.minusSeconds((20 - idx).toLong()),
                    status = OutboxStatus.PENDING,
                    nextAttemptAt = now.minusSeconds(1),
                )
            }
            tenantOutboxStore.save(entries)
            userTransaction.commit()
        } catch (ex: Exception) {
            userTransaction.rollback()
            throw ex
        }

        dataSource.connection.use { connection1 ->
            connection1.autoCommit = false
            val firstClaimed = claimEventIds(connection1, now, 15)

            dataSource.connection.use { connection2 ->
                connection2.autoCommit = false
                val secondClaimed = claimEventIds(connection2, now, 15)

                assertThat(firstClaimed).hasSize(15)
                assertThat(secondClaimed).hasSize(5)
                assertThat(firstClaimed.intersect(secondClaimed)).isEmpty()

                connection2.rollback()
            }

            connection1.rollback()
        }
    }

    private fun claimEventIds(connection: Connection, now: Instant, limit: Int): Set<UUID> {
        val sql =
            """
            SELECT event_id
            FROM tenant_outbox
            WHERE status = 'PENDING'
              AND next_attempt_at <= ?
            ORDER BY created_at ASC
            FOR UPDATE SKIP LOCKED
            LIMIT ?
            """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setTimestamp(1, Timestamp.from(now))
            statement.setInt(2, limit)

            statement.executeQuery().use { resultSet ->
                val ids = linkedSetOf<UUID>()
                while (resultSet.next()) {
                    ids += resultSet.getObject(1, UUID::class.java)
                }
                return ids
            }
        }
    }

    private fun createTenant(): Tenant {
        val tenant = Tenant.create(
            id = TenantId(UUID.randomUUID()),
            name = "Concurrency Test Tenant",
            domain = "concurrency-test-${System.currentTimeMillis()}.example",
            tier = TenantTier.STANDARD,
            dataResidency = DataResidency("US"),
        )
        return tenantRepository.save(tenant)
    }
}
