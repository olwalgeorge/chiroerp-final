package com.chiroerp.tenancy.core.application

import com.chiroerp.tenancy.core.application.command.ActivateTenantCommand
import com.chiroerp.tenancy.core.application.command.CreateTenantCommand
import com.chiroerp.tenancy.core.application.command.SuspendTenantCommand
import com.chiroerp.tenancy.core.application.command.TerminateTenantCommand
import com.chiroerp.tenancy.core.application.exception.TenantLifecycleTransitionException
import com.chiroerp.tenancy.core.application.handler.TenantCommandHandler
import com.chiroerp.tenancy.core.application.service.TenantIsolationService
import com.chiroerp.tenancy.core.application.service.TenantProvisioningService
import com.chiroerp.tenancy.core.application.service.TenantSchemaProvisioner
import com.chiroerp.tenancy.core.domain.event.TenantActivatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantCreatedEvent
import com.chiroerp.tenancy.core.domain.event.TenantDomainEvent
import com.chiroerp.tenancy.core.domain.model.Tenant
import com.chiroerp.tenancy.core.domain.port.TenantEventPublisher
import com.chiroerp.tenancy.core.domain.port.TenantRepository
import com.chiroerp.tenancy.shared.TenantId
import com.chiroerp.tenancy.shared.TenantStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TenantCommandHandlerTest {
    private val repository = InMemoryTenantRepository()
    private val eventPublisher = RecordingTenantEventPublisher()
    private val provisioningService = TenantProvisioningService(
        tenantIsolationService = TenantIsolationService(configuredIsolationStrategy = "AUTO"),
        tenantSchemaProvisioner = NoOpSchemaProvisioner(),
    )

    private val handler = TenantCommandHandler(
        tenantRepository = repository,
        tenantEventPublisher = eventPublisher,
        tenantProvisioningService = provisioningService,
    )

    @Test
    fun `create tenant persists aggregate and publishes created and activated events`() {
        val created = handler.handle(
            CreateTenantCommand(
                name = "Acme Wellness",
                domain = "acme-wellness.example",
                dataResidencyCountry = "US",
            ),
        )

        assertThat(repository.findById(created.id)).isNotNull
        // Tenant should be ACTIVE after successful provisioning
        assertThat(created.status).isEqualTo(TenantStatus.ACTIVE)
        // Should have both created and activated events
        assertThat(eventPublisher.published.filterIsInstance<TenantCreatedEvent>())
            .extracting<TenantId> { it.tenantId }
            .contains(created.id)
        assertThat(eventPublisher.published.filterIsInstance<TenantActivatedEvent>())
            .extracting<TenantId> { it.tenantId }
            .contains(created.id)
    }

    @Test
    fun `activate command moves suspended tenant back to active`() {
        val tenant = handler.handle(
            CreateTenantCommand(
                name = "Sunrise Clinic",
                domain = "sunrise.example",
                dataResidencyCountry = "US",
            ),
        )

        handler.handle(
            SuspendTenantCommand(
                tenantId = tenant.id,
                reason = "Billing issue",
            ),
        )

        val activated = handler.handle(ActivateTenantCommand(tenant.id))

        assertThat(activated.status).isEqualTo(TenantStatus.ACTIVE)
        assertThat(eventPublisher.published.filterIsInstance<TenantActivatedEvent>())
            .extracting<TenantId> { it.tenantId }
            .contains(tenant.id)
    }

    @Test
    fun `activate command rejects terminated tenant`() {
        val tenant = handler.handle(
            CreateTenantCommand(
                name = "Archive Clinic",
                domain = "archive.example",
                dataResidencyCountry = "US",
            ),
        )

        handler.handle(
            TerminateTenantCommand(
                tenantId = tenant.id,
                reason = "Tenant contract closed",
            ),
        )

        assertThatThrownBy {
            handler.handle(ActivateTenantCommand(tenant.id))
        }
            .isInstanceOf(TenantLifecycleTransitionException::class.java)
            .hasMessageContaining("lifecycle transition rejected")
    }

    private class InMemoryTenantRepository : TenantRepository {
        private val store = ConcurrentHashMap<UUID, Tenant>()

        override fun save(tenant: Tenant): Tenant {
            store[tenant.id.value] = tenant
            return tenant
        }

        override fun findById(id: TenantId): Tenant? = store[id.value]

        override fun findByDomain(domain: String): Tenant? {
            val normalized = domain.trim().lowercase()
            return store.values.firstOrNull { it.domain == normalized }
        }

        override fun findAll(limit: Int, offset: Int, status: TenantStatus?): List<Tenant> {
            val filtered = store.values
                .asSequence()
                .filter { status == null || it.status == status }
                .sortedByDescending { it.createdAt }
                .drop(offset)
                .take(limit)
                .toList()
            return filtered
        }
    }

    private class RecordingTenantEventPublisher : TenantEventPublisher {
        val published = mutableListOf<TenantDomainEvent>()

        override fun publish(events: List<TenantDomainEvent>) {
            published += events
        }
    }

    private class NoOpSchemaProvisioner : TenantSchemaProvisioner {
        override fun verifySharedSchema() = Unit
        override fun createSchema(schemaName: String) = Unit
        override fun grantSchemaUsage(schemaName: String) = Unit
        override fun createBootstrapObjects(schemaName: String) = Unit
        override fun seedSchemaReferenceData(schemaName: String, tenantId: TenantId) = Unit
        override fun dropSchema(schemaName: String) = Unit
    }
}
