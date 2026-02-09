package com.chiroerp.tenancy.core.infrastructure

import com.chiroerp.tenancy.core.domain.model.DataResidency
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.model.TenantSettings
import com.chiroerp.tenancy.core.domain.port.TenantRepository
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantStatus
import com.chiroerp.tenancy.shared.TenantTier
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/**
 * Integration tests for TenantJpaRepository.
 * 
 * These tests require a running PostgreSQL instance (via Testcontainers or local).
 * They validate the repository implementation against real database operations.
 */
@QuarkusTest
class TenantJpaRepositoryTest {

    @Inject
    lateinit var tenantRepository: TenantRepository

    private val testTenantId = TenantId(UUID.fromString("11111111-1111-1111-1111-111111111111"))

    @BeforeEach
    @Transactional
    fun cleanup() {
        // Clean up any existing test tenant
        tenantRepository.findById(testTenantId)?.let {
            // Mark as terminated to allow cleanup (if needed by business rules)
        }
    }

    @Test
    @Transactional
    fun `save persists tenant and findById retrieves it`() {
        val tenant = createTestTenant(
            id = TenantId(UUID.randomUUID()),
            domain = "jpa-test-${System.currentTimeMillis()}.example",
        )

        val saved = tenantRepository.save(tenant)
        saved.activate() // Move from PENDING to ACTIVE
        tenantRepository.save(saved)

        val retrieved = tenantRepository.findById(saved.id)

        assertThat(retrieved).isNotNull
        assertThat(retrieved!!.id).isEqualTo(saved.id)
        assertThat(retrieved.name).isEqualTo(saved.name)
        assertThat(retrieved.domain).isEqualTo(saved.domain)
        assertThat(retrieved.tier).isEqualTo(saved.tier)
        assertThat(retrieved.status).isEqualTo(TenantStatus.ACTIVE)
    }

    @Test
    @Transactional
    fun `findByDomain returns tenant with matching domain`() {
        val uniqueDomain = "findby-domain-${System.currentTimeMillis()}.example"
        val tenant = createTestTenant(
            id = TenantId(UUID.randomUUID()),
            domain = uniqueDomain,
        )

        tenantRepository.save(tenant)

        val found = tenantRepository.findByDomain(uniqueDomain)

        assertThat(found).isNotNull
        assertThat(found!!.domain).isEqualTo(uniqueDomain)
    }

    @Test
    @Transactional
    fun `findByDomain returns null for non-existent domain`() {
        val found = tenantRepository.findByDomain("non-existent-domain-${System.currentTimeMillis()}.example")

        assertThat(found).isNull()
    }

    @Test
    @Transactional
    fun `findAll returns paginated results`() {
        // Create multiple tenants
        val tenants = (1..3).map { i ->
            createTestTenant(
                id = TenantId(UUID.randomUUID()),
                domain = "paginated-$i-${System.currentTimeMillis()}.example",
            )
        }
        tenants.forEach { tenantRepository.save(it) }

        val firstPage = tenantRepository.findAll(limit = 2, offset = 0)
        val secondPage = tenantRepository.findAll(limit = 2, offset = 2)

        assertThat(firstPage).hasSizeLessThanOrEqualTo(2)
        // Second page may have results depending on total count
    }

    @Test
    @Transactional
    fun `findAll with status filter returns only matching tenants`() {
        val activeTenant = createTestTenant(
            id = TenantId(UUID.randomUUID()),
            domain = "active-filter-${System.currentTimeMillis()}.example",
        ).also {
            tenantRepository.save(it)
            it.activate()
            tenantRepository.save(it)
        }

        val pendingTenant = createTestTenant(
            id = TenantId(UUID.randomUUID()),
            domain = "pending-filter-${System.currentTimeMillis()}.example",
        ).also {
            tenantRepository.save(it)
            // Leave as PENDING
        }

        val activeResults = tenantRepository.findAll(limit = 100, offset = 0, status = TenantStatus.ACTIVE)
        val pendingResults = tenantRepository.findAll(limit = 100, offset = 0, status = TenantStatus.PENDING)

        assertThat(activeResults.map { it.id }).contains(activeTenant.id)
        assertThat(pendingResults.map { it.id }).contains(pendingTenant.id)
    }

    @Test
    @Transactional
    fun `save updates existing tenant`() {
        val tenant = createTestTenant(
            id = TenantId(UUID.randomUUID()),
            domain = "update-test-${System.currentTimeMillis()}.example",
        )

        tenantRepository.save(tenant)
        tenant.activate()
        
        val newSettings = TenantSettings(
            locale = "de_DE",
            timezone = "Europe/Berlin",
            currency = "EUR",
        )
        tenant.updateSettings(newSettings)
        tenantRepository.save(tenant)

        val retrieved = tenantRepository.findById(tenant.id)

        assertThat(retrieved).isNotNull
        assertThat(retrieved!!.settings.locale).isEqualTo("de_DE")
        assertThat(retrieved.settings.currency).isEqualTo("EUR")
    }

    private fun createTestTenant(
        id: TenantId,
        domain: String,
        tier: TenantTier = TenantTier.STANDARD,
    ): Tenant = Tenant.create(
        id = id,
        name = "Test Tenant",
        domain = domain,
        tier = tier,
        dataResidency = DataResidency("US"),
        now = Instant.now(),
    )
}

