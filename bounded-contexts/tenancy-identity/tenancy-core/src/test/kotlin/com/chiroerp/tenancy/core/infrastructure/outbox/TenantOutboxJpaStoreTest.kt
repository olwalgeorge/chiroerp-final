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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@QuarkusTest
class TenantOutboxJpaStoreTest {
    @Inject
    lateinit var tenantOutboxStore: TenantOutboxStore

    @Inject
    lateinit var tenantRepository: TenantRepository

    @Inject
    lateinit var entityManager: EntityManager

    @BeforeEach
    @Transactional
    fun cleanupOutbox() {
        entityManager.createNativeQuery("DELETE FROM tenant_outbox").executeUpdate()
    }

    @Test
    @Transactional
    fun `save is idempotent by event id`() {
        val tenant = createTenant("outbox-idempotent-${System.currentTimeMillis()}.example")
        val eventId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-02-09T12:00:00Z")

        val entry = sampleEntry(
            eventId = eventId,
            tenantId = tenant.id.value,
            aggregateId = tenant.id.value,
            createdAt = createdAt,
            nextAttemptAt = createdAt,
        )

        tenantOutboxStore.save(listOf(entry))
        tenantOutboxStore.save(listOf(entry.copy(payload = """{"updated":true}""")))

        val pending = tenantOutboxStore.fetchPending(limit = 2000, now = createdAt.plusSeconds(1))
        assertThat(pending.count { it.eventId == eventId }).isEqualTo(1)
    }

    @Test
    @Transactional
    fun `mark failed defers availability until next attempt`() {
        val tenant = createTenant("outbox-retry-${System.currentTimeMillis()}.example")
        val now = Instant.parse("2026-02-09T12:00:00Z")
        val entry = sampleEntry(
            tenantId = tenant.id.value,
            aggregateId = tenant.id.value,
            createdAt = now.minusSeconds(10),
            nextAttemptAt = now.minusSeconds(1),
        )

        tenantOutboxStore.save(listOf(entry))

        tenantOutboxStore.markFailed(
            eventId = entry.eventId,
            attempts = 1,
            nextAttemptAt = now.plusSeconds(30),
            lastError = "kafka timeout",
        )

        val dueNow = tenantOutboxStore.fetchPending(limit = 2000, now = now)
        val dueLater = tenantOutboxStore.fetchPending(limit = 2000, now = now.plusSeconds(31))

        assertThat(dueNow).noneMatch { it.eventId == entry.eventId }
        assertThat(dueLater).anyMatch { it.eventId == entry.eventId && it.publishAttempts == 1 }
    }

    @Test
    @Transactional
    fun `mark published removes event from pending queue`() {
        val tenant = createTenant("outbox-published-${System.currentTimeMillis()}.example")
        val now = Instant.parse("2026-02-09T12:00:00Z")
        val entry = sampleEntry(
            tenantId = tenant.id.value,
            aggregateId = tenant.id.value,
            createdAt = now.minusSeconds(5),
            nextAttemptAt = now.minusSeconds(1),
        )

        tenantOutboxStore.save(listOf(entry))
        tenantOutboxStore.markPublished(entry.eventId, now)

        val pending = tenantOutboxStore.fetchPending(limit = 10, now = now.plusSeconds(1))
        assertThat(pending).noneMatch { it.eventId == entry.eventId }
    }

    @Test
    @Transactional
    fun `markDead sets status to DEAD and excludes from pending`() {
        val tenant = createTenant("outbox-dead-${System.currentTimeMillis()}.example")
        val now = Instant.parse("2026-02-09T12:00:00Z")
        val entry = sampleEntry(
            tenantId = tenant.id.value,
            aggregateId = tenant.id.value,
            createdAt = now.minusSeconds(5),
            nextAttemptAt = now.minusSeconds(1),
        )

        tenantOutboxStore.save(listOf(entry))
        tenantOutboxStore.markDead(
            eventId = entry.eventId,
            attempts = 10,
            lastError = "max attempts exceeded",
        )

        val pending = tenantOutboxStore.fetchPending(limit = 10, now = now.plusSeconds(1))
        assertThat(pending).noneMatch { it.eventId == entry.eventId }
    }

    @Test
    @Transactional
    fun `countPending returns correct count`() {
        val tenant = createTenant("outbox-count-${System.currentTimeMillis()}.example")
        val now = Instant.parse("2026-02-09T12:00:00Z")

        val entry1 = sampleEntry(
            tenantId = tenant.id.value,
            aggregateId = tenant.id.value,
            createdAt = now.minusSeconds(5),
            nextAttemptAt = now.minusSeconds(1),
        )
        val entry2 = sampleEntry(
            tenantId = tenant.id.value,
            aggregateId = tenant.id.value,
            createdAt = now.minusSeconds(4),
            nextAttemptAt = now.minusSeconds(1),
        )

        tenantOutboxStore.save(listOf(entry1, entry2))

        assertThat(tenantOutboxStore.countPending()).isEqualTo(2L)

        tenantOutboxStore.markPublished(entry1.eventId, now)
        assertThat(tenantOutboxStore.countPending()).isEqualTo(1L)
    }

    @Test
    @Transactional
    fun `countDead returns correct count`() {
        val tenant = createTenant("outbox-dead-count-${System.currentTimeMillis()}.example")
        val now = Instant.parse("2026-02-09T12:00:00Z")
        val entry = sampleEntry(
            tenantId = tenant.id.value,
            aggregateId = tenant.id.value,
            createdAt = now.minusSeconds(5),
            nextAttemptAt = now.minusSeconds(1),
        )

        tenantOutboxStore.save(listOf(entry))
        assertThat(tenantOutboxStore.countDead()).isEqualTo(0L)

        tenantOutboxStore.markDead(
            eventId = entry.eventId,
            attempts = 10,
            lastError = "poison message",
        )
        assertThat(tenantOutboxStore.countDead()).isEqualTo(1L)
    }

    private fun createTenant(domain: String): Tenant {
        val tenant = Tenant.create(
            id = TenantId(UUID.randomUUID()),
            name = "Outbox Tenant",
            domain = domain,
            tier = TenantTier.STANDARD,
            dataResidency = DataResidency("US"),
        )
        return tenantRepository.save(tenant)
    }

    private fun sampleEntry(
        tenantId: UUID,
        aggregateId: UUID,
        createdAt: Instant,
        nextAttemptAt: Instant,
        eventId: UUID = UUID.randomUUID(),
    ): TenantOutboxEntry = TenantOutboxEntry(
        eventId = eventId,
        tenantId = tenantId,
        aggregateType = "Tenant",
        aggregateId = aggregateId,
        eventType = "TenantCreated",
        payload = """{"eventType":"TenantCreated"}""",
        occurredAt = createdAt,
        createdAt = createdAt,
        nextAttemptAt = nextAttemptAt,
    )
}
